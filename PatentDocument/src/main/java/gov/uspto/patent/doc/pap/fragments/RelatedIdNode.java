package gov.uspto.patent.doc.pap.fragments;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.doc.pap.items.DocumentIdNode;
import gov.uspto.patent.model.DocumentId;
import gov.uspto.patent.model.DocumentIdType;

public class RelatedIdNode extends DOMFragmentReader<List<DocumentId>> {
    private static final String FRAGMENT_PATH = "/patent-application-publication/subdoc-bibliographic-information/continuity-data";

    private List<DocumentId> relatedDocIds = new ArrayList<DocumentId>();

    public RelatedIdNode(Document document) {
        super(document);
    }

    private void contionationOf(Node fragmentNode) {
        @SuppressWarnings("unchecked")
        List<Node> continuationN = fragmentNode.selectNodes("continuations/continuation-of/parent-child");
        for (Node continuation : continuationN) {
            readIds(continuation, DocumentIdType.CONTINUATION);
        }
    }

    private void continuationPart(Node fragmentNode) {
        @SuppressWarnings("unchecked")
        List<Node> continuationPartN = fragmentNode.selectNodes("continuations/continuation-in-part-of/parent-child");
        for (Node continuationPart : continuationPartN) {
            readIds(continuationPart, DocumentIdType.CONTINUATION_IN_PART);
        }
    }

    private void divisionOf(Node fragmentNode) {
        @SuppressWarnings("unchecked")
        List<Node> divisionalN = fragmentNode.selectNodes("division-of/parent-child");
        for (Node divisional : divisionalN) {
            readIds(divisional, DocumentIdType.DIVISION);

            Node parentPNode = divisional.selectSingleNode("parent-patent");
            if (parentPNode != null) {
                DocumentId documentId = new DocumentIdNode(parentPNode).read();
                relatedDocIds.add(documentId);
            }
        }
    }

    private void nonProvional(Node fragmentNode) {
        @SuppressWarnings("unchecked")
        List<Node> nonProvisNodes = fragmentNode.selectNodes("non-provisional-of-provisional/parent-child");
        for (Node nonprov : nonProvisNodes) {
            readIds(nonprov, null);
        }
    }

    private void pctFiling(Node fragmentNode) {
        @SuppressWarnings("unchecked")
        List<Node> pctNode = fragmentNode.selectNodes("a-371-of-international/parent-child");
        for (Node pctN : pctNode) {

            Node parentStatusNode = pctN.selectSingleNode("parent-status");
            DocumentIdType docIdType = null;
            switch (parentStatusNode.getText()) {
            case "PENDING":
                docIdType = DocumentIdType.REGIONAL_FILING;
                break;
            case "UNKNOWN":
                docIdType = DocumentIdType.REGIONAL_FILING;
                break;
            case "GRANTED":
                docIdType = DocumentIdType.REGIONAL_PUBLICATION;
                break;
            default:
                docIdType = DocumentIdType.REGIONAL_FILING;
            }

            readIds(pctN, docIdType);
        }
    }

    @Override
    public List<DocumentId> read() {
        Node fragmentNode = document.selectSingleNode(FRAGMENT_PATH);
        if (fragmentNode == null) {
            return relatedDocIds;
        }

        contionationOf(fragmentNode);
        continuationPart(fragmentNode);
        divisionOf(fragmentNode);
        pctFiling(fragmentNode);

        //DocumentId documentId = new DocumentIdNode(fragmentNode).read();
        //documentId.setType(DocumentIdType.RELATED_PUBLICATION);

        return relatedDocIds;
    }

    private void readIds(Node node, DocumentIdType docIdType) {

        Node childN = node.selectSingleNode("child");
        if (childN != null) {
            DocumentId documentId = new DocumentIdNode(childN).read();
            documentId.setType(docIdType);
            relatedDocIds.add(documentId);
        }

        Node parentN = node.selectSingleNode("parent");
        if (parentN != null) {
            DocumentId documentId = new DocumentIdNode(parentN).read();
            documentId.setType(docIdType);
            relatedDocIds.add(documentId);
        }
    }

}
