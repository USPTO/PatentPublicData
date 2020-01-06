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
		expectStb.append("<h2 level=\"2\">HEADING TEXT</h2> \n");
		expectStb.append("<p level=\"1\">pargraph text.</p>");
		String expect = expectStb.toString();

		String actual = format.getSimpleHtml(input);

		assertEquals(expect, actual);
	}

	@Test
	public void pLevel() {
		Map<String, String> variations = new HashMap<String, String>();
		variations.put("<p lvl=\"1\"></p>", "<p level=\"1\"></p>");
		variations.put("<p level=\"1\"></p>", "<p level=\"1\"></p>");

		for (Entry<String, String> entry : variations.entrySet()) {
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
		variations.put("<figref idref=\"DRAWINGS\">FIG. 1</figref><i>a</i>",
				"<a idref=\"FIG-1\" id=\"FR-0001\" class=\"figref\">FIG. 1a</a>");
		variations.put("(<figref idref=\"DRAWINGS\">FIG. 1</figref>a)",
				"(\n<a idref=\"FIG-1\" id=\"FR-0001\" class=\"figref\">FIG. 1a</a>)");
		variations.put("<figref idref=\"DRAWINGS\">FIG. 1</figref>a;",
				"<a idref=\"FIG-1\" id=\"FR-0001\" class=\"figref\">FIG. 1a</a>;");
		variations.put("<figref idref=\"DRAWINGS\">FIG. 1</figref><i>a;</i>",
				"<a idref=\"FIG-1\" id=\"FR-0001\" class=\"figref\">FIG. 1a</a>;");
		variations.put("<figref idref=\"DRAWINGS\">FIG. 1</figref><i>aa</i>",
				"<a idref=\"FIG-1\" id=\"FR-0001\" class=\"figref\">FIG. 1</a>aa");
		// variations.put("<figref idref=\"DRAWINGS\">FIG. 1</figref> (a)", "<a
		// idref=\"FIG-1\" id=\"FR-0001\" class=\"figref\">FIG. 1a</a>");
		// variations.put("<figref idref=\"DRAWINGS\">FIG. 1</figref> (<i>a</i>)", "<a
		// idref=\"FIG-1\" id=\"FR-0001\" class=\"figref\">FIG. 1a</a>");

		for (Entry<String, String> entry : variations.entrySet()) {
			String actual = format.getSimpleHtml(entry.getKey());
			assertEquals(entry.getValue(), actual);
		}
	}

	// @Test
	public void tailingFigrefs() {
		Map<String, String> variations = new HashMap<String, String>();

		variations.put("<figref idref=\"DRAWINGS\">FIGS. 1</figref>, <b>2</b> and <b>3</b>c",
				"<a idref=\"FIG-1\" id=\"FR-0001\" class=\"figref\">FIGS. 1</a>, "
						+ "<a idref=\"FIG-2\" id=\"FR-0002\" class=\"figref\">2</a> and <a idref=\"FIG-3\" id=\"FR-0003\" class=\"figref\">3c</a>");

		// variations.put(
		// "<figref idref=\"DRAWINGS\">FIGS. 5 to 8</figref>",
		// "<a idref=\"FIG-5,FIG-6,FIG-7,FIG-8\" id=\"FR-0001\" class=\"figref\">FIGS. 5
		// to 8</a>"
		// );

		for (Entry<String, String> entry : variations.entrySet()) {
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
		stb.append(
				"<thead><row><entry>head1</entry><entry namest=\"offset\" nameend=\"1\" align=\"center\" rowsep=\"1\">head2</entry></row></thead>\n");
		stb.append("<tbody valign=\"top\"><row><entry/><entry morerows=\"1\">cell data</entry></row></tbody>\n");
		stb.append("</tgroup></table>");
		String input = stb.toString();

		String expect = "<table id=\"TBL-0001\">\n" + " <colgroup>\n" + "  <col width=\"21pt\" align=\"left\" />\n"
				+ "  <col width=\"196pt\" align=\"left\" />\n" + " </colgroup>    \n" + " <thead>\n" + "  <tr>\n"
				+ "   <th>head1</th>\n" + "   <th align=\"center\">head2</th>\n" + "  </tr>\n" + " </thead> \n"
				+ " <tbody valign=\"top\">\n" + "  <tr>\n" + "   <td></td>\n" + "   <td rowspan=\"2\">cell data</td>\n"
				+ "  </tr>\n" + " </tbody> \n" + "</table>";

		String actual = format.getSimpleHtml(input);

		assertEquals(expect, actual);
	}

	@Test
	public void MathML_html() {
		String input = "<math><mrow><mrow><msup><mi>x</mi><mn>2</mn></msup><mo>+</mo><mrow><mn>4</mn><mo>+</mo><mi>x</mi></mrow><mo>+</mo><mn>4</mn></mrow><mo>=</mo><mn>0</mn></mrow></math>";

		String expect = "<span id=\"MTH-0001\" class=\"math\" format=\"mathml\">\n" + " <math> \n" + "  <mrow> \n"
				+ "   <mrow> \n" + "    <msup> \n" + "     <mi>\n" + "       x \n" + "     </mi> \n" + "     <mn>\n"
				+ "       2 \n" + "     </mn> \n" + "    </msup> \n" + "    <mo>\n" + "      + \n" + "    </mo> \n"
				+ "    <mrow> \n" + "     <mn>\n" + "       4 \n" + "     </mn> \n" + "     <mo>\n" + "       + \n"
				+ "     </mo> \n" + "     <mi>\n" + "       x \n" + "     </mi> \n" + "    </mrow> \n" + "    <mo>\n"
				+ "      + \n" + "    </mo> \n" + "    <mn>\n" + "      4 \n" + "    </mn> \n" + "   </mrow> \n"
				+ "   <mo>\n" + "     = \n" + "   </mo> \n" + "   <mn>\n" + "     0 \n" + "   </mn> \n" + "  </mrow> \n"
				+ " </math></span>";

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

		String expect = "R\u2081";

		String actual = format.getSimpleHtml(input);

		assertEquals(expect, actual);
	}

	@Test
	public void formulae() {
		// Processing-Instruction marking in-line-formulae in text.
		String input = "<?in-line-formulae description=\"In-line Formulae\" end=\"lead\"?>CH<sub>4</sub><?in-line-formulae description=\"In-line Formulae\" end=\"tail\"?>";

		String expect = "<span id=\"FOR-0001\" class=\"formula\">CH\u2084</span>";

		String actual = format.getSimpleHtml(input);

		assertEquals(expect, actual);
	}

	@Test
	public void Updates_Insert_Deletions() {

		// Processing-Instruction found in Re-issued patents updated text: insert and
		// delete.
		String input = "<p id=\"p-0006\" num=\"0005\">In the past"
				+ "<?insert-start id=\"REI-00008\"  date=\"20180102\" ?>, " + "<?insert-end id=\"REI-00008\" ?>"
				+ "the freezing and/or extended refrigeration of a formulated fried egg has "
				+ "<?delete-start id=\"REI-00009\"  date=\"20180102\" ?>lead<?delete-end id=\"REI-00009\" ?> "
				+ "<?insert-start id=\"REI-00010\"  date=\"20180102\" ?>led <?insert-end id=\"REI-00010\" ?>"
				+ "to a loss of a cohesive texture and the degradation of other sensory perceptions such as mouth feel, taste, elasticity, and/or the food product not being tender or appealing to an individual. Another common problem encountered during delayed consumption, extended refrigeration and/or freezing of a formulated fried egg is that the food product exhibits syneresis, or the loss of water when frozen and reheated, or when stored for an extended period of time."
				+ "</p>";

		String expect = "<p id=\"p-0006\" num=\"0005\">In the past"
				+ "<ins>, </ins>the freezing and/or extended refrigeration of a formulated fried egg has "
				+ "<del>lead</del> "
				+ "<ins>led </ins>to a loss of a cohesive texture and the degradation of other sensory perceptions such as mouth feel, taste, elasticity, and/or the food product not being tender or appealing to an individual. Another common problem encountered during delayed consumption, extended refrigeration and/or freezing of a formulated fried egg is that the food product exhibits syneresis, or the loss of water when frozen and reheated, or when stored for an extended period of time."
				+ "</p>";

		String actual = format.getSimpleHtml(input);

		assertEquals(expect, actual);
	}

	@Test
	public void subSupNonUnicode() {
		String input = "<sub>Z+1</sub> <sup>Z - 1</sup>";

		String expect = "<sub>Z+1</sub> \n<sup>Z - 1</sup>";

		String actual = format.getSimpleHtml(input);

		assertEquals(expect, actual);
	}

}
