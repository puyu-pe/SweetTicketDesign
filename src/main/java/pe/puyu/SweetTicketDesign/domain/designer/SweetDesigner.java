package pe.puyu.SweetTicketDesign.domain.designer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pe.puyu.SweetTicketDesign.domain.builder.SweetPrinterObjectBuilder;
import pe.puyu.SweetTicketDesign.domain.components.block.*;
import pe.puyu.SweetTicketDesign.domain.designer.img.SweetImageBlock;
import pe.puyu.SweetTicketDesign.domain.designer.img.SweetImageHelper;
import pe.puyu.SweetTicketDesign.domain.designer.img.SweetImageInfo;
import pe.puyu.SweetTicketDesign.domain.designer.img.SweetImageStyle;
import pe.puyu.SweetTicketDesign.domain.designer.qr.SweetQrBlock;
import pe.puyu.SweetTicketDesign.domain.designer.qr.SweetQrHelper;
import pe.puyu.SweetTicketDesign.domain.designer.qr.SweetQrInfo;
import pe.puyu.SweetTicketDesign.domain.designer.qr.SweetQrStyle;
import pe.puyu.SweetTicketDesign.domain.designer.text.*;
import pe.puyu.SweetTicketDesign.domain.components.SweetPrinterObjectComponent;
import pe.puyu.SweetTicketDesign.domain.components.properties.SweetCutMode;
import pe.puyu.SweetTicketDesign.domain.components.properties.SweetCutComponent;
import pe.puyu.SweetTicketDesign.domain.components.properties.SweetPropertiesComponent;
import pe.puyu.SweetTicketDesign.domain.components.drawer.SweetOpenDrawerComponent;
import pe.puyu.SweetTicketDesign.domain.components.drawer.SweetPinConnector;
import pe.puyu.SweetTicketDesign.domain.components.SweetDefaultComponentsProvider;
import pe.puyu.SweetTicketDesign.domain.printer.*;

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
        SweetDesignHelper helper = makeSweetHelper(designObject.properties(), designObject.styles());
        blocks.forEach(block -> printBlock(block, helper));
        boolean onlyOpenDrawer = !blocks.isEmpty();
        openDrawerOrCut(designObject.openDrawer(), helper, onlyOpenDrawer);
    }

    private @NotNull SweetDesignHelper makeSweetHelper(
        @Nullable SweetPropertiesComponent propertiesComponent,
        @Nullable Map<String, @Nullable SweetStyleComponent> stylesMap
    ) {
        SweetPropertiesComponent defaultProperties = defaultProvider.getPropertiesComponent();
        propertiesComponent = Optional.ofNullable(propertiesComponent).orElse(defaultProperties);
        int blockWidth = Optional.ofNullable(propertiesComponent.blockWidth()).or(() -> Optional.ofNullable(defaultProperties.blockWidth())).orElse(0);
        SweetProperties.CutProperty cut = makeCutProperty(propertiesComponent, defaultProperties);
        SweetProperties properties = new SweetProperties(Math.max(blockWidth, 0), cut);
        Map<String, SweetStyleComponent> styles = Optional.ofNullable(stylesMap).orElse(defaultProvider.getStyles());
        return new SweetDesignHelper(properties, defaultProvider.getStyleComponent(), styles);
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
        SweetBlockType type = Optional.ofNullable(block.type()).orElse(defaultProvider.getBlockType());
        switch (type) {
            case IMG:
                SweetImageComponent imgComponent = Optional.ofNullable(block.img()).orElse(new SweetImageComponent(null, null));
                SweetImageInfo imgInfo = new SweetImageInfo(
                    Optional.ofNullable(imgComponent.path()).orElse(defaultProvider.getImagePath()),
                    Optional.ofNullable(imgComponent.className()).orElse("")
                );
                SweetImageStyle imageStyle = helper.makeImageStyle(imgInfo.className());
                SweetImageBlock imgBlock = new SweetImageBlock(imgInfo, helper.calcWidthPaperInPx(), imageStyle);
                printImg(imgBlock);
                break;
            case QR:
                SweetQrInfo qrInfo = getSweetQrInfo(block);
                SweetQrStyle qrStyle = helper.makeQrStyle(qrInfo.className());
                SweetQrBlock qrBlock = new SweetQrBlock(helper.calcWidthPaperInPx(), qrInfo, qrStyle);
                printQr(qrBlock);
                break;
            default: // TEXT
                SweetTextBlock textBlock = makeTextBlock(block);
                SweetTable table = makeSweetTable(textBlock, helper);
                table = phase0CalcAutoCharxels(table, helper);
                table = phase1ConsiderGapSpaces(table);
                table = phase2WrapRows(table, helper);
                phase3PrintRow(table, helper);
        }
    }

    private @NotNull SweetQrInfo getSweetQrInfo(@NotNull SweetBlockComponent block) {
        SweetQrComponent qrComponent = Optional.ofNullable(block.qr()).orElse(new SweetQrComponent(null, null, null, null));
        return new SweetQrInfo(
            Optional.ofNullable(qrComponent.data()).orElse(defaultProvider.getStringQr()),
            Optional.ofNullable(qrComponent.className()).orElse(""),
            Optional.ofNullable(qrComponent.qrType()).orElse(defaultProvider.getQrType()),
            Optional.ofNullable(qrComponent.correctionLevel()).orElse(defaultProvider.getQrCorrectionLevel())
        );
    }

    private @NotNull SweetTextBlock makeTextBlock(@NotNull SweetBlockComponent block) {
        char separator = Optional.ofNullable(block.separator()).orElse(defaultProvider.getSeparator());
        var rows = Optional.ofNullable(block.rows()).orElse(new LinkedList<>());
        return new SweetTextBlock(separator, rows);
    }

    private @NotNull SweetTable makeSweetTable(@NotNull SweetTextBlock block, @NotNull SweetDesignHelper helper) {
        SweetTableInfo tableInfo = new SweetTableInfo(block.separator());
        SweetTable table = new SweetTable(tableInfo);
        List<SweetRow> printRows = block.rows().stream()
            .map(rowDto -> {
                List<SweetCellComponent> cellRow = Optional.ofNullable(rowDto).orElse(new LinkedList<>());
                List<SweetCell> row = new LinkedList<>();
                for (SweetCellComponent sweetCellComponent : cellRow) {
                    SweetCellComponent cellDto = Optional.ofNullable(sweetCellComponent).orElse(new SweetCellComponent("", ""));
                    String text = Optional.ofNullable(cellDto.text()).orElse("");
                    String className = Optional.ofNullable(cellDto.className()).orElse("");
                    SweetPrinterStyle sweetPrinterStyle = helper.makePrinterStyleFor(className);
                    SweetStringStyle stringStyle = helper.makeSweetStringStyleFor(className);
                    row.add(new SweetCell(text, sweetPrinterStyle, stringStyle));
                }
                SweetRow printRow = new SweetRow();
                printRow.addAll(row);
                return printRow;
            })
            .toList();
        table.addAll(printRows);
        return table;
    }

    private @NotNull SweetTable phase0CalcAutoCharxels(@NotNull SweetTable table, @NotNull SweetDesignHelper helper) {
        SweetTable newTable = new SweetTable(table.getInfo());
        int blockWidth = helper.getProperties().blockWidth();
        for (SweetRow row : table) {
            SweetRow newRow = new SweetRow();
            int countCharxelZeros = row.countElementsByCharxelZero();
            int sumAllCharxels = row.sumAllCharxels();
            int remainingCharxels = Math.max(0, blockWidth - sumAllCharxels);
            int autoCharxels = countCharxelZeros <= 0 ? 0 : remainingCharxels / countCharxelZeros;
            int autoCharxelsResidue = countCharxelZeros <= 0 ? 0 : remainingCharxels % countCharxelZeros;
            int coveredCharxels = 0;
            for (SweetCell cell : row) {
                int newCharxels = cell.stringStyle().charxels();
                SweetCell newCell;
                if (cell.stringStyle().charxels() == 0) {
                    newCharxels = autoCharxels;
                    if (countCharxelZeros == 1) {
                        newCharxels += autoCharxelsResidue;
                    }
                    --countCharxelZeros;
                } else if (coveredCharxels + newCharxels >= blockWidth) {
                    newCharxels = Math.max(0, blockWidth - coveredCharxels);
                }
                SweetStringStyle newStringStyle = new SweetStringStyle(
                    newCharxels,
                    cell.stringStyle().pad(),
                    cell.stringStyle().align(),
                    cell.stringStyle().normalize()
                );
                newCell = new SweetCell(cell.text(), new SweetPrinterStyle(cell.printerStyle()), newStringStyle);
                newRow.add(newCell);
                coveredCharxels += newCell.stringStyle().charxels();
            }
            newTable.add(newRow);
        }
        return newTable;
    }

    private @NotNull SweetTable phase1ConsiderGapSpaces(@NotNull SweetTable table) {
        SweetTable newTable = new SweetTable(table.getInfo());
        for (SweetRow row : table) {
            SweetRow newRow = new SweetRow();
            for (int i = 0; i < row.size(); ++i) {
                SweetCell cell = row.get(i);
                SweetCell newCell = new SweetCell(cell);
                if (i < row.size() - 1) {
                    SweetStringStyle newStringStyle = new SweetStringStyle(
                        cell.stringStyle().charxels() - 1,
                        cell.stringStyle().pad(),
                        cell.stringStyle().align(),
                        cell.stringStyle().normalize()
                    );
                    newCell = new SweetCell(cell.text(), new SweetPrinterStyle(cell.printerStyle()), newStringStyle);
                }
                newRow.add(newCell);
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
        for (SweetRow row : table) {
            for (int i = 0; i < row.size(); ++i) {
                SweetCell cell = row.get(i);
                boolean isLastElement = i + 1 >= row.size();
                cell = helper.normalize(cell);
                printCell(cell, isLastElement);
                if (!isLastElement) {
                    SweetPrinterStyle gapStyle = new SweetPrinterStyle(
                        1,
                        1,
                        cell.printerStyle().bold(),
                        cell.printerStyle().bgInverted(),
                        cell.printerStyle().charCode()
                    );
                    printer.print(separator.repeat(1), gapStyle);
                }
            }
        }
    }

    private void printCell(SweetCell cell, boolean feed) {
        int spacesAvailable = Math.max(cell.stringStyle().charxels() - (cell.text().length() * cell.printerStyle().fontWidth()), 0);
        int startSpaces = spacesAvailable / 2;
        int endSpaces = spacesAvailable - startSpaces;
        SweetPrinterStyle cellStyle = cell.printerStyle();
        SweetPrinterStyle padStyle = new SweetPrinterStyle(
            1,
            1,
            cellStyle.bold(),
            cellStyle.bgInverted(),
            cell.printerStyle().charCode()
        );
        String pad = cell.stringStyle().pad().toString();
        switch (cell.stringStyle().align()) {
            case CENTER:
                printer.print(pad.repeat(startSpaces), padStyle);
                printer.print(cell.text(), cellStyle);
                printer.print(pad.repeat(endSpaces), padStyle);
                break;
            case RIGHT:
                printer.print(pad.repeat(spacesAvailable), padStyle);
                printer.print(cell.text(), cellStyle);
                break;
            default: // LEFT
                printer.print(cell.text(), cellStyle);
                printer.print(pad.repeat(spacesAvailable), padStyle);
        }
        if (feed) {
            printer.println("", padStyle);
        }
    }


    private @NotNull List<SweetRow> wrapRow(@NotNull SweetRow row, @NotNull SweetDesignHelper helper) {
        List<SweetRow> matrix = new LinkedList<>();
        int numberColumnsMatrix = 0;
        for (SweetCell cell : row) {
            SweetRow newRow = new SweetRow();
            List<String> wrappedText = helper.wrapText(cell.text(), cell.stringStyle().charxels(), cell.printerStyle().fontWidth());
            for (String text : wrappedText) {
                newRow.add(new SweetCell(
                    text,
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

    private void openDrawerOrCut(@Nullable SweetOpenDrawerComponent openDrawer, @NotNull SweetDesignHelper helper, boolean onlyOpenDrawer) {
        SweetProperties.CutProperty cutProperty = helper.getProperties().cutProperty();
        int feed = onlyOpenDrawer ? cutProperty.feed() : 0;
        SweetCutMode mode = onlyOpenDrawer ? cutProperty.mode() : SweetCutMode.FULL;
        SweetCutOptions cutOptions = new SweetCutOptions(feed, mode);
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
            printer.openDrawerWithCut(new SweetDrawerOptions(pin, t1, t2), cutOptions);
        } else {
            printer.cut(cutOptions);
        }
    }

    private void printImg(@NotNull SweetImageBlock imageBlock) {
        try {
            BufferedImage image = SweetImageHelper.toBufferedImage(imageBlock.info().path());
            BufferedImage resizedImage = SweetImageHelper.resize(image, imageBlock.style());
            BufferedImage justifiedImage = SweetImageHelper.justify(resizedImage, imageBlock.widthInPx(), imageBlock.style());
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
                SweetImageStyle imageInfo = new SweetImageStyle(style.scale(), style.size(), style.size(), style.align());
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
