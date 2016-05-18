package gov.uspto.bulkdata.source;

/**
 * FDA National Drug Code Directory
 */
public final class FDA {	
	private static final String FDA_NDC_URL = "http://www.fda.gov/Drugs/InformationOnDrugs/ucm142438.htm";
	private static final String FDA_PREFIX = "ndc";

	private FDA(){ }

	public static String getURL() {
		return FDA_NDC_URL;
	}

	public static String getPrefix() {
		return FDA_PREFIX;
	}
}
