package pe.puyu.SweetTicketDesign.domain.components.block;

import org.jetbrains.annotations.Nullable;

public record SweetImageComponent(
    @Nullable String path,
    @Nullable String className
) {
}
