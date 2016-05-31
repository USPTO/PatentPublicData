package gov.uspto.patent.xml4ip;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
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

	@Override
	public String getProcessText(String rawText) {
		Document document = Parser.parseBodyFragment(rawText, "");
		//this.document = Jsoup.parse(xml, "", Parser.xmlParser());
		//this.document = Jsoup.parse(xml);

		String[] unwantedSections = new String[] { "cross-reference-to-related-applications" };
		for (String removeSection : unwantedSections) {
			for (Element element : document.getElementsByTag(removeSection)) {
				//System.out.println("Removing ["+ removeSection + "] : " + element.html());
				element.remove();
			}
		}		

		// Remove paragraph in drawing description which does not have a figref.
		for (Element element : document.select("description-of-drawings > p")) {
			if (element.select(":has(figref)").isEmpty()){
				//System.err.println("Drawing Descriptino without FGREF" + element.html());
				element.remove();
			}
		}

		/*
		 * Patent Drawing FIGREF tags not needed in index.
		 */
		for (Element element : document.select("figref")) {
			//element.remove();
			element.replaceWith(new TextNode("Patent-Figure", null));
		}

		/*
		 * Patent Claim Reference tags not needed in the index.
		 */
		for (Element element : document.select("claim-ref")) {
			//element.remove();
			element.replaceWith(new TextNode("Patent-Claim", null));
		}

		/*
		 * Patent Citation Reference tags not needed in the index.
		 */
		for (Element element : document.select("patcit")) {
			//element.remove();
			element.replaceWith(new TextNode("Patent-Citation", null));
		}		

		/*
		 * Patent Citation Reference tags not needed in the index.
		 */
		for (Element element : document.select("nplcit")) {
			//element.remove();
			element.replaceWith(new TextNode("Patent-Citation", null));
		}

		/*
		 * HEADING tags, remove since not useful in index.
		 */
		for (Element element : document.select("heading")) {
			element.remove();
		}

		/*
		 * crossref tags, remove since not useful in index; which are internal links to another section.
		 */
		for (Element element : document.select("crossref")) {
			element.remove();
		}

		document.select("br").append("\\n");
		document.select("p").prepend("\\n.\\n    "); // added period to breakup large chunks of text for NLP; example patent US20150064624A1.
		document.select("tr").prepend(".\\n"); // added period to breakup large tables for NLP; example patent US20150017148A1.

		document.select("ul").prepend("\\n");
		document.select("ol").prepend("\\n");
		document.select("li").append("\\n");

		//document.select("table").prepend("\\n\\n");
		document.select("tbody").append("\\n");
		document.select("row").prepend(".\\n"); // added period to breakup large tables for NLP.
		document.select("entry").append(" | ");
		//document.select("entry").prepend("| ");

		document.select("mtable").prepend("\\n\\n");
		document.select("mtr").prepend(".\\n");
		document.select("mrow").prepend(".\\n");

		document.select("sub").prepend("_");
		document.select("sub2").prepend("_");
		document.select("sup").prepend("^");
		document.select("sup2").prepend("^");

		String docStr = document.html().replaceAll("\\s{2,}", " ");
		docStr = docStr.replaceAll("\\\\n", "\n");

		//Whitelist whitelist = Whitelist.simpleText();
		Whitelist whitelist = Whitelist.none();
		//whitelist.addTags("mtable");
		//whitelist.addTags("mtr");
		//whitelist.addTags("mrow");

		OutputSettings outSettings = new Document.OutputSettings();
		outSettings.charset(Charsets.UTF_8);
		outSettings.prettyPrint(false);
		outSettings.escapeMode(EscapeMode.extended);

		docStr = Jsoup.clean(docStr, "", whitelist, outSettings);

		return docStr;
	}
	
	@Override
	public List<String> getParagraphText(String rawText) {
		String textWithPMarks = getProcessText(rawText);
		Document jsoupDoc = Jsoup.parse(textWithPMarks, "", Parser.xmlParser());

		List<String> paragraphs = new ArrayList<String>();
		for (Element element : jsoupDoc.select("P")) {
			paragraphs.add(element.html());
		}

		return paragraphs;
	}

}
