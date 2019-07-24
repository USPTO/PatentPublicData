package gov.uspto.patent.validate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import gov.uspto.patent.model.Abstract;
import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.DocumentId;
import gov.uspto.patent.model.PatentApplication;
import gov.uspto.patent.model.PatentType;

public class AbstractRuleTest {

	public static AbstractRule abstractRule = new AbstractRule();

	@Test
	public void failTooSmall() {
		PatentApplication patent = new PatentApplication(new DocumentId(CountryCode.US, "99999"), PatentType.UTILITY);
		Abstract abst = new Abstract("", new DummyFormattedText());
		patent.setAbstract(abst);
		assertFalse(abstractRule.test(patent));
	}

	@Test
	public void failTooLarge() {
		// Generate 501 random words
		StringBuilder stb = new StringBuilder();
	    for (int i = 0; i < 501; i++) {
			  stb.append(RandomStringUtils.randomAlphabetic(3, 7)).append(" ");
		}
	    String largeAbstractTxt = stb.toString();

		PatentApplication patent = new PatentApplication(new DocumentId(CountryCode.US, "99999"), PatentType.UTILITY);
		Abstract abst = new Abstract(largeAbstractTxt, new DummyFormattedText());
		patent.setAbstract(abst);
		assertFalse(abstractRule.test(patent));
	}

	@Test
	public void passLenGood() {
		String abstractTxt = "The United States Patent and Trademark Office (USPTO) is an agency in the U.S. Department of Commerce that issues patents to inventors and businesses for their inventions, and trademark registration for product and intellectual property identification. The USPTO is unique among federal agencies because it operates solely on fees collected by its users, and not on taxpayer dollars.[2]";
		
		PatentApplication patent = new PatentApplication(new DocumentId(CountryCode.US, "99999"), PatentType.UTILITY);
		Abstract abst = new Abstract(abstractTxt, new DummyFormattedText());
		patent.setAbstract(abst);
		assertTrue(abstractRule.test(patent));
	}

}
