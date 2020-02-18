package gov.uspto.parser.keyvalue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import gov.uspto.patent.PatentReaderException;

/**
 * Read fixed-width Field Value file into list of KeyValue Pairs
 * 
 * <p>
 * Key's column has a fixed width, long value text is wrapped onto next line
 * with empty key column.
 * <p>
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public abstract class KvReaderFixedWidth {

	private final int columnOneWidth;
	private List<String> maintainSpaceFields = new ArrayList<String>();
	private String currentFieldName;

	public KvReaderFixedWidth(int columnOneWidth) {
		this.columnOneWidth = columnOneWidth;
	}

	/**
	 * Fields which space and new lines should be maintained, like HTML pre tag.
	 */
	public void setMaintainSpaceFields(Collection<String> capitalizedFieldNames) {
		this.maintainSpaceFields.addAll(capitalizedFieldNames);
	}

	/**
	 * Read Plaintext Document and parse field values into List of Key Values.
	 * 
	 * @param reader - Reader for plaintext document
	 * @return - List of Key Value Pairs
	 * @throws PatentReaderException
	 * @throws IOException
	 */
	public List<KeyValue> parse(Reader reader) throws PatentReaderException {
		List<KeyValue> keyValues = new ArrayList<KeyValue>();
		currentFieldName = "";

		try (BufferedReader breader = new BufferedReader(reader)) {

			String currentLine;
			while ((currentLine = breader.readLine()) != null) {
				if (currentLine.length() < columnOneWidth) {
					continue;
				}

				String key = currentLine.substring(0, columnOneWidth).trim();
				String value = currentLine.substring(columnOneWidth);
				boolean indented = key.length() <= 1;

				if (isKeyValid(key)) {
					key = keyTransform(key);
					String keyRenamed = keyRename(key);
					if (value.trim().isEmpty()) {
						// Section
						keyValues.add(new KeyValue(keyRenamed, "").setKeyOriginal(key));
					} else {
						// Key-Value
						value = valueTransform(key, keyRenamed, value);
						if (maintainSpaceFields.contains(key.toUpperCase())) {
							value += "\n";
						}
						keyValues.add(new KeyValue(keyRenamed, value).setKeyOriginal(key));
						currentFieldName = key.toLowerCase();
					}
				} else if (indented && !keyValues.isEmpty()) {
					// Continued Value
					int lastLoc = keyValues.size() - 1;
					KeyValue lastKv = keyValues.get(lastLoc);
					value = valueTransform(lastKv.getKeyOriginal(), lastKv.getKey(), value);
					if (maintainSpaceFields.contains(currentFieldName)) {
						value += "\n";
						lastKv.appendValue(value);
					} else {
						lastKv.appendValueNoSpace(value);
					}
					currentFieldName = lastKv.getKey().toUpperCase();
				} else {
					// System.err.println("KvReaderFixedWidth error: " + currentLine);
				}
			}

		} catch (IOException e) {
			throw new PatentReaderException(e);
		}
		
		return postProcess(keyValues);
	}

	public abstract boolean isKeyValid(final String key);

	public abstract String keyTransform(String key);

	public abstract String keyRename(String key);

	public abstract String valueTransform(String key, String keyRenamed, String value);

	/**
	 * Perform Action Across multiple KeyValues or split/tokenize a value into multiple instances of a key
	 * 
	 * @param keyValues
	 * @return
	 */
	public abstract List<KeyValue> postProcess(List<KeyValue> keyValues);
}
