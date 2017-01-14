package gov.uspto.patent.doc.sgml;

import static org.junit.Assert.*;

import org.junit.Test;

import gov.uspto.patent.doc.sgml.FormattedText;

public class FormattedTextTest {

	private FormattedText format = new FormattedText();

	@Test
	public void headingAndParagraphs() {
		StringBuilder stb = new StringBuilder();

		stb.append("<H LVL=\"1\"><STEXT><PDAT>HEADING TEXT</PDAT></STEXT></H>\n");
		stb.append("<PARA ID=\"P-00003\" LVL=\"0\"><PTEXT><PDAT>pargraph text.</PDAT></PTEXT></PARA>");
		String input = stb.toString();

		StringBuilder expectStb = new StringBuilder();
		expectStb.append("<h2 level=\"1\">HEADING TEXT</h2>\n");
		expectStb.append("<p id=\"P-00003\" level=\"0\">pargraph text.</p>");
		String expect = expectStb.toString();

		String actual = format.getSimpleHtml(input);

		assertEquals(expect, actual);
	}

	@Test
	public void figRef() {
		String input = "<FGREF ID=\"DRAWINGS\"><PDAT>FIG. 1</PDAT></FGREF>";

		String expect = "<a id=\"FR-0001\" idref=\"FIG-1\" class=\"figref\">FIG. 1</a>";

		String actual = format.getSimpleHtml(input);

		assertEquals(expect, actual);
	}

	@Test
	public void ClaimRef() {
		String input = "<CLREF ID=\"CLM-00001\"><PDAT>claim 1</PDAT></CLREF>";

		String expect = "<a id=\"CR-0001\" idref=\"CLM-00001\" class=\"claim\">claim 1</a>";

		String actual = format.getSimpleHtml(input);

		assertEquals(expect, actual);
	}

	/*
	@Test
	public void formula() {
		String input = "<in-line-formula>c=a+b</in-line-formula>";

		String expect = "<span id=\"FOR-0001\" class=\"formula\">c=a+b</span>";

		String actual = format.getSimpleHtml(input);

		assertEquals(expect, actual);
	}
	*/

	/*
	@Test
	public void table() {
		String input = "<table><tbody><row><entry>cell1</entry></row></tbody></table>";

		String expect = "<table id=\"TBL-0001\"><tbody><tr><td>cell1</td></tr></tbody></table>";

		String actual = format.getSimpleHtml(input);

		assertEquals(expect, actual);
	}
	*/

	@Test
	public void subsup() {
		String input = "H<SB>2</SB>O<SP>3</SP>";

		String expect = "H<sub>2</sub>O<sup>3</sup>";

		String actual = format.getSimpleHtml(input);

		assertEquals(expect, actual);
	}

	@Test
	public void MathML_html() {
		String intput = "<math><mrow><mrow><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mrow><mn>4</mn><mo>+</mo><mi>x</mi></mrow><mo>+</mo><mn>4</mn></mrow><mo>=</mo><mn>0</mn></mrow></math>";

		String expect = "<span id=\"MTH-0001\" class=\"math\" format=\"mathml\"><math><mrow><mrow><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mrow><mn>4</mn><mo>+</mo><mi>x</mi></mrow><mo>+</mo><mn>4</mn></mrow><mo>=</mo><mn>0</mn></mrow></math></span>";

		String actual = format.getSimpleHtml(intput);

		assertEquals(expect, actual);
	}
}
