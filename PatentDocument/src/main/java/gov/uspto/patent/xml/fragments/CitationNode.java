package gov.uspto.patent.xml.fragments;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.model.Citation;
import gov.uspto.patent.model.DocumentId;
import gov.uspto.patent.model.NplCitation;
import gov.uspto.patent.model.PatCitation;
import gov.uspto.patent.model.classification.Classification;
import gov.uspto.patent.xml.items.ClassificationNationalNode;
import gov.uspto.patent.xml.items.DocumentIdNode;

public class CitationNode extends DOMFragmentReader<List<Citation>> {
	
	private static final String FRAGMENT_PATH = "//us-references-cited|//references-cited"; // current us-patent-grants. 

	private Node citationNode;

	public CitationNode(Document document) {
		super(document);
	}

	@Override
	public List<Citation> read() {

		citationNode = document.selectSingleNode(FRAGMENT_PATH);

		List<Citation> citations = new ArrayList<Citation>();
		List<Citation> patCitations = readPatCitations();
		List<Citation> nplCitations = readNplCitations();

		citations.addAll(patCitations);
		citations.addAll(nplCitations);

		return citations;
	}

	public List<Citation> readNplCitations() {
		List<Citation> nplCitations = new ArrayList<Citation>();

		@SuppressWarnings("unchecked")
		List<Node> nlpcitNodes =  citationNode.selectNodes("us-citation/nplcite|citation/nplcit");

		for (Node nplcit : nlpcitNodes) {
			
			String num = nplcit.selectSingleNode("@num").getText();
			Node citeTxtN = nplcit.selectSingleNode("othercit");
			
			String citeTxt = citeTxtN != null ? citeTxtN.getText() : "";

			// <category>cited by examiner</category>
			Node category = nplcit.getParent().selectSingleNode("category");
			boolean examinerCited = (category.getText().equals("cited by examiner"));

			Citation citation = new NplCitation(num, citeTxt, examinerCited);
			nplCitations.add(citation);
		}

		return nplCitations;
	}

	public List<Citation> readPatCitations() {
		List<Citation> patCitations = new ArrayList<Citation>();

		@SuppressWarnings("unchecked")
		List<Node> patcitNodes = citationNode.selectNodes("citation/patcit|us-citation/patcit");
		for (Node patcit : patcitNodes) {
			String num = patcit.selectSingleNode("@num").getText();

			DocumentId documentId = new DocumentIdNode(patcit).read();

			// <category>cited by examiner</category>
			Node category = patcit.getParent().selectSingleNode("category");
			boolean examinerCited = (category.getText().equals("cited by examiner"));

			PatCitation citation = new PatCitation(num, documentId, examinerCited);

			Classification mainClassification = new ClassificationNationalNode(patcit.getParent()).read();
			citation.setClassification(mainClassification);
			patCitations.add(citation);
		}

		return patCitations;
	}
}
