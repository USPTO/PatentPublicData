package gov.uspto.patent.validate;

public class PatentValidationError extends Exception {

	private static final long serialVersionUID = 7328206990848574155L;

	public PatentValidationError() {
	}

	public PatentValidationError(String message) {
		super(message);
	}

	public PatentValidationError(Throwable cause) {
		super(cause);
	}

	public PatentValidationError(String message, Throwable cause) {
		super(message, cause);
	}

	public PatentValidationError(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
