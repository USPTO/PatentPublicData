package gov.uspto.patent;

import java.util.List;

public interface TextProcessor {
	public String getProcessText(String rawText);
	public List<String> getParagraphText(String rawText);
}
