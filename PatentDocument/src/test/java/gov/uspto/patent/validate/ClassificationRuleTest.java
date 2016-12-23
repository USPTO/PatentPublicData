package gov.uspto.patent.validate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.DocumentId;
import gov.uspto.patent.model.PatentApplication;
import gov.uspto.patent.model.PatentType;
import gov.uspto.patent.model.classification.UspcClassification;

public class ClassificationRuleTest {

	public static ClassificationRule classificationRule = new ClassificationRule();

	@Test
	public void failMissingClassification() {
		PatentApplication patent = new PatentApplication(new DocumentId(CountryCode.US, "99999"), PatentType.UTILITY);
		assertFalse(classificationRule.test(patent));
	}

	@Test
	public void passContainsClassification() {
		PatentApplication patent = new PatentApplication(new DocumentId(CountryCode.US, "99999"), PatentType.UTILITY);
		UspcClassification uspc = new UspcClassification();
		uspc.setTextOriginal("A01Z");
		patent.addClassification(uspc);
		assertTrue(classificationRule.test(patent));
	}

}
