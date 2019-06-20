package gov.uspto.patent.model.classification;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.junit.Test;

public class ClassificationPredicateTest {

	@Test
	public void testFalse() throws ParseException {
		
		CpcClassification wantCpc = new CpcClassification("A01B300", false);
		wantCpc.parseText("A01B300");
		Predicate<PatentClassification> predicate = ClassificationPredicate.isContained(wantCpc);

		CpcClassification checkCpc = new CpcClassification("A01B33/00", false);
		checkCpc.parseText("A01B33/00");

		assertFalse(wantCpc.isContained(checkCpc));
		
		assertFalse(predicate.test(checkCpc));
	}

	@Test
	public void testEqual() throws ParseException {
		
		CpcClassification wantCpc = new CpcClassification("A01B300", true);
		wantCpc.parseText("A01B300");
		Predicate<PatentClassification> predicate = ClassificationPredicate.isContained(wantCpc);

		CpcClassification checkCpc = new CpcClassification("A01B300", true);
		checkCpc.parseText("A01B300");

		assertTrue(wantCpc.isContained(checkCpc));
		
		assertTrue(predicate.test(checkCpc));
	}

	@Test
	public void testChild() throws ParseException {
		// 4/D/D07/D07B/D07B2201/D07B22012051
		CpcClassification wantCpc = new CpcClassification("D07B", false);
		wantCpc.parseText("D07B");
		Predicate<PatentClassification> predicate = ClassificationPredicate.isContained(wantCpc);

		CpcClassification checkCpc = new CpcClassification("D07B22012051", false);
		checkCpc.parseText("D07B22012051");

		assertTrue(wantCpc.isContained(checkCpc));
		
		assertTrue(predicate.test(checkCpc));
	}

	@Test
	public void testList() throws ParseException {
		// 4/D/D07/D07B/D07B2201/D07B22012051
		CpcClassification wantCpc = new CpcClassification("D07B", true);
		wantCpc.parseText("D07B");
		Predicate<PatentClassification> predicate = ClassificationPredicate.isContained(wantCpc);

		CpcClassification cpc1 = new CpcClassification("D07B22012051", true);
		cpc1.parseText("D07B22012051");
		CpcClassification cpc2 = new CpcClassification("A01B300", true);
		cpc2.parseText("A01B300");
		Set<CpcClassification> cpcClasses = new HashSet<CpcClassification>();
		cpcClasses.add(cpc1);
		cpcClasses.add(cpc2);

		//assertTrue(wantCpc.isContained(cpcClasses));
		assertTrue(cpcClasses.stream().anyMatch(predicate));
	}
}
