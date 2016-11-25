package gov.uspto.patent.validate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
		String largeAbstractTxt = "The United States Patent and Trademark Office (USPTO) is an agency in the U.S. Department of Commerce that issues patents to inventors and businesses for their inventions, and trademark registration for product and intellectual property identification. The USPTO is unique among federal agencies because it operates solely on fees collected by its users, and not on taxpayer dollars.[2] Its operating structure is like a business in that it receives requests for services applications for patents and trademark registrations and charges fees projected to cover the cost of performing the services it provides.[2][3] The USPTO is based in Alexandria, Virginia, after a 2005 move from the Crystal City area of neighboring Arlington, Virginia. The offices under Patents and the Chief Information Officer that remained just outside the southern end of Crystal City completed moving to Randolph Square, a brand-new building in Shirlington Village, on April 27, 2009. The head of the USPTO is Michelle K. Lee. She took up her new role on January 13, 2014, initially in a temporary Deputy role.[4] On March 13, she formally took office as Director after being nominated by President Barack Obama and confirmed by the U.S. Senate.[5] She formerly served as the Director of the USPTO's Silicon Valley satellite office.[4] The USPTO cooperates with the European Patent Office (EPO) and the Japan Patent Office (JPO) as one of the Trilateral Patent Offices. The USPTO is also a Receiving Office, an International Searching Authority and an International Preliminary Examination Authority for international patent applications filed in accordance with the Patent Cooperation Treaty.";

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
