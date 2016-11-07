package gov.uspto.patent.doc.xml;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.dom4j.DocumentException;
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
import org.xml.sax.SAXException;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;

import gov.uspto.patent.FreetextConfig;
import gov.uspto.patent.FreetextConfig.FieldType;
import gov.uspto.patent.TextProcessor;
import gov.uspto.patent.mathml.MathML;
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
            "type" };

    public static final ImmutableSet<String> HEADER_ELEMENTS = ImmutableSet.of("heading", "p[id^=h-]");
    public static final ImmutableSet<String> TABLE_ELEMENTS = ImmutableSet.of("tr", "entry", "row", "table");
    public static final ImmutableSet<String> LIST_ELEMENTS = ImmutableSet.of("ul", "ol", "li", "dl", "dt", "dd");

    private static final Map<String, String> FREETEXT_REPLACE_DEFAULT = new HashMap<String, String>();
    static {
        FREETEXT_REPLACE_DEFAULT.put("figref", "Patent-Figure");
        FREETEXT_REPLACE_DEFAULT.put("claim-ref", "Patent-Claim");
        FREETEXT_REPLACE_DEFAULT.put("patcit", "Patent-Citation");
        FREETEXT_REPLACE_DEFAULT.put("nplcit", "Patent-Citation");
    }

    private static final Collection<String> FREETEXT_REMOVE_DEFAULT = new HashSet<String>();
    static {
        FREETEXT_REMOVE_DEFAULT.add("cross-reference-to-related-applications");
        FREETEXT_REMOVE_DEFAULT.add("crossref");
    }

    @Override
    public String getPlainText(String xmlRawText, FreetextConfig textConfig) {
        Document document = Parser.parseBodyFragment(xmlRawText, "");

        Collection<String> removeEls = !textConfig.getRemoveElements().isEmpty() ? textConfig.getRemoveElements()
                : FREETEXT_REMOVE_DEFAULT;

        Map<String, String> replacEls = !textConfig.getReplaceElements().isEmpty() ? textConfig.getReplaceElements()
                : FREETEXT_REPLACE_DEFAULT;
        for (String xmlElementName : replacEls.keySet()) {
            for (Element element : document.select(xmlElementName)) {
                element.replaceWith(new TextNode(replacEls.get(xmlElementName), null));
            }
        }

        // Remove paragraph in drawing description which does not have a figref.
        for (Element element : document.select("description-of-drawings > p")) {
            if (element.select(":has(figref)").isEmpty()) {
                // System.err.println("Drawing Descriptino without FGREF" +
                // element.html());
                element.remove();
            }
        }

        if (textConfig.keepType(FieldType.HEADER)) {
            document.select("heading").prepend("\\n").append("\\n");
            // Header Paragraphs which have an id starting with "h-".
            document.select("p[id^=h-]").append("\\n");
        } else {
            removeEls.addAll(HEADER_ELEMENTS);
        }

        if (textConfig.keepType(FieldType.TABLE)) {
            document.select("table").prepend("\\n").append("\\n");
            document.select("tr").prepend(".\\n"); // added period to breakup large
            // tables for NLP; example patent
            // US20150017148A1.
            document.select("row").append("\\n");
            document.select("entry").append(" | ");
        } else {
            removeEls.addAll(TABLE_ELEMENTS);
        }

        if (textConfig.keepType(FieldType.LIST)) {
            document.select("ul").append("\\n\\t");
            document.select("ol").prepend("\\n\\t");
            document.select("li").append("\\n\\t* ");

            document.select("dl").append("\\n\\t");
            document.select("dt").append("\\t* ").prepend(" | ");
            document.select("dd").append("\\n");
        } else {
            removeEls.addAll(LIST_ELEMENTS);
        }

        if (textConfig.keepType(FieldType.MATHML)) {
            /*
             * MathML in string form.
             */
            for (Element element : document.select("math")) {
                Reader reader = new StringReader(element.outerHtml());
                try {
                    MathML mathml = MathML.read(reader);
                    String stringForm = mathml.getStringForm();
                    element.replaceWith(new TextNode(stringForm, null));
                } catch (SAXException | DocumentException e) {
                    //LOGGER.error("");
                }
            }
        }

        document.select("p").prepend("\\n    ");
        document.select("br").append("\\n");
        document.select("sub").prepend("_");
        document.select("sub2").prepend("_");
        document.select("sup").prepend("^");
        document.select("sup2").prepend("^");

        /*
         * Remove Elements.
         */
        for (String xmlElementName : removeEls) {
            document.select(xmlElementName).remove();
        }

        String docStr = document.html().replaceAll("\\s{2,}", " ");
        docStr = docStr.replaceAll("\\\\n", "\n");

        OutputSettings outSettings = new Document.OutputSettings();
        outSettings.charset(Charsets.UTF_8);
        outSettings.prettyPrint(false);
        outSettings.escapeMode(EscapeMode.extended);

        docStr = Jsoup.clean(docStr, "", Whitelist.none(), outSettings);

        return docStr;
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
