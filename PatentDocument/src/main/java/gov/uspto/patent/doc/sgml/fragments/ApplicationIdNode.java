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

		Node patentNode = document.selectSingleNode("/PATDOC/SDOBI/B200");
		if (patentNode == null) {
			return null;
		}

		Node docNumN = patentNode.selectSingleNode("B210/DNUM/PDAT");
		if (docNumN == null) {
			LOGGER.warn("Patent dos not have an Application document-id");
			return null;
		}

		//Node seriesCodeN = patentNode.selectSingleNode("B211US/PDAT/PDAT");
		//String seriesCode = seriesCodeN != null ? seriesCodeN.getText() : null;

		DocumentId documentId = new DocumentId(defaultCountryCode, docNumN.getText(), null);

		Node dateN = patentNode.selectSingleNode("B220/DATE/PDAT");
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
