package gov.uspto.patent.doc.pap.fragments;

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

public class ApplicationIdNode extends DOMFragmentReader<DocumentId> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationIdNode.class);

    private static final String FRAGMENT_DOCNUM = "//domestic-filing-data/application-number/doc-number";
    private static final String FRAGMENT_DATE = "//domestic-filing-data/filing-date";

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
        Node docNumN = document.selectSingleNode(FRAGMENT_DOCNUM);
        if (docNumN == null) {
            LOGGER.warn("Patent does not have an Application document-id.");
            return null;
        }

        Node appDateN = document.selectSingleNode(FRAGMENT_DATE);
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
