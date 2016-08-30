package gov.uspto.patent.model;

public enum PatentCorpus {
	/*
	 *  PGPub: Patent application published 18 months from priority date.
	 */
	PGPUB("US-PGPUB", "Pre-Grant Publication"),
	
	/*
	 * USPAT: U.S Patents in text, 1970 to present.
	 */
	USPAT("USPAT", "U.S. Patent"),
	
	/*
	 * USOCR: U.S Patents ocred from 1920-1970, and some of 1971-1976.
	 */
	USOCR("USOCR", "U.S OCR Patent"),

	/*
	 * EPO: European Patents, 38 countries make up the European Patent Convention "EPC". Headquarters Munich, Germany.
	 */
	EPO("EPO", "European Patent"),
	
	/*
	 * Japanese text patents 1976 to present. Headquarters Tokyo, Japan.
	 */
	JPO("JPO", "Japanese Patent"),

	/*
	 * IBMTDB: IBM defensive invention publications 1958 to 1998.
	 */
	IBMTDB("IBM_TDB", "IBM Technical Disclosure Bulletin"),

	/*
	 * Derwent World Patents database by Thomson Reuters. Used by WIPO.
	 */
	DERWENT("DWPI", "Derwent World Patents Index");

	private String desc;
	private String altType;
	
	private PatentCorpus(String altType, String desc){
		this.desc = desc;
		this.altType = altType;
	}

	public String getDescription(){
		return desc;
	}

	public String getAltType() {
		return altType;
	}
}
