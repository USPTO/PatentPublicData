package gov.uspto.patent.xml.fragments;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.model.Citation;
import gov.uspto.patent.model.CitationType;
import gov.uspto.patent.model.DocumentId;
import gov.uspto.patent.model.classification.Classification;
import gov.uspto.patent.xml.items.ClassificationNationalNode;
import gov.uspto.patent.xml.items.DocumentIdNode;

public class CitationNode extends DOMFragmentReader<List<Citation>> {
	private static final String FRAGMENT_PATH = "//us-references-cited/us-citation/patcit"; // current us-patent-grants. 

	private final static String FRAGMENT_PATH2 = "//references-cited/citation/patcit"; // pre 2012 us-patent-grants.

	private List<Citation> citations;

	public CitationNode(Document document) {
		super(document);
	}

	@Override
	public List<Citation> read() {
		citations = new ArrayList<Citation>();

		@SuppressWarnings("unchecked")
		List<Node> patcitNodes = document.selectNodes(FRAGMENT_PATH);
		readCitations(patcitNodes);

		@SuppressWarnings("unchecked")
		List<Node> patcitNodes2 = document.selectNodes(FRAGMENT_PATH2);
		readCitations(patcitNodes2);

		return citations;
	}

	private void readCitations(List<Node> patcitNodes) {
		for (Node patcit : patcitNodes) {
			Citation citation = readCitation(patcit);
			citations.add(citation);
		}
	}

	public Citation readCitation(Node citeNode) {
		String num = citeNode.selectSingleNode("@num").getText();

		DocumentId documentId = new DocumentIdNode(citeNode).read();

		// <category>cited by examiner</category>
		Node category = citeNode.getParent().selectSingleNode("category");
		boolean examinerCited = (category.getText().equals("cited by examiner"));

		Citation citation = new Citation(num, CitationType.PATCIT, documentId, examinerCited);

		Classification mainClassification = new ClassificationNationalNode(citeNode.getParent()).read();
		citation.setClassification(mainClassification);

		return citation;
	}
}
