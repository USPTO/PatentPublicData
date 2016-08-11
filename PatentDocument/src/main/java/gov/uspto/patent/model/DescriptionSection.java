package gov.uspto.patent.model;

import gov.uspto.patent.FreetextField;
import gov.uspto.patent.TextProcessor;

public class DescriptionSection extends FreetextField {

	private final DescSection section;
	private String rawText;

	public DescriptionSection(final DescSection section, final String rawText, TextProcessor rawTextProcessor) {
		super(rawTextProcessor);
		this.section = section;
		this.rawText = rawText;
	}

	public DescSection getSection() {
		return section;
	}

	@Override
	public void setRawText(String fieldRawText) {
		this.rawText = fieldRawText;
	}

	@Override
	public String getRawText() {
		return rawText;
	}

	@Override
	public String toString() {
		return "DescriptionSection [section=" + section + ", rawText=" + rawText + "]";
	}
}
