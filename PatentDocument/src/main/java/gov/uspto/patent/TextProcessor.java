package gov.uspto.patent;

import java.util.List;

import gov.uspto.patent.doc.simplehtml.FreetextConfig;

public interface TextProcessor {
    public String getPlainText(String xmlRawText, FreetextConfig textConfig);
	public String getSimpleHtml(String xmlRawText);
	public List<String> getParagraphText(String xmlRawText);
}
