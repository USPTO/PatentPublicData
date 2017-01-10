package gov.uspto.patent.doc.greenbook.fragments;

import org.dom4j.Document;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.DocumentDate;
import gov.uspto.patent.model.DocumentId;

public class ApplicationIdNode extends DOMFragmentReader<DocumentId> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationIdNode.class);

	private static final CountryCode DEFAULT_COUNTRYCODE = CountryCode.US;
	private CountryCode defaultCountryCode;

	public ApplicationIdNode(Document document) {
		this(document, DEFAULT_COUNTRYCODE);
	}

	public ApplicationIdNode(Document document, CountryCode defaultCountryCode) {
		super(document);
		this.defaultCountryCode = defaultCountryCode;
	}

	@Override
	public DocumentId read() {
		Node docNumN = document.selectSingleNode("/DOCUMENT/PATN/APN");
		if (docNumN == null) {
			LOGGER.warn("Invalid application-id 'APN' field not found");
			return null;
		}

		DocumentId documentId = new DocumentId(defaultCountryCode, docNumN.getText());

		Node dateN = document.selectSingleNode("/DOCUMENT/PATN/APD");
		if (dateN != null) {
			String dateTxt = dateN.getText();
			try {
				documentId.setDate(new DocumentDate(dateN.getText()));
			} catch (InvalidDataException e) {
				LOGGER.warn("Failed to parse date: '{}'", dateTxt, e);
			}
		}

		return documentId;
	}
}
