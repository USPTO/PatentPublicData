package gov.uspto.patent.doc.greenbook;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Whitelist;

import com.google.common.base.Charsets;

import gov.uspto.patent.TextProcessor;

/**
 * Parse and Clean Formated Text Fields, such as Description, Abstract and Claims.
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class FormattedText implements TextProcessor {

	private static final Pattern CLAIM_REF = Pattern.compile("\\bclaim ([0-9](?:(?: or |-)[0-9])?)\\b");
	private static final Pattern PATENT_FIG = Pattern.compile("\\bFIGS?. ([0-30][()A-z]*(?:(?: to |-)[0-30][()A-z]*)?)\\b");

	private static final String[] HTML_WHITELIST = new String[] { "p", "table", "tr", "td"  }; // "ul", "li"

	@Override
	public String getPlainText(String rawText) {
		Document jsoupDoc = Jsoup.parse(rawText, "", Parser.xmlParser());

		for (Element element : jsoupDoc.select("PAC")) { // Header Field.
			element.remove();
		}
		
		jsoupDoc.select("TBL").append("\\n.\\n"); // Table Field

		jsoupDoc.select("PAR").prepend("\\n\\t").append("\\n");

		String textStr = jsoupDoc.html();
		textStr = cleanText(textStr);

		OutputSettings outSettings = new Document.OutputSettings();
		outSettings.charset(Charsets.UTF_8);
		outSettings.prettyPrint(false);
		outSettings.escapeMode(EscapeMode.extended);

		String fieldTextCleaned = Jsoup.clean(textStr, "", Whitelist.none(), outSettings);

		return fieldTextCleaned;
	}

	@Override
	public String getSimpleHtml(String rawText) {
		Document jsoupDoc = Jsoup.parse(rawText, "", Parser.xmlParser());

		// Remove Header Fields.
		jsoupDoc.select("PAC").remove();

		// Rename all "para" tags to "p".
		jsoupDoc.select("PAR").tagName("p");
		jsoupDoc.select("TBL").tagName("table");

		String textStr = jsoupDoc.html();
		textStr = cleanText(textStr);

		// Whitelist whitelist = Whitelist.simpleText();
		Whitelist whitelist = Whitelist.none();
		whitelist.addTags(HTML_WHITELIST);

		OutputSettings outSettings = new Document.OutputSettings();
		outSettings.charset(Charsets.UTF_8);
		outSettings.prettyPrint(false);
		outSettings.escapeMode(EscapeMode.extended);

		String fieldTextCleaned = Jsoup.clean(textStr, "", whitelist, outSettings);

		return fieldTextCleaned;
	}

	/**
	 * Transform Patent Figure and Patent Claims and their accompanied number
	 * to simply "Patent-Claim" or "Patent-Figure"
	 * 
	 * @param text
	 * @return
	 */
	protected String cleanText(String text) {
		text = text.replaceAll("\\\\n", "\n");
		text = CLAIM_REF.matcher(text).replaceAll("Patent-Claim");
		text = PATENT_FIG.matcher(text).replaceAll("Patent-Figure");
		return text;
	}

	@Override
	public List<String> getParagraphText(String rawText) {
		String textWithPMarks = getSimpleHtml(rawText);
		Document jsoupDoc = Jsoup.parse(textWithPMarks, "", Parser.xmlParser());

		List<String> paragraphs = new ArrayList<String>();
		for (Element element : jsoupDoc.select("p")) {
			paragraphs.add(element.html());
		}

		return paragraphs;
	}
}
