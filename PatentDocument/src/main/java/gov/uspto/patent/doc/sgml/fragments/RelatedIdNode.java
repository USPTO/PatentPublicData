package gov.uspto.patent.doc.sgml.fragments;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;

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
    private static final String FRAGMENT_PATH = "/PATDOC/SDOBI/B600";

    List<DocumentId> docIds = new ArrayList<DocumentId>();

    public RelatedIdNode(Document document) {
        super(document);
    }

    @Override
    public List<DocumentId> read() {
        Node relatedN = document.selectSingleNode(FRAGMENT_PATH);
        if (relatedN == null) {
            return docIds;
        }

        /*
         * Additional
         */
        List<Node> additionN = relatedN.selectNodes("B610");
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
        List<Node> divitionalNodes = relatedN.selectNodes("B620");
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
        Node continuationN = relatedN.selectSingleNode("B630");

        if (continuationN != null) {
            Node continueN = continuationN.selectSingleNode("B631");
            readChildParent(divitionalNodes, DocumentIdType.CONTINUATION);

            Node continueInPartN = continuationN.selectSingleNode("B632/PARENT-US");
            readChildParent(divitionalNodes, DocumentIdType.CONTINUATION_IN_PART);

            Node continueReIssueN = continuationN.selectSingleNode("B633/PARENT-US");
            readChildParent(divitionalNodes, DocumentIdType.CONTINUATION_REISSUE);
        }

        /*
         * ReIssue
         */
        Node reIssueN = relatedN.selectSingleNode("B640"); // /PARENT-US ?? Whats the nesting..
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
        List<Node> provisionalNodes = relatedN.selectNodes("B680US/DOC");
        for(Node provisionalN: provisionalNodes){
            DocumentId docId = new DocNode(provisionalN).read();
            docId.setType(DocumentIdType.PROVISIONAL);
            docIds.add(docId);
        }

        return docIds;
    }

    private void readChildParent(List<Node> nodes, DocumentIdType docIdType) {
        for (Node node : nodes) {
            Node childDocN = node.selectSingleNode("PARENT-US/CDOC/DOC");
            DocumentId childDocId = new DocNode(childDocN).read();
            childDocId.setType(docIdType);
            docIds.add(childDocId);

            Node parentDocN = node.selectSingleNode("PARENT-US/PDOC/DOC");
            DocumentId parentDocId = new DocNode(childDocN).read();
            parentDocId.setType(docIdType);
            docIds.add(parentDocId);
        }
    }

}
