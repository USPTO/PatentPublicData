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

import com.google.common.base.Charsets;

import gov.uspto.patent.TextProcessor;

/**
 * Parse and Clean Formated Text Fields, such as Description, Abstract and Claims.
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class FormattedText implements TextProcessor {

	private static final String[] HTML_WHITELIST = new String[] { "p", "table", "tr", "td", "span" }; // "ul", "li"
    private static final String[] HTML_WHITELIST_ATTRIB = new String[] { "class", "id", "num", "idref", "text" };

	@Override
	public String getPlainText(String rawText) {

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


		jsoupDoc.select("paragraph").prepend("\\n    ").append("\\n");
		//jsoupDoc.select("claim-text").prepend("\\n * ");

		jsoupDoc.select("section").prepend("\\n ---------------- \n").append("\\n");

		jsoupDoc.select("table").prepend("\\n").append("\\n");
		jsoupDoc.select("row").append("\\n");
		jsoupDoc.select("entry").append(" | ");

		jsoupDoc.select("ul").append("\\n\\t");
		jsoupDoc.select("ol").append("\\n\\t");
		jsoupDoc.select("li").append("\\n * ");

		jsoupDoc.select("subscript").prepend("_");
		jsoupDoc.select("superscript").prepend("^");

		String textStr = jsoupDoc.html();
		textStr = textStr.replaceAll("\\\\n", "\n");

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

		for (Element element : jsoupDoc.select("cross-reference-to-related-applications")) {
			element.children().remove();
			element.remove();
		}

		for (Element element : jsoupDoc.select("heading")) {
			element.remove();
		}

		// Remove first paragraph in drawing description if it does not have a
		// figref.
		for (Element element : jsoupDoc.select("brief-description-of-drawings section paragraph:first-child")) {
			if (element.select(":has(cross-reference)").isEmpty()) {
				// System.err.println("Drawing Description without
				// Patent-Figure" + element.html());
				element.remove();
			}
		}

		for (Element element : jsoupDoc.select("cross-reference")) {
            element.tagName("span");
            element.addClass("figref");
            String text = element.text();
            element.attr("text", text);
            element.text("Patent-Figure");
			//element.replaceWith(new TextNode("Patent-Figure", null));
		}

		for (Element element : jsoupDoc.select("dependent-claim-reference")) {
            element.addClass("claim");
            String text = element.text();
            element.attr("text", text);
            element.text("Patent-Claim");
            element.tagName("span");
			//element.replaceWith(new TextNode("Patent-Claim", null));
		}

		// Remove Paragraph Numbers.
		jsoupDoc.select("paragraph number:first-child").remove();

		jsoupDoc.select("subscript").prepend("_");
		jsoupDoc.select("superscript").prepend("^");
		jsoupDoc.select("section").prepend("\\n\\n    ");

		// Rename all "paragraph" tags to "p".
		jsoupDoc.select("paragraph").tagName("p");

		// Table Elements.
		jsoupDoc.select("row").tagName("tr");
		jsoupDoc.select("entry").append("td");

		// handle nested claim-text similar as paragraphs.
		//jsoupDoc.select("claim-text").tagName("li");

		// Single List Type.
		jsoupDoc.select("ol").tagName("ul");

		String textStr = jsoupDoc.html();
		textStr = textStr.replaceAll("\\\\n", "\n");

		// Whitelist whitelist = Whitelist.simpleText();
		Whitelist whitelist = Whitelist.none();
		whitelist.addTags(HTML_WHITELIST);
        whitelist.addAttributes(":all", HTML_WHITELIST_ATTRIB);

		OutputSettings outSettings = new Document.OutputSettings();
		outSettings.charset(Charsets.UTF_8);
		outSettings.prettyPrint(false);
		outSettings.escapeMode(EscapeMode.extended);

		String fieldTextCleaned = Jsoup.clean(textStr, "", whitelist, outSettings);

		return fieldTextCleaned;
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
