package gov.uspto.patent.validate;

import static org.junit.Assert.assertFalse;

import org.junit.Test;

import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.DocumentId;
import gov.uspto.patent.model.PatentApplication;
import gov.uspto.patent.model.PatentType;

public class TitleRuleTest {

	public static TitleRule titleRule = new TitleRule();

	@Test
	public void failMissingTitle() {
		PatentApplication patent = new PatentApplication(new DocumentId(CountryCode.US, "99999"), PatentType.UTILITY);
		assertFalse(titleRule.test(patent));
	}

	@Test
	public void failEmptyTitle() {
		PatentApplication patent = new PatentApplication(new DocumentId(CountryCode.US, "99999"), PatentType.UTILITY);
		patent.setTitle("");
		assertFalse(titleRule.test(patent));
	}

	@Test
	public void failShortTitle() {
		PatentApplication patent = new PatentApplication(new DocumentId(CountryCode.US, "99999"), PatentType.UTILITY);
		patent.setTitle("Arr");
		assertFalse(titleRule.test(patent));
	}
	
	@Test
	public void failLongTitle() {
		PatentApplication patent = new PatentApplication(new DocumentId(CountryCode.US, "99999"), PatentType.UTILITY);
		patent.setTitle(
				"Arrangement for improving availability of services in a communication system Arrangement for improving availability of services in a communication system Arrangement for improving availability of services in a communication system Arrangement for improving availability of services in a communication system Arrangement for improving availability of services in a communication system Arrangement for improving availability of services in a communication system Arrangement for improving availability of services in a communication system");
		assertFalse(titleRule.test(patent));
	}
}
