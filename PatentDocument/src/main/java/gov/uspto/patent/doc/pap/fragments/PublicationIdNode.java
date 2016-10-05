package gov.uspto.patent.doc.pap.fragments;

import org.dom4j.Document;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.doc.pap.items.DocumentIdNode;
import gov.uspto.patent.model.DocumentId;
import gov.uspto.patent.model.DocumentIdType;

public class PublicationIdNode extends DOMFragmentReader<DocumentId> {
	private static final Logger LOGGER = LoggerFactory.getLogger(PublicationIdNode.class);

	private static final String FRAGMENT_PATH = "//subdoc-bibliographic-information";

	public PublicationIdNode(Document document) {
	    super(document);
	}

    @Override
    public DocumentId read() {
        Node parentNode = document.selectSingleNode(FRAGMENT_PATH);
        if (parentNode == null){
            LOGGER.warn("Patent does not have an Application ID.");
            return null;
        }

        DocumentId documentId = new DocumentIdNode(parentNode).read();
        if (documentId != null){
            documentId.setType(DocumentIdType.PUBLISHED);
        }

        return documentId;
    }

}
