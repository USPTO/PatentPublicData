package gov.uspto.patent.validate;

import java.util.List;

import gov.uspto.patent.TextProcessor;
import gov.uspto.patent.doc.simplehtml.FreetextConfig;

public class DummyFormattedText implements TextProcessor {

	@Override
	public String getPlainText(String xmlRawText, FreetextConfig textConfig) {
		return xmlRawText;
	}

	@Override
	public String getSimpleHtml(String xmlRawText) {
		return null;
	}

	@Override
	public List<String> getParagraphText(String xmlRawText) {
		return null;
	}
}
