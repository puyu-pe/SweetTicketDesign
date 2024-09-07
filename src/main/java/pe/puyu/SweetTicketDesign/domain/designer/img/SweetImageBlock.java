package pe.puyu.SweetTicketDesign.domain.designer.img;

import org.jetbrains.annotations.NotNull;

public record SweetImageBlock(
    @NotNull SweetImageInfo info,
    @NotNull Integer widthInPx,
    @NotNull SweetImageStyle style
) {
}
