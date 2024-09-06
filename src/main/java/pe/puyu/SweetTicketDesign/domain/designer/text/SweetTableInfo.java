package pe.puyu.SweetTicketDesign.domain.designer.text;

import org.jetbrains.annotations.NotNull;

public record SweetTableInfo(
    @NotNull Character separator
) {
    public SweetTableInfo(SweetTableInfo tableInfo) {
        this(tableInfo.separator);
    }
}
