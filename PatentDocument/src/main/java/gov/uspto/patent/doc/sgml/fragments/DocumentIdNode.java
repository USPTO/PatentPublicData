package gov.uspto.patent.doc.sgml.fragments;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.CountryCodeHistory;
import gov.uspto.patent.model.DocumentDate;
import gov.uspto.patent.model.DocumentId;

public class DocumentIdNode extends DOMFragmentReader<DocumentId> {

	private static final Logger LOGGER = LoggerFactory.getLogger(DocumentIdNode.class);

	private static final XPath PARENTXP = DocumentHelper.createXPath("/PATDOC/SDOBI/B100");
	private static final XPath DOCNUMXP = DocumentHelper.createXPath("B110/DNUM/PDAT");
	private static final XPath KINDXP = DocumentHelper.createXPath("B130/PDAT");
	private static final XPath COUNTRYXP = DocumentHelper.createXPath("B190/PDAT");
	private static final XPath DATEXP = DocumentHelper.createXPath("B140/DATE/PDAT");

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

		Node parentNode = PARENTXP.selectSingleNode(document);
		if (parentNode == null) {
			return null;
		}

		Node docNumN = DOCNUMXP.selectSingleNode(parentNode);
		if (docNumN == null) {
			LOGGER.warn("Patent does not have a document-id.");
			return null;
		}

		Node kindN = KINDXP.selectSingleNode(parentNode);
		String kindCode = kindN != null ? kindN.getText() : null;

		Node countryN = COUNTRYXP.selectSingleNode(parentNode);
		String countryStr = countryN != null ? countryN.getText() : null;

		Node dateN = DATEXP.selectSingleNode(parentNode);
		DocumentDate docDate = null;
		if (dateN == null || (dateN != null && dateN.getText().trim().isEmpty())) {
			LOGGER.warn("Invalid Date, Empty or Missing : {}", parentNode.asXML());
		} else {
			try {
				docDate = new DocumentDate(dateN.getText());
			} catch (InvalidDataException e) {
				LOGGER.warn("{} : {}", e.getMessage(), parentNode.asXML());
			}
		}

		CountryCode countryCode = fallbackCountryCode;
		try {
			countryCode = CountryCode.fromString(countryStr);
		} catch (InvalidDataException e2) {
			if (docDate != null) {
				countryCode = CountryCodeHistory.getCurrentCode(countryStr, docDate.getYear());
				if (countryCode == CountryCode.UNKNOWN) {
					LOGGER.warn("{} : {}", e2.getMessage(), parentNode.asXML());
				} else {
					LOGGER.debug("Historic Country Code Matched '{}' yr {} : '{}'", countryStr, docDate.getYear(), countryCode);
				}
			}
			else {
				LOGGER.warn("{} : {}", e2.getMessage(), parentNode.asXML());
			}
		}

		String docId = docNumN.getText();

		DocumentId documentId = new DocumentId(countryCode, docId, kindCode);
		documentId.setRawText(docNumN.getText());
		if (docDate != null) {
			documentId.setDate(docDate);
		}

		return documentId;
	}

}
