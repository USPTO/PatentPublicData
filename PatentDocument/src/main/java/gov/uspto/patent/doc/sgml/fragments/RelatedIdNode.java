package gov.uspto.patent.doc.sgml.fragments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.doc.sgml.items.DocNode;
import gov.uspto.patent.model.DocumentId;
import gov.uspto.patent.model.DocumentIdType;

/**
 * Related Patents or Applications.
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class RelatedIdNode extends DOMFragmentReader<List<DocumentId>> {

	private static final XPath RELIDSXP = DocumentHelper.createXPath("/PATDOC/SDOBI/B600");
	private static final XPath ADDITIONALXP = DocumentHelper.createXPath("B610");
	private static final XPath DIVISIONALXP = DocumentHelper.createXPath("B620");
	private static final XPath CONTINUATIONXP = DocumentHelper.createXPath("B630");
	private static final XPath REISSUEXP = DocumentHelper.createXPath("B640");
	private static final XPath PROVISIONALXP = DocumentHelper.createXPath("B680US/DOC");

	private static final XPath CHILDDOCXP = DocumentHelper.createXPath("PARENT-US/CDOC/DOC");
	private static final XPath PARENTDOCXP = DocumentHelper.createXPath("PARENT-US/PDOC/DOC");

	
    List<DocumentId> docIds = new ArrayList<DocumentId>();

    public RelatedIdNode(Document document) {
        super(document);
    }

    @Override
    public List<DocumentId> read() {
        Node relatedN = RELIDSXP.selectSingleNode(document);
        if (relatedN == null) {
            return docIds;
        }

        /*
         * Additional
         */
        List<Node> additionN = ADDITIONALXP.selectNodes(relatedN);
        readChildParent(additionN, DocumentIdType.ADDITION);

        /*
         * Divisional
         * 
         * <B620><PARENT-US>
         * 		<CDOC><DOC><DNUM><PDAT>09/590050</PDAT></DNUM></DOC></CDOC>
         * 		<PDOC><DOC><DNUM><PDAT>09/249710</PDAT></DNUM><DATE><PDAT>19990212</PDAT></DATE><CTRY><PDAT>US</PDAT></CTRY><KIND><PDAT>00</PDAT></KIND></DOC></PDOC><PSTA><PDAT>00</PDAT></PSTA>
         * </PARENT-US></B620>
         * 
         */
        // 
        List<Node> divitionalNodes = DIVISIONALXP.selectNodes(relatedN);
        readChildParent(divitionalNodes, DocumentIdType.DIVISION);

        /*
         * Continuations
         * 
         * <B630><B631><PARENT-US>
         * 		<CDOC><DOC><DNUM><PDAT>09/413215</PDAT></DNUM></DOC></CDOC>
         * 		<PDOC><DOC><DNUM><PDAT>PCT/NO98/00107</PDAT></DNUM><DATE><PDAT>19980402</PDAT></DATE><CTRY><PDAT>US</PDAT></CTRY><KIND><PDAT>00</PDAT></KIND></DOC></PDOC><PSTA><PDAT>00</PDAT></PSTA>
         * </PARENT-US></B631></B630>
         * 
         */
        Node continuationN = CONTINUATIONXP.selectSingleNode(relatedN);

        if (continuationN != null) {
            Node continueN = continuationN.selectSingleNode("B631");
            readChildParent(continueN, DocumentIdType.CONTINUATION);

            Node continueInPartN = continuationN.selectSingleNode("B632/PARENT-US");
            readChildParent(continueInPartN, DocumentIdType.CONTINUATION_IN_PART);

            Node continueReIssueN = continuationN.selectSingleNode("B633/PARENT-US");
            readChildParent(continueReIssueN, DocumentIdType.CONTINUATION_REISSUE);
        }

        /*
         * ReIssue
         */
        Node reIssueN = REISSUEXP.selectSingleNode(relatedN); // /PARENT-US ?? Whats the nesting..
        if (reIssueN != null){
            Node reIssueDivitionalN = relatedN.selectSingleNode("B641US/PARENT-US");
            Node reIssueReExamN = relatedN.selectSingleNode("B645/PARENT-US");
            Node reIssueMergeReExamN = relatedN.selectSingleNode("B645US/PDAT");
    
            Node rePublishN = relatedN.selectSingleNode("B650/DOC");
            Node substituteN = relatedN.selectSingleNode("B660/PARENT-US");
            Node provisionalApplicationN = relatedN.selectSingleNode("B680US/DOC");
        }
        
        /*
         * Provisional
         * 
         * <B680US>
         * 		<DOC><DNUM><PDAT>60/049070</PDAT></DNUM><DATE><PDAT>19970711</PDAT></DATE><KIND><PDAT>00</PDAT></KIND></DOC>
         * </B680US>
         */
        List<Node> provisionalNodes = PROVISIONALXP.selectNodes(relatedN);
        for(Node provisionalN: provisionalNodes){
            DocumentId docId = new DocNode(provisionalN).read();
            docId.setType(DocumentIdType.PROVISIONAL);
            docIds.add(docId);
        }

        return docIds;
    }

    private void readChildParent(Node node, DocumentIdType docIdType) {
        if (node != null) {
            readChildParent(Collections.singletonList(node), docIdType);
        }
    }

    private void readChildParent(List<Node> nodes, DocumentIdType docIdType) {
        for (Node node : nodes) {
            Node childDocN = CHILDDOCXP.selectSingleNode(node);
            if (childDocN != null) {
	            DocumentId childDocId = new DocNode(childDocN).read();
	            childDocId.setType(docIdType);
	            docIds.add(childDocId);
            }

            Node parentDocN = PARENTDOCXP.selectSingleNode(node);
            if (parentDocN != null) {
            	DocumentId parentDocId = new DocNode(parentDocN).read();
            	parentDocId.setType(docIdType);
            	docIds.add(parentDocId);
            }
        }
    }

}
