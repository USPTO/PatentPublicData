package gov.uspto.patent.model.classification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.Set;

import org.junit.Test;

public class PatentClassificationTest {

	@Test
	public void depth() throws ParseException {
		CpcClassification cpc = new CpcClassification();
		cpc.parseText("D07B22012051");

		int depth = cpc.getDepth();
		int expect = 5;
		
		assertEquals(expect, depth);
	}

	@Test
	public void flatten() throws ParseException {
		UspcClassification uspc = new UspcClassification();
		uspc.parseText("PLT101");

		UspcClassification uspc2 = new UspcClassification();
		uspc2.parseText("PLT102");

		uspc.addChild(uspc2);

		Set<PatentClassification> classes = uspc.flatten();

		// System.out.println(classes);

		assertTrue(classes.contains(uspc));
		assertTrue(classes.contains(uspc2));
	}
}
