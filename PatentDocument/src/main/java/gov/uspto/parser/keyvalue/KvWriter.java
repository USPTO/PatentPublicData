package gov.uspto.parser.keyvalue;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public abstract class KvWriter implements AutoCloseable {

	public void write(List<KeyValue> keyValues) throws IOException {
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

		writeRecord();
	}

	public void write(List<KeyValue> keyValues, Collection<String> sections) throws IOException {
		startRecord();
		for (KeyValue kv : keyValues) {
			if (kv.getValue().isEmpty() && sections.contains(kv.getKey())) {
				startSection(kv);
			} else {
				addField(kv);
			}
		}
		writeRecord();
	}

	public abstract String getRootElementName();

	public abstract <T extends KvWriter> T startRecord();

	public abstract <T extends KvWriter> T startSection(KeyValue kv);

	public abstract <T extends KvWriter> T endCurrentSection();

	public abstract <T extends KvWriter> T addField(KeyValue kv) throws IOException;

	public abstract void writeRecord() throws IOException;
}
