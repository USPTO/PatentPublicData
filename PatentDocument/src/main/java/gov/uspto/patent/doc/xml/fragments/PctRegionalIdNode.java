package gov.uspto.patent.doc.xml.fragments;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.doc.xml.items.DocumentIdNode;
import gov.uspto.patent.model.DocumentId;
import gov.uspto.patent.model.DocumentIdType;

public class PctRegionalIdNode extends DOMFragmentReader<List<DocumentId>> {
    private static final String FRAGMENT_PATH = "//pct-or-regional-filing-data/document-id";
    private static final String FRAGMENT_PATH2 = "//pct-or-regional-publishing-data/document-id";

    public PctRegionalIdNode(Document document) {
        super(document);
    }

    @Override
    public List<DocumentId> read() {
        List<DocumentId> pctDocIds = new ArrayList<DocumentId>();

        Node fragmentNode = document.selectSingleNode(FRAGMENT_PATH);
        if (fragmentNode != null) {
            DocumentId documentId = new DocumentIdNode(fragmentNode).read();
            if (documentId != null) {
                documentId.setType(DocumentIdType.REGIONAL_FILING);
            }

            pctDocIds.add(documentId);
        }

        Node fragmentNode2 = document.selectSingleNode(FRAGMENT_PATH2);
        if (fragmentNode2 != null) {
            DocumentId documentId = new DocumentIdNode(fragmentNode2).read();
            if (documentId != null) {
                documentId.setType(DocumentIdType.REGIONAL_PUBLICATION);
            }
            pctDocIds.add(documentId);
        }

        return pctDocIds;
    }

}
