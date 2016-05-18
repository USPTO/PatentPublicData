package gov.uspto.patent.model;

import gov.uspto.patent.TextProcessor;

public class Abstract {

	private final String rawText;
	private TextProcessor rawTextProcessor;

	public Abstract(String rawText, TextProcessor rawTextProcessor) {
		this.rawText = rawText;
		this.rawTextProcessor = rawTextProcessor;
	}

	public String getRawText() {
		return rawText;
	}

	public String getProcessedText() {
		return rawTextProcessor.getProcessText(rawText);
	}
}
