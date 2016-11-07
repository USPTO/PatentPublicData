package gov.uspto.patent.doc.css_brs;

import java.util.Collections;
import java.util.List;

import gov.uspto.patent.FreetextConfig;
import gov.uspto.patent.TextProcessor;

public class FormattedText implements TextProcessor {

    @Override
    public String getPlainText(String rawText, FreetextConfig textConfig) {
        return rawText;
    }

    @Override
    public String getSimpleHtml(String rawText) {
        return rawText;
    }

    @Override
    public List<String> getParagraphText(String rawText) {
        return Collections.emptyList();
    }
}
