package gov.uspto.patent.serialize.solr;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.lang3.time.FastDateFormat;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonGenerator.Feature;

/**
 * Build Solr JSON Document
 * 
 * @author Brian G. Feldman <brian.feldman@uspto.gov>
 *
 */
public class SolrJson {

	/*
	 * FastDateFormat is Thread-Safe version of SimpleDateFormat
	 */
	private static final FastDateFormat DATE_ISO_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss'Z'");

	private JsonGenerator jGenerator;
	private JsonFactory jfactory = new JsonFactory();
	private final boolean useDynamicFieldEndings;
	private final boolean pretty;

	public SolrJson(boolean pretty, boolean useDynamicFieldEndings) {
		this.pretty = pretty;
		this.useDynamicFieldEndings = useDynamicFieldEndings;
	}

	public void open(Writer writer) throws IOException {
		if (jGenerator == null) {
			JsonGenerator jGenerator = jfactory.createGenerator(writer);
			jGenerator.configure(Feature.ESCAPE_NON_ASCII, false);
			jGenerator.configure(Feature.AUTO_CLOSE_TARGET, false);
			if (pretty) {
				jGenerator.useDefaultPrettyPrinter();
				// jGenerator.setPrettyPrinter(new DefaultPrettyPrinter());
			}

			jGenerator.writeStartObject(); // root.
			jGenerator.writeFieldName("add");
			jGenerator.writeStartObject();
			this.jGenerator = jGenerator;
		}
	}

	public void startDocument() throws IOException {
		jGenerator.writeFieldName("doc");
		jGenerator.writeStartObject();
	}

	public void endDocument() throws IOException {
		jGenerator.writeEndObject();
	}

	public boolean isClosed() {
		return (jGenerator != null && jGenerator.isClosed());
	}

	public void close() throws IOException {
		if (!isClosed()) {
			jGenerator.writeEndObject(); // add
			jGenerator.writeEndObject(); // root
			jGenerator.flush();
			jGenerator.close();
			jGenerator = null;
		}
	}

	public void addStringField(String fieldName, Enum<?> value) throws IOException {
		addField(new SolrField(fieldName, SolrFieldType.STRING, useDynamicFieldEndings, false), valueOrEmpty(value));
	}

	public void addStringField(String fieldName, String value) throws IOException {
		addField(new SolrField(fieldName, SolrFieldType.STRING, useDynamicFieldEndings, false), value);
	}

	public void addStringField(String fieldName, Collection<String> values) throws IOException {
		addField(new SolrField(fieldName, SolrFieldType.STRING, useDynamicFieldEndings, true), values);
	}

	public void addTextField(String fieldName, String value) throws IOException {
		addField(new SolrField(fieldName, SolrFieldType.TEXT, useDynamicFieldEndings, false), value);
	}

	public void addTextField(String fieldName, Collection<String> values) throws IOException {
		addField(new SolrField(fieldName, SolrFieldType.TEXT, useDynamicFieldEndings, true), values);
	}

	public void addDateField(String fieldName, Date value) throws IOException {
		addField(new SolrField(fieldName, SolrFieldType.DATE, useDynamicFieldEndings, false), value);
	}

	public void addBooleanField(String fieldName, String value) throws IOException {
		addField(new SolrField(fieldName, SolrFieldType.BOOLEAN, useDynamicFieldEndings, false), Boolean.valueOf(value));
	}

	public void addField(SolrField field, Collection<String> values) throws IOException {
		writeArray(field.build(), values);
	}

	public void addField(SolrField field, Boolean value) throws IOException {
		try {
			jGenerator.writeBooleanField(field.build(), value);
		} catch (JsonGenerationException e) {
			System.err.println("!! SolrJson Failed to write field: '" + field.getName() + "'\n");
		}
	}

	public void addField(String fieldName, Boolean value) throws IOException {
		try {
			jGenerator.writeBooleanField(new SolrField(fieldName, SolrFieldType.BOOLEAN, useDynamicFieldEndings, false).build(), value);
		} catch (JsonGenerationException e) {
			System.err.println("!! SolrJson Failed to write field: '" + fieldName + "'\n");
		}
	}

	public void addField(SolrField field, String value) throws IOException {
		try {
			jGenerator.writeStringField(field.build(), valueOrEmpty(value));
		} catch (JsonGenerationException e) {
			System.err.println("!! SolrJson Failed to write field: '" + field.getName() + "'\n");
		}
	}

	public void addField(SolrField field, Date value) throws IOException {
		try {
			jGenerator.writeStringField(field.build(), value != null ? DATE_ISO_FORMAT.format(value) : "");
		} catch (JsonGenerationException e) {
			System.err.println("!! SolrJson Failed to write field: '" + field.getName() + "'\n");
		}
	}

	private void writeArray(String fieldName, Collection<String> strings) throws IOException {
		jGenerator.writeFieldName(fieldName);
		jGenerator.writeStartArray(strings.size());
		for (String tok : strings) {
			jGenerator.writeString(valueOrEmpty(tok));
		}
		jGenerator.writeEndArray();

	}

	/**
	 * JsonObjects can not set a null value so return empty string.
	 * 
	 * @param value
	 * @return
	 */
	private String valueOrEmpty(String value) {
		return value == null ? "" : value;
	}

	private String valueOrEmpty(Enum<?> value) {
		return value == null ? "" : value.toString();
	}

}
