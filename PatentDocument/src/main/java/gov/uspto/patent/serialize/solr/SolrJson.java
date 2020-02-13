package gov.uspto.patent.serialize.solr;

import java.io.IOException;
import java.io.Writer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Date;

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

	private DateTimeFormatter dFormat = DateTimeFormatter.ISO_DATE;
	private DateTimeFormatter dtFormat = DateTimeFormatter.ISO_INSTANT;

	private JsonFactory jfactory = new JsonFactory();
	private JsonGenerator jGenerator;
	private final boolean useDynamicFieldEndings;
	private final boolean pretty;

	public SolrJson(boolean pretty, boolean useDynamicFieldEndings) {
		this.pretty = pretty;
		this.useDynamicFieldEndings = useDynamicFieldEndings;
	}

	public void setDateFormat(DateTimeFormatter dFormat) {
		this.dFormat = dFormat;
	}

	public void setDateTimeFormat(DateTimeFormatter dtFormat) {
		this.dtFormat = dtFormat;
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

	/*
	 * String and Text
	 */
	public void addStringField(String fieldName, Enum<?> value) throws IOException {
		addField(new SolrField(fieldName, SolrFieldType.STRING, useDynamicFieldEndings, false), valueOrEmpty(value));
	}

	public void addStringField(String fieldName, CharSequence value) throws IOException {
		addStringField(fieldName, value, false);
	}

	public void addStringField(String fieldName, CharSequence value, boolean caseInsensitive) throws IOException {
		if (caseInsensitive) {
			addField(new SolrField(fieldName, SolrFieldType.STRING_LOWERCASE, useDynamicFieldEndings, false), value);
		} else {
			addField(new SolrField(fieldName, SolrFieldType.STRING, useDynamicFieldEndings, false), value);
		}
	}

	public void addStringField(String fieldName, Collection<String> values) throws IOException {
		addStringField(fieldName, values, false);
	}

	public void addStringField(String fieldName, Collection<String> values, boolean caseInsensitive)
			throws IOException {
		if (caseInsensitive) {
			addField(new SolrField(fieldName, SolrFieldType.STRING_LOWERCASE, useDynamicFieldEndings, true), values);
		} else {
			addField(new SolrField(fieldName, SolrFieldType.STRING, useDynamicFieldEndings, true), values);
		}
	}

	public void addTextField(String fieldName, CharSequence value) throws IOException {
		addField(new SolrField(fieldName, SolrFieldType.TEXT, useDynamicFieldEndings, false), value);
	}

	public void addTextField(String fieldName, Collection<String> values) throws IOException {
		addField(new SolrField(fieldName, SolrFieldType.TEXT, useDynamicFieldEndings, true), values);
	}

	public void addField(SolrField field, Collection<String> values) throws IOException {
		writeStrArray(field.build(), values);
	}

	public void addField(SolrField field, CharSequence value) throws IOException {
		try {
			jGenerator.writeStringField(field.build(), valueOrEmpty(value));
		} catch (JsonGenerationException e) {
			System.err.println("!! SolrJson Failed to write field: '" + field.getName() + "'\n");
		}
	}

	private void writeStrArray(String fieldName, Collection<String> values) throws IOException {
		jGenerator.writeFieldName(fieldName);
		jGenerator.writeStartArray(values.size());
		for (String tok : values) {
			jGenerator.writeString(valueOrEmpty(tok));
		}
		jGenerator.writeEndArray();
	}

	private String valueOrEmpty(CharSequence value) {
		return value == null ? "" : value.toString();
	}

	private String valueOrEmpty(Enum<?> value) {
		return value == null ? "" : value.toString();
	}

	/*
	 * Date
	 */
	public void addDateField(String fieldName, Date value) throws IOException {
		addField(new SolrField(fieldName, SolrFieldType.DATE, useDynamicFieldEndings, false), dateValueOrEmpty(value));
	}

	public void addDateField(String fieldName, LocalDate value) throws IOException {
		addField(new SolrField(fieldName, SolrFieldType.DATE, useDynamicFieldEndings, false), dateValueOrEmpty(value));
	}

	public void addField(SolrField field, LocalDate value) throws IOException {
		try {
			jGenerator.writeStringField(field.build(), dateValueOrEmpty(value));
		} catch (JsonGenerationException e) {
			System.err.println("!! SolrJson Failed to write field: '" + field.getName() + "'\n");
		}
	}

	public void addField(SolrField field, Date value) throws IOException {
		LocalDate date = value.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		addField(field, date);
	}

	/**
	 * Collection of Date values
	 * 
	 * @param field
	 * @param values - Collection of Date or LocalDate
	 * @throws IOException
	 */
	public void addDateField(SolrField field, Collection<?> values) throws IOException {
		writeDateArray(field.build(), values);
	}

	private void writeDateArray(String fieldName, Collection<?> values) throws IOException {
		jGenerator.writeFieldName(fieldName);
		jGenerator.writeStartArray(values.size());
		if (Collection.class.isAssignableFrom(LocalDate.class)) {
			for (Object tok : values) {
				jGenerator.writeString(dateValueOrEmpty((LocalDate) tok));
			}
		} else if (Collection.class.isAssignableFrom(Date.class)) {
			for (Object tok : values) {
				jGenerator.writeString(dateValueOrEmpty((Date) tok));
			}
		} else {
			System.err.println("!! SolrJson Failed to write field: '" + fieldName + "'\n");
		}
		jGenerator.writeEndArray();
	}

	private String dateValueOrEmpty(LocalDate value) {
		return value == null ? "" : value.format(dFormat);
	}

	private String dateValueOrEmpty(Date value) {
		LocalDate date = value.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		return dateValueOrEmpty(date);
	}

	/*
	 * DateTime
	 */
	public void addDateTimeField(String fieldName, Date value) throws IOException {
		addField(new SolrField(fieldName, SolrFieldType.DATE, useDynamicFieldEndings, false), dateTimeValueOrEmpty(value));
	}

	public void addDateTimeField(String fieldName, LocalDate value) throws IOException {
		addField(new SolrField(fieldName, SolrFieldType.DATE, useDynamicFieldEndings, false), dateTimeValueOrEmpty(value));
	}

	private String dateTimeValueOrEmpty(LocalDateTime value) {
		return value == null ? "" : dtFormat.format(value.toInstant(ZoneOffset.UTC));
	}

	private String dateTimeValueOrEmpty(LocalDate value) {
		return value == null ? "" : dateTimeValueOrEmpty(value.atStartOfDay());
	}

	private String dateTimeValueOrEmpty(Date value) {
		LocalDate date = value.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		return dateValueOrEmpty(date);
	}	

	/**
	 * Collection of Date Time values
	 * @param field
	 * @param values - Collection of Date or LocalDate
	 * @throws IOException
	 */
	public void addDateTimeField(SolrField field, Collection<?> values) throws IOException {
		writeDateTimeArray(field.build(), values);
	}	

	private void writeDateTimeArray(String fieldName, Collection<?> values) throws IOException {
		jGenerator.writeFieldName(fieldName);
		jGenerator.writeStartArray(values.size());
		if (Collection.class.isAssignableFrom(LocalDate.class)) {
			for (Object tok : values) {
				jGenerator.writeString(dateTimeValueOrEmpty((LocalDate) tok));
			}
		} else if (Collection.class.isAssignableFrom(Date.class)) {
			for (Object tok : values) {
				jGenerator.writeString(dateTimeValueOrEmpty((Date) tok));
			}
		} else {
			System.err.println("!! SolrJson Failed to write field: '" + fieldName + "'\n");
		}
		jGenerator.writeEndArray();
	}

	/*
	 * Boolean
	 */
	public void addBooleanField(String fieldName, String value) throws IOException {
		addField(new SolrField(fieldName, SolrFieldType.BOOLEAN, useDynamicFieldEndings, false),
				Boolean.valueOf(value));
	}

	public void addField(String fieldName, Boolean value) throws IOException {
		try {
			jGenerator.writeBooleanField(
					new SolrField(fieldName, SolrFieldType.BOOLEAN, useDynamicFieldEndings, false).build(), value);
		} catch (JsonGenerationException e) {
			System.err.println("!! SolrJson Failed to write field: '" + fieldName + "'\n");
		}
	}

	public void addField(SolrField field, Boolean value) throws IOException {
		try {
			jGenerator.writeBooleanField(field.build(), value);
		} catch (JsonGenerationException e) {
			System.err.println("!! SolrJson Failed to write field: '" + field.getName() + "'\n");
		}
	}

	/*
	 * Number (Integer, Float, Double)
	 */
	public <T extends Number> void addNumberField(String fieldName, T value) throws IOException {
		SolrFieldType fieldType = null;
		if (value instanceof Integer) {
			fieldType = SolrFieldType.INTEGER;
		} else if (value instanceof Float) {
			fieldType = SolrFieldType.FLOAT;
		} else if (value instanceof Double) {
			fieldType = SolrFieldType.DOUBLE;
		}
		addField(new SolrField(fieldName, fieldType, useDynamicFieldEndings, false), value);
	}

	public <T extends Number> void addNumberField(SolrField field, Collection<T> values) throws IOException {
		writeNumArray(field.build(), values);
	}

	public <T extends Number> void addField(String fieldName, T value) throws IOException {
		SolrField field = null;
		try {
			if (value instanceof Integer) {
				field = new SolrField(fieldName, SolrFieldType.INTEGER, useDynamicFieldEndings, false);
				jGenerator.writeNumberField(field.build(), (Integer) value);
			} else if (value instanceof Float) {
				field = new SolrField(fieldName, SolrFieldType.FLOAT, useDynamicFieldEndings, false);
				jGenerator.writeNumberField(field.build(), (Float) value);
			} else if (value instanceof Double) {
				field = new SolrField(fieldName, SolrFieldType.DOUBLE, useDynamicFieldEndings, false);
				jGenerator.writeNumberField(field.build(), (Double) value);
			}
		} catch (JsonGenerationException e) {
			if (field != null) {
				System.err.println("!! SolrJson Failed to write field: '" + field.getName() + "'\n");
			}
		}
	}

	public <T extends Number> void addField(SolrField field, T value) throws IOException {
		try {
			if (value == null) {
				if (SolrFieldType.INTEGER == field.getType()) {
					jGenerator.writeNumberField(field.build(), 0);
				} else if (SolrFieldType.FLOAT == field.getType()) {
					jGenerator.writeNumberField(field.build(), 0.0f);
				} else if (SolrFieldType.DOUBLE == field.getType()) {
					jGenerator.writeNumberField(field.build(), 0.0d);
				}
			}
			if (value instanceof Integer) {
				jGenerator.writeNumberField(field.build(), (Integer) value);
			} else if (value instanceof Float) {
				jGenerator.writeNumberField(field.build(), (Float) value);
			} else if (value instanceof Double) {
				jGenerator.writeNumberField(field.build(), (Double) value);
			}
		} catch (JsonGenerationException e) {
			System.err.println("!! SolrJson Failed to write field: '" + field.getName() + "'\n");
		}
	}

	private <T extends Number> void writeNumArray(String fieldName, Collection<T> values) throws IOException {
		jGenerator.writeFieldName(fieldName);
		jGenerator.writeStartArray(values.size());
		for (Number tok : values) {
			if (tok != null) {
				if (tok instanceof Integer) {
					jGenerator.writeNumber((Integer) tok);
				} else if (tok instanceof Float) {
					jGenerator.writeNumber((Float) tok);
				} else if (tok instanceof Double) {
					jGenerator.writeNumber((Double) tok);
				}
			}
		}
		jGenerator.writeEndArray();
	}

}
