package pe.puyu.SweetTicketDesign.domain.designer.qr;

import org.jetbrains.annotations.NotNull;

public record SweetQrBlock(
    @NotNull Integer widthInPx,
    @NotNull SweetQrInfo info,
    @NotNull SweetQrStyle style
) {
}
