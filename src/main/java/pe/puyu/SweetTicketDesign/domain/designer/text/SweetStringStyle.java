package pe.puyu.SweetTicketDesign.domain.designer.text;

import org.jetbrains.annotations.NotNull;
import pe.puyu.SweetTicketDesign.domain.components.block.SweetJustify;

public record SweetStringStyle(
    @NotNull Integer charxels,
    @NotNull Character pad,
    @NotNull SweetJustify align,
    @NotNull Boolean normalize
) {
    public SweetStringStyle(SweetStringStyle stringStyle) {
        this(
            stringStyle.charxels,
            stringStyle.pad,
            stringStyle.align,
            stringStyle.normalize
        );
    }
}
