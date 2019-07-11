package gov.uspto.patent.doc.pap.fragments;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.doc.pap.items.DocumentIdNode;
import gov.uspto.patent.model.DocumentId;
import gov.uspto.patent.model.DocumentIdType;

public class PublicationIdNode extends DOMFragmentReader<DocumentId> {
	private static final Logger LOGGER = LoggerFactory.getLogger(PublicationIdNode.class);

	private static final XPath DOCIDXP = DocumentHelper
			.createXPath("/patent-application-publication/subdoc-bibliographic-information/document-id");

	public PublicationIdNode(Document document) {
		super(document);
	}

	@Override
	public DocumentId read() {
		Node parentNode = DOCIDXP.selectSingleNode(document);
		if (parentNode == null) {
			LOGGER.warn("Patent does not have an Application ID.");
			return null;
		}

		DocumentId documentId = new DocumentIdNode(parentNode).read();
		if (documentId != null) {
			documentId.setType(DocumentIdType.PUBLISHED);
		}

		return documentId;
	}

}
