package gov.uspto.patent.doc.xml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.nodes.Document.OutputSettings.Syntax;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Parser;
import org.jsoup.parser.Tag;
import org.jsoup.safety.Whitelist;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;

import gov.uspto.patent.TextProcessor;
import gov.uspto.patent.doc.simplehtml.FreetextConfig;
import gov.uspto.patent.doc.simplehtml.HtmlToPlainText;
import gov.uspto.patent.mathml.MathmlEscaper;

/**
 * Parse and Clean Formated Text Fields, such as Description, Abstract and
 * Claims.
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class FormattedText implements TextProcessor {

    private static final String[] HTML_WHITELIST_TAGS = new String[] { "bold", "h1", "h2", "h3", "h4", "h5", "h6", "p",
            "table", "tr", "td", "ul", "ol", "li", "dl", "dt", "dd", "a", "span" };
    private static final String[] HTML_WHITELIST_ATTRIB = new String[] { "class", "id", "num", "idref", "format",
            "type", "level" };

    public static final ImmutableSet<String> HEADER_ELEMENTS = ImmutableSet.of("heading", "p[id^=h-]");
    public static final ImmutableSet<String> TABLE_ELEMENTS = ImmutableSet.of("tr", "entry", "row", "table");
    public static final ImmutableSet<String> LIST_ELEMENTS = ImmutableSet.of("ul", "ol", "li", "dl", "dt", "dd");

	@Override
	public String getPlainText(String rawText, FreetextConfig textConfig) {
		String simpleHtml = getSimpleHtml(rawText);
		Document simpleDoc = Jsoup.parse(simpleHtml, "", Parser.xmlParser());

		HtmlToPlainText htmlConvert = new HtmlToPlainText(textConfig);
		return htmlConvert.getPlainText(simpleDoc);
	}

    @Override
    public String getSimpleHtml(String rawText) {
        Document document = Parser.parseBodyFragment(rawText, "");
        // this.document = Jsoup.parse(xml, "", Parser.xmlParser());
        // this.document = Jsoup.parse(xml);

        String[] unwantedSections = new String[] { "cross-reference-to-related-applications" };
        for (String removeSection : unwantedSections) {
            for (Element element : document.getElementsByTag(removeSection)) {
                // System.out.println("Removing ["+ removeSection + "] : " +
                // element.html());
                element.remove();
            }
        }

        /*
         * HEADING tags
         */
        document.select("heading").tagName("h2");
        // Header Paragraphs which have an id starting with "h-".
        document.select("p[id^=h-]").tagName("h4");

        // Remove paragraph in drawing description which do not describe a figref.
        for (Element element : document.select("description-of-drawings > p")) {
            if (element.select(":has(figref)").isEmpty()) {
                element.remove();
            }
        }

        /*
         * Patent Drawing FIGREF tags not needed in index.
         */
        for (Element element : document.select("figref")) {
            element.tagName("a");
            element.addClass("figref");
        }

        /*
         * Patent Claim Reference
         * 
         * <claim-ref idref="CLM-00001">claim 1</claim-ref>
         * 
         * <a idref="CLM-00001" class="claim">Patent-Claim</a>
         */
        for (Element element : document.select("claim-ref")) {
            element.tagName("a");
            element.addClass("claim");
        }

        /*
         * Patent Citation Reference tags not needed in the index.
         */
        for (Element element : document.select("patcit")) {
            element.tagName("a");
            element.addClass("patcite");
        }

        /*
         * Patent Citation Reference tags not needed in the index.
         */
        for (Element element : document.select("nplcit")) {
            element.tagName("a");
            element.addClass("nplcite");
        }

        /*
         * crossref tags are internal links to another section.
         */
        for (Element element : document.select("crossref")) {
            element.tagName("a");
            element.addClass("crossref");
        }

        /*
         * Escape MathML math elements, to maintain all xml elements after sending through Cleaner.
         */
        boolean mathFound = false;
        for (Element element : document.select("math")) {
            mathFound = true;

            String mathml = MathmlEscaper.escape(element.html());

            Element newEl = new Element(Tag.valueOf("span"), "");
            newEl.addClass("math");
            newEl.attr("format", "mathml");
            newEl.appendChild(new TextNode(mathml, null));
            element.replaceWith(newEl);
        }

        document.select("br").append("\\n");
        document.select("tbody").append("\\n");

        document.select("sub").prepend("_");
        document.select("sub2").prepend("_");
        document.select("sup").prepend("^");
        document.select("sup2").prepend("^");

        document.select("ol").tagName("ul");

        document.select("row").tagName("tr");
        document.select("entry").tagName("td");

        //document.select("p:matchesOwn((?is) +?)").remove(); // remove paragraphs which contain only spaces.

        String docStr = document.html().replaceAll("\\s{2,}", " ");
        docStr = docStr.replaceAll("\\\\n", "\n");

        Whitelist whitelist = Whitelist.none();
        whitelist.addTags(HTML_WHITELIST_TAGS);
        whitelist.addAttributes(":all", HTML_WHITELIST_ATTRIB);

        OutputSettings outSettings = new Document.OutputSettings();
        outSettings.charset(Charsets.UTF_8);
        outSettings.syntax(Syntax.html);
        outSettings.outline(true);
        outSettings.prettyPrint(false);
        outSettings.escapeMode(EscapeMode.extended);

        docStr = Jsoup.clean(docStr, "", whitelist, outSettings);

        if (mathFound) {
            docStr = MathmlEscaper.unescape(docStr);
        }

        return docStr;
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
