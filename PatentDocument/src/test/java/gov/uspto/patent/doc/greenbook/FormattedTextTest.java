package gov.uspto.patent.doc.greenbook;

import static org.junit.Assert.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import gov.uspto.patent.doc.greenbook.FormattedText;

public class FormattedTextTest {

    private static Map<String, String> validFromTo = new LinkedHashMap<String, String>();
    static {
        validFromTo.put("in claim 1 or 2", "in Patent-Claim");
        validFromTo.put("of claim 2-3", "of Patent-Claim");
        validFromTo.put("PAR  FIG. 1 is a side elevational view", "PAR  Patent-Figure is a side elevational view");
        validFromTo.put("PAR  FIG. 3A is a perspective view", "PAR  Patent-Figure is a perspective view");
        validFromTo.put("PAR  FIG. 1a-1c is a side view", "PAR  Patent-Figure is a side view");
        validFromTo.put("PAR  FIGS. 1-2 are top views", "PAR  Patent-Figure are top views");
        //validFromTo.put("illustrated in FIGS. 2 to 5.", "illustrated in Patent-Figure.");
        //validFromTo.put("as shown in FIG. 1(b) and may be", "as shown in Patent-Figure and may be");
        //validFromTo.put("PAR  FIGS. 1(a) and 1(b) are graphs showing", "PAR  Patent-Figure are graphs showing");
        //validFromTo.put("current shown in FIG. 1(a) is", "current shown in Patent-Figure is");
    }

    private static Map<String, String> validFromToHTML = new LinkedHashMap<String, String>();
    static {
        validFromToHTML.put("in claim 1 or 2", "in <a class=\"claim\">claim 1 or 2</a>");
        validFromToHTML.put("of claim 2-3", "of <a class=\"claim\">claim 2-3</a>");
        validFromToHTML.put("PAR  FIG. 1 is a side elevational view",
                "PAR  <a class=\"figref\">FIG. 1</a> is a side elevational view");
        validFromToHTML.put("PAR  FIG. 3A is a perspective view",
                "PAR  <a class=\"figref\">FIG. 3A</a> is a perspective view");
        validFromToHTML.put("PAR  FIG. 1a-1c is a side view", "PAR  <a class=\"figref\">FIG. 1a-1c</a> is a side view");
        validFromToHTML.put("PAR  FIGS. 1-2 are top views", "PAR  <a class=\"figref\">FIGS. 1-2</a> are top views");
        validFromToHTML.put("FIG. 4 is a schematic representation", "<a class=\"figref\">FIG. 4</a> is a schematic representation");
        validFromToHTML.put("FIGS. 5A and 5B together comprise", "<a class=\"figref\">FIGS. 5A and 5B</a> together comprise");
        validFromToHTML.put("illustrated in FIGS. 2 to 5.", "illustrated in <a class=\"figref\">FIGS. 2 to 5</a>.");
        
        //validFromToHTML.put("as shown in FIG. 1(b) and may be", "as shown in <a class=\"figref\">FIG. 1(b)</a> and may be");
        //validFromToHTML.put("PAR  FIGS. 1(a) and 1(b) are graphs showing", "PAR  <a class=\"figref\">FIGS. 1(a) and 1(b)</a> are graphs showing");
        //validFromToHTML.put("current shown in FIG. 1(a) is", "current shown in <a class=\"figref\">FIG. 1(a)</a> is");
    }

    @Test
    public void CleanText() {
        FormattedText format = new FormattedText();
        for (Entry<String, String> entry : validFromTo.entrySet()) {
            String actual = format.cleanText(entry.getKey());
            assertEquals(entry.getValue(), actual);
        }
    }

    @Test
    public void createRefs() {
        FormattedText format = new FormattedText();
        for (Entry<String, String> entry : validFromToHTML.entrySet()) {
            String actual = format.createRefs(entry.getKey());
            assertEquals(entry.getValue(), actual);
        }
    }

    @Test
    public void normalizedSimpleHtml() {
        FormattedText format = new FormattedText();

        StringBuilder stb = new StringBuilder();
        stb.append("<PAC>SECTION TITLE</PAC>\n");
        stb.append("<PAR>Paragraph text, referenceing FIG. 1 is a side elevational view</PAR>\n");
        stb.append("<PAR>More text now referenceing FIG. 2B is a top view</PAR>\n");

        StringBuilder expectStb = new StringBuilder();
        expectStb.append("<h2>SECTION TITLE</h2>\n");
        expectStb.append("<p>Paragraph text, referenceing <a class=\"figref\">FIG. 1</a> is a side elevational view</p>\n");
        expectStb.append("<p>More text now referenceing <a class=\"figref\">FIG. 2B</a> is a top view</p>\n");
        String expect = expectStb.toString();

        String actual = format.getSimpleHtml(stb.toString());
        assertEquals(expect, actual);
    }

}
