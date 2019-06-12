package gov.uspto.patent.serialize.solr;

public enum SolrFieldType {
	STRING("s"),
	TEXT("t"),
	INTEGER("i"),
	LONG("l"),
	FLOAT("f"),
	DOUBLE("d"),
	BOOLEAN("b"),
	DATE("dt");

	private final String dynamicFieldEnding;

	SolrFieldType(String dynamicField){
		this.dynamicFieldEnding = dynamicField;
	}

	public String getDynamicFieldEnding() {
		return dynamicFieldEnding;
	}
}
