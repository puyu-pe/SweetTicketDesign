package pe.puyu.SweetTicketDesign.domain.printer;

import org.jetbrains.annotations.NotNull;
import pe.puyu.SweetTicketDesign.domain.components.properties.SweetCutMode;
import pe.puyu.SweetTicketDesign.domain.components.drawer.SweetPinConnector;

import java.awt.image.BufferedImage;

public interface SweetPrinter {
    void print(@NotNull String text, @NotNull SweetPrinterStyle style);

    void println(@NotNull String text, @NotNull SweetPrinterStyle style);

    void printImg(@NotNull BufferedImage image);

    void printQr(@NotNull String data, @NotNull SweetPrinterQrHints hints);

    void cut(@NotNull SweetCutOptions options);

    void openDrawerWithCut(@NotNull SweetDrawerOptions drawerOptions, @NotNull SweetCutOptions cutOptions);
}
