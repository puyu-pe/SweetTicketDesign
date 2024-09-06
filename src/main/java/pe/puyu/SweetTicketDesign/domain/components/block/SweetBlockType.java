package pe.puyu.SweetTicketDesign.domain.components.block;

import org.jetbrains.annotations.Nullable;

public enum SweetBlockType {
    IMG("IMG"),

    QR("QR"),

    TEXT("TEXT");

    private final String value;

    SweetBlockType(String value) {
        this.value = value;
    }

    public static SweetBlockType fromValue(String value) {
        for (SweetBlockType type : SweetBlockType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return TEXT;
    }

    public static @Nullable SweetBlockType fromValueNullable(@Nullable String value) {
        if(value == null) return null;
        else return fromValue(value);
    }

    public String getValue() {
        return value;
    }


}
