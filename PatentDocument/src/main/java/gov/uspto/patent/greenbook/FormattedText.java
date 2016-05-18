package gov.uspto.patent.greenbook;

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
import org.jsoup.select.Elements;

import com.google.common.base.Charsets;

import gov.uspto.patent.TextProcessor;

/**
 * Parse and Clean Formated Text Fields, such as Description, Abstract and Claims.
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class FormattedText implements TextProcessor {

	private static final Pattern CLAIM_REF = Pattern.compile("\\bclaim ([0-9](?:-[0-9])?)\\b");
	private static final Pattern PATENT_FIG = Pattern.compile("\\bFIG. ([0-30](?:-[0-30])?)\\b");

	@Override
	public String getProcessText(String rawText) {
		Document jsoupDoc = Jsoup.parse(rawText, "", Parser.xmlParser());

		for (Element element : jsoupDoc.select("PAC")) { // Header Field.
			element.remove();
		}
		jsoupDoc.select("TBL").append("\\n.\\n"); // Table Field

		// Rename all "para" tags to "p".
		Elements elements = jsoupDoc.select("PAR");
		elements.tagName("p");
		//jsoupDoc.select("PAR").prepend("\\n.\\n    "); // Paragraph Field.

		String textStr = jsoupDoc.html();
		textStr = textStr.replaceAll("\\\\n", "\n");
		textStr = CLAIM_REF.matcher(textStr).replaceAll("Patent-Claim");
		textStr = PATENT_FIG.matcher(textStr).replaceAll("Patent-Figure");

		//Whitelist whitelist = Whitelist.simpleText();
		Whitelist whitelist = Whitelist.none();
		whitelist.addTags("p");
		//whitelist.addTags("mtable");
		//whitelist.addTags("mtr");
		//whitelist.addTags("mrow");

		OutputSettings outSettings = new Document.OutputSettings();
		outSettings.charset(Charsets.UTF_8);
		outSettings.prettyPrint(false);
		outSettings.escapeMode(EscapeMode.extended);

		String fieldTextCleaned = Jsoup.clean(textStr, "", whitelist, outSettings);

		return fieldTextCleaned;
	}

	@Override
	public List<String> getParagraphText(String rawText) {
		String textWithPMarks = getProcessText(rawText);
		Document jsoupDoc = Jsoup.parse(textWithPMarks, "", Parser.xmlParser());

		List<String> paragraphs = new ArrayList<String>();
		for (Element element : jsoupDoc.select("p")) {
			paragraphs.add(element.html());
		}

		return paragraphs;
	}
}
