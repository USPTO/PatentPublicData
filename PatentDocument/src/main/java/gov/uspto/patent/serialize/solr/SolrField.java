package gov.uspto.patent.serialize.solr;

public class SolrField implements Field {

	private final String name;
	private final SolrFieldType fieldType;
	private final boolean multivalued;
	private final boolean useDynamicEnding;

	public SolrField(String name, SolrFieldType fieldType) {
		this(name, fieldType, false, false);
	}

	public SolrField(String name, SolrFieldType fieldType, boolean useDynamicEnding, boolean multivalued) {
		this.name = name;
		this.fieldType = fieldType;
		this.useDynamicEnding = useDynamicEnding;
		this.multivalued = multivalued;
	}

	@Override
	public String getName() {
		return name;
	}

	public SolrFieldType getType() {
		return fieldType;
	}

	public String build() {
		StringBuilder stb = new StringBuilder();
		stb.append(name);
		if (useDynamicEnding) {
			stb.append("_");
			if (multivalued) {
				stb.append(fieldType.getMultiValueDynamicFieldEnding());
			} else {
				stb.append(fieldType.getDynamicFieldEnding());
			}
		}
		return stb.toString();
	}

}
