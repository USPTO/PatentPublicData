package gov.uspto.patent;

import gov.uspto.patent.doc.simplehtml.FreetextConfig;

public abstract class FreetextField implements TextField {

    private TextProcessor textProcessor;

    public FreetextField(TextProcessor formatedTextProcessor) {
        this.textProcessor = formatedTextProcessor;
    }

    public String getText(TextType textType) {
        switch (textType) {
        case RAWTEXT:
            return getRawText();
        case PLAINTEXT:
            return getPlainText();
        case NORMALIZED:
            return getSimpleHtml();
        default:
            return null;
        }
    }

    public String getPlainText() {
        return textProcessor.getPlainText(getRawText(), FreetextConfig.getDefault());
    }

    public String getPlainText(FreetextConfig textConfig) {
        return textProcessor.getPlainText(getRawText(), textConfig);
    }

    public String getSimpleHtml() {
        return textProcessor.getSimpleHtml(getRawText());
    }

    public TextProcessor getTextProcessor() {
        return textProcessor;
    }
}
