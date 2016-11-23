package gov.uspto.patent.validate;

import gov.uspto.patent.model.Patent;
import gov.uspto.patent.model.PatentType;

/**
 * 
 * DESIGN Patents only require 1 claim, abstract and description are optional. 
 * UTILITY Patents (majority of patents) require 1 or more claims, and usually have an abstract and description.
 *
 */
public class DescriptionRule implements Validator<Patent> {
	
	private static String NAME = "Description";
	private static String MESSAGE = "Utility Patent Missing DESCRIPTION";

	@Override
	public boolean test(Patent patent) {
		if (PatentType.UTILITY.equals(patent.getPatentType())) {
			if (patent.getDescription() == null || patent.getDescription().getAllPlainText().length() < 100) {
				return false;
			}
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
