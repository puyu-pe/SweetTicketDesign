package pe.puyu.SweetTicketDesign.domain.designer.text;

import org.jetbrains.annotations.NotNull;
import pe.puyu.SweetTicketDesign.domain.printer.SweetPrinterStyle;

public record SweetCell(
    @NotNull String text,
    @NotNull SweetPrinterStyle printerStyle,
    @NotNull SweetStringStyle stringStyle
) {

    public SweetCell(SweetCell otherCell){
        this(
            otherCell.text,
            new SweetPrinterStyle(otherCell.printerStyle),
            new SweetStringStyle(otherCell.stringStyle)
        );
    }

}
