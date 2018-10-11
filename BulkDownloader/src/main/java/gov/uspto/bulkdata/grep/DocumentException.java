package gov.uspto.bulkdata.grep;

public class DocumentException extends Exception {

	private String message = null;

	public DocumentException() {
		super();
	}

	public DocumentException(String message) {
		super(message);
		this.message = message;
	}

	public DocumentException(Throwable cause) {
		super(cause);
	}

	@Override
	public String toString() {
		return message;
	}

	@Override
	public String getMessage() {
		return message;
	}
}
