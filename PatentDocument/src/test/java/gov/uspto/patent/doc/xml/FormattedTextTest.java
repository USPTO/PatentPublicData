package gov.uspto.patent.doc.xml;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
	public void pLevel(){
		Map<String, String> variations = new HashMap<String, String>();
		variations.put("<p lvl=\"1\"></p>", "<p level=\"1\"></p>");
		variations.put("<p level=\"1\"></p>", "<p level=\"1\"></p>");

		for(Entry<String, String> entry : variations.entrySet()){
			String actual = format.getSimpleHtml(entry.getKey());
			assertEquals(entry.getValue(), actual);
		}
	}

	@Test
	public void figRef() {
		String input = "<figref idref=\"DRAWINGS\">FIG. 1</figref>";

		String expect = "<a idref=\"FIG-1\" id=\"FR-0001\" class=\"figref\">FIG. 1</a>";

		String actual = format.getSimpleHtml(input);

		assertEquals(expect, actual);
	}

	@Test
	public void tailingEntityText() {
		Map<String, String> variations = new HashMap<String, String>();
		variations.put("<figref idref=\"DRAWINGS\">FIG. 1</figref><i>a</i>", "<a idref=\"FIG-1\" id=\"FR-0001\" class=\"figref\">FIG. 1a</a>");
		variations.put("(<figref idref=\"DRAWINGS\">FIG. 1</figref>a)", "(<a idref=\"FIG-1\" id=\"FR-0001\" class=\"figref\">FIG. 1a</a>)");
		variations.put("<figref idref=\"DRAWINGS\">FIG. 1</figref>a;", "<a idref=\"FIG-1\" id=\"FR-0001\" class=\"figref\">FIG. 1a</a>;");
		variations.put("<figref idref=\"DRAWINGS\">FIG. 1</figref><i>a;</i>", "<a idref=\"FIG-1\" id=\"FR-0001\" class=\"figref\">FIG. 1a</a>;");
		variations.put("<figref idref=\"DRAWINGS\">FIG. 1</figref><i>aa</i>", "<a idref=\"FIG-1\" id=\"FR-0001\" class=\"figref\">FIG. 1</a>aa");
		//variations.put("<figref idref=\"DRAWINGS\">FIG. 1</figref> (a)", "<a idref=\"FIG-1\" id=\"FR-0001\" class=\"figref\">FIG. 1a</a>");
		//variations.put("<figref idref=\"DRAWINGS\">FIG. 1</figref> (<i>a</i>)", "<a idref=\"FIG-1\" id=\"FR-0001\" class=\"figref\">FIG. 1a</a>");

		for(Entry<String, String> entry : variations.entrySet()){
			String actual = format.getSimpleHtml(entry.getKey());
			assertEquals(entry.getValue(), actual);
		}
	}

	@Test
	public void tailingFigrefs() {
		Map<String, String> variations = new HashMap<String, String>();
		
		variations.put(
				"<figref idref=\"DRAWINGS\">FIGS. 1</figref>, <b>2</b> and <b>3</b>c", 
					"<a idref=\"FIG-1\" id=\"FR-0001\" class=\"figref\">FIGS. 1</a>, "
					+ "<a idref=\"FIG-2\" id=\"FR-0002\" class=\"figref\">2</a> and <a idref=\"FIG-3\" id=\"FR-0003\" class=\"figref\">3c</a>"
				);
		
		//variations.put(
		//		"<figref idref=\"DRAWINGS\">FIGS. 5 to 8</figref>", 
		//			"<a idref=\"FIG-5,FIG-6,FIG-7,FIG-8\" id=\"FR-0001\" class=\"figref\">FIGS. 5 to 8</a>"
		//		);

		for(Entry<String, String> entry : variations.entrySet()){
			String actual = format.getSimpleHtml(entry.getKey());
			assertEquals(entry.getValue(), actual);
		}
	}

	@Test
	public void ClaimRef() {
		String input = "<claim-ref idref=\"CLM-00001\">claim 1</claim-ref>";

		String expect = "<a idref=\"CLM-00001\" id=\"CR-0001\" class=\"claim\">claim 1</a>";

		String actual = format.getSimpleHtml(input);

		assertEquals(expect, actual);
	}

	@Test
	public void table() {
		StringBuilder stb = new StringBuilder();
		stb.append("<table frame=\"none\" colsep=\"0\" rowsep=\"0\">\n");
		stb.append("<tgroup align=\"left\" colsep=\"0\" rowsep=\"0\" cols=\"2\">\n");
		stb.append("<colspec colname=\"offset\" colwidth=\"21pt\" align=\"left\"/>\n");
		stb.append("<colspec colname=\"1\" colwidth=\"196pt\" align=\"left\"/>\n");
		stb.append("<thead><row><entry>head1</entry><entry namest=\"offset\" nameend=\"1\" align=\"center\" rowsep=\"1\">head2</entry></row></thead>\n");
		stb.append("<tbody valign=\"top\"><row><entry/><entry morerows=\"1\">cell data</entry></row></tbody>\n");
		stb.append("</tgroup></table>");
		String input = stb.toString();

		StringBuilder expectStb = new StringBuilder();
		expectStb.append("\n\n\n<table id=\"TBL-0001\">");
		expectStb.append("<colgroup><col width=\"21pt\" align=\"left\" /><col width=\"196pt\" align=\"left\" /></colgroup>\n");
		expectStb.append("<thead><tr><th>head1</th><th align=\"center\">head2</th></tr></thead>\n");
		expectStb.append("<tbody valign=\"top\"><tr><td></td><td rowspan=\"2\">cell data</td></tr></tbody>\n");
		expectStb.append("</table>");
		String expect = expectStb.toString();
		
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
	public void subSupSpaceUnicode() {
		String input = "R<sub>1 </sub>";

		String expect = "R\u2081 ";

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
