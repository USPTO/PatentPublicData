package gov.uspto.patent.validate;

import java.util.StringTokenizer;

import gov.uspto.patent.PatentDocFormat;
import gov.uspto.patent.PatentDocFormatDetect;
import gov.uspto.patent.model.Patent;
import gov.uspto.patent.model.PatentType;

/**
 * Patent Abstract Rule for Utility Patents
 * 
 * <p>
 * <pre>
 * 1) Not Design Patent
 * 2) Abstract Field Exists
 * 3) Abstract length more than 10 characters
 * 3) Abstract length less than 150 words (RedBook XML) or less that 500
 * </pre>
 * </p>
 * 
 * DESIGN Patents only require 1 claim, abstract and description are optional.
 * UTILITY Patents (majority of patents) require 1 or more claims, and usually
 * have an abstract and description.
 *
 */
public class AbstractRule implements Validator<Patent> {

	private static String NAME = "Abstact";
	private static String MESSAGE = "Patent ABSTRACT field: missing or failed size constraints : ";

	@Override
	public boolean test(Patent patent) {
		PatentDocFormat docFormat = PatentDocFormatDetect.fromPatent(patent);

		if (!PatentType.DESIGN.equals(patent.getPatentType())) {
			StringTokenizer tokenizer = tokenize(patent.getAbstract().getPlainText());
			int tokens = tokenizer.countTokens();

			if (patent.getAbstract() == null || patent.getAbstract().getPlainText().length() < 10) {
				MESSAGE = MESSAGE + " BELOW MINIMUM SIZE: 10; " + patent.getAbstract().getPlainText();
				return false;
			} else if ((PatentDocFormat.RedbookGrant.equals(docFormat)
					|| PatentDocFormat.RedbookApplication.equals(docFormat)) && tokens > 150) {
				MESSAGE = MESSAGE + " REDBOOK XML : ABOVE MAX WORD SIZE: 150; " + patent.getAbstract().getPlainText();
				return false;
			}
			else if (tokens > 500) {
				MESSAGE = MESSAGE + " ABOVE MAX WORD SIZE: 500; " + patent.getAbstract().getPlainText();
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

	public StringTokenizer tokenize(String text) {
		return new StringTokenizer(text.trim(), " \t\n\r\f,:;?![]()'");
	}
}
