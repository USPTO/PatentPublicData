package gov.uspto.patent.doc.sgml;

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

    public static final ImmutableSet<String> HEADER_ELEMENTS = ImmutableSet.of("H");
    public static final ImmutableSet<String> TABLE_ELEMENTS = ImmutableSet.of("tr", "entry", "row", "table");
    public static final ImmutableSet<String> LIST_ELEMENTS = ImmutableSet.of("UL", "LI");

    private static final Map<String, String> FREETEXT_REPLACE_DEFAULT = new HashMap<String, String>();
    static {
        FREETEXT_REPLACE_DEFAULT.put("FGREF", "Patent-Figure");
        FREETEXT_REPLACE_DEFAULT.put("CLREF", "Patent-Claim");
        FREETEXT_REPLACE_DEFAULT.put("TBLREF", "Table-Reference");
        FREETEXT_REPLACE_DEFAULT.put("nplcit", "Patent-Citation");
    }

    private static final Collection<String> FREETEXT_REMOVE_DEFAULT = new HashSet<String>();
    static {
        FREETEXT_REMOVE_DEFAULT.add("RELAPP");
        FREETEXT_REMOVE_DEFAULT.add("crossref");
    }

    @Override
    public String getPlainText(String rawText, FreetextConfig textConfig) {
        Document jsoupDoc = Jsoup.parse(rawText, "", Parser.xmlParser());

        Collection<String> removeEls = !textConfig.getRemoveElements().isEmpty() ? textConfig.getRemoveElements()
                : FREETEXT_REMOVE_DEFAULT;

        Map<String, String> replacEls = !textConfig.getReplaceElements().isEmpty() ? textConfig.getReplaceElements()
                : FREETEXT_REPLACE_DEFAULT;
        for (String xmlElementName : replacEls.keySet()) {
            for (Element element : jsoupDoc.select(xmlElementName)) {
                element.replaceWith(new TextNode(replacEls.get(xmlElementName), null));
            }
        }

        // Remove paragraph in drawing description which does not have a figref.
        for (Element element : jsoupDoc.select("DRWDESC BTEXT PARA:first-child")) {
            if (element.select(":has(FGREF)").isEmpty()) {
                System.err.println("Drawing Description without FGREF" + element.html());
                element.remove();
            }
        }

        if (textConfig.keepType(FieldType.HEADER)) {
            jsoupDoc.select("H").prepend("\\n").append("\\n");
        } else {
            removeEls.addAll(HEADER_ELEMENTS);
        }

        if (textConfig.keepType(FieldType.TABLE)) {
            jsoupDoc.select("table").prepend("\\n").append("\\n");
            jsoupDoc.select("row").append("\\n");
            jsoupDoc.select("entry").append(" | ");
        } else {
            removeEls.addAll(TABLE_ELEMENTS);
        }

        if (textConfig.keepType(FieldType.LIST)) {
            jsoupDoc.select("UL").append("\\n    ");
            jsoupDoc.select("LI").append("\n * ");
        } else {
            removeEls.addAll(LIST_ELEMENTS);
        }

        if (textConfig.keepType(FieldType.MATHML)) {
            /*
             * MathML in string form.
             */
            for (Element element : jsoupDoc.select("math")) {
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

        jsoupDoc.select("PARA").prepend("\\n    ");
        jsoupDoc.select("SB").prepend("_");
        jsoupDoc.select("SP").prepend("^");

        /*
         * Remove Elements
         */
        for (String xmlElementName : removeEls) {
            jsoupDoc.select(xmlElementName).remove();
        }

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

        for (Element element : jsoupDoc.select("FGREF")) {
            element.tagName("a");
            element.addClass("figref");
        }

        for (Element element : jsoupDoc.select("CLREF")) {
            element.tagName("a");
            element.addClass("claim");
        }

        /*
         * for (Element element : jsoupDoc.select("CLM PARA PTEXT > PDAT")) {
         * //String text = element.text(); //element.replaceWith(new
         * Node("claim-text")); element.unwrap();
         * //element.tagName("claim-text"); }
         */

        // Remove paragraph in drawing description which does not have a figref.
        for (Element element : jsoupDoc.select("DRWDESC BTEXT PARA:first-child")) {
            if (element.select(":has(FGREF)").isEmpty()) {
                System.err.println("Drawing Descriptino without FGREF" + element.html());
                element.remove();
            }
        }

        // Remove boiler plate section, first paragraph talking about related
        // application, which are already being captured within other fields.
        for (Element element : jsoupDoc.select("RELAPP")) {
            element.remove();
        }

        // Paragraph headers.
        jsoupDoc.select("H").tagName("h2");

        // Remove any paragraph headers.
        for (Element element : jsoupDoc.select("TBLREF")) {
            element.replaceWith(new TextNode("Table-Reference", null));
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

        jsoupDoc.select("CLM PARA").unwrap();
        // jsoupDoc.select("CLM CLMSTEP").tagName("claim-text");
        jsoupDoc.select("CLM CLMSTEP").tagName("li");

        // Rename all "para" tags to "p".
        jsoupDoc.select("PARA").tagName("p");

        jsoupDoc.select("SB").prepend("_");
        jsoupDoc.select("SP").prepend("^");

        String textStr = jsoupDoc.html();
        textStr = textStr.replaceAll("\\\\n", "\n");

        Whitelist whitelist = Whitelist.none();
        whitelist.addTags(HTML_WHITELIST_TAGS);
        whitelist.addAttributes(":all", HTML_WHITELIST_ATTRIB);

        OutputSettings outSettings = new Document.OutputSettings();
        outSettings.charset(Charsets.UTF_8);
        outSettings.syntax(Syntax.html);
        outSettings.outline(true);
        outSettings.prettyPrint(false);
        outSettings.escapeMode(EscapeMode.extended);

        String fieldTextCleaned = Jsoup.clean(textStr, "", whitelist, outSettings);
        // fieldTextCleaned = fieldTextCleaned.replaceAll("\\s+(\\r|\\n)\\s+", "
        // ");

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
