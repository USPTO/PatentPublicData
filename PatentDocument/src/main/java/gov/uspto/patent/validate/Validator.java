package gov.uspto.patent.validate;

import java.util.function.Predicate;

public interface Validator<T> extends Predicate<T> {
	public String getName();
	public String getMessage();
}
