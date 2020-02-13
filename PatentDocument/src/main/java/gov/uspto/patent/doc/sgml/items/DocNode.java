package gov.uspto.patent.doc.sgml.items;

import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.ItemReader;
import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.CountryCodeHistory;
import gov.uspto.patent.model.DocumentDate;
import gov.uspto.patent.model.DocumentId;

/**
 * <h3>Patent Document Identification</h3>
 * <p>
 * <li>DNUM PDAT Document number
 * <li>DATE PDAT Document date
 * <li>CTRY PDAT Publishing country or organization (ST.3)
 * <li>KIND PDAT Document kind (ST.16)
 * <li>BNUM PDAT Bulletin number
 * <li>DTXT STEXT Descriptive text
 * </p>
 * <p>
 * 
 * <pre>
 *{@code
 *<DOC>
 *	<DNUM><PDAT>12345678</PDAT></DNUM>
 *	<DATE><PDAT>19990212</PDAT></DATE>
 *	<CTRY><PDAT>US</PDAT></CTRY>
 *	<KIND><PDAT>A1</PDAT></KIND>
 *</DOC>
 *}
 * </pre>
 * </p>
 *
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class DocNode extends ItemReader<DocumentId> {
	private static final Logger LOGGER = LoggerFactory.getLogger(DocNode.class);

	private static final CountryCode DEFAULT_COUNTRYCODE = CountryCode.US;

	private static final XPath DOCNUMXP = DocumentHelper.createXPath("DNUM/PDAT");
	private static final XPath DATEXP = DocumentHelper.createXPath("DATE/PDAT");
	private static final XPath COUNTRYXP = DocumentHelper.createXPath("CTRY/PDAT");
	private static final XPath KINDXP = DocumentHelper.createXPath("KIND/PDAT");

	private CountryCode fallbackCountryCode;

	public DocNode(Node itemNode) {
		this(itemNode, DEFAULT_COUNTRYCODE);
	}

	/**
	 * Constuctor
	 * 
	 * @param itemNode
	 * @param fallbackCountryCode - CountryCode to use when country is Null or
	 *                            Invalid.
	 */
	public DocNode(Node itemNode, CountryCode fallbackCountryCode) {
		super(itemNode);
		this.fallbackCountryCode = fallbackCountryCode != null ? fallbackCountryCode : DEFAULT_COUNTRYCODE;
	}

	@Override
	public DocumentId read() {
		return readDocId(itemNode);
	}

	public DocumentId readDocId(Node parentNode) {
		Node docNumN = DOCNUMXP.selectSingleNode(parentNode);
		if (docNumN == null) {
			LOGGER.warn("Invalid document id can not be Null, from: {}", parentNode.asXML());
			return null;
		}

		Node countryN = COUNTRYXP.selectSingleNode(parentNode);
		String countryStr = countryN != null ? countryN.getText() : null;

		Node dateN = DATEXP.selectSingleNode(parentNode);
		DocumentDate docDate = null;
		if (dateN != null && dateN.getText().trim().isEmpty()) {
			try {
				docDate = new DocumentDate(dateN.getText());
			} catch (InvalidDataException e) {
				LOGGER.warn("{} : {}", e.getMessage(), parentNode.asXML());
			}
		}

		CountryCode countryCode = fallbackCountryCode;
		if (countryStr != null) {
			try {
				countryCode = CountryCode.fromString(countryStr);
			} catch (InvalidDataException e2) {
				if (countryCode != null && docDate != null) {
					countryCode = CountryCodeHistory.getCurrentCode(countryStr, docDate.getYear());
					if (countryCode == CountryCode.UNKNOWN) {
						LOGGER.warn("{} : {}", e2.getMessage(), parentNode.asXML());
					} else {
						LOGGER.debug("Historic Country Code Matched '{}' yr {} : '{}'", countryStr, docDate.getYear(),
								countryCode);
					}
				}
			}
		}

		
		/*
		 * Fix for duplication of CountryCode, country reappears in the doc number field
		 */
		String docNumber = docNumN.getText();
		if (docNumber.length() > 2 && docNumber.substring(0, 2).equalsIgnoreCase(countryCode.toString())) {
			// WO 2005/023894 => 2005/023894
			docNumber = docNumber.substring(2).trim();
			if (docNumber.startsWith("/")) {
				// WO/03/001333 => 03/001333
				docNumber = docNumber.substring(1);
			}
			LOGGER.debug("Removed duplicate CountryCode '{}' -- from: '{}' doc-number: {} => {}",
					countryCode.toString(), itemNode.getParent().getName(), docNumN.getText(), docNumber);
		}

		Node kindCodeN = KINDXP.selectSingleNode(parentNode);
		String kindCode = kindCodeN != null ? kindCodeN.getText() : null;

		DocumentId documentId = new DocumentId(countryCode, docNumber.replaceAll("/", ""), kindCode);
		documentId.setRawText(docNumber);
		if (docDate != null) {
			documentId.setDate(docDate);
		}

		return documentId;
	}
}
