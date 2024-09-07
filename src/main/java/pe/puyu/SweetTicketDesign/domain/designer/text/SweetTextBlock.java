package pe.puyu.SweetTicketDesign.domain.designer.text;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pe.puyu.SweetTicketDesign.domain.components.block.SweetCellComponent;

import java.util.List;

public record SweetTextBlock(
    @NotNull Character separator,
    @NotNull List<@Nullable List<@Nullable SweetCellComponent>> rows
) {
}
