package pe.puyu.SweetTicketDesign.domain.printer;

import org.jetbrains.annotations.NotNull;
import pe.puyu.SweetTicketDesign.domain.components.properties.SweetCutMode;

public record SweetCutOptions(
    @NotNull Integer feed,
    @NotNull SweetCutMode mode
) {
}
