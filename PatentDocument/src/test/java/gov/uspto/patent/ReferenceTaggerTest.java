package gov.uspto.patent;

import static org.junit.Assert.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

public class ReferenceTaggerTest {

	private static Map<String, String> text2Anno = new LinkedHashMap<String, String>();
	static {
		text2Anno.put("in claim 1 or 2",
				"in <a class=\"claim\" idref=\"CLM-0001\">claim 1</a> or <a class=\"claim\" idref=\"CLM-0002\">claim 2</a>");
		text2Anno.put("of claim 2-3", "of <a class=\"claim\" idref=\"CLM-0002 - CLM-0003\">claim 2-3</a>");
		text2Anno.put("PAR  FIG. 1 is a side elevational view",
				"PAR  <a class=\"figref\" idref=\"FIG-1\">FIG-1</a> is a side elevational view");
		text2Anno.put("PAR  FIG. 3A is a perspective view",
				"PAR  <a class=\"figref\" idref=\"FIG-3A\">FIG-3A</a> is a perspective view");
		text2Anno.put("PAR  FIG. 1a-1c is a side view",
				"PAR  <a class=\"figref\" idref=\"FIG-1a - FIG-1c\">FIG. 1a-1c</a> is a side view");
		text2Anno.put("PAR  FIGS. 1-2 are top views",
				"PAR  <a class=\"figref\" idref=\"FIG-1 - FIG-2\">FIGS. 1-2</a> are top views");
		text2Anno.put("FIG. 4 is a schematic representation",
				"<a class=\"figref\" idref=\"FIG-4\">FIG-4</a> is a schematic representation");
		text2Anno.put("FIGS. 5A and 5B together comprise",
				"<a class=\"figref\" idref=\"FIG-5A\">FIG-5A</a> and <a class=\"figref\" idref=\"FIG-5B\">FIG-5B</a> together comprise");
		text2Anno.put("illustrated in FIGS. 2 to 5.",
				"illustrated in <a class=\"figref\" idref=\"FIG-2 - FIG-5\">FIGS. 2 to 5</a>.");

		// text2Anno.put("as shown in FIG. 1(b) and may be", "as shown in <a
		// class=\"figref\">FIG. 1(b)</a> and may be");
		// text2Anno.put("PAR FIGS. 1(a) and 1(b) are graphs showing", "PAR <a
		// class=\"figref\">FIGS. 1(a) and 1(b)</a> are graphs showing");
		// text2Anno.put("current shown in FIG. 1(a) is", "current shown in <a
		// class=\"figref\">FIG. 1(a)</a> is");
	}

	private static Map<String, String> text2NormAnno = new LinkedHashMap<String, String>();
	static {
		text2NormAnno.put("in claim 1 or 2", "in Patent-Claim");
		text2NormAnno.put("of claim 2-3", "of Patent-Claim");
		text2NormAnno.put("PAR  FIG. 1 is a side elevational view", "PAR  Patent-Figure is a side elevational view");
		text2NormAnno.put("PAR  FIG. 3A is a perspective view", "PAR  Patent-Figure is a perspective view");
		text2NormAnno.put("PAR  FIG. 1a-1c is a side view", "PAR  Patent-Figure is a side view");
		text2NormAnno.put("PAR  FIGS. 1-2 are top views", "PAR  Patent-Figure are top views");
		// text2NormAnno.put("illustrated in FIGS. 2 to 5.", "illustrated in
		// Patent-Figure.");
		// text2NormAnno.put("as shown in FIG. 1(b) and may be", "as shown in
		// Patent-Figure and may be");
		// text2NormAnno.put("PAR FIGS. 1(a) and 1(b) are graphs showing", "PAR
		// Patent-Figure are graphs showing");
		// text2NormAnno.put("current shown in FIG. 1(a) is", "current shown in
		// Patent-Figure is");
	}

	@Test
	public void normRefs() {
		for (Entry<String, String> entry : text2NormAnno.entrySet()) {
			String actual = ReferenceTagger.normRefs(entry.getKey());
			assertEquals(entry.getValue(), actual);
		}
	}

	@Test
	public void markRefs() {
		for (Entry<String, String> entry : text2Anno.entrySet()) {
			String actual = ReferenceTagger.markRefs(entry.getKey());
			assertEquals(entry.getValue(), actual);
		}
	}

	@Test
	public void createClaimId() {
		String claimId = ReferenceTagger.createClaimId("claim 1");
		String expect = "CLM-0001";
		assertEquals(expect, claimId);
	}

	@Test
	public void createFigId() {
		String figId = ReferenceTagger.createFigId("Fig. 1A");
		String expect = "FIG-1A";
		assertEquals(expect, figId);
	}

	@Test
	public void createFigIdMulti() {
		String figId = ReferenceTagger.createFigId("FIGS. 1A and 1B");
		String expect = "FIG-1A,FIG-1B";
		assertEquals(expect, figId);
	}
}

