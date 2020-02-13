package gov.uspto.patent.validate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import gov.uspto.patent.model.Patent;

/**
 * Patent Validator
 * 
 * Verifies Patent meets minimum requirements, also helpful in finding parsing errors.
 * 
 *  @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class PatentValidator {

	private Collection<Validator<Patent>> validators;

	public PatentValidator(Collection<Validator<Patent>> validators) {
		this.validators = validators;
	}

	public void verify(Patent patent) throws PatentValidationError {
		StringBuilder combinedErrors = new StringBuilder();
		for (Validator<Patent> validator : validators) {
			if (!validator.test(patent)) {
				if (combinedErrors.length() > 0) {
					combinedErrors.append(", ");
				}

				combinedErrors.append(validator.getMessage());
			}
		}

		if (combinedErrors.length() > 0) {
			throw new PatentValidationError(combinedErrors.toString());
		}
	}

	public static PatentValidator allRules() {
		List<Validator<Patent>> rules = new ArrayList<Validator<Patent>>();
		rules.add(new TitleRule());
		rules.add(new AbstractRule());
		rules.add(new DescriptionRule());
		rules.add(new ClaimRule());
		rules.add(new ClassificationRule());
		//rules.add(new DescriptionFiguresRule());
		return new PatentValidator(rules);
	}
}
