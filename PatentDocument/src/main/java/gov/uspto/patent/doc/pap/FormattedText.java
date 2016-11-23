package gov.uspto.patent.doc.pap;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Parser;
import org.jsoup.parser.Tag;
import org.jsoup.safety.Whitelist;

import com.google.common.base.Charsets;

import gov.uspto.patent.TextProcessor;
import gov.uspto.patent.doc.simplehtml.FreetextConfig;
import gov.uspto.patent.doc.simplehtml.HtmlToPlainText;
import gov.uspto.patent.mathml.MathmlEscaper;

/**
 * Parse and Clean Formated Text Fields, such as Description, Abstract and Claims.
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class FormattedText implements TextProcessor {

    private static final String[] HTML_WHITELIST_TAGS = new String[] { "bold", "h1", "h2", "h3", "h4", "h5", "h6", "p",
            "table", "tr", "td", "ul", "ol", "li", "dl", "dt", "dd", "a", "span" };
    private static final String[] HTML_WHITELIST_ATTRIB = new String[] { "class", "id", "num", "idref", "format",
            "type" };

	@Override
	public String getPlainText(String rawText, FreetextConfig textConfig) {
		String simpleHtml = getSimpleHtml(rawText);
		Document simpleDoc = Jsoup.parse(simpleHtml, "", Parser.xmlParser());

		HtmlToPlainText htmlConvert = new HtmlToPlainText(textConfig);
		return htmlConvert.getPlainText(simpleDoc);
	}

    @Override
    public String getSimpleHtml(String rawText) {

        Document jsoupDoc = Jsoup.parse(rawText, "", Parser.xmlParser());

        for (Element element : jsoupDoc.select("cross-reference-to-related-applications")) {
            element.remove();
        }

        // Heading tags to H2.
        jsoupDoc.select("heading").tagName("h2");

        // Remove first paragraph in drawing description if it does not have a figref.
        for (Element element : jsoupDoc.select("brief-description-of-drawings section paragraph:first-child")) {
            if (element.select(":has(cross-reference)").isEmpty()) {
                // System.err.println("Drawing Description without
                // Patent-Figure" + element.html());
                element.remove();
            }
        }

        for (Element element : jsoupDoc.select("cross-reference")) {
            //String target = element.attr("target");
            //if ("drawings".equals(target.toLowerCase().trim()){
            element.tagName("a");
            element.addClass("figref");
            //}
        }

        for (Element element : jsoupDoc.select("dependent-claim-reference")) {
            element.tagName("a");
            element.addClass("claim");
        }

        /*
         * Math, change mathml to text to maintain all nodes after sending through Cleaner.
         */
        boolean mathFound = false;
        for (Element element : jsoupDoc.select("math")) {
            mathFound = true;
            String mathml = MathmlEscaper.escape(element.html());

            Element newEl = new Element(Tag.valueOf("span"), "");
            newEl.addClass("math");
            newEl.attr("format", "mathml");
            newEl.appendChild(new TextNode(mathml, null));
            element.replaceWith(newEl);
        }

        jsoupDoc.select("subscript").prepend("_");
        jsoupDoc.select("superscript").prepend("^");
        jsoupDoc.select("section").prepend("\\n\\n    ");

        // Remove Paragraph Numbers.
        jsoupDoc.select("paragraph number:first-child").remove();

        // Rename all "paragraph" tags to "p".
        jsoupDoc.select("paragraph").tagName("p");

        // Table Elements.
        jsoupDoc.select("row").tagName("tr");
        jsoupDoc.select("entry").append("td");

        // handle nested claim-text similar as paragraphs.
        //jsoupDoc.select("claim-text").tagName("li");

        String textStr = jsoupDoc.html();
        textStr = textStr.replaceAll("\\\\n", "\n");

        // Whitelist whitelist = Whitelist.simpleText();
        Whitelist whitelist = Whitelist.none();
        whitelist.addTags(HTML_WHITELIST_TAGS);
        whitelist.addAttributes(":all", HTML_WHITELIST_ATTRIB);

        OutputSettings outSettings = new Document.OutputSettings();
        outSettings.charset(Charsets.UTF_8);
        outSettings.prettyPrint(false);
        outSettings.escapeMode(EscapeMode.extended);

        String fieldTextCleaned = Jsoup.clean(textStr, "", whitelist, outSettings);

        if (mathFound) {
            fieldTextCleaned = MathmlEscaper.unescape(fieldTextCleaned);
        }

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
