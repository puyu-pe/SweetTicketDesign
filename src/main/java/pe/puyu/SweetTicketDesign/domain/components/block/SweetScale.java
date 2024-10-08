package pe.puyu.SweetTicketDesign.domain.components.block;

import org.jetbrains.annotations.Nullable;

public enum SweetScale {
    DEFAULT("DEFAULT"),

    FAST("FAST"),

    SMOOTH("SMOOTH"),

    REPLICATE("REPLICATE"),

    AREA_AVERAGING("AREA_AVERAGING");

    private final String value;

    SweetScale(@Nullable String value) {
        this.value = value;
    }

    public static SweetScale fromValue(String value) {
        for (SweetScale type : SweetScale.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return SMOOTH;
    }

    public static @Nullable SweetScale fromValueNullable(@Nullable String value) {
        for (SweetScale type : SweetScale.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return null;
    }

    public String getValue() {
        return value;
    }
}
