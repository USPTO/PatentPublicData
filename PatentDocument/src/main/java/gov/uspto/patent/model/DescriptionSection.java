package gov.uspto.patent.model;

import gov.uspto.patent.TextProcessor;

public class DescriptionSection {

	private final DescSection section;
	private final String rawText;
	private TextProcessor rawTextProcessor;

	public DescriptionSection(final DescSection section, final String rawText, TextProcessor rawTextProcessor) {
		this.section = section;
		this.rawText = rawText;
		this.rawTextProcessor = rawTextProcessor;
	}

	public DescSection getSection() {
		return section;
	}

	public String getRawText() {
		return rawText;
	}

	public String getProcessedText() {
		return rawTextProcessor.getProcessText(rawText);
	}

	public TextProcessor getTextProcessor() {
		return rawTextProcessor;
	}

	@Override
	public String toString() {
		return "DescriptionSection [section=" + section + ", rawText=" + rawText + "]";
	}
}
