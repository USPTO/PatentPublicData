package gov.uspto.patent.validate;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import gov.uspto.patent.model.Claim;
import gov.uspto.patent.model.ClaimType;
import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.DocumentId;
import gov.uspto.patent.model.PatentApplication;
import gov.uspto.patent.model.PatentType;

public class ClaimRuleTest {

	public static ClaimRule claimRule = new ClaimRule();

	@Test
	public void emptyClaimListFail() {
		PatentApplication patent = new PatentApplication(new DocumentId(CountryCode.US, "99999"), PatentType.UTILITY);
		List<Claim> claims = new ArrayList<Claim>();
		patent.setClaim(claims);
		assertFalse(claimRule.test(patent));
	}

	@Test
	public void smallFirstClaimFail() {
		PatentApplication patent = new PatentApplication(new DocumentId(CountryCode.US, "99999"), PatentType.UTILITY);
		List<Claim> claims = new ArrayList<Claim>();
		Claim claim = new Claim("CLAIM-1", "my first claim", ClaimType.INDEPENDENT, new DummyFormattedText());
		claims.add(claim);
		patent.setClaim(claims);
		assertFalse(claimRule.test(patent));
	}

}
