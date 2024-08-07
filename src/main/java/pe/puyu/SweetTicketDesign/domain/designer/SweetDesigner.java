package pe.puyu.SweetTicketDesign.domain.designer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pe.puyu.SweetTicketDesign.domain.builder.SweetPrinterObjectBuilder;
import pe.puyu.SweetTicketDesign.domain.designer.img.SweetImageBlock;
import pe.puyu.SweetTicketDesign.domain.designer.img.SweetImageHelper;
import pe.puyu.SweetTicketDesign.domain.designer.img.SweetImageInfo;
import pe.puyu.SweetTicketDesign.domain.designer.qr.SweetQrBlock;
import pe.puyu.SweetTicketDesign.domain.designer.qr.SweetQrHelper;
import pe.puyu.SweetTicketDesign.domain.designer.qr.SweetQrInfo;
import pe.puyu.SweetTicketDesign.domain.designer.qr.SweetQrStyle;
import pe.puyu.SweetTicketDesign.domain.designer.text.*;
import pe.puyu.SweetTicketDesign.domain.components.block.SweetBlockComponent;
import pe.puyu.SweetTicketDesign.domain.components.SweetPrinterObjectComponent;
import pe.puyu.SweetTicketDesign.domain.components.block.SweetQrType;
import pe.puyu.SweetTicketDesign.domain.components.properties.SweetCutMode;
import pe.puyu.SweetTicketDesign.domain.components.properties.SweetCutComponent;
import pe.puyu.SweetTicketDesign.domain.components.properties.SweetPropertiesComponent;
import pe.puyu.SweetTicketDesign.domain.components.drawer.SweetOpenDrawerComponent;
import pe.puyu.SweetTicketDesign.domain.components.drawer.SweetPinConnector;
import pe.puyu.SweetTicketDesign.domain.components.SweetDefaultComponentsProvider;
import pe.puyu.SweetTicketDesign.domain.components.block.SweetCellComponent;
import pe.puyu.SweetTicketDesign.domain.printer.SweetPrinter;
import pe.puyu.SweetTicketDesign.domain.printer.SweetPrinterStyle;
import pe.puyu.SweetTicketDesign.domain.printer.SweetPrinterQrHints;

import java.awt.image.BufferedImage;
import java.util.*;

public class SweetDesigner {
    private final @NotNull SweetPrinterObjectBuilder builder;
    private final @NotNull SweetPrinter printer;
    private final @NotNull SweetDefaultComponentsProvider defaultProvider;

    public SweetDesigner(
        @NotNull pe.puyu.SweetTicketDesign.domain.builder.SweetPrinterObjectBuilder builder,
        @NotNull SweetPrinter printer,
        @NotNull SweetDefaultComponentsProvider defaultProvider
    ) {
        this.builder = builder;
        this.printer = printer;
        this.defaultProvider = defaultProvider;
    }

    public void paintDesign() {
        SweetPrinterObjectComponent designObject = builder.build();
        List<SweetBlockComponent> blocks = Optional
            .ofNullable(designObject.blocks())
            .orElse(defaultProvider.getDataComponent());
        SweetDesignHelper helper = makeSweetHelper(designObject.properties());
        blocks.forEach(block -> printBlock(block, helper));
        SweetProperties.CutProperty cutProperty = helper.getProperties().cutProperty();
        printer.cut(cutProperty.feed(), cutProperty.mode());
        openDrawer(defaultProvider.getOpenDrawerComponent());
    }

    private @NotNull SweetDesignHelper makeSweetHelper(@Nullable SweetPropertiesComponent propertiesComponent) {
        SweetPropertiesComponent defaultProperties = defaultProvider.getPropertiesComponent();
        propertiesComponent = Optional.ofNullable(propertiesComponent).orElse(defaultProperties);
        int blockWidth = Optional.ofNullable(propertiesComponent.blockWidth()).or(() -> Optional.ofNullable(defaultProperties.blockWidth())).orElse(0);
        String charCode = Optional.ofNullable(propertiesComponent.charCode()).or(() -> Optional.ofNullable(defaultProperties.charCode())).orElse("");
        boolean normalize = Optional
            .ofNullable(propertiesComponent.normalize())
            .or(() -> Optional.ofNullable(defaultProperties.normalize()))
            .or(() -> Optional.ofNullable(defaultProvider.getStyleComponent().normalize()))
            .orElse(false);
        SweetProperties.CutProperty cut = makeCutProperty(propertiesComponent, defaultProperties);
        SweetProperties properties = new SweetProperties(Math.max(blockWidth, 0), normalize, charCode, cut);
        return new SweetDesignHelper(properties, defaultProvider.getStyleComponent());
    }

    private SweetProperties.CutProperty makeCutProperty(@NotNull SweetPropertiesComponent properties, @NotNull SweetPropertiesComponent defaultProperties) {
        int feed = Optional.ofNullable(properties.cutMode())
            .map(SweetCutComponent::feed)
            .or(() -> Optional.ofNullable(defaultProperties.cutMode()).map(SweetCutComponent::feed))
            .orElse(4);
        SweetCutMode cutMode = Optional.ofNullable(properties.cutMode())
            .map(SweetCutComponent::mode)
            .or(() -> Optional.ofNullable(defaultProperties.cutMode()).map(SweetCutComponent::mode))
            .orElse(SweetCutMode.PART);
        return new SweetProperties.CutProperty(feed, cutMode);
    }

    private void printBlock(@Nullable SweetBlockComponent block, @NotNull SweetDesignHelper helper) {
        if (block == null) return;
        if (block.imgPath() != null && !block.imgPath().isBlank()) {
            String imgPath = block.imgPath();
            SweetImageInfo imageInfo = helper.makeImageInfo(block.styles());
            SweetImageBlock imgBlock = new SweetImageBlock(imgPath, helper.calcWidthPaperInPx(), imageInfo);
            printImg(imgBlock);
        } else if (block.qr() != null) {
            SweetQrInfo qrInfo = helper.makeQrInfo(block.qr(), defaultProvider.getQrComponent());
            SweetQrStyle qrStyle = helper.makeQrStyles(block.styles());
            SweetQrBlock qrBlock = new SweetQrBlock(helper.calcWidthPaperInPx(), qrInfo, qrStyle);
            printQr(qrBlock);
        } else {
            SweetTextBlock textBlock = makeTextBlock(block);
            SweetTable table = makeSweetTable(textBlock, helper);
            table = phase1CalcWidthAndNormalizeSpan(table, helper);
            table = phase2WrapRows(table, helper);
            phase3PrintRow(table, helper);
        }
    }

    private @NotNull SweetTextBlock makeTextBlock(@NotNull SweetBlockComponent block) {
        SweetBlockComponent defaultBlock = defaultProvider.getBlockComponent();
        int gap = Math.max(Optional.ofNullable(block.gap()).or(() -> Optional.ofNullable(defaultBlock.gap())).orElse(1), 1);
        char separator = Optional.ofNullable(block.separator()).or(() -> Optional.ofNullable(defaultBlock.separator())).orElse(' ');
        int nColumns = Math.max(Optional.ofNullable(block.nColumns()).or(() -> Optional.ofNullable(defaultBlock.nColumns())).orElse(0), 0);
        var rows = Optional.ofNullable(block.rows()).orElse(new LinkedList<>());
        var styles = Optional.ofNullable(block.styles()).orElse(new HashMap<>());
        return new SweetTextBlock(gap, separator, nColumns, styles, rows);
    }

    private @NotNull SweetTable makeSweetTable(@NotNull SweetTextBlock block, @NotNull SweetDesignHelper helper) {
        SweetTableInfo tableInfo = new SweetTableInfo(block.gap(), block.separator(), Math.max(block.nColumns(), 0));
        SweetTable table = new SweetTable(tableInfo);
        List<SweetRow> printRows = block.rows().stream()
            .map(rowDto -> {
                List<SweetCellComponent> cellRow = Optional.ofNullable(rowDto).orElse(new LinkedList<>());
                List<SweetCell> row = new LinkedList<>();
                for (int i = 0; i < cellRow.size(); ++i) {
                    SweetCellComponent defaultCell = defaultProvider.getCellComponent();
                    SweetCellComponent cellDto = Optional.ofNullable(cellRow.get(i)).orElse(defaultCell);
                    String text = Optional.ofNullable(cellDto.text()).or(() -> Optional.ofNullable(defaultCell.text())).orElse("");
                    String className = Optional.ofNullable(cellDto.className()).or(() -> Optional.ofNullable(defaultCell.className())).orElse("");
                    SweetPrinterStyle sweetPrinterStyle = helper.makePrinterStyleFor(className, i, block.styles());
                    SweetStringStyle stringStyle = helper.makeSweetStringStyleFor(className, i, block.styles());
                    row.add(new SweetCell(text, 0, sweetPrinterStyle, stringStyle));
                }
                SweetRow printRow = new SweetRow();
                printRow.addAll(row);
                return printRow;
            })
            .toList();
        table.addAll(printRows);
        return table;
    }

    private @NotNull SweetTable phase1CalcWidthAndNormalizeSpan(@NotNull SweetTable table, @NotNull SweetDesignHelper helper) {
        SweetTable newTable = new SweetTable(table.getInfo());
        for (SweetRow row : table) {
            SweetRow newRow = new SweetRow();
            int remainingWidth = helper.getProperties().blockWidth();
            int blockWidth = remainingWidth;
            int nColumns = table.getInfo().maxNumberOfColumns();
            int coveredColumns = 0;
            for (int i = 0; i < row.size(); ++i) {
                SweetCell cell = row.get(i);
                int span = Math.min(Math.max(cell.stringStyle().span(), 0), nColumns); // normalize span in range (0, max)
                int cellWidth = nColumns == 0 ? 0 : Math.min(span * blockWidth / nColumns, remainingWidth);
                int coverWidthByCell = cellWidth;
                boolean isLastItem = i + 1 >= row.size();
                coveredColumns += span;
                if (!isLastItem && (remainingWidth - cellWidth) > 0) { // is not the last item
                    int gap = table.getInfo().gap();
                    cellWidth = Math.max(cellWidth - gap, 0); // consider intermediate space
                }
                if (isLastItem && coveredColumns >= nColumns) {
                    cellWidth = Math.max(remainingWidth, 0); // cover all remaining width
                }
                remainingWidth -= coverWidthByCell;
                SweetStringStyle newStringStyle = new SweetStringStyle(
                    span,
                    cell.stringStyle().pad(),
                    cell.stringStyle().align(),
                    cell.stringStyle().normalize()
                );
                SweetPrinterStyle sweetPrinterStyle = new SweetPrinterStyle(cell.printerStyle());
                newRow.add(new SweetCell(cell.text(), cellWidth, sweetPrinterStyle, newStringStyle));
            }
            newTable.add(newRow);
        }
        return newTable;
    }

    private @NotNull SweetTable phase2WrapRows(@NotNull SweetTable table, @NotNull SweetDesignHelper helper) {
        SweetTable newTable = new SweetTable(table.getInfo());
        for (SweetRow row : table) {
            newTable.addAll(wrapRow(row, helper));
        }
        return newTable;
    }

    private void phase3PrintRow(@NotNull SweetTable table, @NotNull SweetDesignHelper helper) {
        SweetTableInfo tableInfo = table.getInfo();
        String separator = tableInfo.separator().toString();
        int gap = Math.max(tableInfo.gap(), 0);
        for (SweetRow row : table) {
            int remainingWidth = helper.getProperties().blockWidth();
            for (int i = 0; i < row.size(); ++i) {
                SweetCell cell = row.get(i);
                boolean isLastElement = i + 1 >= row.size();
                cell = helper.justifyCell(cell);
                cell = helper.normalize(cell);
                if (!isLastElement) {
                    SweetPrinterStyle gapStyle = new SweetPrinterStyle(
                        1,
                        1,
                        cell.printerStyle().bold(),
                        cell.printerStyle().bgInverted(),
                        cell.printerStyle().charCode()
                    );
                    printer.print(cell.text(), cell.printerStyle());
                    remainingWidth -= cell.width();
                    if (remainingWidth > 0) {
                        printer.print(separator.repeat(gap), gapStyle);
                        remainingWidth -= gap;
                    }
                } else {
                    printer.println(cell.text(), cell.printerStyle());
                }
            }
        }
    }

    private @NotNull List<SweetRow> wrapRow(@NotNull SweetRow row, @NotNull SweetDesignHelper helper) {
        List<SweetRow> matrix = new LinkedList<>();
        int numberColumnsMatrix = 0;
        for (SweetCell cell : row) {
            SweetRow newRow = new SweetRow();
            List<String> wrappedText = helper.wrapText(cell.text(), cell.width(), cell.printerStyle().fontWidth());
            for (String text : wrappedText) {
                newRow.add(new SweetCell(
                    text,
                    cell.width(),
                    new SweetPrinterStyle(cell.printerStyle()),
                    new SweetStringStyle(cell.stringStyle())
                ));
            }
            matrix.add(newRow);
            numberColumnsMatrix = Math.max(newRow.size(), numberColumnsMatrix);
        }
        List<SweetRow> wrappedRow = new LinkedList<>();
        for (int j = 0; j < numberColumnsMatrix; ++j) {
            SweetRow newRow = new SweetRow();
            for (SweetRow currentRow : matrix) {
                if (!currentRow.existsIndex(j)) {
                    if (currentRow.existsIndex(0)) {
                        SweetCell firstCell = currentRow.get(0);
                        newRow.add(new SweetCell(
                            "",
                            firstCell.width(),
                            new SweetPrinterStyle(firstCell.printerStyle()),
                            new SweetStringStyle(firstCell.stringStyle()))
                        );
                    }
                } else {
                    newRow.add(new SweetCell(currentRow.get(j)));

                }
            }
            wrappedRow.add(newRow);
        }
        return wrappedRow;
    }

    private void openDrawer(@Nullable SweetOpenDrawerComponent openDrawer) {
        if (openDrawer != null) {
            SweetPinConnector pin = SweetPinConnector.Pin_2;
            int t1 = 120, t2 = 240;
            SweetOpenDrawerComponent defaultOpenDrawer = defaultProvider.getOpenDrawerComponent();
            pin = Optional.ofNullable(defaultOpenDrawer.pin()).orElse(pin);
            t1 = Optional.ofNullable(defaultOpenDrawer.t1()).orElse(t1);
            t2 = Optional.ofNullable(defaultOpenDrawer.t2()).orElse(t2);
            pin = Optional.ofNullable(openDrawer.pin()).orElse(pin);
            t1 = Optional.ofNullable(openDrawer.t1()).orElse(t1);
            t2 = Optional.ofNullable(openDrawer.t2()).orElse(t2);
            printer.openDrawer(pin, t1, t2);
        }
    }

    private void printImg(@NotNull SweetImageBlock imageBlock) {
        try {
            BufferedImage image = SweetImageHelper.toBufferedImage(imageBlock.imgPath());
            BufferedImage resizedImage = SweetImageHelper.resize(image, imageBlock.imageInfo());
            BufferedImage justifiedImage = SweetImageHelper.justify(resizedImage, imageBlock.widthInPx(), imageBlock.imageInfo());
            printer.printImg(justifiedImage);
        } catch (Exception ignored) {

        }
    }

    private void printQr(@NotNull SweetQrBlock qrBlock) {
        try {
            SweetQrInfo qrInfo = qrBlock.info();
            SweetQrStyle style = qrBlock.style();
            if (qrInfo.qrType() == SweetQrType.IMG) {
                BufferedImage qrImage = SweetQrHelper.generateQr(qrInfo, style.size());
                SweetImageInfo imageInfo = new SweetImageInfo(style.scale(), style.size(), style.size(), style.align());
                BufferedImage justifiedQr = SweetImageHelper.justify(qrImage, qrBlock.widthInPx(), imageInfo);
                printer.printImg(justifiedQr);
            } else {
                SweetPrinterQrHints hints = new SweetPrinterQrHints(style.size(), style.align(), qrInfo.correctionLevel());
                printer.printQr(qrInfo.data(), hints);
            }
        } catch (Exception ignored) {
        }
    }

}
