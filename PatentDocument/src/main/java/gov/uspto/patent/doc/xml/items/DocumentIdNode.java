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
		//super(itemNode, itemNode.getName());
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

		if (docNumber.substring(0,2).toLowerCase().equals(countryCode.toString().toLowerCase())) {
			docNumber = docNumber.substring(2).trim();
			LOGGER.debug("Removed duplicate CountryCode '{}' doc-number: {} => {}", countryCode.toString(), docNumN.getText(), docNumber);
		}

		// Seems application number format changed in 2004 from short year to long year.
		Matcher matcher = SHORT_YEAR.matcher(docNumber);
		if (matcher.matches()) {
			if (matcher.group(1).equals("0")) {
				docNumber = "20" + docNumber;
				LOGGER.debug("Expanded Short Year, doc-number: {} => {}{}", matcher.group(0), countryCode, docNumber);
			}
			else if (matcher.group(1).equals("9")) {
				docNumber = "19" + docNumber;
				LOGGER.debug("Expanded Short Year, doc-number: {} => {}{}", matcher.group(0), countryCode, docNumber);
			}
		}

		if (!docNumber.startsWith("PCT/")) {
			docNumber = docNumber.replace("/", "");
		}

		docNumber = docNumber.replaceAll("[\\s-]", "");

		/*
		if (docNumber.startsWith("PCT/")) {
				String countryPCT = docNumber.substring(4, 6);
				try {
					countryCode = CountryCode.fromString(countryPCT);
				} catch (InvalidDataException e2) {
					LOGGER.warn("Invalid CountryCode '{}', from: {}", countryPCT, itemNode.asXML(), e2);
				}
				
				docNumber = docNumber.substring(4);
			}
		}
		*/

		Node kindN = itemNode.selectSingleNode("kind");
		String kindCode = kindN != null ? kindN.getText() : null;

		DocumentId documentId = new DocumentId(countryCode, docNumber, kindCode);
		documentId.setRawText(docNumN.getText());

		Node dateN = itemNode.selectSingleNode("date");
		if (dateN != null) {
			try {
				documentId.setDate(new DocumentDate(dateN.getText()));
			} catch (InvalidDataException e) {
				LOGGER.warn("Failed to parse date from: {}", itemNode.asXML(), e);
			}
		}

		return documentId;

	}
}
