package pe.puyu.SweetTicketDesign.domain.components.block;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public record SweetBlockComponent(
    @Nullable Character separator,
    @Nullable SweetQrComponent qr,
    @Nullable String imgPath,
    @Nullable Map<@NotNull String, @Nullable SweetStyleComponent> styles,
    @Nullable List<@Nullable List<@Nullable SweetCellComponent>> rows
) {
}
