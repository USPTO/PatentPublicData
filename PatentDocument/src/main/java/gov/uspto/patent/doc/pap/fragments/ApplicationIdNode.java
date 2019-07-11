package gov.uspto.patent.doc.pap.fragments;

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
import gov.uspto.patent.model.DocumentIdType;

public class ApplicationIdNode extends DOMFragmentReader<DocumentId> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationIdNode.class);

	private static final XPath FILING_XP = DocumentHelper.createXPath("/patent-application-publication/subdoc-bibliographic-information/domestic-filing-data");
	private static final XPath DOCID_XP = DocumentHelper.createXPath("application-number/doc-number");
	private static final XPath DATE_XP = DocumentHelper.createXPath("filing-date");

	private static final CountryCode DEFAULT_COUNTRYCODE = CountryCode.US;

	private CountryCode countryCode;

	public ApplicationIdNode(Document document) {
		this(document, DEFAULT_COUNTRYCODE);
	}

	public ApplicationIdNode(Document document, CountryCode countryCode) {
		super(document);
		this.countryCode = countryCode;
	}

	@Override
	public DocumentId read() {
		Node parentNode = FILING_XP.selectSingleNode(document);
		
		Node docNumN = DOCID_XP.selectSingleNode(parentNode);
		if (docNumN == null) {
			LOGGER.warn("Patent does not have an Application document-id.");
			return null;
		}

		Node appDateN = DATE_XP.selectSingleNode(parentNode);
		DocumentDate appDate = null;
		if (appDateN != null) {
			try {
				appDate = new DocumentDate(appDateN.getText());
			} catch (InvalidDataException e) {
				LOGGER.warn("Failed parsing application filling date.");
			}
		}

		DocumentId documentId = new DocumentId(countryCode, docNumN.getText());
		documentId.setType(DocumentIdType.APPLICATION);
		documentId.setDate(appDate);
		return documentId;
	}

}
