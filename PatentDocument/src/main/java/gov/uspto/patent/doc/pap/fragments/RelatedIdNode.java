package gov.uspto.patent.doc.pap.fragments;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.doc.pap.items.DocumentIdNode;
import gov.uspto.patent.model.DocumentId;
import gov.uspto.patent.model.DocumentIdType;

public class RelatedIdNode extends DOMFragmentReader<List<DocumentId>> {
	
	private static final XPath RELIDSXP = DocumentHelper.createXPath("/patent-application-publication/subdoc-bibliographic-information/continuity-data");
	private static final XPath CONTINUATION_XP = DocumentHelper.createXPath("continuations/continuation-of/parent-child");
	private static final XPath CONTINUATION_PART_XP = DocumentHelper.createXPath("continuations/continuation-in-part-of/parent-child");
	private static final XPath DIVISION_XP = DocumentHelper.createXPath("division-of/parent-child");
	private static final XPath NONPROVISIONAL_XP = DocumentHelper.createXPath("non-provisional-of-provisional/parent-child");
	private static final XPath PCT_XP = DocumentHelper.createXPath("a-371-of-international/parent-child");
	private static final XPath PARENT_XP = DocumentHelper.createXPath("parent");
	private static final XPath CHILD_XP = DocumentHelper.createXPath("child");

    private List<DocumentId> relatedDocIds;

    public RelatedIdNode(Document document) {
        super(document);
    }

    @Override
    public List<DocumentId> read() {
    	relatedDocIds = new ArrayList<DocumentId>();

        Node fragmentNode = RELIDSXP.selectSingleNode(document);
        if (fragmentNode == null) {
            return relatedDocIds;
        }

        contionationOf(fragmentNode);
        continuationPart(fragmentNode);
        divisionOf(fragmentNode);
        pctFiling(fragmentNode);
        nonProvional(fragmentNode);


        return relatedDocIds;
    }    

    private void contionationOf(Node fragmentNode) {
        List<Node> continuationN = CONTINUATION_XP.selectNodes(fragmentNode);
        for (Node continuation : continuationN) {
            readIds(continuation, DocumentIdType.CONTINUATION);
        }
    }

    private void continuationPart(Node fragmentNode) {
        List<Node> continuationPartN = CONTINUATION_PART_XP.selectNodes(fragmentNode);
        for (Node continuationPart : continuationPartN) {
            readIds(continuationPart, DocumentIdType.CONTINUATION_IN_PART);
        }
    }

    private void divisionOf(Node fragmentNode) {
        List<Node> divisionalN = DIVISION_XP.selectNodes(fragmentNode);
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
        List<Node> nonProvisNodes = NONPROVISIONAL_XP.selectNodes(fragmentNode);
        for (Node nonprov : nonProvisNodes) {
            readIds(nonprov, null);
        }
    }

    private void pctFiling(Node fragmentNode) {
        List<Node> pctNode = PCT_XP.selectNodes(fragmentNode);
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

    private void readIds(Node node, DocumentIdType docIdType) {

        Node childN = CHILD_XP.selectSingleNode(node);
        if (childN != null) {
            DocumentId documentId = new DocumentIdNode(childN).read();
            documentId.setType(docIdType);
            relatedDocIds.add(documentId);
        }

        Node parentN = PARENT_XP.selectSingleNode(node);
        if (parentN != null) {
            DocumentId documentId = new DocumentIdNode(parentN).read();
            documentId.setType(docIdType);
            relatedDocIds.add(documentId);
        }
    }

}
