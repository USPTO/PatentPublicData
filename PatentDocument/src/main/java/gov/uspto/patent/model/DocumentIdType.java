package gov.uspto.patent.model;

/**
 * 
 * DocumentIdType matches closely to the parent XML node name, of which the documentID was retrieved. 
 * Example: "<parent>/relation/document-id".
 * 
 */
public enum DocumentIdType {
	PUBLISHED,
	RELATED_PUBLICATION,
	APPLICATION,
	REGIONAL_FILING,
	NATIONAL_FILING,
	INTERNATIONAL_FILING,
	REGIONAL_PUBLICATION,
	ADDITION,
	CONTINUATION,
	CONTINUATION_IN_PART,
	CONTINUATION_REISSUE,
	DIVISION,
	REEXAMINATION,
	REISSUE,
	SUBSITUTION,
	USREEX,
	UTILITY_MODEL,
	PROVISIONAL;
}
