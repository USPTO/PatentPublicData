package gov.uspto.patent;

public class PatentReaderException extends Exception {

	private static final long serialVersionUID = -5099296371469164979L;

	public PatentReaderException() {
	}

	public PatentReaderException(String message) {
		super(message);
	}

	public PatentReaderException(Throwable cause) {
		super(cause);
	}

	public PatentReaderException(String message, Throwable cause) {
		super(message, cause);
	}

	public PatentReaderException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
