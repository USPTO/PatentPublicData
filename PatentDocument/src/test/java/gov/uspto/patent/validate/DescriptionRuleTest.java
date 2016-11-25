package gov.uspto.patent.validate;

import static org.junit.Assert.assertFalse;

import org.junit.Test;

import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.DescSection;
import gov.uspto.patent.model.Description;
import gov.uspto.patent.model.DescriptionSection;
import gov.uspto.patent.model.DocumentId;
import gov.uspto.patent.model.PatentApplication;
import gov.uspto.patent.model.PatentType;

public class DescriptionRuleTest {

	public static DescriptionRule descriptionRule = new DescriptionRule();

	@Test
	public void failMissing() {
		PatentApplication patent = new PatentApplication(new DocumentId(CountryCode.US, "99999"), PatentType.UTILITY);
		patent.setDescription(null);
		assertFalse(descriptionRule.test(patent));
	}
	
	@Test
	public void failTooSmall() {
		PatentApplication patent = new PatentApplication(new DocumentId(CountryCode.US, "99999"), PatentType.UTILITY);
		Description desc = new Description();
		DescriptionSection descSection = new DescriptionSection(DescSection.BRIEF_SUMMARY, "", new DummyFormattedText());
		desc.addSection(descSection);
		patent.setDescription(desc);
		assertFalse(descriptionRule.test(patent));
	}

}
