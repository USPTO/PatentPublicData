package gov.uspto.patent.validate;

import gov.uspto.patent.model.DescriptionSection;
import gov.uspto.patent.model.Patent;
import gov.uspto.patent.model.PatentType;

/**
 * Description Rule
 * 
 *<p>
 * For Utility Patent ensure content exists
 *</p>
 */
public class DescriptionRule implements Validator<Patent> {

	private static String NAME = "Description";
	private static String MESSAGE = "Utility Patent DESCRIPTION: missing or failed size constraints";

	@Override
	public boolean test(Patent patent) {
		if (PatentType.UTILITY.equals(patent.getPatentType())) {
			if (patent.getDescription() == null || patent.getDescription().getAllPlainText().length() < 100) {
				return false;
			}

			for (DescriptionSection section : patent.getDescription().getSections()) {
				if (section.getRawText().length() < 25) {
					return false;
				}

				if (section.getPlainText().length() < 25) {
					return false;
				}

				if (section.getSimpleHtml().length() < 25) {
					return false;
				}
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
