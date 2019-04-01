package gov.uspto.patent.doc.xml.items;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.ItemReader;
import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.DocumentDate;
import gov.uspto.patent.model.DocumentId;

public class DocumentIdNode extends ItemReader<DocumentId> {
	private static final Logger LOGGER = LoggerFactory.getLogger(DocumentIdNode.class);

	private static final String ITEM_NODE_NAME = "document-id";

	private static final Pattern SHORT_YEAR = Pattern.compile("^([09])[0-9][/-]\\d+");

	private static final CountryCode DEFAULT_COUNTRYCODE = CountryCode.US;

	private CountryCode fallbackCountryCode;

	public DocumentIdNode(Node itemNode) {
		this(itemNode, DEFAULT_COUNTRYCODE);
	}

	public DocumentIdNode(Node itemNode, CountryCode fallbackCountryCode) {
		super(itemNode, ITEM_NODE_NAME);
		// super(itemNode, itemNode.getName());
		this.fallbackCountryCode = fallbackCountryCode;
	}

	@Override
	public DocumentId read() {
		if (itemNode == null) {
			return null;
		}

		Node docNumN = itemNode.selectSingleNode("doc-number");
		if (docNumN == null) {
			LOGGER.warn("Invalid doc-number can not be Null, from: {}", itemNode.asXML());
			return null;
		}

		Node countryN = itemNode.selectSingleNode("country");
		CountryCode countryCode = fallbackCountryCode;
		String country = countryN != null ? countryN.getText() : null;
		try {
			countryCode = CountryCode.fromString(country);
		} catch (InvalidDataException e2) {
			LOGGER.warn("Invalid CountryCode '{}', from: {}", country, itemNode.asXML(), e2);
		}

		String docNumber = docNumN.getText();

		/*
		 * Fix for duplication of CountryCode, country reappears in the doc number field
		 */
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

		Node dateN = itemNode.selectSingleNode("date");
		DocumentDate docDate = null;
		if (dateN != null) {
			try {
				docDate = new DocumentDate(dateN.getText());
			} catch (InvalidDataException e) {
				LOGGER.warn("Failed to parse date from: {}", itemNode.asXML(), e);
			}
		}

		/*
		 * Normalize YEAR in application number, in 2004 they changed, from two digit
		 * year form to four digit year
		 */
		Matcher matcher = SHORT_YEAR.matcher(docNumber);
		if (matcher.matches()) {
			if (matcher.group(1).equals("0")) {
				if (docDate != null && docDate.getYear() <= 2000) {
					LOGGER.warn("Expand of possible Short Year, skipped due to year mismatch; doc-number: {} year: {}",
							matcher.group(0), docDate.getYear());
				} else {
					docNumber = "20" + docNumber;
					LOGGER.debug("Expanded Short Year, doc-number: {} => {}{}", matcher.group(0), countryCode,
							docNumber);
				}
			} else if (matcher.group(1).equals("9")) {
				if (docDate != null && docDate.getYear() <= 1900 && docDate.getYear() > 2000) {
					LOGGER.warn("Expand of possible Short Year, skipped due to year mismatch; doc-number: {} year: {}",
							matcher.group(0), docDate.getYear());
				} else {
					docNumber = "19" + docNumber;
					LOGGER.debug("Expanded Short Year, doc-number: {} => {}{}", matcher.group(0), countryCode,
							docNumber);
				}
			}
		}

		if (!docNumber.startsWith("PCT/")) {
			docNumber = docNumber.replace("/", "");
		}

		docNumber = docNumber.replaceAll("[\\s-]", "");

		/*
		 * if (docNumber.startsWith("PCT/")) { String countryPCT =
		 * docNumber.substring(4, 6); try { countryCode =
		 * CountryCode.fromString(countryPCT); } catch (InvalidDataException e2) {
		 * LOGGER.warn("Invalid CountryCode '{}', from: {}", countryPCT,
		 * itemNode.asXML(), e2); }
		 * 
		 * docNumber = docNumber.substring(4); } }
		 */

		Node kindN = itemNode.selectSingleNode("kind");
		String kindCode = kindN != null ? kindN.getText().trim() : null;
		if (countryCode == CountryCode.US && "00".equals(kindCode)) {
			kindCode = null;
		}

		DocumentId documentId = new DocumentId(countryCode, docNumber, kindCode);
		documentId.setRawText(docNumN.getText());

		if (dateN != null && docDate != null) {
			documentId.setDate(docDate);
		}

		return documentId;

	}
}
