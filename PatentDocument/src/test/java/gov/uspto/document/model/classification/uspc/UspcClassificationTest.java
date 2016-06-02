package gov.uspto.document.model.classification.uspc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Sets;

import gov.uspto.patent.model.classification.UspcClassification;

public class UspcClassificationTest {

	private static Map<String, String> validFromTo = new LinkedHashMap<String, String>();

	static {
		validFromTo.put("  2 52", "002/052000000");
		validFromTo.put(" D2907", "D02/907000000"); // leading space.
		validFromTo.put(" 47 72", "047/072000000");
		validFromTo.put("D 2908", "D02/908000000");
		validFromTo.put("002227000", "002/227000000");
		validFromTo.put("D11152", "D11/152000000");
		validFromTo.put("D20 42", "D20/042000000");
		validFromTo.put("141 10", "141/010000000");
		validFromTo.put("141390", "141/390000000");
		validFromTo.put("PLT101", "PLT/101000000");

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
	}

	@Test(expected = ParseException.class)
	public void failBlank() throws ParseException {
		UspcClassification.fromText("a");
	}

	@Test
	public void validParseCheck() throws ParseException {
		for (Entry<String, String> uspc : validFromTo.entrySet()) {
			UspcClassification uspcClass = UspcClassification.fromText(uspc.getKey());
			assertEquals(uspc.getValue(), uspcClass.toTextNormalized());
		}
	}

	@Test
	public void testFacet() throws ParseException {
		UspcClassification uspcClass = UspcClassification.fromText("PLT101");
		List<String> facets = uspcClass.toFacet();
		//System.out.println(facets);
		
		Set<String> expect = Sets.newHashSet("0/PLT", "1/PLT/PLT101000000");

		assertTrue(facets.containsAll(expect));
	}

	@Test
	public void testRangeFacet() throws ParseException {
		UspcClassification uspcClass = UspcClassification.fromText("707  3-  5");
		List<String> facets = uspcClass.toFacet();
		//System.out.println(facets);

		Set<String> expect = Sets.newHashSet("0/707", "1/707/707003000000", "1/707/707004000000", "1/707/707005000000");

		assertTrue(facets.containsAll(expect));
	}
}
