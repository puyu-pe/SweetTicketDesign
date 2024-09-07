package pe.puyu.SweetTicketDesign.domain.designer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pe.puyu.SweetTicketDesign.domain.designer.img.SweetImageStyle;
import pe.puyu.SweetTicketDesign.domain.designer.qr.SweetQrStyle;
import pe.puyu.SweetTicketDesign.domain.designer.text.SweetCell;
import pe.puyu.SweetTicketDesign.domain.designer.text.SweetStringStyle;
import pe.puyu.SweetTicketDesign.domain.components.block.*;
import pe.puyu.SweetTicketDesign.domain.printer.SweetPrinterStyle;

import java.text.Normalizer;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

public class SweetDesignHelper {

    private final @NotNull SweetProperties _properties;
    private final @NotNull SweetStyleComponent _defaultStyle;
    private final @NotNull Map<String, @Nullable SweetStyleComponent> _styles;

    public SweetDesignHelper(
        @NotNull SweetProperties properties,
        @NotNull SweetStyleComponent defaultStyle,
        @NotNull Map<String, @Nullable SweetStyleComponent> styles
    ) {
        this._properties = properties;
        this._defaultStyle = defaultStyle;
        this._styles = styles;
    }

    public @NotNull SweetPrinterStyle makePrinterStyleFor(@NotNull String className) {
        int fontWidth = Optional.ofNullable(_defaultStyle.fontWidth()).orElse(1);
        int fontHeight = Optional.ofNullable(_defaultStyle.fontHeight()).orElse(1);
        boolean bold = Optional.ofNullable(_defaultStyle.bold()).orElse(false);
        boolean bgInverted = Optional.ofNullable(_defaultStyle.bgInverted()).orElse(false);
        String charCode = Optional.ofNullable(_defaultStyle.charCode()).orElse("WPC1252");
        fontWidth = findStylePropertyByClass(className, fontWidth, SweetStyleComponent::fontWidth);
        fontHeight = findStylePropertyByClass(className, fontHeight, SweetStyleComponent::fontHeight);
        bold = findStylePropertyByClass(className, bold, SweetStyleComponent::bold);
        bgInverted = findStylePropertyByClass(className, bgInverted, SweetStyleComponent::bgInverted);
        charCode = findStylePropertyByClass(className, charCode, SweetStyleComponent::charCode);
        return new SweetPrinterStyle(fontWidth, fontHeight, bold, bgInverted, charCode);
    }

    public @NotNull SweetStringStyle makeSweetStringStyleFor(@NotNull String className) {
        int charxels = Optional.ofNullable(_defaultStyle.charxels()).orElse(1);
        char pad = Optional.ofNullable(_defaultStyle.pad()).orElse(' ');
        SweetJustify align = Optional.ofNullable(_defaultStyle.align()).orElse(SweetJustify.LEFT);
        boolean normalize = Optional.ofNullable(_defaultStyle.normalize()).orElse(false);
        charxels = findStylePropertyByClass(className, charxels, SweetStyleComponent::charxels);
        pad = findStylePropertyByClass(className, pad, SweetStyleComponent::pad);
        align = findStylePropertyByClass(className, align, SweetStyleComponent::align);
        normalize = findStylePropertyByClass(className, normalize, SweetStyleComponent::normalize);
        charxels = Math.max(Math.min(charxels, _properties.blockWidth()), 0); // normalize charxels
        return new SweetStringStyle(charxels, pad, align, normalize);
    }

    public @NotNull <U> U findStylePropertyByClass(String className, U defaultValue, Function<? super SweetStyleComponent, U> mapper) {
        Optional<SweetStyleComponent> findByComodin = Optional.ofNullable(_styles.get("*"));
        Optional<SweetStyleComponent> findByClassName = Optional.ofNullable(_styles.get(className));
        U value = findByComodin.map(mapper).orElse(defaultValue);
        return findByClassName.map(mapper).orElse(value);
    }

    public @NotNull SweetImageStyle makeImageStyle(@NotNull String className) {
        SweetScale sweetScale = Optional.ofNullable(_defaultStyle.scale()).orElse(SweetScale.SMOOTH);
        int width = Optional.ofNullable(_defaultStyle.width()).orElse(290);
        int height = Optional.ofNullable(_defaultStyle.height()).orElse(290);
        SweetJustify align = Optional.ofNullable(_defaultStyle.align()).orElse(SweetJustify.LEFT);
        sweetScale = findStylePropertyByClass(className, sweetScale, SweetStyleComponent::scale);
        width = findStylePropertyByClass(className, width, SweetStyleComponent::width);
        height = findStylePropertyByClass(className, height, SweetStyleComponent::height);
        align = findStylePropertyByClass(className, align, SweetStyleComponent::align);
        //normalize width and height
        width = Math.max(Math.min(width, calcWidthPaperInPx()), 0);
        height = Math.max(0, height);
        return new SweetImageStyle(sweetScale, width, height, align);
    }

    public @NotNull SweetQrStyle makeQrStyle(@NotNull String className) {
        SweetJustify align = Optional.ofNullable(_defaultStyle.align()).orElse(SweetJustify.CENTER);
        int size = Optional.ofNullable(_defaultStyle.width()).orElse(250);
        SweetScale scale = Optional.ofNullable(_defaultStyle.scale()).orElse(SweetScale.SMOOTH);
        align = findStylePropertyByClass(className, align, SweetStyleComponent::align);
        size = findStylePropertyByClass(className, size, SweetStyleComponent::width); // first height
        size = findStylePropertyByClass(className, size, SweetStyleComponent::height); // priority width
        scale = findStylePropertyByClass(className, scale, SweetStyleComponent::scale);
        //normalize size
        size = Math.max(0, Math.min(size, calcWidthPaperInPx()));
        return new SweetQrStyle(align, size, scale);
    }

    public @NotNull List<String> wrapText(String text, int numberOfCharactersAvailable, int fontWidth) {
        List<String> wrappedText = new LinkedList<>();
        if (text.length() * fontWidth <= numberOfCharactersAvailable) {
            wrappedText.add(text);
            return wrappedText;
        }
        numberOfCharactersAvailable = Math.max(0, numberOfCharactersAvailable);
        fontWidth = Math.max(0, fontWidth);
        String[] splitWords = text.split("\\s+"); // divide in words
        List<String> words = new LinkedList<>();
        String space = " ";
        for (String word : splitWords) {
            int sliceWidth = fontWidth == 0 ? 0 : numberOfCharactersAvailable / fontWidth;
            words.addAll(sliceWordInEqualParts(word, sliceWidth));
        }
        for (int i = 0; i < words.size(); ++i) {
            StringBuilder newString = new StringBuilder();
            String currentWord = words.get(i);
            int numberCharactersCoveredByCurrentWord = currentWord.length() * fontWidth;
            int midCharacterCovered = 0;
            while ((newString.length() * fontWidth) + numberCharactersCoveredByCurrentWord + midCharacterCovered <= numberOfCharactersAvailable) {
                newString.append(newString.isEmpty() ? currentWord : space + currentWord);
                midCharacterCovered = 1;
                ++i;
                if (i >= words.size())
                    break;
                currentWord = words.get(i);
                numberCharactersCoveredByCurrentWord = currentWord.length() * fontWidth;
            }
            wrappedText.add(newString.toString());
            --i;
        }
        return wrappedText;
    }

    public @NotNull List<String> sliceWordInEqualParts(String word, int sliceWidth) {
        int lastPart = sliceWidth <= 0 ? 0 : word.length() % sliceWidth;
        lastPart = lastPart > 0 ? 1 : 0;
        int numberOfParts = sliceWidth <= 0 ? 0 : word.length() / sliceWidth + lastPart;
        List<String> slicedWords = new LinkedList<>();
        for (int currentPart = 0, startIndex = 0; currentPart < numberOfParts; ++currentPart, startIndex += sliceWidth) {
            int endIndex = Math.min(startIndex + sliceWidth, word.length());
            slicedWords.add(word.substring(startIndex, endIndex));
        }
        return slicedWords;
    }

    public @NotNull SweetProperties getProperties() {
        return _properties;
    }

    public @NotNull SweetCell normalize(@NotNull SweetCell cell) {
        if (cell.stringStyle().normalize()) {
            String normalized = Normalizer.normalize(cell.text(), Normalizer.Form.NFD);
            String textNormalized = Pattern.compile("\\p{InCombiningDiacriticalMarks}+").matcher(normalized)
                .replaceAll("")
                .replaceAll("[^\\p{ASCII}]", "");
            return new SweetCell(
                textNormalized,
                new SweetPrinterStyle(cell.printerStyle()),
                new SweetStringStyle(cell.stringStyle())
            );
        }
        return new SweetCell(cell);
    }

    public int calcWidthPaperInPx() {
        int pixelsPerCharacter = 11;
        return _properties.blockWidth() * pixelsPerCharacter + _properties.blockWidth();
    }

}

