package pe.puyu.SweetTicketDesign.domain.components.block;


import org.jetbrains.annotations.Nullable;

public enum SweetJustify {
    CENTER("CENTER"),

    LEFT("LEFT"),

    RIGHT("RIGHT");

    private final String value;

    SweetJustify(String value) {
        this.value = value;
    }

    public static SweetJustify fromValue(String value) {
        for (SweetJustify type : SweetJustify.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return LEFT;
    }

    public static @Nullable SweetJustify fromValueNullable(@Nullable String value) {
        for (SweetJustify type : SweetJustify.values()) {
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
