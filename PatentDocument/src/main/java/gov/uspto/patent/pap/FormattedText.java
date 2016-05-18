package gov.uspto.patent.pap;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.nodes.TextNode;
import org.jsoup.nodes.Document.OutputSettings;
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

	@Override
	public String getProcessText(String rawText) {

		Document jsoupDoc = Jsoup.parse(rawText, "", Parser.xmlParser());

		for (Element element : jsoupDoc.select("cross-reference-to-related-applications")) {
			element.children().remove();
			element.remove();
		}

		for (Element element : jsoupDoc.select("heading")) {
			element.remove();
		}

		// Remove first paragraph in drawing description if it does not have a figref.
		for (Element element : jsoupDoc.select("brief-description-of-drawings section paragraph:first-child")) {
			if (element.select(":has(cross-reference)").isEmpty()) {
				//System.err.println("Drawing Description without Patent-Figure" + element.html());
				element.remove();
			}
		}

		for (Element element : jsoupDoc.select("cross-reference")) {
			element.replaceWith(new TextNode("Patent-Figure", null));
		}

		for (Element element : jsoupDoc.select("dependent-claim-reference")) {
			element.replaceWith(new TextNode("Patent-Claim", null));
		}

		// Remove Paragraph Numbers.
		for (Element element : jsoupDoc.select("paragraph number:first-child")) {
			element.remove();
		}

		// Rename all "paragraph" tags to "p".
		Elements elements = jsoupDoc.select("paragraph");
		elements.tagName("p");
		//jsoupDoc.select("paragraph").prepend("\\n.\\n    ");

		// handle nested claim-text similar to paragraphs.
		jsoupDoc.select("claim-text").append("\\n.\\n    ");

		jsoupDoc.select("section").prepend("\\n\\n    ");

		jsoupDoc.select("table").prepend("\\n");
		jsoupDoc.select("table").append("\\n");
		jsoupDoc.select("row").prepend(".\\n"); // period added to break block for NLP.
		jsoupDoc.select("entry").append(" | ");

		jsoupDoc.select("ul").prepend("\\n");
		jsoupDoc.select("ol").prepend("\\n");
		jsoupDoc.select("li").append("\\n");

		jsoupDoc.select("subscript").prepend("_");
		jsoupDoc.select("superscript").prepend("^");

		String textStr = jsoupDoc.html();
		textStr = textStr.replaceAll("\\\\n", "\n");

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
