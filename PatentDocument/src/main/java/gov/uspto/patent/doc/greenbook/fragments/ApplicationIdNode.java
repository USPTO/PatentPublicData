package gov.uspto.patent.doc.greenbook.fragments;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.DocumentDate;
import gov.uspto.patent.model.DocumentId;

public class ApplicationIdNode extends DOMFragmentReader<DocumentId> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationIdNode.class);
	
	private static final XPath PATXP = DocumentHelper.createXPath("/DOCUMENT/PATN");
	private static final XPath APNUMXP = DocumentHelper.createXPath("APN");
	private static final XPath APDATEXP = DocumentHelper.createXPath("APD");

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
		Node parentNode = PATXP.selectSingleNode(document);
		
		Node docNumN = APNUMXP.selectSingleNode(parentNode);
		if (docNumN == null) {
			LOGGER.warn("Invalid application-id 'APN' field not found");
			return null;
		}

		String docId = docNumN.getText().trim();
		if (docId.endsWith("&")) {
			docId = docId.substring(0, docId.length()-1);
			// LOGGER.warn("!!!!!!!!!!!!!! Application ID ends with & '{}' -> '{}'", docNumN.getText(), docId);
		}

		DocumentId documentId = new DocumentId(defaultCountryCode, docId);
		documentId.setRawText(docNumN.getText().trim());

		Node dateN = APDATEXP.selectSingleNode(parentNode);
		if (dateN != null) {
			try {
				documentId.setDate(new DocumentDate(dateN.getText().trim()));
			} catch (InvalidDataException e) {
				LOGGER.warn("{} : {}", e.getMessage(), dateN.getParent().asXML());
			}
		}

		return documentId;
	}
}
