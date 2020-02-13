package gov.uspto.patent.doc.xml.fragments;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.doc.xml.items.RelationNode;
import gov.uspto.patent.model.DocumentId;
import gov.uspto.patent.model.DocumentIdType;

/**
 * 
 * Cross Reference to related Applications or Publications.
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class Relations extends DOMFragmentReader<List<DocumentId>> {
	// us-divisional-reissue/us-relation

	private static final String PARENT_PATH = "/*/*/us-related-documents";

	private static final String ADDITION = "addition/relation";
	private static final String CONTINUATION = "continuation/relation";
	private static final String CONTINUATION_IN_PART = "continuation-in-part/relation";
	private static final String CONTINUATION_REISSUE = "continuing-reissue/relation";
	private static final String DIVISION = "division/relation";

	private static final String REEXAMINATION = "reexamination/relation";
	private static final String REISSUE = "reissue/relation";
	private static final String SUBSITUTION = "substitution/relation";
	private static final String USREEX = "us-reexamination-reissue-merger/relation";
	private static final String UTILITY_MODEL = "utility-model-basis/relation";

	private List<DocumentId> docIds;

	public Relations(Document document) {
		super(document);
	}

	@Override
	public List<DocumentId> read() {
		docIds = new ArrayList<DocumentId>();

		Node parentNode = document.selectSingleNode(PARENT_PATH);
		if (parentNode == null) {
			return docIds;
		}

		getDocIds(parentNode, ADDITION, DocumentIdType.ADDITION);

		getDocIds(parentNode, CONTINUATION, DocumentIdType.CONTINUATION);

		getDocIds(parentNode, CONTINUATION_IN_PART, DocumentIdType.CONTINUATION_IN_PART);

		getDocIds(parentNode, CONTINUATION_REISSUE, DocumentIdType.CONTINUATION_REISSUE);

		getDocIds(parentNode, DIVISION, DocumentIdType.DIVISION);

		getDocIds(parentNode, REEXAMINATION, DocumentIdType.REEXAMINATION);

		getDocIds(parentNode, REISSUE, DocumentIdType.REISSUE);

		getDocIds(parentNode, SUBSITUTION, DocumentIdType.SUBSITUTION);

		getDocIds(parentNode, USREEX, DocumentIdType.USREEX);

		getDocIds(parentNode, UTILITY_MODEL, DocumentIdType.UTILITY_MODEL);

		return docIds;
	}

	public void getDocIds(Node parentNode, String xmlPath, DocumentIdType docIdType) {
		Node node = parentNode.selectSingleNode(xmlPath);
		if (node != null) {
			docIds.addAll(new RelationNode(node, docIdType).read());
		}
	}

}
