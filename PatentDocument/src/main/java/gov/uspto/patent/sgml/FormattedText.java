package gov.uspto.patent.sgml;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.nodes.Node;
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
		org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(rawText, "", Parser.xmlParser());

		for (Element element : jsoupDoc.select("CLREF")) {
			element.replaceWith(new TextNode("Patent-Claim", null));
		}

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

		// Rename all "para" tags to "p".
		Elements elements = jsoupDoc.select("para");
		elements.tagName("p");
		//jsoupDoc.select("para").prepend("\\n.\\n    "); // period added to break block for NLP.

		/*
		 *  Within the following:
		 *    1) inserting newlines to maintain some formating
		 *    2) inserting periods to breakup some blocks, large blocks cause NLP to hang or run for a long time.
		 */
		jsoupDoc.select("table").prepend("\\n");
		jsoupDoc.select("table").append("\\n");
		jsoupDoc.select("row").prepend(".\\n"); // period added to break block for NLP.
		jsoupDoc.select("entry").append(" | ");

		jsoupDoc.select("UL").prepend("\\n");
		jsoupDoc.select("LI").append("\\n");

		jsoupDoc.select("SB").prepend("_");
		jsoupDoc.select("SP").prepend("^");

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
