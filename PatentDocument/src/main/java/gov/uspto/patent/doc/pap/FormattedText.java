package gov.uspto.patent.doc.pap;

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
import gov.uspto.patent.TextProcessor;
import gov.uspto.patent.FreetextConfig.FieldType;
import gov.uspto.patent.mathml.MathML;
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

    public static final ImmutableSet<String> HEADER_ELEMENTS = ImmutableSet.of("heading");
    public static final ImmutableSet<String> TABLE_ELEMENTS = ImmutableSet.of("tr", "entry", "row", "table");
    public static final ImmutableSet<String> LIST_ELEMENTS = ImmutableSet.of("ul", "ol", "li", "dl", "dt", "dd");
    
    private static final Map<String, String> FREETEXT_REPLACE_DEFAULT = new HashMap<String, String>();
    static {
        FREETEXT_REPLACE_DEFAULT.put("cross-reference", "Patent-Figure");
        FREETEXT_REPLACE_DEFAULT.put("dependent-claim-reference", "Patent-Claim");
    }

    private static final Collection<String> FREETEXT_REMOVE_DEFAULT = new HashSet<String>();
    static {
        FREETEXT_REMOVE_DEFAULT.add("cross-reference-to-related-applications");
        FREETEXT_REMOVE_DEFAULT.add("crossref");
        FREETEXT_REMOVE_DEFAULT.add("paragraph number:first-child"); // Remove Paragraph Numbers.
    }

    @Override
    public String getPlainText(String rawXmlText, FreetextConfig textConfig) {
        Document jsoupDoc = Jsoup.parse(rawXmlText, "", Parser.xmlParser());

        Collection<String> removeEls = !textConfig.getRemoveElements().isEmpty() ? textConfig.getRemoveElements()
                : FREETEXT_REMOVE_DEFAULT;
        
        Map<String, String> replacEls = !textConfig.getReplaceElements().isEmpty() ? textConfig.getReplaceElements()
                : FREETEXT_REPLACE_DEFAULT;
        for (String xmlElementName : replacEls.keySet()) {
            for (Element element : jsoupDoc.select(xmlElementName)) {
                element.replaceWith(new TextNode(replacEls.get(xmlElementName), null));
            }
        }

        // Remove first paragraph in drawing description if it does not have a figref.
        for (Element element : jsoupDoc.select("brief-description-of-drawings section paragraph:first-child")) {
            if (element.select(":has(cross-reference)").isEmpty()) {
                //System.err.println("Drawing Description without Patent-Figure" + element.html());
                element.remove();
            }
        }

        if (textConfig.keepType(FieldType.HEADER)) {
            jsoupDoc.select("heading").prepend("\\n").append("\\n");
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
            jsoupDoc.select("ul").append("\\n\\t");
            jsoupDoc.select("ol").append("\\n\\t");
            jsoupDoc.select("li").append("\\n * ");
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

        jsoupDoc.select("section").prepend("\\n ---------------- \n").append("\\n");
        jsoupDoc.select("paragraph").prepend("\\n    ");
        //jsoupDoc.select("claim-text").prepend("\\n * ");
        jsoupDoc.select("subscript").prepend("_");
        jsoupDoc.select("superscript").prepend("^");

        /*
         * remove elements
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
