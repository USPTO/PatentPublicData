package gov.uspto.patent.doc.xml;

import static org.junit.Assert.*;

import org.junit.Test;

import gov.uspto.patent.doc.simplehtml.FreetextConfig;
import gov.uspto.patent.doc.simplehtml.HtmlFieldType;
import gov.uspto.patent.doc.xml.FormattedText;

public class FormattedTextCustomizeTest {

    @Test
    public void MathML_html() {
        FormattedText format = new FormattedText();

        StringBuilder stb = new StringBuilder();
        stb.append("<p id=\"h-1\">SECTION TITLE</p>");
        stb.append("<p><maths>");
        stb.append(
                "<math><mrow><mrow><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mrow><mn>4</mn><mo>+</mo><mi>x</mi></mrow><mo>+</mo><mn>4</mn></mrow><mo>=</mo><mn>0</mn></mrow></math>");
        stb.append("</maths></p>");

        StringBuilder expectStb = new StringBuilder();
        expectStb.append("<h4 id=\"h-1\">SECTION TITLE</h4>");
        expectStb.append("<p level=\"\"><span id=\"MTH-0001\" class=\"math\" format=\"mathml\">");
        expectStb.append("<math><mrow><mrow><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mrow><mn>4</mn><mo>+</mo><mi>x</mi></mrow><mo>+</mo><mn>4</mn></mrow><mo>=</mo><mn>0</mn></mrow></math></span></p>");
        String expect = expectStb.toString();

        String actual = format.getSimpleHtml(stb.toString());
        assertEquals(expect, actual);
    }

    @Test
    public void MathML_plaintext() {
        FormattedText format = new FormattedText();

        StringBuilder stb = new StringBuilder();
        stb.append("<p id=\"h-1\">SECTION TITLE</p>");
        stb.append("<p><maths>");
        stb.append(
                "<math><mrow><mrow><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mrow><mn>4</mn><mo>+</mo><mi>x</mi></mrow><mo>+</mo><mn>4</mn></mrow><mo>=</mo><mn>0</mn></mrow></math>");
        stb.append("</maths></p>");

        StringBuilder expectStb = new StringBuilder();
        expectStb.append("   \n SECTION TITLE\n \n");
        expectStb.append("  math(mrow(mrow(msup(mi(x)mn(2))mo(+)mrow(mn(4)mo(+)mi(x))mo(+)mn(4))mo(=)mn(0)))  \n");
        String expect = expectStb.toString();

        String actual = format.getPlainText(stb.toString(), new FreetextConfig());
        //assertEquals(expect, actual);
    }

    @Test
    public void plaintextRemoveMath() {
        FormattedText format = new FormattedText();

        StringBuilder stb = new StringBuilder();
        stb.append("<p id=\"h-1\">SECTION TITLE</p>");
        stb.append("<p><maths>");
        stb.append(
                "<math><mrow><mrow><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mrow><mn>4</mn><mo>&InvisibleTimes;</mo><mi>x</mi></mrow><mo>+</mo><mn>4</mn></mrow><mo>=</mo><mn>0</mn></mrow></math>");
        stb.append("</maths></p>");

        String expect = "\nSECTION TITLE\n\n\n";

        FreetextConfig textConfig = new FreetextConfig();
        textConfig.remove(HtmlFieldType.MATHML);

        String actual = format.getPlainText(stb.toString(), textConfig);
        assertEquals(expect, actual);
    }

    @Test
    public void plaintextRemoveHeaders() {
        FormattedText format = new FormattedText();

        StringBuilder stb = new StringBuilder();
        stb.append("<p id=\"h-1\">SECTION TITLE</p>");
        stb.append("<p>Section Text here</p>");

        String expect = "\nSection Text here\n";

        FreetextConfig textConfig = new FreetextConfig();
        textConfig.remove(HtmlFieldType.HEADER);

        String actual = format.getPlainText(stb.toString(), textConfig);
        assertEquals(expect, actual);
    }

    @Test
    public void plaintextRemoveTable() {
        FormattedText format = new FormattedText();

        StringBuilder stb = new StringBuilder();
        stb.append("<p id=\"h-1\">SECTION TITLE</p>");
        stb.append("<p><table><row><entry>text</entry></row></table></p>");

        StringBuilder expectStb = new StringBuilder();
        expectStb.append("\nSECTION TITLE\n\n\n\n");
        String expect = expectStb.toString();

        FreetextConfig textConfig = new FreetextConfig();
        textConfig.remove(HtmlFieldType.TABLE);

        String actual = format.getPlainText(stb.toString(), textConfig);
        //assertEquals(expect, actual);
    }
}
