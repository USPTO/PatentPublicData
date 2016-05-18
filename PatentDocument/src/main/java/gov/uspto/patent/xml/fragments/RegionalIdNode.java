package gov.uspto.patent.xml.fragments;

import org.dom4j.Document;
import org.dom4j.Node;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.model.DocumentId;
import gov.uspto.patent.model.DocumentIdType;
import gov.uspto.patent.xml.items.DocumentIdNode;

public class RegionalIdNode extends DOMFragmentReader<DocumentId> {
	private static final String FRAGMENT_PATH = "//pct-or-regional-filing-data/document-id";
	private static final String FRAGMENT_PATH2 = "//pct-or-regional-publishing-data/document-id";

	public RegionalIdNode(Document document) {
		super(document);
	}

	@Override
	public DocumentId read() {

		DocumentId documentId = null;

		Node fragmentNode = document.selectSingleNode(FRAGMENT_PATH);
		if (fragmentNode != null) {
			documentId = new DocumentIdNode(fragmentNode).read();
			if (documentId != null){
				documentId.setType(DocumentIdType.REGIONAL_FILING);
			}
			
			return documentId;
		}


		Node fragmentNode2 = document.selectSingleNode(FRAGMENT_PATH2);
		if (fragmentNode2 != null) {
			documentId = new DocumentIdNode(fragmentNode2).read();
			if (documentId != null){
				documentId.setType(DocumentIdType.REGIONAL_PUBLICATION);
			}
		}

		return documentId;
	}
	
}
