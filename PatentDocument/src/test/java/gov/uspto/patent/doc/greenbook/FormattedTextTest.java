package gov.uspto.patent.doc.greenbook;

import static org.junit.Assert.*;

import org.junit.Test;

import gov.uspto.patent.doc.greenbook.FormattedText;

public class FormattedTextTest {

    @Test
    public void SimpleHtml() {
        FormattedText format = new FormattedText();

        StringBuilder stb = new StringBuilder();
        stb.append("<PAC>SECTION TITLE</PAC>\n");
        stb.append("<PAR>Paragraph text, referenceing FIG. 1 is a side elevational view</PAR>\n");
        stb.append("<PAR>More text now referenceing FIG. 2B is a top view</PAR>\n");

        StringBuilder expectStb = new StringBuilder();
        expectStb.append("<h2 id=\"H-0001\">SECTION TITLE</h2>\n");
        expectStb.append("<p id=\"P-0001\">Paragraph text, referenceing <a class=\"figref\" idref=\"FIG-1\" id=\"FR-0001\">FIG-1</a> is a side elevational view</p>\n");
        expectStb.append("<p id=\"P-0002\">More text now referenceing <a class=\"figref\" idref=\"FIG-2B\" id=\"FR-0002\">FIG-2B</a> is a top view</p>\n");
        String expect = expectStb.toString();

        String actual = format.getSimpleHtml(stb.toString());
        assertEquals(expect, actual);
    }

}
