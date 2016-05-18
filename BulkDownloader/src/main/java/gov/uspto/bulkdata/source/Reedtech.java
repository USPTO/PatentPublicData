package gov.uspto.bulkdata.source;

/**
 * 
 * Reedtech configuration.
 *
 */
public final class Reedtech {
	private static final String APPLICATION_URL = "http://patents.reedtech.com/parbft.php";
	private static final String APPLICATION_PREFIX = "ipa";

	private static final String GRANT_URL = "http://patents.reedtech.com/pgrbft.php";
	private static final String GRANT_PREFIX = "ipg";

	private Reedtech(){}
	
	public static String getURL(PatentDocType docType) throws IllegalArgumentException {
		switch(docType){
			case GRANT:
				return GRANT_URL;
			case APPLICATION:
				return APPLICATION_URL;
		}

		throw new IllegalArgumentException("Unhandled PatentDocType: " + docType);
	}

	public static String getPrefix(PatentDocType docType) throws IllegalArgumentException {
		switch(docType){
			case GRANT:
				return GRANT_PREFIX;
			case APPLICATION:
				return APPLICATION_PREFIX;
		}

		throw new IllegalArgumentException("Unhandled PatentDocType: " + docType);
	}
}
