package gov.uspto.patent.doc.greenbook;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;

import gov.uspto.patent.ReferenceTagger;
import gov.uspto.patent.TextProcessor;
import gov.uspto.patent.doc.simplehtml.FreetextConfig;
import gov.uspto.patent.doc.simplehtml.HtmlToPlainText;

/**
 * Parse and Clean Formated Text Fields, such as Description, Abstract and
 * Claims.
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class FormattedText implements TextProcessor {

	private static final String[] HTML_WHITELIST = new String[] { "b", "p", "h2", "pre", "a", "li",	"ul", "span" };
	private static final String[] HTML_WHITELIST_ATTRIB = new String[] { "class", "id", "num", "idref", "level" };

	@Override
	public String getPlainText(String rawText, FreetextConfig textConfig) {
		Document jsoupDoc = Jsoup.parse(rawText, "", Parser.xmlParser());

		for (int j = 0; j < 4; j++) {
			for (Element paragraph : jsoupDoc.select("PA" + j)) {
				int level = j;
				StringBuilder stb = new StringBuilder();
				for (int i = 0; i <= level; i++) {
					stb.append("&nbsp;");
				}
				paragraph.prepend(stb.toString());
			}
		}

		String simpleHtml = getSimpleHtml(jsoupDoc.outerHtml());
		Document simpleDoc = Jsoup.parse(simpleHtml, "", Parser.xmlParser());

		HtmlToPlainText htmlConvert = new HtmlToPlainText(textConfig);
		return htmlConvert.getPlainText(simpleDoc);
	}

	@Override
	public String getSimpleHtml(String rawText) {	
		rawText = ReferenceTagger.markRefs(rawText);

		// Wrap with body tag so jsoup maintains "<p><pre></pre></p>".
		Document jsoupDoc = Jsoup.parse("<body>" + rawText + "</body>", "", Parser.xmlParser());
		jsoupDoc.outputSettings().prettyPrint(false);

		// Give id to each FigRef
		Elements figEls = jsoupDoc.select("a.figref");
		for (int i = 1; i <= figEls.size(); i++) {
			Element element = figEls.get(i - 1);
			element.attr("id", "FR-" + Strings.padStart(String.valueOf(i), 4, '0'));
		}
		
		// rename header to "h2"
		Elements headerEls = jsoupDoc.select("PAC");
		for (int i = 1; i <= headerEls.size(); i++) {
			Element element = headerEls.get(i - 1);
			element.attr("id", "H-" + Strings.padStart(String.valueOf(i), 4, '0'));
			element.tagName("h2");
		}

		// Rename all "para" tags to "p".
		jsoupDoc.select("PAR").tagName("p");

		// Rename PA1, PA2 .... tags to "p" and add level attribute.
		for (int j = 0; j < 4; j++) {
			jsoupDoc.select("PA" + j).tagName("p").attr("level", String.valueOf(j));
		}

		jsoupDoc.select("PAL").tagName("p");

		// Give id to each paragraph
		Elements parEls = jsoupDoc.select("p");
		for (int i = 1; i <= parEls.size(); i++) {
			Element element = parEls.get(i - 1);
			element.attr("id", "P-" + Strings.padStart(String.valueOf(i), 4, '0'));
		}

		/*
		 * Greenbook tables are just freetext 
		 * with newline and white space formating
		 * maintain by using a html "pre" tag.
		 */
		Elements tblEls = jsoupDoc.select("TBL");
		for (int i = 1; i <= tblEls.size(); i++) {
			Element tbl = tblEls.get(i - 1);
			tbl.attr("id", "P-" + Strings.padStart(String.valueOf(i), 4, '0'));
			tbl.tagName("pre");
			tbl.attr("class", "freetext-table");

			Element par = jsoupDoc.createElement("p");
			tbl.replaceWith(par);
			par.appendChild(tbl);
		}

		String textStr = jsoupDoc.outerHtml();
		textStr = textStr.replaceAll("\\\\n", "\n");

		Whitelist whitelist = Whitelist.none();
		whitelist.addTags(HTML_WHITELIST);
		whitelist.addAttributes(":all", HTML_WHITELIST_ATTRIB);

		OutputSettings outSettings = new Document.OutputSettings();
		outSettings.charset(Charsets.UTF_8);
		outSettings.prettyPrint(false);
		outSettings.escapeMode(EscapeMode.extended);

        Cleaner cleaner = new Cleaner(whitelist);
        Document clean = cleaner.clean(jsoupDoc);
        clean.outputSettings(outSettings);
        return clean.body().html();
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
