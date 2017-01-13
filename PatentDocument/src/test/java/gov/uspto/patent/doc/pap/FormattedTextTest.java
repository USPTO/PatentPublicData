package gov.uspto.patent.doc.pap;

import static org.junit.Assert.*;

import org.junit.Test;

import gov.uspto.patent.doc.pap.FormattedText;

public class FormattedTextTest {

	private FormattedText format = new FormattedText();

	@Test
	public void headingAndParagraphs() {
		StringBuilder stb = new StringBuilder();
		stb.append("<heading lvl=2>HEADING TEXT</heading>\n");
		stb.append("<paragraph lvl=1>pargraph text.</paragraph>");
		String input = stb.toString();

		StringBuilder expectStb = new StringBuilder();
		expectStb.append("<h2 level=\"2\">HEADING TEXT</h2>\n");
		expectStb.append("<p level=\"1\">pargraph text.</p>");
		String expect = expectStb.toString();

		String actual = format.getSimpleHtml(input);

		assertEquals(expect, actual);
	}

	@Test
	public void figRef() {
		String input = "<cross-reference target=\"DRAWINGS\">FIG. 1</cross-reference>";

		String expect = "<a id=\"FR-0001\" idref=\"FIG-1\" class=\"figref\">FIG. 1</a>";

		String actual = format.getSimpleHtml(input);

		assertEquals(expect, actual);
	}

	@Test
	public void ClaimRef() {
		String input = "<dependent-claim-reference depends_on=\"CLM-00001\"><claim-text>claim 1</claim-text></dependent-claim-reference>";

		String expect = "<a id=\"CR-0001\" idref=\"CLM-00001\" class=\"claim\">claim 1</a>";

		String actual = format.getSimpleHtml(input);

		assertEquals(expect, actual);
	}

	@Test
	public void formula() {
		String input = "<in-line-formula>c=a+b</in-line-formula>";

		String expect = "<span id=\"FOR-0001\" class=\"formula\">c=a+b</span>";

		String actual = format.getSimpleHtml(input);

		assertEquals(expect, actual);
	}

	@Test
	public void table() {
		String input = "<table><tbody><row><entry>cell1</entry></row></tbody></table>";

		String expect = "<table id=\"TBL-0001\"><tbody><tr><td>cell1</td></tr></tbody></table>";

		String actual = format.getSimpleHtml(input);

		assertEquals(expect, actual);
	}

	@Test
	public void subsup() {
		String input = "H<subscript>2</subscript>O<superscript>3</superscript>";

		String expect = "H<sub>2</sub>O<sup>3</sup>";

		String actual = format.getSimpleHtml(input);

		assertEquals(expect, actual);
	}

	@Test
	public void MathML_html() {
		String intput = "<math><mrow><mrow><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mrow><mn>4</mn><mo>+</mo><mi>x</mi></mrow><mo>+</mo><mn>4</mn></mrow><mo>=</mo><mn>0</mn></mrow></math>";

		String expect = "<span id=\"MTH-0001\" class=\"math\" format=\"mathml\"><mrow><mrow><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mrow><mn>4</mn><mo>+</mo><mi>x</mi></mrow><mo>+</mo><mn>4</mn></mrow><mo>=</mo><mn>0</mn></mrow></span>";

		String actual = format.getSimpleHtml(intput);

		assertEquals(expect, actual);
	}
}
