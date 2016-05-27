package gov.uspto.patent.sgml.items;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Node;

import gov.uspto.parser.dom4j.ItemReader;
import gov.uspto.patent.model.DocumentId;
/**
 * Parent-US Node
 *
 *<li>DNUM PDAT Document number
 *<li>CDOC DOC Child document
 *<li>PDOC DOC Parent document
 *<li>PSTA PDAT Parent application status
 *<li>PPUB DOC Patent associated with parent document
 * <PARENT-US>
 * 		<CDOC>
 * 			<DOC><DNUM><PDAT>09/590050</PDAT></DNUM></DOC>
 * 		</CDOC>
 * 		<PDOC>
 * 			<DOC><DNUM><PDAT>09/249710</PDAT></DNUM><DATE><PDAT>19990212</PDAT></DATE><CTRY><PDAT>US</PDAT></CTRY><KIND><PDAT>00</PDAT></KIND></DOC>
 * 		</PDOC>
 * 		<PSTA><PDAT>00</PDAT></PSTA>
 * </PARENT-US>
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class ParentUs extends ItemReader<List<DocumentId>> {

	public ParentUs(Node itemNode) {
		super(itemNode);
	}

	/**
	 * @TODO Should we mark which documents are Parents and which are Child? Currently just capturing the IDs.
	 */
	@Override
	public List<DocumentId> read() {
		List<DocumentId> docs = new ArrayList<DocumentId>();

		Node childDocN = itemNode.selectSingleNode("CDOC/DOC");
		if (childDocN != null){
			DocumentId childDocId = new DocNode(childDocN).read();
			docs.add(childDocId);
		}

		Node parentDocN = itemNode.selectSingleNode("PDOC/DOC");
		if (parentDocN != null){
			DocumentId docId = new DocNode(parentDocN).read();
			docs.add(docId);
		}

		//itemNode.selectSingleNode("PSTA/PDAT");
		//itemNode.selectSingleNode("PPUB/DOC");

		return docs;
	}
}
