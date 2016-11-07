package gov.uspto.patent;

import java.util.List;

public interface TextProcessor {
    public String getPlainText(String xmlRawText, FreetextConfig textConfig);
	public String getSimpleHtml(String xmlRawText);
	public List<String> getParagraphText(String xmlRawText);
}
