package gov.uspto.parser.keyvalue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import gov.uspto.common.text.WordUtil;
import gov.uspto.patent.PatentReaderException;

/**
 * Read Field-Space-Value file into list of KeyValue Pairs
 * 
 * <p>
 * Key space value, long value text is wrapped with indented on next line.
 * <p>
 * 
 * <p>
 * When keys' size and column are not variable then take a look at the KvReaderFixedWidth class.
 * </p>
 * 
 * <p>
 * Example of flat fields with no sections:
 * 
 * <pre>
 * WKU  039305848
 * APN  4584481
 * APT  1
 * ART  316
 * TTL Method for performing chip level electromagnetic interference reduction,
 *       and associated apparatus
 * URPN 2003/0169838
 * </pre>
 * </p>
 * 
 * <p>
 * Example same as above with the addition of section "PATN" and "INVT" each
 * having subfields directly following underneath.
 * 
 * <pre>
 * PATN
 * WKU  039305848
 * APN  4584481
 * INVT
 * NAM  Doe; John
 * STR  1 Main St
 * CTY  Springfield
 * </pre>
 * </p>
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */

public abstract class KvReader {

	private final int minKeyLength;
	private final int maxKeyLength;
	private final int indentLength;
	private List<String> maintainSpaceFields = new ArrayList<String>();
	private String currentFieldName;

	public KvReader(int minKeyLength, int maxKeyLength, int indentLength) {
		this.minKeyLength = minKeyLength;
		this.maxKeyLength = maxKeyLength;
		this.indentLength = indentLength;
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
				boolean indented = isIndented(currentLine, indentLength);
				if (indented && currentLine.trim().length() >= minKeyLength
						&& currentLine.trim().length() <= maxKeyLength && isKeyValid(currentLine)) {
					// Section
					String key = keyTransform(currentLine);
					keyValues.add(new KeyValue(key, ""));
				} else if (indented && !keyValues.isEmpty()) {
					// Continued Value
					int lastLoc = keyValues.size() - 1;
					KeyValue lastKv = keyValues.get(lastLoc);
					String value = valueTransform(lastKv.getKey(), currentLine);
					if (maintainSpaceFields.contains(currentFieldName)) {
						value += "\n";
						lastKv.appendValue(value);
					} else {
						lastKv.appendValueNoSpace(value);
					}
					currentFieldName = lastKv.getKey().toUpperCase();
				} else {
					// Key Value
					String[] parts = parseLineTokens(currentLine);
					if (parts.length == 2) {
						String key = keyTransform(parts[0]);
						String value = valueTransform(key, parts[1]);
						if (maintainSpaceFields.contains(key.toUpperCase())) {
							value += "\n";
						}
						keyValues.add(new KeyValue(key, value));
						currentFieldName = key.toLowerCase();
					} else {
						// Section
						String key = keyTransform(currentLine);
						if (key.length() >= minKeyLength && key.length() <= maxKeyLength) {
							keyValues.add(new KeyValue(key, ""));
						} else {
							// System.err.println("KvReader line error: " + currentLine);
						}
					}
				}
			}

		} catch (IOException e) {
			throw new PatentReaderException(e);
		}

		return keyValues;
	}

	/**
	 * Parse Line into parts
	 * 
	 * @param line
	 * @return
	 */
	private String[] parseLineTokens(final String line) {
		// Parse Key Value
		String tline = line.trim();

		int idx = tline.indexOf(' ');
		if (!(idx >= minKeyLength && idx <= maxKeyLength)) {
			return new String[] { line };
		}

		// Key
		String key = tline.substring(0, idx);
		if (isKeyValid(key)) {
			// Key-Value
			String value = tline.substring(idx, tline.length());
			return new String[] { key, value };
		} else {
			// System.err.println(" Error: '" + key + "' : " + tline);
			return new String[] { line };
		}
	}

	private boolean isIndented(String str, int indentLength) {
		return WordUtil.countLeadChar(str, ' ') >= indentLength;
	}

	public abstract String keyTransform(String key);

	public abstract boolean isKeyValid(final String key);

	public abstract String valueTransform(String key, String value);

}
