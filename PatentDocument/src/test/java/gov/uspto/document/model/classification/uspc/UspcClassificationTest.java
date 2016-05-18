package gov.uspto.document.model.classification.uspc;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import gov.uspto.patent.model.classification.UspcClassification;


public class UspcClassificationTest {

	private static Map<String, String> validFromTo = new LinkedHashMap<String, String>();
	static {
		validFromTo.put("  2 52", "002/052000");
		validFromTo.put(" D2907",  "D02/907000"); // leading space.
		validFromTo.put(" 47 72", "047/072000");
		validFromTo.put("D 2908", "D02/908000");
		validFromTo.put("002227000", "002/227000");
		validFromTo.put("D11152", "D11/152000");
		validFromTo.put("D20 42", "D20/042000");
		validFromTo.put("141 10", "141/010000");
		validFromTo.put("141390", "141/390000");
		validFromTo.put("PLT101", "PLT/101000");
		
		validFromTo.put("D11143-144", "D11/143000"); // range.
		validFromTo.put("707  3-  5", "D11/143000"); // range.
	}

	@Test(expected = ParseException.class)
	public void failBlank() throws ParseException {
		 UspcClassification.fromText("a");
	}

	@Test
	public void validParseCheck() throws ParseException {
		for (Entry<String,String> uspc: validFromTo.entrySet()){
			UspcClassification uspcClass = UspcClassification.fromText(uspc.getKey());
			assertEquals( uspc.getValue(), uspcClass.toTextNormalized());
		}
	}

	@Test
	public void testFacet() throws ParseException {
		UspcClassification uspcClass = UspcClassification.fromText("PLT101");
		List<String> facets = uspcClass.toFacet();
		assertEquals("1/PLT/PLT101000", facets.get(facets.size()-1));		
	}
}
