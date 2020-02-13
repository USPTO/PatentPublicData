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
		expectStb.append("<h2 level=\"1\">HEADING TEXT</h2> \n");
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

	@Test
	public void table() {
		String input = "<table frame=\"none\" colsep=\"0\" rowsep=\"0\">\n" +
				"<tgroup align=\"left\" colsep=\"0\" rowsep=\"0\" cols=\"2\">\n" +
				"<colspec colname=\"offset\" colwidth=\"21pt\" align=\"left\"/>\n" +
				"<colspec colname=\"1\" colwidth=\"196pt\" align=\"left\"/>\n" +
				"<thead><row><entry>head1</entry><entry namest=\"offset\" nameend=\"1\" align=\"center\" rowsep=\"1\">head2</entry></row></thead>\n" +
				"<tbody valign=\"top\">"+ 
				"<row><entry/><entry morerows=\"1\">cell data</entry></row>" + 
				"</tbody>\n" +
				"</tgroup></table>";

		String expect = "<table id=\"TBL-0001\">\n" + 
				" <colgroup>\n" + 
				"  <col width=\"21pt\" align=\"left\" />\n" + 
				"  <col width=\"196pt\" align=\"left\" />\n" + 
				" </colgroup> \n" + 
				" <thead>\n" + 
				"  <tr>\n" + 
				"   <th>head1</th>\n" + 
				"   <th align=\"center\">head2</th>\n" + 
				"  </tr>\n" + 
				" </thead> \n" + 
				" <tbody valign=\"top\">\n" + 
				"  <tr>\n" + 
				"   <td></td>\n" + 
				"   <td rowspan=\"2\">cell data</td>\n" + 
				"  </tr>\n" + 
				" </tbody> \n" + 
				"</table>";

		
		String actual = format.getSimpleHtml(input);

		assertEquals(expect, actual);
	}
	

	@Test
	public void subSupUnicode() {
		String input = "H<SB>2</SB>O <SP>1 + 2</SP>";

		String expect = "H\u2082O \u00B9 \u207A \u00B2";

		String actual = format.getSimpleHtml(input);

		assertEquals(expect, actual);
	}

	@Test
	public void subSupNonUnicode() {
		String input = "<SB>Z+1</SB> <SP>Z - 1</SP>";

		String expect = "<sub>Z+1</sub> \n" + 
				"<sup>Z - 1</sup>";

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
