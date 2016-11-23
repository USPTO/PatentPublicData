package gov.uspto.patent.validate;

import gov.uspto.patent.model.Patent;

/**
 * Patent should have Classifications
 */
public class ClassificationRule implements Validator<Patent> {

	private static String NAME = "Classifications";
	private static String MESSAGE = "Patent Missing CLASSIFICATION";

	@Override
	public boolean test(Patent patent) {
		if (patent.getClassification() == null || patent.getClassification().isEmpty()){
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
