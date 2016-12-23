package gov.uspto.patent.model.classification;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class ClassificationTokenizerTest {

	@Test
	public void partsToFacets() throws ParseException {
		CpcClassification cpc = new CpcClassification();
		cpc.parseText("D07B2201/2051");

		String[] actual = ClassificationTokenizer.partsToFacet(cpc.getParts());

		String[] expected = new String[] { "0/D", "1/D/D07", "2/D/D07/D07B", "3/D/D07/D07B/D07B2201",
				"4/D/D07/D07B/D07B2201/D07B22012051" };

		// System.out.println(Arrays.toString(actual));

		assertArrayEquals(expected, actual);
	}

	@Test
	public void fromFacets() throws ParseException {
		CpcClassification cpc = new CpcClassification();
		cpc.parseText("D07B2201/2051");

		String[] actual = ClassificationTokenizer.partsToFacet(cpc.getParts());

		String[] expected = new String[] { "0/D", "1/D/D07", "2/D/D07/D07B", "3/D/D07/D07B/D07B2201",
				"4/D/D07/D07B/D07B2201/D07B22012051" };
		assertArrayEquals(expected, actual);

		List<String> classificationFacets = Arrays.asList(actual);
		
		List<CpcClassification> cpcClasses = ClassificationTokenizer.fromFacets(classificationFacets, CpcClassification.class);
		//System.out.println(cpcClasses);

		assertEquals(cpc, cpcClasses.get(0));
	}

	@Test
	public void testMostSpecificClasses() throws ParseException {
		String[] facets = new String[]{"0/E03D", "0/G06F", "1/E03D/E03D9", "1/G06F/G06F3", "2/E03D/E03D9/E03D908", "2/G06F/G06F3/G06F3041"};
		String[] expect = new String[]{"E03D908", "G06F3041"};

		List<String> cpcClass = ClassificationTokenizer.getMostSpecificClasses(Arrays.asList(facets));

		Collections.sort(cpcClass);

		assertEquals(Arrays.asList(expect), cpcClass);
	}

	@Test
	public void partsToTree() throws ParseException {
		CpcClassification cpc = new CpcClassification();
		cpc.parseText("D07B2201/2051");

		String[] actual = ClassificationTokenizer.partsToTree(cpc.getParts());

		String[] expected = new String[] { "D 07 B 2201 2051", "D 07 B 2201", "D 07 B", "D 07", "D" };

		// System.out.println(Arrays.toString(actual));
		assertArrayEquals(expected, actual);
	}
}
