package gov.uspto.patent.doc.xml.fragments;

import org.dom4j.Document;
import org.dom4j.Node;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.doc.xml.items.DocumentIdNode;
import gov.uspto.patent.model.DocumentId;
import gov.uspto.patent.model.DocumentIdType;

public class PublicationIdNode extends DOMFragmentReader<DocumentId> {
	private static final String FRAGMENT_PATH = "//publication-reference/document-id";

	public PublicationIdNode(Document document) {
		super(document);
	}

	@Override
	public DocumentId read() {
		Node parentNode = document.selectSingleNode(FRAGMENT_PATH);
		if (parentNode == null){
			return null;
		}
		
		DocumentId documentId = new DocumentIdNode(parentNode).read();

		if (documentId != null) {
			documentId.setType(DocumentIdType.PUBLISHED);
		}

		return documentId;
	}

}
