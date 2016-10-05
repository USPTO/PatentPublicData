package gov.uspto.patent.doc.xml.fragments;

import org.dom4j.Document;
import org.dom4j.Node;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.doc.xml.items.DocumentIdNode;
import gov.uspto.patent.model.DocumentId;
import gov.uspto.patent.model.DocumentIdType;

public class RelatedIdNode extends DOMFragmentReader<DocumentId> {
	private static final String FRAGMENT_PATH = "//related-publication/document-id";

	public RelatedIdNode(Document document) {
		super(document);
	}

	@Override
	public DocumentId read() {
		Node fragmentNode = document.selectSingleNode(FRAGMENT_PATH);
		if (fragmentNode == null){
			return null;
		}

		DocumentId documentId = new DocumentIdNode(fragmentNode).read();

		if (documentId != null){
			documentId.setType(DocumentIdType.RELATED_PUBLICATION);
		}

		return documentId;
	}

}
