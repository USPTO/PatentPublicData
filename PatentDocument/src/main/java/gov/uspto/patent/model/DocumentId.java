package gov.uspto.patent.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import gov.uspto.patent.InvalidDataException;

/**
 * Document ID for Patents and Patent Applications.
 *
 *<p>
 * Note: US Patent Id numbers are not guaranteed to be numeric, they can 
 * have a "D" for a Design Patent, "PP" for Plant Patent, or an "X" 
 * for an X-Patent. 
 *<p>
 *
 *<p>
 * Note: X-Patent's even though they are over 100 years old can still be cited in current patents.
 *</p>
 *
 * @see WIPO ST.14.
 * @see https://en.wikipedia.org/wiki/X-Patent
 */
public class DocumentId implements Comparable<DocumentId> {
	private DocumentIdType docIdType;
	private CountryCode countryCode;
	private String docNumber;
	private String name; // name used in citation.
	private String kindCode; // different patent offices have different kindcodes.
	private DocumentDate date;
	private PatentType patentType; // defined with application id or derived from kindcode.
	private String rawText; // capture raw string before parsing into parts; mainly for debugging.
	private boolean allowLeadingZeros; // Need this for the one case in greenbook where the appId has a zero

	/*
	 * Parsing of Document Id into its parts, such as Citation PatentIds.
	 */
	private static final Pattern PARSE_PATTERN = Pattern.compile("^(\\D\\D)(\\d{1,4}[/-])?(X?\\d+)(\\D\\d?)?$");

	public DocumentId(CountryCode countryCode, String docNumber) throws IllegalArgumentException {
		this(countryCode, docNumber, null);
	}

	public DocumentId(CountryCode countryCode, String docNumber, boolean allowLeadingZeros)
			throws IllegalArgumentException {
		this(countryCode, docNumber, null, allowLeadingZeros);
	}

	public DocumentId(CountryCode countryCode, String docNumber, String kindCode) {
		Preconditions.checkNotNull(countryCode, "CountryCode can not be set to Null");
		Preconditions.checkNotNull(docNumber, "DocNumber can not be set to Null");

		this.countryCode = countryCode;
		setDocNumber(docNumber);
		this.kindCode = kindCode;
	}

	public DocumentId(CountryCode countryCode, String docNumber, String kindCode, boolean allowLeadingZeros) {
		Preconditions.checkNotNull(countryCode, "CountryCode can not be set to Null");
		Preconditions.checkNotNull(docNumber, "DocNumber can not be set to Null");
		this.allowLeadingZeros = allowLeadingZeros;
		this.countryCode = countryCode;
		setDocNumber(docNumber);
		this.kindCode = kindCode;
	}

	public void setRawText(String raw) {
		this.rawText = raw;
	}

	public String getRawText() {
		return rawText;
	}

	public void setPatentType(PatentType patentType) {
		this.patentType = patentType;
	}

	public PatentType getPatentType() {
		return patentType;
	}

	public void setType(DocumentIdType docIdType) {
		Preconditions.checkNotNull(docIdType, "DocumentIdType can not be set to Null");
		this.docIdType = docIdType;
	}

	public DocumentIdType getType() {
		return docIdType;
	}

	private void setDocNumber(String publicationId) {
		Preconditions.checkNotNull(publicationId, "DocNumber can not be set to Null!");
		// Remove Leading Zeros.
		if (!allowLeadingZeros) {
			this.docNumber = publicationId.replaceFirst("^0+(?!$)", "");
		} else {
			this.docNumber = publicationId;
		}
	}

	public String getDocNumber() {
		return docNumber;
	}

	/**
	 * Full String ID Representation, example: US12345A1
	 * 
	 * @return
	 */
	public String getId() {
		StringBuilder strb = new StringBuilder();

		if (!docNumber.startsWith("PCT/")) {
			strb.append(countryCode);
		}
		strb.append(docNumber);

		if (kindCode != null) {
			strb.append(kindCode);
		}

		return strb.toString();
	}

	/**
	 * Full String ID Representation, leaving off kindcode, example: US12345
	 * 
	 * @return
	 */
	public String getIdNoKind() {
		StringBuilder strb = new StringBuilder();

		if (!docNumber.startsWith("PCT/")) {
			strb.append(countryCode);
		}
		strb.append(docNumber);

		return strb.toString();
	}

	/**
	 * Full String ID Representation, example: US12345A1
	 * 
	 * @param zeroPadMinLen - anything under provided length will be padded with
	 *                      leading zeros
	 * @return
	 */
	public String getId(int zeroPadMinLen) {
		StringBuilder strb = new StringBuilder().append(countryCode);

		strb.append(Strings.padStart(docNumber, zeroPadMinLen, '0'));

		if (kindCode != null) {
			strb.append(kindCode);
		}

		return strb.toString();
	}

	public CountryCode getCountryCode() {
		return countryCode;
	}

	public void setKindCode(String kindCode) {
		this.kindCode = kindCode;
	}

	public String getKindCode() {
		return kindCode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public DocumentDate getDate() {
		return date;
	}

	public void setDate(DocumentDate date) {
		this.date = date;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}

		if (!(o instanceof DocumentId)) {
			return false;
		}

		DocumentId other = (DocumentId) o;
		if (this.docNumber != null && this.docNumber != null) {
			if (this.docNumber.equals(other.docNumber) && this.docNumber.equals(other.docNumber)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	public String toTextNoKind() {
		return getIdNoKind();
	}

	public String toText() {
		return getId();
	}

	public String toText(int zeroPadMinLen) {
		return getId(zeroPadMinLen);
	}

	/**
	 * Parse Patent DocumentId into its parts.
	 * 
	 * <p>Note: this may not work for all variations from all countries, may countries
	 * may have a different numbering system, many also have multiple different
	 * numbering systems.</p>
	 * 
	 * @see http://www.wipo.int/export/sites/www/standards/en/pdf/07-02-02.pdf
	 * 
	 * @param documentIdStr
	 * @return DocumentId
	 * @throws InvalidDataException
	 */
	public static DocumentId fromText(final String documentIdStr, int year) throws InvalidDataException {
		String docIdStr = documentIdStr.replaceAll(" ", "");
		Matcher matcher = PARSE_PATTERN.matcher(docIdStr);
		if (matcher.matches()) {
			String country = matcher.group(1);

			CountryCode cntyCode = CountryCode.fromString(country);
			if (CountryCode.UNKNOWN == cntyCode || year < 1978) {
				cntyCode = CountryCodeHistory.getCurrentCode(country, year);
			}

			String applicationYear = matcher.group(2); // applications ids
														// sometimes has the
														// year.
			String id = matcher.group(3);
			String kindCode = matcher.group(4);

			DocumentId docId = new DocumentId(cntyCode, id, kindCode);
			if (applicationYear != null && applicationYear.length() == 4) {
				docId.setDate(new DocumentDate(applicationYear.replace("/", "")));
			}
			docId.setRawText(documentIdStr);
			return docId;
		} else {
			throw new InvalidDataException("Failed to parse DocumentId text: " + documentIdStr);
		}
	}

	public static List<DocumentId> getByType(Collection<DocumentId> docIds, DocumentIdType type) {
		List<DocumentId> redIds = new LinkedList<DocumentId>();

		for (DocumentId docId : docIds) {
			if (docId != null && docId.getType() == type) {
				redIds.add(docId);
			}
		}

		return redIds;
	}

	@Override
	public int compareTo(DocumentId o) {
		if (getDate() == null || o.getDate() == null || getDate().getDate() == null || o.getDate().getDate() == null) {
			return 1;
		}
		return getDate().getDate().compareTo(o.getDate().getDate());
	}

	@Override
	public String toString() {
		return "DocumentId [docIdType=" + docIdType + ", countryCode=" + countryCode + ", docNumber=" + docNumber
				+ ", name=" + name + ", kindCode=" + kindCode + ", date=" + date + ", patentType=" + patentType
				+ ", getId()=" + getId() + "]";
	}

}
