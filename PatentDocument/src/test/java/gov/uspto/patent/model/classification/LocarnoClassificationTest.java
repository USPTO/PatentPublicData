package gov.uspto.patent.model.classification;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.util.List;

import org.junit.Test;

public class LocarnoClassificationTest {

	@Test
	public void facets() throws ParseException {
		LocarnoClassification uspc = new LocarnoClassification("01-06", false);
		uspc.parseText("01-06");
		List<String> facets = uspc.getTree().getLeafFacets();
		assertEquals("0/01", facets.get(0));
		assertEquals("1/01/06", facets.get(1));
	}

	@Test
	public void facets2() throws ParseException {
		LocarnoClassification uspc = new LocarnoClassification("0106", false);
		uspc.parseText("0106");
		List<String> facets = uspc.getTree().getLeafFacets();
		assertEquals("0/01", facets.get(0));
		assertEquals("1/01/06", facets.get(1));
	}

}
