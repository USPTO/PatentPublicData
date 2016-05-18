package gov.uspto.patent.sgml.fragments;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.model.DocumentId;

/**
 * Related Patents or Applications.
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class RelatedIdNode extends DOMFragmentReader<List<DocumentId>> {
	private static final String FRAGMENT_PATH = "/PATDOC/SDOBI/B600";

	public RelatedIdNode(Document document){
		super(document);
	}

	@Override
	public List<DocumentId> read() {

		List<DocumentId> docIds = new ArrayList<DocumentId>();
		
		Node relatedN = document.selectSingleNode(FRAGMENT_PATH);

		/*
		 * Additional
		 */
		Node additionN = relatedN.selectSingleNode("B610/PARTY-US/");

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
		for(Node divitionalN: divitionalNodes){
			divitionalN.selectSingleNode("/PARENT-US/CDOC/DOC/DNUM/PDAT");
		}
		
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
		
		Node continueN = continuationN.selectSingleNode("B631/PARENT-US");
		Node continueInPartN = continuationN.selectSingleNode("B632/PARENT-US");
		Node continueReIssueN = continuationN.selectSingleNode("B633/PARENT-US");

		/*
		 * ReIssue
		 */
		Node reIssueN = relatedN.selectSingleNode("B640"); // /PARENT-US ?? Whats the nesting..
		Node reIssueDivitionalN = relatedN.selectSingleNode("B641US/PARENT-US");
		Node reIssueReExamN = relatedN.selectSingleNode("B645/PARENT-US");
		Node reIssueMergeReExamN = relatedN.selectSingleNode("B645US/PDAT");
		
		Node rePublishN = relatedN.selectSingleNode("B650/DOC");
		Node substituteN = relatedN.selectSingleNode("B660/PARENT-US");
		Node provisionalApplicationN = relatedN.selectSingleNode("B680US/DOC");

		/*
		 * Provisional
		 * 
		 * <B680US>
		 * 		<DOC><DNUM><PDAT>60/049070</PDAT></DNUM><DATE><PDAT>19970711</PDAT></DATE><KIND><PDAT>00</PDAT></KIND></DOC>
		 * </B680US>
		 */
		
		return docIds;
	}

}
