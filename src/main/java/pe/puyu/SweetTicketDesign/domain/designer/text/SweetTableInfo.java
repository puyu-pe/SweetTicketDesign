package pe.puyu.SweetTicketDesign.domain.designer.text;

import org.jetbrains.annotations.NotNull;

public record SweetTableInfo(
    @NotNull Integer gap,
    @NotNull Character separator
) {

    public SweetTableInfo(SweetTableInfo tableInfo) {
        this(tableInfo.gap, tableInfo.separator);
    }
}
