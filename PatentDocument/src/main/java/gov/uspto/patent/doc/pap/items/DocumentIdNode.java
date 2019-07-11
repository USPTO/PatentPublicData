package gov.uspto.patent.doc.pap.items;

import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;
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

	private static final CountryCode DEFAULT_COUNTRYCODE = CountryCode.US;
	
	private static final XPath DOCIDXP = DocumentHelper.createXPath("doc-number");
	private static final XPath KINDXP = DocumentHelper.createXPath("kind-code");
	private static final XPath DATEXP = DocumentHelper.createXPath("document-date");
	private static final XPath COUNTRYXP = DocumentHelper.createXPath("country-code");

	private CountryCode fallbackCountryCode;

	public DocumentIdNode(Node itemNode) {
		this(itemNode, DEFAULT_COUNTRYCODE);
	}

	public DocumentIdNode(Node itemNode, CountryCode fallbackCountryCode) {
		super(itemNode, ITEM_NODE_NAME);
		this.fallbackCountryCode = fallbackCountryCode;
	}

	@Override
	public DocumentId read() {
		if (itemNode == null) {
			return null;
		}

		Node docNumN = DOCIDXP.selectSingleNode(itemNode);
		if (docNumN == null) {
			LOGGER.warn("Invalid doc-number missing : {}", itemNode.asXML());
			return null;
		}
		String docNumber = docNumN.getText().trim();

		Node countryN = COUNTRYXP.selectSingleNode(itemNode);
		CountryCode countryCode = CountryCode.UNKNOWN;
		if (docNumber.startsWith("PCT/")) {
			countryCode = CountryCode.WO;
		}
		else if (countryN == null || countryN.getText().trim().isEmpty()){
			LOGGER.debug("Invalid CountryCode missing: using fallback CountryCode '{}' : {}", fallbackCountryCode, itemNode.asXML());
		    countryCode = fallbackCountryCode;
		} else {
    		try {
    			countryCode = CountryCode.fromString(countryN.getText().trim());
    			if (CountryCode.UNDEFINED.equals(countryCode)){
    			    countryCode = fallbackCountryCode;
    			}
    		} catch (InvalidDataException e2) {
    			LOGGER.warn("{} : {}", e2.getMessage(), itemNode.asXML());
    		}
		}

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

		Node kindN = KINDXP.selectSingleNode(itemNode);
		String kindCode = kindN != null ? kindN.getText().trim() : null;

		DocumentId documentId = new DocumentId(countryCode, docNumber, kindCode);

		Node dateN = DATEXP.selectSingleNode(itemNode);
		if (dateN != null) {
			try {
				documentId.setDate(new DocumentDate(dateN.getText().trim()));
			} catch (InvalidDataException e) {
				LOGGER.warn("{} : {}", itemNode.asXML());
			}
		}

		return documentId;

	}
}
