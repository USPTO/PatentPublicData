package gov.uspto.bulkdata.source;

public enum PatentDocType {
	GRANT ("us-patent-grant"), 
	APPLICATION ("us-patent-application");
	
	private final String xmlTag;
	
	private PatentDocType(String xmlTag){
		this.xmlTag =xmlTag;
	}

	public String getXmlTag(){
		return xmlTag;
	}
}