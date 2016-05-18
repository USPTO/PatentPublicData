package gov.uspto.patent.pap.fragments;

import org.dom4j.Document;
import org.dom4j.Node;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.model.DocumentId;
import gov.uspto.patent.model.DocumentIdType;
import gov.uspto.patent.xml.items.DocumentIdNode;

public class RelatedIdNode extends DOMFragmentReader<DocumentId> {
	private static final String FRAGMENT_PATH = "/patent-application-publication/subdoc-bibliographic-information/continuity-data";

	public RelatedIdNode(Document document) {
		super(document);
	}

	@Override
	public DocumentId read() {
		Node fragmentNode = document.selectSingleNode(FRAGMENT_PATH);
		if (fragmentNode == null) {
			return null;
		}

		DocumentId documentId = new DocumentIdNode(fragmentNode).read();
		documentId.setType(DocumentIdType.RELATED_PUBLICATION);

		return documentId;
	}

}
