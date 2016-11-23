package gov.uspto.patent.validate;

import gov.uspto.patent.model.Patent;
import gov.uspto.patent.model.PatentType;

/**
 * 
 * DESIGN Patents only require 1 claim, abstract and description are optional. 
 * UTILITY Patents (majority of patents) require 1 or more claims, and usually have an abstract and description.
 *
 */
public class AbstractRule implements Validator<Patent> {

	private static String NAME = "Abstact";
	private static String MESSAGE = "Utility Patent Missing ABSTRACT field";

	@Override
	public boolean test(Patent patent) {
		if (PatentType.UTILITY.equals(patent.getPatentType())) {
			if (patent.getAbstract() == null || patent.getAbstract().getPlainText().length() < 10) {
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
