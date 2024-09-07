package pe.puyu.SweetTicketDesign.domain.designer;

import org.jetbrains.annotations.NotNull;
import pe.puyu.SweetTicketDesign.domain.components.properties.SweetCutMode;

public record SweetProperties(
    @NotNull Integer blockWidth,
    @NotNull CutProperty cutProperty
) {

    public record CutProperty(
        @NotNull Integer feed,
        @NotNull SweetCutMode mode
    ) {

    }

}
