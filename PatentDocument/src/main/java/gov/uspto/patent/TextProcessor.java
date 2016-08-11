package gov.uspto.patent;

import java.util.List;

public interface TextProcessor {
	public String getPlainText(String rawText);
	public String getSimpleHtml(String rawText);
	public List<String> getParagraphText(String rawText);
}
