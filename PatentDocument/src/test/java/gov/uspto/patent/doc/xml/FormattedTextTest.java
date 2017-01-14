package gov.uspto.patent.doc.xml;

import static org.junit.Assert.*;

import org.junit.Test;

import gov.uspto.patent.doc.xml.FormattedText;

public class FormattedTextTest {

	private FormattedText format = new FormattedText();

	@Test
	public void headingAndParagraphs() {
		StringBuilder stb = new StringBuilder();
		stb.append("<heading lvl=2>HEADING TEXT</heading>\n");
		stb.append("<p lvl=1>pargraph text.</p>");
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
		String input = "<figref idref=\"DRAWINGS\">FIG. 1</figref>";

		String expect = "<a idref=\"FIG-1\" id=\"FR-0001\" class=\"figref\">FIG. 1</a>";

		String actual = format.getSimpleHtml(input);

		assertEquals(expect, actual);
	}

	@Test
	public void ClaimRef() {
		String input = "<claim-ref idref=\"CLM-00001\">claim 1</claim-ref>";

		String expect = "<a idref=\"CLM-00001\" id=\"CR-0001\" class=\"claim\">claim 1</a>";

		String actual = format.getSimpleHtml(input);

		assertEquals(expect, actual);
	}

	/*
	 * @Test public void formula() { String input =
	 * "<in-line-formula>c=a+b</in-line-formula>";
	 * 
	 * String expect = "<span id=\"FOR-0001\" class=\"formula\">c=a+b</span>";
	 * 
	 * String actual = format.getSimpleHtml(input);
	 * 
	 * assertEquals(expect, actual); }
	 */

	@Test
	public void table() {
		String input = "<table><tbody><row><entry>cell1</entry></row></tbody></table>";

		String expect = "<table id=\"TBL-0001\"><tbody><tr><td>cell1</td></tr></tbody></table>";

		String actual = format.getSimpleHtml(input);

		assertEquals(expect, actual);
	}

	@Test
	public void MathML_html() {
		String input = "<math><mrow><mrow><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mrow><mn>4</mn><mo>+</mo><mi>x</mi></mrow><mo>+</mo><mn>4</mn></mrow><mo>=</mo><mn>0</mn></mrow></math>";

		String expect = "<span id=\"MTH-0001\" class=\"math\" format=\"mathml\"><math><mrow><mrow><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mrow><mn>4</mn><mo>+</mo><mi>x</mi></mrow><mo>+</mo><mn>4</mn></mrow><mo>=</mo><mn>0</mn></mrow></math></span>";

		String actual = format.getSimpleHtml(input);

		assertEquals(expect, actual);
	}

	@Test
	public void formulae() {

		String input = "<?in-line-formulae description=\"In-line Formulae\" end=\"lead\"?>CH<sub>4</sub><?in-line-formulae description=\"In-line Formulae\" end=\"tail\"?>";

		String expect = "<span id=\"FOR-0001\" class=\"formula\">CH\u2084</span>";

		String actual = format.getSimpleHtml(input);

		assertEquals(expect, actual);
	}

	@Test
	public void subSupUnicode() {
		String input = "H<sub>2</sub>O <sup>1 + 2</sup>";

		String expect = "H\u2082O \u00B9 \u207A \u00B2";

		String actual = format.getSimpleHtml(input);

		assertEquals(expect, actual);
	}

	@Test
	public void subSupNonUnicode() {
		String input = "<sub>Z+1</sub> <sup>Z - 1</sup>";

		String expect = "<sub>Z+1</sub> <sup>Z - 1</sup>";

		String actual = format.getSimpleHtml(input);

		assertEquals(expect, actual);
	}

}
