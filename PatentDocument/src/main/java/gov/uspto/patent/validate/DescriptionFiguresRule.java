package gov.uspto.patent.validate;

import gov.uspto.patent.model.Patent;
import gov.uspto.patent.model.PatentType;

public class DescriptionFiguresRule implements Validator<Patent> {
	private static String NAME = "DescriptionFigures";
	private static String MESSAGE = "Utility Patent Missing DESCRIPTION OF DRAWING FIGURES";

	@Override
	public boolean test(Patent patent) {
		if (PatentType.UTILITY.equals(patent.getPatentType())) {
			if (patent.getDescription() == null || patent.getDescription().getFigures().isEmpty() ) {
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
