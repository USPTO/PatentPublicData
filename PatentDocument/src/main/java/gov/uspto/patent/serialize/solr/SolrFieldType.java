package gov.uspto.patent.serialize.solr;

public enum SolrFieldType {
	TEXT("txt", "txt"),
	STRING("str", "ss"),
	INTEGER("i", "is"),
	LONG("l", "ls"),
	FLOAT("f", "fs"),
	DOUBLE("d", "ds"),
	BOOLEAN("b", "bs"),
	DATE("dt", "dts"),

	STRING_LOWERCASE("s_lower", "s_lower"),
	DESCENDENT_PATH("descendent_path", "descendent_path"),
	ANCESTOR_PATH("ancestor_path", "ancestor_path"),
	TEXT_ENG_SPLIT("txt_en_split", "txt_en_split"), // tokenize whitespace, lowercase, porter stem, 
	TEXT_ENG("txt_en", "txt_en"); // tokenize, lowercase, possessives, porter stem, stopwords

	private final String dynamicFieldEnding;
	private final String multiValueDynamicFieldEnding;

	SolrFieldType(String dynamicField, String multiValueDynamicFieldEnding){
		this.dynamicFieldEnding = dynamicField;
		this.multiValueDynamicFieldEnding = multiValueDynamicFieldEnding;
	}

	public String getDynamicFieldEnding() {
		return dynamicFieldEnding;
	}

	public String getMultiValueDynamicFieldEnding() {
		return multiValueDynamicFieldEnding;
	}
}
