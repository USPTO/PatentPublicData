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
		Node docNumN = document.selectSingleNode("/DOCUMENT/PATN/WKU");
		if (docNumN == null) {
			LOGGER.warn("Invalid document-id, field 'WKU' not found: {}", document.asXML());
			return null;
		}

		String patNum = docNumN.getText().substring(1, 8);
		DocumentId documentId = new DocumentId(fallbackCountryCode, patNum);

		Node dateN = document.selectSingleNode("/DOCUMENT/PATN/ISD");
		if (dateN != null) {
			String dateTxt = dateN.getText();
			try {
				documentId.setDate(new DocumentDate(dateTxt));
			} catch (InvalidDataException e) {
				LOGGER.warn("Failed to parse date: {}", dateTxt, e);
			}
		}

		return documentId;
	}

}
