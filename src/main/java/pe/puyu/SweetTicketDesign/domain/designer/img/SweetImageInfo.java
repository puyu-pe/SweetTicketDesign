package pe.puyu.SweetTicketDesign.domain.designer.img;

import org.jetbrains.annotations.NotNull;

public record SweetImageInfo(
    @NotNull String path,
    @NotNull String className
) {
}
