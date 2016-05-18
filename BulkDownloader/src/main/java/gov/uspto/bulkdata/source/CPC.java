package gov.uspto.bulkdata.source;

/**
 * CPC Classification Scheme
 */
public final class CPC {	
	private static final String CPC_SCHEME_URL = "http://www.cooperativepatentclassification.org/cpcSchemeAndDefinitions/Bulk.html";
	private static final String CPC_PREFIX = "CPCSchemeXML";

	private CPC(){ }

	public static String getURL() {
		return CPC_SCHEME_URL;
	}

	public static String getPrefix() {
		return CPC_PREFIX;
	}
}
