package gov.uspto.patent.validate;

import gov.uspto.patent.model.Patent;

/**
 * All patents should have at least one claim
 */
public class ClaimRule implements Validator<Patent> {

	private static String NAME = "Claims";
	private static String MESSAGE = "Patent CLAIMS: missing or failed size constraints";

	@Override
	public boolean test(Patent patent) {
		if (patent.getClaims() == null || patent.getClaims().isEmpty() || patent.getClaims().get(0).getRawText().length() < 15) {
			return false;
		}
		return true;
	}

	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	public String getMessage() {
		return MESSAGE;
	}
}
