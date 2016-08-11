package gov.uspto.patent.sgml;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
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

	private static final String[] HTML_WHITELIST = new String[] { "p", "table", "tr", "td" }; // "ul", "li"
	
	@Override
	public String getPlainText(String rawText) {
		Document jsoupDoc = Jsoup.parse(rawText, "", Parser.xmlParser());

		for (Element element : jsoupDoc.select("CLREF")) {
			element.replaceWith(new TextNode("Patent-Claim", null));
		}

		// Remove paragraph in drawing description which does not have a figref.
		for (Element element : jsoupDoc.select("DRWDESC BTEXT PARA:first-child")) {
			if (element.select(":has(FGREF)").isEmpty()) {
				System.err.println("Drawing Description without FGREF" + element.html());
				element.remove();
			}
		}

		// Rename all varients of "(FIG|Figure) [0-99]" by inserting a placeholder.
		for (Element element : jsoupDoc.select("FGREF")) {
			element.replaceWith(new TextNode("Patent-Figure", null));
		}

		// Remove boiler plate section, first paragraph talking about related application, which are already being captured within other fields.
		for (Element element : jsoupDoc.select("RELAPP")) {
			element.remove();
		}

		// Remove any paragraph headers.
		for (Element element : jsoupDoc.select("H")) {
			element.remove();
		}

		// Remove any paragraph headers.
		for (Element element : jsoupDoc.select("TBLREF")) {
			element.replaceWith(new TextNode("Table-Reference", null));
		}

		jsoupDoc.select("para").prepend("\\n    ").append("\\n");

		jsoupDoc.select("table").prepend("\\n").append("\\n");
		jsoupDoc.select("row").append("\\n");
		jsoupDoc.select("entry").append(" | ");

		jsoupDoc.select("UL").append("\\n    ");
		jsoupDoc.select("LI").append("\n * ");

		jsoupDoc.select("SB").prepend("_");
		jsoupDoc.select("SP").prepend("^");

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

		// CLREF is found within the claim as they relate to other claims.
		for (Element element : jsoupDoc.select("CLREF")) {
			System.err.println("Claim Ref: " + element.outerHtml());
			element.replaceWith(new TextNode("Patent-Claim", null));
		}

		/*
		for (Element element : jsoupDoc.select("CLM PARA PTEXT > PDAT")) {
			//String text = element.text();
			//element.replaceWith(new Node("claim-text"));
			element.unwrap();
			//element.tagName("claim-text");
		}
		*/

		// Remove paragraph in drawing description which does not have a figref.
		for (Element element : jsoupDoc.select("DRWDESC BTEXT PARA:first-child")) {
			if (element.select(":has(FGREF)").isEmpty()) {
				System.err.println("Drawing Descriptino without FGREF" + element.html());
				element.remove();
			}
		}

		// Rename all varients of "(FIG|Figure) [0-99]" by inserting a placeholder.
		for (Element element : jsoupDoc.select("FGREF")) {
			element.replaceWith(new TextNode("Patent-Figure", null));
		}

		// Remove boiler plate section, first paragraph talking about related application, which are already being captured within other fields.
		for (Element element : jsoupDoc.select("RELAPP")) {
			element.remove();
		}

		// Remove any paragraph headers.
		for (Element element : jsoupDoc.select("H")) {
			element.remove();
		}

		// Remove any paragraph headers.
		for (Element element : jsoupDoc.select("TBLREF")) {
			element.replaceWith(new TextNode("Table-Reference", null));
		}

		jsoupDoc.select("CLM PARA").unwrap();
		//jsoupDoc.select("CLM CLMSTEP").tagName("claim-text");
		jsoupDoc.select("CLM CLMSTEP").tagName("li");

		// Rename all "para" tags to "p".
		jsoupDoc.select("PARA").tagName("p");

		jsoupDoc.select("SB").prepend("_");
		jsoupDoc.select("SP").prepend("^");

		String textStr = jsoupDoc.html();
		textStr = textStr.replaceAll("\\\\n", "\n");

		Whitelist whitelist = Whitelist.none();
		whitelist.addTags(HTML_WHITELIST);

		OutputSettings outSettings = new Document.OutputSettings();
		outSettings.charset(Charsets.UTF_8);
		outSettings.prettyPrint(false);
		outSettings.escapeMode(EscapeMode.extended);

		String fieldTextCleaned = Jsoup.clean(textStr, "", whitelist, outSettings);
		//fieldTextCleaned = fieldTextCleaned.replaceAll("\\s+(\\r|\\n)\\s+", " ");

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
