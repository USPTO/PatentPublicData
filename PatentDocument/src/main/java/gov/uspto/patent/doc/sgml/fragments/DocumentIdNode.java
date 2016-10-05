package gov.uspto.patent.doc.sgml.fragments;

import org.dom4j.Document;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.DocumentDate;
import gov.uspto.patent.model.DocumentId;

public class DocumentIdNode extends DOMFragmentReader<DocumentId> {

	private static final Logger LOGGER = LoggerFactory.getLogger(DocumentIdNode.class);

	private static final CountryCode DEFAULT_COUNTRYCODE = CountryCode.US;

	private CountryCode fallbackCountryCode;

	public DocumentIdNode(Document document) {
		this(document, DEFAULT_COUNTRYCODE);
	}

	public DocumentIdNode(Document document, CountryCode fallbackCountryCode) {
		super(document);
		this.fallbackCountryCode = fallbackCountryCode;
	}

	@Override
	public DocumentId read() {

		Node patentNode = document.selectSingleNode("/PATDOC/SDOBI/B100");
		if (patentNode == null) {
			return null;
		}

		Node docNumN = patentNode.selectSingleNode("B110/DNUM/PDAT");
		if (docNumN == null) {
			LOGGER.warn("Patent does not have a document-id.");
			return null;
		}

		Node kindN = patentNode.selectSingleNode("B130/PDAT");
		String kindCode = kindN != null ? kindN.getText() : null;

		Node countryN = patentNode.selectSingleNode("B190/PDAT");
		String country = countryN != null ? countryN.getText() : null;
		CountryCode countryCode = fallbackCountryCode;
		try {
			countryCode = CountryCode.fromString(country);
		} catch (InvalidDataException e2) {
			LOGGER.warn("Invalid CountryCode: {}, from: {}", country, patentNode.asXML(), e2);
		}

		DocumentId documentId = new DocumentId(countryCode, docNumN.getText(), kindCode);

		Node dateN = patentNode.selectSingleNode("B140/DATE/PDAT");
		if (dateN != null) {
			try {
				documentId.setDate(new DocumentDate(dateN.getText()));
			} catch (InvalidDataException e) {
				LOGGER.warn("Failed to parse date from: {}", patentNode.asXML(), e);
			}
		}

		return documentId;
	}

}
