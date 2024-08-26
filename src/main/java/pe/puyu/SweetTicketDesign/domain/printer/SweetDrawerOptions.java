package pe.puyu.SweetTicketDesign.domain.printer;

import org.jetbrains.annotations.NotNull;
import pe.puyu.SweetTicketDesign.domain.components.drawer.SweetPinConnector;

public record SweetDrawerOptions(
    @NotNull SweetPinConnector pin,
    @NotNull Integer t1,
    @NotNull Integer t2
) {
}
