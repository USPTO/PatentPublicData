package gov.uspto.patent.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import gov.uspto.patent.InvalidDataException;
import org.apache.commons.lang3.builder.CompareToBuilder;

/**
 * Document ID for Patents and Patent Applications
 *
 * <p>
 * <h3>US Patent Document IDs are alpha-numeric (letters and numbers)</h3>
 * <ul>
 * <li>Design Patent</li>
 * <ul>
 * <li>start with "D" e.g. D321987</li>
 * </ul>
 * <li>Plant Patent</li>
 * <ul>
 * <li>start with "PP" e.g. PP07514</li>
 * </ul>
 * <li>Reissued Patents</li>
 * <ul>
 * <li>start with "RE" e.g. RE12345</li>
 * </ul>
 * <li>Fractional Patents</li>
 * <ul>
 * <li>end with [A-O] e.g. D90793&frac12; becomes D90793H</li>
 * <li>issued 1790? to 1966</li>
 * <li>purpose was to group patent families issued together</li>
 * </ul>
 * <li>Additions of Improvements</li>
 * <ul>
 * <li>start with "AI" e.g. AI000318</li>
 * <li>issued 1838 to 1861</li>
 * <li>about 300 issued</li>
 * <li>replace by a continuing patent application (continuation, divisional, or
 * continuation-in-part)</li>
 * </ul>
 * <li>X-Patents</li>
 * <ul>
 * <li>start with "X" e.g. X007640 or X9670H</li>
 * <li>issued July 1790 to July 1836</li>
 * <li>USPTO fire of July 1836, majority of patents destroyed</li>
 * <li>approximately 106 patent where recovered; inventors where asked to
 * provide their copies</li>
 * </ul>
 * <li>X-Patents Reissued</li>
 * <ul>
 * <li>start with "RX" e.g. RX00116</li>
 * </ul>
 * </ul>
 * </p>
 *
 * <p>
 * <h3>Defensive Publication or Disclosure</h3> Mostly unneeded once
 * applications became public after 18 months, started in 1999 from the American
 * Inventors Protection Act "AIPA"; Similar protections exist by filing and
 * abandoning an application.
 * <ul>
 * <li>Statutory Invention Registration (SIR) / H-Documents</li>
 * <ul>
 * <li>start with "H" e.g. H001234</li>
 * <li>issued May 8, 1985 to March 16, 2013 (America Invents Act "AIA")</li>
 * <li>replaced Defensive Publication</li>
 * <li>replaced by the publishing of applications</li>
 * </ul>
 * <li>Defensive Publication / Technical Disclosure / T-Documents</li>
 * <ul>
 * <li>start with "T" e.g. T855019</li>
 * <li>issued April 1968 to May 8, 1985</li>
 * <li>replaced by Statutory Invention Registration (SIR)</li>
 * </ul>
 * <li>IBM technical disclosure bulletin (TDB)</li>
 * <ul>
 * <li>issued 1968 to 1998</li>
 * <li>more than 83,500 issued</li>
 * <li>Used by patent examiners</li>
 * <li>cited 48,000+ times in US patents</li>
 * <li>example cite: IBM Technical Disclosure Bulletin, vol. 36, No. 6A, Jun.
 * 1993, pp. 261-264.</li>
 * </ul>
 * </ul>
 *
 * <p>
 * <h3>Take Note</h3> Any patent can cite anything from the past, which includes
 * disclosures, X-Patents and Fractional Patents.
 * </p>
 *
 * @see WIPO ST.14.
 * @see https://en.wikipedia.org/wiki/X-Patent
 */
public class DocumentId implements Comparable<DocumentId> {
	private DocumentIdType docIdType;
	private CountryCode countryCode;
	private String applicationYear;
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
	private static final Pattern PARSE_PATTERN = Pattern
			.compile("^(\\D\\D)(\\d{2,4}[/-])?((?:D|PP|RE|AI|H|T|X|RX|\\d-)?\\d+)(\\D\\d?)?$");

	public DocumentId(CountryCode countryCode, String docNumber) {
		this(countryCode, docNumber, null);
	}

	public DocumentId(CountryCode countryCode, String docNumber, boolean allowLeadingZeros) {
		this(countryCode, docNumber, null, allowLeadingZeros);
	}

	public DocumentId(CountryCode countryCode, String docNumber, String kindCode) {
		this(countryCode, docNumber, kindCode, false);
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

	/**
	 * Determine DocType
	 *
	 * <p>
	 * 1) DocumentIdType.APPLICATION or DocumentIdType.PROVISIONAL then
	 * DocType.Application <br/>
	 * 2) CountryCode.US & docNumber starts with date year then DocType.Application
	 * <br/>
	 * 2.1) CountryCode.WO & docNumber starts with PCT/US with date year then
	 * DocType.Application <br/>
	 * 3) CountryCode.US and kindCode then DocType.Patent </br>
	 * 4) CountryCode.US and docNumber starts with any of (D|PP|RE|AI|X|RX) then
	 * DocType.Patent <br/>
	 * <br/>
	 * </p>
	 * 
	 * @return DocType (Patent, Application) or Null
	 */
	public DocType getDocType() {
		if (docIdType == DocumentIdType.APPLICATION || docIdType == DocumentIdType.PROVISIONAL) {
			return DocType.Application;
		}

		if (CountryCode.US.equals(countryCode)
				|| CountryCode.WO.equals(countryCode) && docNumber.startsWith("PCT/US")) {

			String docYear = getDate() != null ? String.valueOf(getDate().getYear()) : null;

			// docNumber should be normalized to 4 digit year at this point.
			if (docYear != null && (applicationYear != null || docNumber.startsWith("PCT/US" + docYear))) {
				return DocType.Application;
			} else if (kindCode != null && kindCode.length() > 0) {
				return DocType.Patent;
			} else if (docNumber.matches("^(?:D|PP|RE|AI|X|RX).+$")) {
				return DocType.Patent;
			} else if (docNumber.length() <= 8) {
				return DocType.Patent;
			}
		}

		return null;
	}

	private void setDocNumber(String publicationId) {
		Preconditions.checkNotNull(publicationId, "DocNumber can not be set to Null!");
		this.docNumber = publicationId;
	}

	/**
	 * Document Number
	 * <p>
	 * Beware: If this is an application, the application year is also needed to
	 * make it unique.
	 * </p>
	 * 
	 * @return
	 */
	public String getDocNumber() {
		return docNumber;
	}

	/**
	 * Set Application Year and DocType to Application ; only use for Patent
	 * Applications
	 * 
	 * @param yearStr
	 * @throws InvalidDataException
	 */
	public void setApplicationYear(String yearStr) throws InvalidDataException {
		this.applicationYear = yearStr;
		setType(DocumentIdType.APPLICATION);
		if (yearStr.length() == 4) {
			setDate(new DocumentDate(yearStr));
		}
	}

	/**
	 * Set Application Year and DocType to Application ; only use for Patent
	 * Applications
	 * 
	 * @param yearStr
	 * @param date
	 * @throws InvalidDataException
	 */
	public void setApplicationYear(String yearStr, DocumentDate date) throws InvalidDataException {
		this.applicationYear = yearStr;
		setType(DocumentIdType.APPLICATION);
		setDate(date);
	}

	public String getApplicationYear() {
		return applicationYear;
	}

	/**
	 * Full String ID Representation, example: US12345A1
	 * 
	 * @return
	 */
	public String getId() {
		StringBuilder strb = new StringBuilder();

		if (docNumber.startsWith("PCT/")) {
			return docNumber;
		}

		strb.append(countryCode);

		if (applicationYear != null) {
			strb.append(applicationYear);
			strb.append(normDocNumber(docNumber));
		} else {
			strb.append(removeLeadingZeros(normDocNumber(docNumber)));
		}

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

		if (docNumber.startsWith("PCT/")) {
			return docNumber;
		}

		strb.append(countryCode);

		if (applicationYear != null) {
			strb.append(applicationYear);
			strb.append(normDocNumber(docNumber));
		} else {
			strb.append(removeLeadingZeros(normDocNumber(docNumber)));
		}

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
		StringBuilder strb = new StringBuilder();

		if (docNumber.startsWith("PCT/")) {
			return docNumber;
		}

		strb.append(countryCode);

		if (applicationYear != null) {
			strb.append(applicationYear);
			strb.append(normDocNumber(docNumber));
		} else {
			if (CountryCode.US.equals(countryCode) && docNumber.matches("\\d+")) {
				strb.append(Strings.padStart(docNumber, zeroPadMinLen, '0'));
			} else {
				strb.append(normDocNumber(docNumber));
			}
		}

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
		if (date == null) {
			return DocumentDate.getEmpty();
		}
		return date;
	}

	public void setDate(DocumentDate date) {
		this.date = date;
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

	private String normDocNumber(String docNum) {
		return docNum.replaceAll("[^A-Z0-9\\.]+", "");
	}

	private String removeLeadingZeros(String docNum) {
		if (!allowLeadingZeros) {
			String docNumber;
			if (CountryCode.US.equals(countryCode)) {
				docNumber = docNum.replaceFirst("^(D|RE|PP|AI|H|T|X|RX)0+(?!$)", "$1").replaceFirst("^0+(?!$)", "");
			} else {
				docNumber = docNum.replaceFirst("^0+(?!$)", "");
			}
			return docNumber;
		} else {
			return docNum;
		}
	}

	public static DocumentId fromPCTText(final String docIdStr) throws InvalidDataException {
		if (docIdStr.length() > 13 && docIdStr.startsWith("PCT/")) {
			String cntryCode = docIdStr.substring(4, 6);
			String applicationYear;
			String applicationNumber;
			if (docIdStr.length() == 17) {
				// PCT/CCYYYY/999999 --> 4-digit year ; 6 digit number
				applicationYear = docIdStr.substring(6, 10);
				applicationNumber = docIdStr.substring(11);
			} else {
				// PCT/CCYY/99999 -> 2-digit year ; 5 digit number
				applicationYear = docIdStr.substring(6, 8);
				applicationNumber = docIdStr.substring(9);
				
			}

			DocumentId docId;
			if ("US".equals(cntryCode)) {
				docId = new DocumentId(CountryCode.US, applicationNumber);
			} else {
				docId = new DocumentId(CountryCode.WO, docIdStr);
			}

			docId.setRawText(docIdStr);
			if (applicationYear.length() == 4) {
				docId.setApplicationYear(applicationYear);
			} else {
				try {
					DocumentDate date = new DocumentDate("19" + applicationYear);
					docId.setApplicationYear(applicationYear, date);
				} catch (InvalidDataException e) {
					docId.setApplicationYear(applicationYear);
				}
			}
			return docId;
		}
		return null;
	}
	
	/**
	 * Parse Patent DocumentId into its parts.
	 * 
	 * <p>
	 * Note: this may not work for all variations from all countries, may countries
	 * may have a different numbering system, many also have multiple different
	 * numbering systems.
	 * </p>
	 * 
	 * @see http://www.wipo.int/export/sites/www/standards/en/pdf/07-02-02.pdf
	 * 
	 * @param documentIdStr
	 * @return DocumentId
	 * @throws InvalidDataException
	 */
	public static DocumentId fromText(final String documentIdStr, int year) throws InvalidDataException {
		String docIdStr = documentIdStr.replaceAll(" ", "");

		if (docIdStr.length() > 13 && docIdStr.startsWith("PCT/")) {
			return DocumentId.fromPCTText(documentIdStr);
		}

		if (docIdStr.length() < 4) {
			throw new InvalidDataException("Failed to parse DocumentId text [too small]: '" + documentIdStr + "'");
		}

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
			if (applicationYear != null && applicationYear.length() == 5) {
				docId.setApplicationYear(applicationYear.replace("/", ""));
			}
			docId.setRawText(documentIdStr);
			return docId;
		} else {
			throw new InvalidDataException("Failed to parse DocumentId text: '" + documentIdStr + "'");
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

	public static List<DocumentId> getByDocType(Collection<DocumentId> docIds, DocType type) {
		List<DocumentId> redIds = new LinkedList<DocumentId>();

		for (DocumentId docId : docIds) {
			if (docId != null && docId.getDocType() == type) {
				redIds.add(docId);
			}
		}

		return redIds;
	}

	@Override
	public final int hashCode() {
		if (docNumber != null && countryCode != null) {
			return getIdNoKind().hashCode();
		} else {
			return 0;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof DocumentId) || this.docNumber == null) {
			return false;
		} else {
			DocumentId other = (DocumentId) o;
			return getIdNoKind().equals(other.getIdNoKind());
		}
	}

	@Override
	public int compareTo(DocumentId o) {
		if (getDate() == null || o.getDate() == null || getDate().getDate() == null || o.getDate().getDate() == null) {
			return 1;
		}

		return new CompareToBuilder().append(getDate().getDate(), o.getDate().getDate())
				.append(getIdNoKind(), o.getIdNoKind()).build();
	}

	@Override
	public String toString() {
		return "DocumentId [docIdType=" + docIdType + ", countryCode=" + countryCode + ", docNumber=" + docNumber
				+ ", name=" + name + ", kindCode=" + kindCode + ", date=" + date + ", patentType=" + patentType
				+ ", getId()=" + getId() + "]";
	}

}
