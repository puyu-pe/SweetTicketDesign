package pe.puyu.SweetTicketDesign.application.components;

import org.jetbrains.annotations.NotNull;
import pe.puyu.SweetTicketDesign.domain.components.block.*;
import pe.puyu.SweetTicketDesign.domain.components.drawer.SweetOpenDrawerComponent;
import pe.puyu.SweetTicketDesign.domain.components.drawer.SweetPinConnector;
import pe.puyu.SweetTicketDesign.domain.components.properties.SweetCutMode;
import pe.puyu.SweetTicketDesign.domain.components.properties.SweetCutComponent;
import pe.puyu.SweetTicketDesign.domain.components.properties.SweetPropertiesComponent;
import pe.puyu.SweetTicketDesign.domain.components.SweetDefaultComponentsProvider;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DefaultComponentsProvider implements SweetDefaultComponentsProvider {

    @Override
    public @NotNull SweetPropertiesComponent getPropertiesComponent() {
        int charactersWidth = 42; // 42 characters equals 72 mm approx.
        return new SweetPropertiesComponent(
            charactersWidth,
            new SweetCutComponent(4, SweetCutMode.PART)
        );
    }

    @Override
    public @NotNull SweetStyleComponent getStyleComponent() {
        return new SweetStyleComponent(
            1,
            1,
            false,
            false,
            false,
            ' ',
            SweetJustify.LEFT,
            0,
            SweetScale.SMOOTH,
            290,
            29,
            "WPC1252"
        );
    }

    @Override
    public @NotNull Character getSeparator() {
        return ' ';
    }

    @Override
    public @NotNull String getStringQr() {
        return "";
    }

    @Override
    public @NotNull SweetQrType getQrType() {
        return SweetQrType.IMG;
    }

    @Override
    public @NotNull SweetQrCorrectionLevel getQrCorrectionLevel() {
        return SweetQrCorrectionLevel.Q;
    }

    @Override
    public @NotNull String getImagePath() {
        return "";
    }

    @Override
    public @NotNull SweetOpenDrawerComponent getOpenDrawerComponent() {
        return new SweetOpenDrawerComponent(SweetPinConnector.Pin_2, 120, 240);
    }

    @Override
    public @NotNull List<SweetBlockComponent> getDataComponent() {
        return new LinkedList<>();
    }

    @Override
    public @NotNull SweetBlockType getBlockType() {
        return SweetBlockType.TEXT;
    }

    @Override
    public @NotNull Map<String, SweetStyleComponent> getStyles() {
        return Map.of();
    }
}
