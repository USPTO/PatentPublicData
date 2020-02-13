package gov.uspto.patent.model.classification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

public class CpCClassificationTest {

	private static Map<String, String> validFromTo = new LinkedHashMap<String, String>();
	static {
		validFromTo.put("A01B300", "A01B 3/00");
		validFromTo.put("A01B3/00", "A01B 3/00");
		validFromTo.put("A01B33/00", "A01B 33/00");

		validFromTo.put("D07B22012051", "D07B 2201/2051");
		validFromTo.put("D07B 2201/2051", "D07B 2201/2051");
		validFromTo.put("D07B2201/2051", "D07B 2201/2051");
		validFromTo.put("D07B2201/20903", "D07B 2201/20903");

		validFromTo.put("D21", "D21");
		validFromTo.put("D21B", "D21B");
		validFromTo.put("D21B 1/00", "D21B 1/00");

		// RANGES
		validFromTo.put("G06F 1/32-3296", "G06F 1/32-3296");
		validFromTo.put("G06F 1/32-1/3296", "G06F 1/32-3296");
		validFromTo.put("A01B 33/00-34/00", "A01B 33/00-34/00");
		validFromTo.put("D05B 1/00-25/00", "D05B 1/00-25/00");
	}

	@Test(expected = ParseException.class)
	public void failBlank() throws ParseException {
		CpcClassification cpc = new CpcClassification("", false);
		cpc.parseText("");
	}

	@Test
	public void validParseCheck() throws ParseException {
		for (Entry<String, String> check : validFromTo.entrySet()) {
			CpcClassification cpc = new CpcClassification(check.getKey(), false);
			cpc.parseText(check.getKey());

			assertEquals("parse of " + check.getValue() + ";", check.getValue(), cpc.getTextNormalized());

			// System.out.println(cpc.toString());
		}
	}

	@Test
	public void parseGroupRange() throws ParseException {
		String cpctext = "G04B 19/00-19/34";

		CpcClassification cpc = new CpcClassification(cpctext, false);
		cpc.parseText(cpctext);

		assertEquals("G", cpc.getSection());
		assertEquals("04", cpc.getMainClass());
		assertEquals("B", cpc.getSubClass());

		String[] mainGroup = cpc.getMainGroup();
		assertEquals(1, mainGroup.length);
		assertEquals("19", mainGroup[0]);

		String[] subGroup = cpc.getSubGroup();
		assertEquals(2, subGroup.length);
		assertEquals("00", subGroup[0]);
		assertEquals("34", subGroup[1]);

		// System.out.println( cpc.getTextNormalized() );
		assertEquals("parse of " + cpctext + ";", "G04B 19/00-34", cpc.getTextNormalized());
	}

	@Test
	public void parseSubGroupRange() throws ParseException {
		String cpctext = "G04B 19/00-34";

		CpcClassification cpc = new CpcClassification(cpctext, false);
		cpc.parseText(cpctext);

		assertEquals("G", cpc.getSection());
		assertEquals("04", cpc.getMainClass());
		assertEquals("B", cpc.getSubClass());

		String[] mainGroup = cpc.getMainGroup();
		assertEquals(1, mainGroup.length);
		assertEquals("19", mainGroup[0]);

		String[] subGroup = cpc.getSubGroup();
		assertEquals(2, subGroup.length);
		assertEquals("00", subGroup[0]);
		assertEquals("34", subGroup[1]);

		// System.out.println( cpc.getTextNormalized() );
		assertEquals("parse of " + cpctext + ";", "G04B 19/00-34", cpc.getTextNormalized());
	}

	@Test
	public void parseSubGroupRange2() throws ParseException {
		String cpctext = "G06F 1/32-3296";

		CpcClassification cpc = new CpcClassification(cpctext, false);
		cpc.parseText(cpctext);

		assertEquals("G", cpc.getSection());
		assertEquals("06", cpc.getMainClass());
		assertEquals("F", cpc.getSubClass());

		String[] mainGroup = cpc.getMainGroup();
		assertEquals(1, mainGroup.length);
		assertEquals("1", mainGroup[0]);

		String[] subGroup = cpc.getSubGroup();
		assertEquals(2, subGroup.length);
		assertEquals("32", subGroup[0]);
		assertEquals("3296", subGroup[1]);

		// System.out.println( cpc.getTextNormalized() );
		assertEquals("parse of " + cpctext + ";", "G06F 1/32-3296", cpc.getTextNormalized());
	}

	@Test
	public void testEquals() throws ParseException {
		CpcClassification cpc1 = new CpcClassification("D07B2201", false);
		cpc1.parseText("D07B2201");

		CpcClassification cpc2 = new CpcClassification("D07B2201", false);
		cpc2.parseText("D07B2201");

		assertEquals(cpc1, cpc2);
	}

	// @Test
	public void testEqualsUnder() throws ParseException {
		CpcClassification cpc1 = new CpcClassification("D07B", false);
		cpc1.parseText("D07B");

		CpcClassification cpc2 = new CpcClassification("D07B2201", false);
		cpc2.parseText("D07B2201");

		assertTrue(cpc1.isContained(cpc2));
	}

	@Test
	public void textNormalized() throws ParseException {
		CpcClassification cpcClass = new CpcClassification("D07B2201/2051", false);
		cpcClass.parseText("D07B2201/2051");

		String expect = "D07B 2201/2051";
		assertEquals(expect, cpcClass.getTextNormalized());
	}

	@Test
	public void facets() throws ParseException {
		String cpctext = "G06F 1/32-1/3296";
		CpcClassification cpc = new CpcClassification(cpctext, false);
		cpc.parseText(cpctext);

		//System.out.println(cpc.getTree().getLeafFacets());

		List<String> facetTokens = cpc.getTree().getLeafFacets();
		assertEquals("expect 6 facet tokens;", 6, facetTokens.size());
		assertEquals("0/G", facetTokens.get(0));
		assertEquals("1/G/06", facetTokens.get(1));
		assertEquals("2/G/06/F00", facetTokens.get(2));
		assertEquals("3/G/06/F00/001", facetTokens.get(3));
		assertEquals("4/G/06/F00/001/3200", facetTokens.get(4));
		assertEquals("4/G/06/F00/001/3296", facetTokens.get(5));
	}

	@Test
	public void searchTokens() throws ParseException {
		String cpctext = "G06F 1/32-1/3296";
		CpcClassification cpc = new CpcClassification(cpctext, false);
		cpc.parseText(cpctext);

		//System.out.println(cpc.getSearchTokens());

		List<String> tokens = cpc.getSearchTokens();
		assertEquals("expect 2 tokens;", 2, tokens.size());
		assertEquals("G06F000013200000", tokens.get(0));
		assertEquals("G06F000013296000", tokens.get(1));
	}

	@Test
	public void standardize() throws ParseException {
		CpcClassification cpcClass = new CpcClassification("D07B2201/2051", false);
		cpcClass.parseText("D07B2201/2051");
		String expect = "D07B022012051";
		assertEquals(expect, cpcClass.standardize());
	}

	@Test
	public void filterCPC() throws ParseException {
		Set<PatentClassification> clazs = new TreeSet<PatentClassification>();

		CpcClassification cpcClass = new CpcClassification("D21", true);
		cpcClass.parseText("D21");
		clazs.add(cpcClass);

		CpcClassification cpcClass2 = new CpcClassification("D07B2201/2051", false);
		cpcClass2.parseText("D07B2201/2051");
		clazs.add(cpcClass2);

		CpcClassification cpcClass3 = new CpcClassification("D07B2201", false);
		cpcClass3.parseText("D07B2201");
		clazs.add(cpcClass3);

		Map<String, List<CpcClassification>> cpcClasses = CpcClassification.filterCpc(clazs);
		assertEquals(cpcClasses.get("inventive").size(), 1);
		assertEquals(cpcClass, cpcClasses.get("inventive").get(0));

		assertEquals(cpcClasses.get("additional").size(), 2);
		assertEquals(cpcClass3, cpcClasses.get("additional").get(0));
		assertEquals(cpcClass2, cpcClasses.get("additional").get(1));
	}
}
