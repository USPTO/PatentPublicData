package gov.uspto.patent.model.classification;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

public class UspcClassificationTest {

	private static Map<String, String> validFromTo = new LinkedHashMap<String, String>();

	static {
		validFromTo.put("604385101", "604/385.101");
		validFromTo.put("280124108", "280/124.108");
		validFromTo.put("374 E5041", "374/E5.041");
		validFromTo.put("180 65245", "180/65.245");

		validFromTo.put("  2 52", "2/52");
		validFromTo.put(" 47 72", "47/72");
		validFromTo.put("002227000", "2/227");
		validFromTo.put("141 10", "141/10");
		validFromTo.put("141390", "141/390");

		validFromTo.put("303DIG  900", "303/DIG 900"); // pg020108.zip:681:US6336689B1
		validFromTo.put("200 6145 R- 6145 M", "200/61.45R-61.45M");

		// Ranges
		validFromTo.put("280 87041- 87043", "280/87.041-87.043");
		validFromTo.put("2989003-890054", "298/900.3-890.054");
		validFromTo.put("422 63- 681", "422/63-68.1");
		validFromTo.put("2504931-504 R", "250/493.1-504R");
		validFromTo.put("235 61 R- 59 TP", "235/61R-59TP");

		// validFromTo.put("707 3- 5", "707/3-5");
		validFromTo.put("252 6292 PZ", "252/62.92PZ"); // ipg180116.zip:4370:US9871188B2
		validFromTo.put(" 24FOR 129 B", "24/FOR 129B");

	}

	public void parseCheck(Map<String, String> checkMap) throws ParseException {
		for (Entry<String, String> check : checkMap.entrySet()) {
			UspcClassification uspc = new UspcClassification(check.getKey(), false);
			uspc.parseText(check.getKey());
			assertEquals("parsed: " + check.getValue(), check.getValue(), uspc.getTextNormalized());
		}
	}

	@Test(expected = ParseException.class)
	public void failBlank() throws ParseException {
		UspcClassification uspc = new UspcClassification("", false);
		uspc.parseText("");
	}

	@Test
	public void validParseCheck() throws ParseException {
		parseCheck(validFromTo);
	}

	@Test
	public void DClass() throws ParseException {
		// D-Design Class
		Map<String, String> designCheck = new LinkedHashMap<String, String>();
		designCheck.put(" D2907", "D2/907"); // leading space.
		designCheck.put("D 2908", "D2/908");
		designCheck.put("D11152", "D11/152");
		designCheck.put("D20 42", "D20/42");
		designCheck.put("D11143-144", "D11/143-144"); // range.
		parseCheck(designCheck);
	}

	@Test
	public void PLTClass() throws ParseException {
		// PLT-Plant Class
		Map<String, String> plantCheck = new LinkedHashMap<String, String>();
		plantCheck.put("PLT101", "PLT/101");
		plantCheck.put("plt101", "PLT/101");
		plantCheck.put("PLT/101", "PLT/101");
		parseCheck(plantCheck);
	}

	@Test
	public void IndentedSubClass() throws ParseException {
		Map<String, String> alphaCheck = new LinkedHashMap<String, String>();
		alphaCheck.put("02934R", "29/34R");
		alphaCheck.put(" 2934R", "29/34R");
		alphaCheck.put(" 29/34R", "29/34R");
		alphaCheck.put("29/34R", "29/34R");
		parseCheck(alphaCheck);
	}

	@Test
	public void AlphaSubClass() throws ParseException {
		// Alpha subclasses
		Map<String, String> alphaCheck = new LinkedHashMap<String, String>();
		alphaCheck.put("02934R", "29/34R");
		alphaCheck.put(" 29/34R", "29/34R");
		parseCheck(alphaCheck);
	}

	@Test
	public void ESubClass() throws ParseException {
		// ECLA-Derived Subclasses
		Map<String, String> eCheck = new LinkedHashMap<String, String>();
		eCheck.put("257/E29.012", "257/E29.012");
		eCheck.put("257/E2308-E23082", "257/E23.08-E23.082"); // range.
		parseCheck(eCheck);
	}

	@Test
	public void DIGSubClass() throws ParseException {
		// DIG Subclasses
		UspcClassification uspc = new UspcClassification(" 29/DIG 42", false);
		uspc.parseText(" 29/DIG 42");
		assertEquals("29/DIG 42", uspc.getTextNormalized());

		UspcClassification uspc2 = new UspcClassification("303DIG  900", false);
		uspc2.parseText("303DIG  900");
		assertEquals("303/DIG 900", uspc2.getTextNormalized());

	}

	@Test
	public void FORSubClass() throws ParseException {
		// FOR Subclasses
		Map<String, String> forCheck = new LinkedHashMap<String, String>();
		forCheck.put(" 33/FOR 100", "33/FOR 100");
		// forCheck.put("438FOR 363-FOR 385", "438/FOR 363-385"); // range.
		parseCheck(forCheck);
	}

	// @Test(expected = ParseException.class)
	public void failLongRange() throws ParseException {
		UspcClassification uspc = new UspcClassification("2989003-890054", false);
		uspc.parseText("2989003-890054"); // range too long
		System.out.println(uspc.getTextNormalized());
	}

	@Test
	public void depth() throws ParseException {
		UspcClassification uspc = new UspcClassification(" 29/DIG 42", false);
		uspc.parseText(" 29/DIG 42");
		assertEquals(2, uspc.getDepth());

		UspcClassification uspc2 = new UspcClassification("29/34R", false);
		uspc2.parseText("29/34R");
		assertEquals(2, uspc2.getDepth());
	}

	@Test
	public void facets() throws ParseException {
		UspcClassification uspc = new UspcClassification("PLT2631", false);
		uspc.parseText("PLT2631");
		List<String> facets = uspc.getTree().getLeafFacets();
		assertEquals("0/PLT", facets.get(0));
		assertEquals("1/PLT/263.1", facets.get(1));
	}

	@Test
	public void searchTokens() throws ParseException {
		UspcClassification uspc = new UspcClassification("D11143-144", false);
		uspc.parseText("D11143-144");
		List<String> tokens = uspc.getSearchTokens();
		assertEquals("D11143000", tokens.get(0));
		assertEquals("D11144000", tokens.get(1));
	}

	/*
	 * @Test public void testEquals() throws ParseException { UspcClassification
	 * uspc1 = UspcClassification.fromText(" D2907"); UspcClassification uspc2 =
	 * UspcClassification.fromText(" D2907"); assertEquals(uspc1, uspc2); }
	 * 
	 * @Test public void testEqualsUnder() throws ParseException { // D14/314 is
	 * subclass of D14/300 UspcClassification uspc1 =
	 * UspcClassification.fromText("D14300"); UspcClassification uspc2 =
	 * UspcClassification.fromText("D14314"); assertTrue(uspc1.equalOrUnder(uspc2));
	 * }
	 * 
	 * @Test public void testEqualsUnder2() throws ParseException { // 417/161.1A
	 * UspcClassification uspc1 = UspcClassification.fromText("417160");
	 * UspcClassification uspc2 = UspcClassification.fromText("417161.1A");
	 * assertTrue(uspc1.equalOrUnder(uspc2)); }
	 */

}
