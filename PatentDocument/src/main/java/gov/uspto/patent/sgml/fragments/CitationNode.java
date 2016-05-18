package gov.uspto.patent.sgml.fragments;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.model.Citation;
import gov.uspto.patent.model.CitationType;
import gov.uspto.patent.model.DocumentId;
import gov.uspto.patent.sgml.items.DocNode;

public class CitationNode extends DOMFragmentReader<List<Citation>> {

	private static final String FRAGMENT_PATH = "/PATDOC/SDOBI/B500/B560/B561";

	public CitationNode(Document document) {
		super(document);
	}

	@Override
	public List<Citation> read() {
		List<Citation> citations = new ArrayList<Citation>();

		@SuppressWarnings("unchecked")
		List<Node> citeNodes = document.selectNodes(FRAGMENT_PATH);
		for (int i = 0; i < citeNodes.size(); i++) {

			Node citeNode = citeNodes.get(i);

			boolean examinerCited = ( citeNode.selectSingleNode( "CITED-BY-EXAMINER" ) != null ); 

			Node citeDoc = citeNode.selectSingleNode("PCIT/DOC");

			DocumentId docId = new DocNode(citeDoc).read();

			citations.add(new Citation(String.valueOf(i), CitationType.PATCIT, docId, examinerCited));
		}

		return citations;
	}

}
