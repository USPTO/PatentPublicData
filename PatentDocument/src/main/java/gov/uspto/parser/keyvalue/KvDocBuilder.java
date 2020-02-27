package gov.uspto.parser.keyvalue;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.List;

import gov.uspto.common.io.DocumentBuilder;

public abstract class KvDocBuilder implements DocumentBuilder<List<KeyValue>> {

	@Override
	public void write(List<KeyValue> keyValues, Writer writer) throws IOException {
		startRecord();
		for (KeyValue kv : keyValues) {
			if (kv.getKey().trim().isEmpty()) {
				continue;
			}

			if (kv.getValue().trim().isEmpty()) { // auto detect section.
				startSection(kv);
			} else {
				addField(kv);
			}
		}

		writeRecord(writer);
	}
	
	public void write(List<KeyValue> keyValues, Collection<String> sections, Writer writer) throws IOException {
		startRecord();
		for (KeyValue kv : keyValues) {
			if (kv.getValue().isEmpty() && sections.contains(kv.getKey())) {
				startSection(kv);
			} else {
				addField(kv);
			}
		}
		writeRecord(writer);
	}

	public abstract String getRootElementName();

	public abstract <T extends KvDocBuilder> T startRecord();

	public abstract <T extends KvDocBuilder> T startSection(KeyValue kv);

	public abstract <T extends KvDocBuilder> T endCurrentSection();

	public abstract <T extends KvDocBuilder> T addField(KeyValue kv) throws IOException;

	public abstract void writeRecord(Writer writer) throws IOException;

}
