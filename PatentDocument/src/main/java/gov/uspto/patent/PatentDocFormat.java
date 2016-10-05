package gov.uspto.patent;

public enum PatentDocFormat {
	RedbookApplication("text/redbook-xml", "ipa", "us-patent-application", "<us-patent-application"),
	RedbookGrant("text/redbook-xml",	"ipg", "us-patent-grant", "<us-patent-grant"),
	Pap("text/pap-xml", "pa", "patent-application-publication", "<patent-application-publication>"),
	Sgml("text/grant-sgml", "pg", "PATDOC", "<PATDOC "),
	Greenbook("text/greenbook-aps", "pftaps", "PATN", "PATN"),
    CpcMasterFile("text/cpc-masterfile", "", "uspat:CPCMasterClassificationRecord", "<uspat:CPCMasterClassificationRecord>"),
	//CSS_BRS("text/css-brs", "", "*** BRS DOCUMENT BOUNDARY ***", ""),
	Unknown("UNKOWN", "UNKOWN", "UNKOWN", "UNKOWN");

	private String parentElement;
	private String mimeType;
	private String match;
	private String bulkFileStartWith;

	private PatentDocFormat(String mimeType, String bulkFileStartWith, String parentElement, String match) {
		this.mimeType = mimeType;
		this.bulkFileStartWith = bulkFileStartWith;
		this.parentElement = parentElement;
		this.match = match;
	}

	public String getMimeType() {
		return mimeType;
	}
	
	public String getBulkFileStartWith(){
		return bulkFileStartWith;
	}

	public String getParentElement() {
		return parentElement;
	}
	
	public String getMatch(){
		return match;
	}

	public static PatentDocFormat findMimeType(String filename) {
		for (PatentDocFormat type : PatentDocFormat.values()) {
			if (filename.startsWith(type.getBulkFileStartWith())) {
				return type;
			}
		}
		return PatentDocFormat.Unknown;
	}
}
