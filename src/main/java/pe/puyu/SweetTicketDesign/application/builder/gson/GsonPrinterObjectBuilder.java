package pe.puyu.SweetTicketDesign.application.builder.gson;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pe.puyu.SweetTicketDesign.domain.components.block.*;
import pe.puyu.SweetTicketDesign.domain.components.SweetPrinterObjectComponent;
import pe.puyu.SweetTicketDesign.domain.components.drawer.SweetOpenDrawerComponent;
import pe.puyu.SweetTicketDesign.domain.components.drawer.SweetPinConnector;
import pe.puyu.SweetTicketDesign.domain.components.properties.SweetCutMode;
import pe.puyu.SweetTicketDesign.domain.components.properties.SweetCutComponent;
import pe.puyu.SweetTicketDesign.domain.components.properties.SweetPropertiesComponent;
import pe.puyu.SweetTicketDesign.domain.builder.SweetPrinterObjectBuilder;
import pe.puyu.SweetTicketDesign.domain.builder.DesignObjectBuilderException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GsonPrinterObjectBuilder implements SweetPrinterObjectBuilder {

    private final JsonObject printerObject;

    public GsonPrinterObjectBuilder(@NotNull String jsonString) {
        this(JsonParser.parseString(jsonString).getAsJsonObject());
    }

    public GsonPrinterObjectBuilder(@NotNull JsonObject jsonObject) {
        this.printerObject = jsonObject;
    }

    @Override
    public @NotNull SweetPrinterObjectComponent build() {
        try {
            SweetPropertiesComponent properties = buildPropertiesComponent();
            List<SweetBlockComponent> blocks = buildBlockComponent();
            SweetOpenDrawerComponent openDrawer = buildOpenDrawerComponent();
            Map<String, SweetStyleComponent> styles = buildStylesComponent();
            return new SweetPrinterObjectComponent(properties, blocks, openDrawer, styles);
        } catch (Exception e) {
            throw new DesignObjectBuilderException(String.format("GsonPrinterObjectBuilder throw an exception: %s", e.getMessage()), e);
        }
    }

    private @Nullable SweetPropertiesComponent buildPropertiesComponent() {
        if (printerObject.has("properties") && printerObject.get("properties").isJsonObject()) {
            GsonObject propertiesElement = new GsonObject(printerObject.get("properties").getAsJsonObject());
            return new SweetPropertiesComponent(
                propertiesElement.getInt("blockWidth"),
                buildPrinterCutModeProperty(propertiesElement.getElement("cut"))
            );
        }
        return null;
    }

    private @Nullable List<SweetBlockComponent> buildBlockComponent() {
        if (printerObject.has("data")) {
            List<SweetBlockComponent> blocks = new LinkedList<>();
            JsonElement element = printerObject.get("data");
            if (element.isJsonObject()) {
                blocks.add(toBlockComponent(element));
            } else if (element.isJsonArray()) {
                element.getAsJsonArray().forEach(blockElement -> blocks.add(toBlockComponent(blockElement)));
            }
            return blocks;
        }
        return null;
    }

    private @Nullable SweetBlockComponent toBlockComponent(JsonElement element) {
        if (element.isJsonPrimitive()) {
            SweetCellComponent cell = new SweetCellComponent(null, element.getAsString());
            List<SweetCellComponent> row = new LinkedList<>();
            row.add(cell);
            List<List<SweetCellComponent>> rows = new LinkedList<>();
            rows.add(row);
            return new SweetBlockComponent(
                SweetBlockType.TEXT,
                null,
                null,
                null,
                rows
            );
        } else if (element.isJsonObject()) {
            GsonObject blockElement = new GsonObject(element.getAsJsonObject());
            return new SweetBlockComponent(
                SweetBlockType.fromValueNullable(blockElement.getString("type")),
                blockElement.getCharacter("separator"),
                buildQrComponent(blockElement.getElement("qr")),
                buildImageComponent(blockElement.getElement("img")),
                buildRows(blockElement.getElement("rows"))
            );
        }
        return null;
    }

    private @Nullable Map<String, SweetStyleComponent> buildStylesComponent() {
        if (printerObject.has("styles") && printerObject.get("styles").isJsonObject()) {
            Map<String, SweetStyleComponent> styles = new HashMap<>();
            printerObject.getAsJsonObject("styles").asMap().forEach((key, element) -> styles.put(key, toStyleComponent(element)));
            return styles;
        }
        return null;
    }

    private @Nullable SweetStyleComponent toStyleComponent(@NotNull JsonElement element) {
        if (element.isJsonObject()) {
            GsonObject styleElement = new GsonObject(element.getAsJsonObject());
            return new SweetStyleComponent(
                styleElement.getInt("fontWidth"),
                styleElement.getInt("fontHeight"),
                styleElement.getBoolean("bold"),
                styleElement.getBoolean("normalize"),
                styleElement.getBoolean("bgInverted"),
                styleElement.getCharacter("pad"),
                SweetJustify.fromValueNullable(styleElement.getString("align")),
                styleElement.getInt("charxels"),
                SweetScale.fromValueNullable(styleElement.getString("scale")),
                styleElement.getInt("width"),
                styleElement.getInt("height"),
                styleElement.getString("charCode")
            );
        }
        return null;
    }

    private @Nullable List<List<SweetCellComponent>> buildRows(@Nullable JsonElement element) {
        if (element != null && element.isJsonArray()) {
            List<List<SweetCellComponent>> rows = new LinkedList<>();
            element.getAsJsonArray().forEach(rowElement -> rows.add(toRow(rowElement)));
            return rows;
        }
        return null;
    }

    private List<SweetCellComponent> toRow(@NotNull JsonElement element) {
        List<SweetCellComponent> row = new LinkedList<>();
        if (element.isJsonPrimitive()) {
            SweetCellComponent cell = new SweetCellComponent(null, element.getAsString());
            row.add(cell);
            return row;
        } else if (element.isJsonObject()) {
            GsonObject cellElement = new GsonObject(element.getAsJsonObject());
            SweetCellComponent cell = new SweetCellComponent(cellElement.getString("class"), cellElement.getString("text"));
            row.add(cell);
            return row;
        } else if (element.isJsonArray()) {
            element.getAsJsonArray().forEach(item -> row.addAll(toRow(item)));
        }
        return row;
    }

    private @Nullable SweetCutComponent buildPrinterCutModeProperty(@Nullable JsonElement element) {
        if (element != null && element.isJsonObject()) {
            GsonObject cut = new GsonObject(element.getAsJsonObject());
            return new SweetCutComponent(
                cut.getInt("feed"),
                SweetCutMode.fromValue(cut.getString("mode"))
            );
        }
        return null;
    }

    private @Nullable SweetOpenDrawerComponent buildOpenDrawerComponent() {
        if (printerObject.has("openDrawer")) {
            JsonElement openDrawer = printerObject.get("openDrawer");
            if (openDrawer.isJsonObject()) {
                GsonObject openDrawerElement = new GsonObject(printerObject.get("openDrawer").getAsJsonObject());
                return new SweetOpenDrawerComponent(
                    SweetPinConnector.fromValue(openDrawerElement.getInt("pin")),
                    openDrawerElement.getInt("t1"),
                    openDrawerElement.getInt("t2")
                );
            } else if (openDrawer.isJsonPrimitive() && openDrawer.getAsJsonPrimitive().getAsBoolean()) {
                return new SweetOpenDrawerComponent(null, null, null);
            }
        }
        return null;
    }

    private @Nullable SweetQrComponent buildQrComponent(@Nullable JsonElement element) {
        if (element == null) {
            return null;
        }
        if (element.isJsonPrimitive()) {
            String data = element.getAsString();
            return new SweetQrComponent(data, "", null, null);
        } else if (element.isJsonObject()) {
            GsonObject qrElement = new GsonObject(element.getAsJsonObject());
            return new SweetQrComponent(
                qrElement.getString("data"),
                qrElement.getString("class"),
                SweetQrType.fromValue(qrElement.getString("type")),
                SweetQrCorrectionLevel.fromValue(qrElement.getString("correctionLevel"))
            );
        }
        return null;
    }

    public @Nullable SweetImageComponent buildImageComponent(@Nullable JsonElement element) {
        if (element == null) {
            return null;
        }
        if (element.isJsonPrimitive()) {
            String path = element.getAsString();
            return new SweetImageComponent(path, "");
        } else if (element.isJsonObject()) {
            GsonObject qrElement = new GsonObject(element.getAsJsonObject());
            return new SweetImageComponent(
                qrElement.getString("path"),
                qrElement.getString("class")
            );
        }
        return null;
    }

}
