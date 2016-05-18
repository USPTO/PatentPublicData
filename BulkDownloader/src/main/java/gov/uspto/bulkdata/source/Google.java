package gov.uspto.bulkdata.source;

/**
 * Google, configuration.
 */
public final class Google {	
	private static final String APPLICATION_URL = "http://www.google.com/googlebooks/uspto-patents-applications-text.html";
	private static final String APPLICATION_PREFIX = "ipa";
	
	private static final String GRANT_URL = "http://www.google.com/googlebooks/uspto-patents-grants-text.html";
	private static final String GRANT_PREFIX = "ipg";

	private Google(){ }

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
