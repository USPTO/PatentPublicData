package gov.uspto.document.model.classification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Test;

import gov.uspto.patent.model.classification.CpcClassification;
import gov.uspto.patent.model.classification.IpcClassification;

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
	}

	@Test(expected = ParseException.class)
	public void failBlank() throws ParseException {
		CpcClassification.fromText("");
	}

	@Test
	public void validParseCheck() throws ParseException {
		for (Entry<String,String> cpc: validFromTo.entrySet()){
			CpcClassification cpcClass = CpcClassification.fromText(cpc.getKey());
			assertEquals( cpc.getValue(), cpcClass.toTextNormalized());
		}
	}

	@Test
	public void testCollapse() throws ParseException {
		String[] facets = new String[]{"0/E03D", "0/G06F", "1/E03D/E03D9", "1/G06F/G06F3", "2/E03D/E03D9/E03D908", "2/G06F/G06F3/G06F3041"};
		String[] expect = new String[]{"E03D908", "G06F3041"};

		List<String> cpcClass = CpcClassification.getMostSpecificClasses(Arrays.asList(facets));

		Collections.sort(cpcClass);

		assertEquals(Arrays.asList(expect), cpcClass);
	}

	@Test
	public void testEquals() throws ParseException {
		CpcClassification cpc1 = CpcClassification.fromText("D07B2201");
		CpcClassification cpc2 = CpcClassification.fromText("D07B2201");
		assertEquals(cpc1, cpc2);
	}

	@Test
	public void testEqualsUnder() throws ParseException {
		CpcClassification cpc1 = CpcClassification.fromText("D07B");
		CpcClassification cpc2 = CpcClassification.fromText("D07B2201");
		assertTrue(cpc1.equalOrUnder(cpc2));
	}

	@Test
	public void testClassTree() throws ParseException {
		CpcClassification cpcClass = CpcClassification.fromText("D07B2201/2051");
		String[] expect = new String[]{"D", "D07", "D07B", "D07B2201/00", "D07B2201/2051"};
		
		Set<String> classTreeSet = cpcClass.getClassTree();
		List<String> classTree = new ArrayList<String>(classTreeSet.size());
		classTree.addAll(classTreeSet);
		assertEquals(Arrays.asList(expect), classTree);
	}

	@Test
	public void testToTextNormalized() throws ParseException {
		CpcClassification cpcClass = CpcClassification.fromText("D07B2201/2051");
		String expect = "D07B 2201/2051";
		assertEquals(expect, cpcClass.toTextNormalized());
	}

	@Test
	public void testStandardize() throws ParseException {
		CpcClassification cpcClass = CpcClassification.fromText("D07B2201/2051");
		String expect = "D07B022012051";
		assertEquals(expect, cpcClass.standardize());
	}

	@Test
	public void testFacet() throws ParseException {
		CpcClassification cpcClass = CpcClassification.fromText("D07B2201/2051");		
		List<String> facets = cpcClass.toFacet();
		System.out.println(facets);
		assertEquals("4/D/D07/D07B/D07B2201/D07B22012051", facets.get(facets.size()-1));
	}

	@Test
	public void testFromFacet() throws ParseException {
		CpcClassification cpcClass = CpcClassification.fromText("D07B2201/2051");
		List<String> facets = cpcClass.toFacet();

		List<CpcClassification> classObjList = CpcClassification.fromFacets(facets);
		assertEquals(cpcClass, classObjList.get(0));
	}
}
