package gov.uspto.patent.validate;

import gov.uspto.patent.model.Patent;

/**
 * Patent Title Rule
 * 
 *<p><pre>
 * 1) Title Field Exists
 * 2) Title length 3 or more characters
 * 3) Title length less than 500 characters
 *</pre></p> 
 */
public class TitleRule implements Validator<Patent> {

	private static String NAME = "Title";
	private static String MESSAGE = "Patent TITLE: missing or failed size constraints";

	@Override
	public boolean test(Patent patent) {
		if (patent.getTitle() == null || patent.getTitle().length() < 4 | patent.getTitle().length() > 500) {
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
