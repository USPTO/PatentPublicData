package gov.uspto.patent.doc.greenbook;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import gov.uspto.patent.FreetextConfig;
import gov.uspto.patent.FreetextConfig.FieldType;
import gov.uspto.patent.TextProcessor;

/**
 * Parse and Clean Formated Text Fields, such as Description, Abstract and Claims.
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class FormattedText implements TextProcessor {

    private static final Pattern CLAIM_REF = Pattern.compile("\\bclaim ([0-9](?:(?: or |-)[0-9])?)\\b");
    private static final Pattern PATENT_FIG = Pattern
            .compile("\\bFIGS?. ([0-30][()A-z]*(?:(?: to |-)[0-30][()A-z]*)?)\\b");

    private static final String[] HTML_WHITELIST = new String[] { "p", "h2", "table", "tr", "td", "a", "li" }; // "ul", "li"
    private static final String[] HTML_WHITELIST_ATTRIB = new String[] { "class", "id", "num", "idref" };
    
    private static final Map<String, String> FREETEXT_REPLACE_DEFAULT = new HashMap<String, String>();
    static {
        FREETEXT_REPLACE_DEFAULT.put("a.figref", "Patent-Figure");
        FREETEXT_REPLACE_DEFAULT.put("a.claim", "Patent-Claim");
    }

    private static final Collection<String> FREETEXT_REMOVE_DEFAULT = new HashSet<String>();

    @Override
    public String getPlainText(String rawText, FreetextConfig textConfig) {
        rawText = createRefs(rawText);
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

        if (textConfig.keepType(FieldType.HEADER)) {
            jsoupDoc.select("PAC").prepend("\\n").append("\\n");
        } else {
            removeEls.add("PAC");
        }

        if (textConfig.keepType(FieldType.TABLE)) {
            jsoupDoc.select("TBL").append("\\n.\\n");
        } else {
            removeEls.add("TBL");
        }

        jsoupDoc.select("PAR").prepend("\\n    ");
        jsoupDoc.select("PA1").prepend("\\n        ");
        jsoupDoc.select("PA2").prepend("\\n            ");
        jsoupDoc.select("PAL").prepend("\\n   * ");

        /*
         * Remove Elements.
         */
        for (String xmlElementName : removeEls) {
            jsoupDoc.select(xmlElementName).remove();
        }

        String textStr = jsoupDoc.html();
        textStr = cleanText(textStr);

        OutputSettings outSettings = new Document.OutputSettings();
        outSettings.charset(Charsets.UTF_8);
        outSettings.prettyPrint(false);
        outSettings.escapeMode(EscapeMode.extended);

        String fieldTextCleaned = Jsoup.clean(textStr, "", Whitelist.none(), outSettings);

        return fieldTextCleaned;
    }

    @Override
    public String getSimpleHtml(String rawText) {
        rawText = createRefs(rawText);
        Document jsoupDoc = Jsoup.parse(rawText, "", Parser.xmlParser());

        // rename header to "h2"
        jsoupDoc.select("PAC").tagName("h2");

        // Rename all "para" tags to "p".
        jsoupDoc.select("PAR").tagName("p");
        jsoupDoc.select("PA1").tagName("p");
        jsoupDoc.select("PA2").tagName("p");

        jsoupDoc.select("PAL").tagName("li");
        jsoupDoc.select("TBL").tagName("table");

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

    public String createRefs(String rawText) {
        StringBuilder stb = new StringBuilder(rawText);
        Matcher clmMatcher = CLAIM_REF.matcher(rawText);
        int additionalChars = 0;
        while (clmMatcher.find()) {
            String fullMatch = clmMatcher.group(0);
            String newStr = "<a class=\"claim\">" + fullMatch + "</a>";
            stb.replace(clmMatcher.start() + additionalChars, clmMatcher.end() + additionalChars, newStr);
            additionalChars = additionalChars + (newStr.length() - fullMatch.length());
        }
        String htmlText = stb.toString();

        stb = new StringBuilder(htmlText);
        Matcher figMatcher = PATENT_FIG.matcher(htmlText);
        additionalChars = 0;
        while (figMatcher.find()) {
            String fullMatch = figMatcher.group(0);
            String newStr = "<a class=\"figref\">" + fullMatch + "</a>";
            stb.replace(figMatcher.start() + additionalChars, figMatcher.end() + additionalChars, newStr);
            additionalChars = additionalChars + (newStr.length() - fullMatch.length());
        }
        return stb.toString();
    }

    /**
     * Transform Patent Figure and Patent Claims and their accompanied number
     * to simply "Patent-Claim" or "Patent-Figure"
     * 
     * @param text
     * @return
     */
    protected String cleanText(String text) {
        text = text.replaceAll("\\\\n", "\n");
        text = CLAIM_REF.matcher(text).replaceAll("Patent-Claim");
        text = PATENT_FIG.matcher(text).replaceAll("Patent-Figure");
        return text;
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
