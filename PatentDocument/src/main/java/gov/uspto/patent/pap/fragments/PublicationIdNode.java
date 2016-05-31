package gov.uspto.patent.pap.fragments;

import org.dom4j.Document;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.DocumentDate;
import gov.uspto.patent.model.DocumentId;
import gov.uspto.patent.model.DocumentIdType;

public class PublicationIdNode extends DOMFragmentReader<DocumentId> {
	private static final Logger LOGGER = LoggerFactory.getLogger(PublicationIdNode.class);

	private static final String FRAGMENT_PATH = "//subdoc-bibliographic-information/document-id";
	private static final CountryCode DEFAULT_COUNTRYCODE = CountryCode.US;

	private CountryCode fallbackCountryCode;

	public PublicationIdNode(Document document) {
		this(document, DEFAULT_COUNTRYCODE);
	}

	/**
	 * Constructor
	 * 
	 * @param document
	 * @param fallbackCountryCode - CountryCode used when no country or valid CountryCode is found.
	 */
	public PublicationIdNode(Document document, CountryCode fallbackCountryCode) {
		super(document);
		this.fallbackCountryCode = fallbackCountryCode;
	}

	@Override
	public DocumentId read() {

		Node parentNode = document.selectSingleNode(FRAGMENT_PATH);
		if (parentNode == null) {
			return null;
		}

		Node docNumN = parentNode.selectSingleNode("doc-number");
		if (docNumN == null) {
			LOGGER.error("Invalid document-id can not be Null.");
			return null;
		}

		Node countryN = parentNode.selectSingleNode("country");
		String country = countryN != null ? countryN.getText() : null;

		CountryCode countryCode = fallbackCountryCode;
		if (country != null) {
			try {
				countryCode = CountryCode.fromString(country);
			} catch (InvalidDataException e) {
				LOGGER.info("Invalid CountryCode: {}", country, e);
			}
		}

		Node kindN = parentNode.selectSingleNode("kind-code");
		String kindCode = kindN != null ? kindN.getText() : null;

		DocumentId documentId = new DocumentId(countryCode, docNumN.getText(), kindCode);
		documentId.setType(DocumentIdType.PUBLISHED);

		Node dateN = parentNode.selectSingleNode("document-date");
		if (dateN != null) {
			try {
				documentId.setDate(new DocumentDate(dateN.getText()));
			} catch (InvalidDataException e) {
				LOGGER.error("Failed to parse date from: {}", parentNode.asXML(), e);
			}
		}

		return documentId;
	}

}
