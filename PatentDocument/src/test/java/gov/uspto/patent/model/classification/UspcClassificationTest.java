package gov.uspto.patent.model.classification;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

public class UspcClassificationTest {

	private static Map<String, String> validFromTo = new LinkedHashMap<String, String>();

	static {
		validFromTo.put("  2 52", "002/052000000");
		validFromTo.put(" D2907", "D02/907000000"); // leading space.
		validFromTo.put("  D 38", "D03/800000000"); // leading 2 spaces.
		validFromTo.put(" 47 72", "047/072000000");
		validFromTo.put("D 2908", "D02/908000000");
		validFromTo.put("002227000", "002/227000000");
		validFromTo.put("D11152", "D11/152000000");
		validFromTo.put("D20 42", "D20/042000000");
		validFromTo.put("141 10", "141/010000000");
		validFromTo.put("141390", "141/390000000");
		validFromTo.put("PLT101", "PLT/101000000");
		validFromTo.put("Plt101", "PLT/101000000");

		validFromTo.put("337  2 2902", "337/002029020");
		validFromTo.put(" 47  101 R", "047/001010R00");
		validFromTo.put(" 24FOR 129 B", "024/FOR01290B");

		validFromTo.put("257E2308-E23082", "257/E23080000,E23082000"); // range.

		validFromTo.put("2504931-504 R", "250/493100000,5040R0000"); // range.
		validFromTo.put("235 61 R- 59 TP", "235/0590TP000,0610R0000"); // range.

		validFromTo.put("D11143-144", "D11/143000000,144000000"); // range.
		validFromTo.put("707  3-  5", "707/003000000,004000000,005000000"); // range.
		validFromTo.put("280 87041- 87043", "280/087041000,870410000,870420000,870430000"); // range.
		validFromTo.put("438FOR 363-FOR 385", "438/FOR036300,FOR038500"); // range.
		validFromTo.put("200 6145 R- 6145 M", "200/061450M00,061450R00"); // range.
		
		//validFromTo.put("188 79.5GE", "188/79.5GE");
		
		//validFromTo.put("2989003-890054", "298/9003,"); // range.
	}

	@Test(expected = ParseException.class)
	public void failLongRange() throws ParseException {
		UspcClassification uspc = new UspcClassification();
		uspc.parseText("2989003-890054"); // range too long
	}

	@Test(expected = ParseException.class)
	public void failBlank() throws ParseException {
		UspcClassification uspc = new UspcClassification();
		uspc.parseText("");
	}

	/*
	@Test
	public void testEquals() throws ParseException {
		UspcClassification uspc1 = UspcClassification.fromText(" D2907");
		UspcClassification uspc2 = UspcClassification.fromText(" D2907");
		assertEquals(uspc1, uspc2);
	}

	@Test
	public void testEqualsUnder() throws ParseException {
		// D14/314 is subclass of D14/300
		UspcClassification uspc1 = UspcClassification.fromText("D14300");
		UspcClassification uspc2 = UspcClassification.fromText("D14314");
		assertTrue(uspc1.equalOrUnder(uspc2));
	}
	
	@Test
	public void testEqualsUnder2() throws ParseException {
		// 417/161.1A
		UspcClassification uspc1 = UspcClassification.fromText("417160");
		UspcClassification uspc2 = UspcClassification.fromText("417161.1A");
		assertTrue(uspc1.equalOrUnder(uspc2));
	}
	*/

	@Test
	public void validParseCheck() throws ParseException {
		for (Entry<String, String> check : validFromTo.entrySet()) {
			UspcClassification uspc = new UspcClassification();
			uspc.parseText(check.getKey());
			assertEquals(check.getValue(), uspc.getTextNormalized());
		}
	}
}
