package pe.puyu.SweetTicketDesign.domain.components;

import org.jetbrains.annotations.NotNull;
import pe.puyu.SweetTicketDesign.domain.components.block.*;
import pe.puyu.SweetTicketDesign.domain.components.properties.SweetPropertiesComponent;
import pe.puyu.SweetTicketDesign.domain.components.drawer.SweetOpenDrawerComponent;

import java.util.List;
import java.util.Map;

public interface SweetDefaultComponentsProvider {

    @NotNull
    SweetCellComponent getCellComponent();

    @NotNull
    SweetPropertiesComponent getPropertiesComponent();

    @NotNull
    SweetStyleComponent getStyleComponent();

    @NotNull
    Character getSeparator();

    @NotNull
    String getStringQr();

    @NotNull
    SweetQrType getQrType();

    @NotNull
    SweetQrCorrectionLevel getQrCorrectionLevel();

    @NotNull
    String getImagePath();

    @NotNull
    SweetOpenDrawerComponent getOpenDrawerComponent();

    @NotNull
    List<SweetBlockComponent> getDataComponent();

    @NotNull
    SweetBlockType getBlockType();

    @NotNull
    Map<String, SweetStyleComponent> getStyles();
}
