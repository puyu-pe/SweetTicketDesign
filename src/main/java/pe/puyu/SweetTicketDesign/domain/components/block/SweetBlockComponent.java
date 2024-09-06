package pe.puyu.SweetTicketDesign.domain.components.block;

import org.jetbrains.annotations.Nullable;

import java.util.*;

public record SweetBlockComponent(
    @Nullable SweetBlockType type,
    @Nullable Character separator,
    @Nullable SweetQrComponent qr,
    @Nullable SweetImageComponent img,
    @Nullable List<@Nullable List<@Nullable SweetCellComponent>> rows
) {
}
