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

        String expect = "<h4 id=\"h-1\">SECTION TITLE</h4> \n" + 
        		"<p><span id=\"MTH-0001\" class=\"math\" format=\"mathml\">\n" + 
        		"  <math> \n" + 
        		"   <mrow> \n" + 
        		"    <mrow> \n" + 
        		"     <msup> \n" + 
        		"      <mi>\n" + 
        		"        x \n" + 
        		"      </mi> \n" + 
        		"      <mn>\n" + 
        		"        2 \n" + 
        		"      </mn> \n" + 
        		"     </msup> \n" + 
        		"     <mo>\n" + 
        		"       + \n" + 
        		"     </mo> \n" + 
        		"     <mrow> \n" + 
        		"      <mn>\n" + 
        		"        4 \n" + 
        		"      </mn> \n" + 
        		"      <mo>\n" + 
        		"        + \n" + 
        		"      </mo> \n" + 
        		"      <mi>\n" + 
        		"        x \n" + 
        		"      </mi> \n" + 
        		"     </mrow> \n" + 
        		"     <mo>\n" + 
        		"       + \n" + 
        		"     </mo> \n" + 
        		"     <mn>\n" + 
        		"       4 \n" + 
        		"     </mn> \n" + 
        		"    </mrow> \n" + 
        		"    <mo>\n" + 
        		"      = \n" + 
        		"    </mo> \n" + 
        		"    <mn>\n" + 
        		"      0 \n" + 
        		"    </mn> \n" + 
        		"   </mrow> \n" + 
        		"  </math></span></p>";

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

        String actual = format.getPlainText(stb.toString(), new FreetextConfig(true, false));
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

        String expect = "\nSECTION TITLE\n\n";

        FreetextConfig textConfig = new FreetextConfig(true, false);
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

        FreetextConfig textConfig = new FreetextConfig(true, false);
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

        FreetextConfig textConfig = new FreetextConfig(true, false);
        textConfig.remove(HtmlFieldType.TABLE);

        String actual = format.getPlainText(stb.toString(), textConfig);
        //assertEquals(expect, actual);
    }
    
    @Test
    public void plaintextNonPrettyPrint() {
    	Boolean prettyPrint = false;
    	
        FormattedText format = new FormattedText();

        StringBuilder stb = new StringBuilder();
        stb.append("<p id=\"h-1\">SECTION TITLE</p>");
        stb.append("<p><table><row><entry>text</entry></row></table></p>");

        String expect = "\\nSECTION TITLE\\n \\n \\n text \\n\\n";

        FreetextConfig textConfig = new FreetextConfig(prettyPrint, false);

        String actual = format.getPlainText(stb.toString(), textConfig);
        assertEquals(expect, actual);
    }
    
    @Test
    public void plaintextReplace() {
    	Boolean prettyPrint = true;
    	
        FormattedText format = new FormattedText();

        StringBuilder stb = new StringBuilder();
        stb.append("<p>");
        stb.append("<a class=\"figref\">1232<a>");
        stb.append("</p>");

        String expect = "\nFIG-REF";

        FreetextConfig textConfig = new FreetextConfig(prettyPrint, false);
        textConfig.replace(HtmlFieldType.FIGREF, "FIG-REF");

        String actual = format.getPlainText(stb.toString(), textConfig);
        assertEquals(expect, actual);
    }
    
}
