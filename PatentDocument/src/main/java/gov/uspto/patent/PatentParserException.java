package gov.uspto.patent;

public class PatentParserException extends Exception {

	private static final long serialVersionUID = -5099296371469164979L;

	public PatentParserException() {
	}

	public PatentParserException(String message) {
		super(message);
	}

	public PatentParserException(Throwable cause) {
		super(cause);
	}

	public PatentParserException(String message, Throwable cause) {
		super(message, cause);
	}

	public PatentParserException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
