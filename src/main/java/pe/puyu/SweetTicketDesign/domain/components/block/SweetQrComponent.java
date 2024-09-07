package pe.puyu.SweetTicketDesign.domain.components.block;

import org.jetbrains.annotations.Nullable;

public record SweetQrComponent(
    @Nullable String data,
    @Nullable String className,
    @Nullable SweetQrType qrType,
    @Nullable SweetQrCorrectionLevel correctionLevel
) {
}
