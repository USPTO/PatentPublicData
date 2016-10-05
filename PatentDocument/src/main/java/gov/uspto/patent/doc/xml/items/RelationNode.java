package gov.uspto.patent.doc.xml.items;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Node;

import gov.uspto.parser.dom4j.ItemReader;
import gov.uspto.patent.model.DocumentId;
import gov.uspto.patent.model.DocumentIdType;

/*
 * Used by:
 * 		addition
 * 		continuation
 * 		continuation-in-part
 * 		continuing-reissue
 * 		division
 * 		reexamination	
 * 		reissue
 * 		substitution
 * 		us-reexamination-reissue-merger
 * 		utility-model-basis
 * 
 * 		us-divisional-reissue/us-relation
 */

public class RelationNode extends ItemReader<List<DocumentId>> {
	private static final String ITEM_NODE_NAME = "relation";
	private static final String REL_PARENT = "parent-doc/document-id";
	private static final String REL_CHILD = "child-doc/document-id";

	private final DocumentIdType docIdType;
	
	public RelationNode(Node itemNode, DocumentIdType docIdType){
		super(itemNode, ITEM_NODE_NAME);
		this.docIdType = docIdType;
	}

	@Override
	public List<DocumentId> read() {
		List<DocumentId> docIds = new ArrayList<DocumentId>();
		
		Node parentN = itemNode.selectSingleNode(REL_PARENT);
		DocumentId parentDocId = new DocumentIdNode(parentN).read();
		parentDocId.setType(docIdType);
		docIds.add(parentDocId);

		Node childN = itemNode.selectSingleNode(REL_CHILD);
		if (childN != null){
			DocumentId childDocId = new DocumentIdNode(childN).read();
			childDocId.setType(docIdType);
			if (!childDocId.equals(parentDocId)){
				docIds.add(childDocId);
			}
		}

		return docIds;
	}
}
