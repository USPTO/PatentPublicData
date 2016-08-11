package gov.uspto.patent.model;

import gov.uspto.patent.FreetextField;
import gov.uspto.patent.TextProcessor;

public class Abstract extends FreetextField {

	private String rawText;

	public Abstract(String rawText, TextProcessor rawTextProcessor) {
		super(rawTextProcessor);
		this.rawText = rawText;
	}

	@Override
	public void setRawText(String fieldRawText) {
		this.rawText = fieldRawText;
	}
	
	public String getRawText() {
		return rawText;
	}

	@Override
	public String toString() {
		return "Abstract [rawText=" + rawText + "]";
	}
}
