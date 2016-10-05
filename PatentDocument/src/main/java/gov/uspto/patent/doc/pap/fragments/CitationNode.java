package gov.uspto.patent.doc.pap.fragments;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.model.Citation;

/**
 * 
 *<pre><code>
 * <!ELEMENT citation  ((cited-patent-literature | cited-non-patent-literature), relevant-section?) >
 * <!ELEMENT cited-patent-literature  (document-id,party*,classification-ipc?, classification-us?) >
 * <!ELEMENT cited-non-patent-literature  (#PCDATA | custom-character | highlight)* >
 * <code><pre>
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class CitationNode extends DOMFragmentReader<List<Citation>> {
	private static final String PATENT_PATH = "//citation";

	public CitationNode(Document document) {
		super(document);
	}

	@Override
	public List<Citation> read() {
		List<Citation> citations = new ArrayList<Citation>();

		@SuppressWarnings("unchecked")
		List<Node> citeNodes = document.selectNodes(PATENT_PATH);
		for (Node citeNode : citeNodes) {
			Node patCiteN = citeNode.selectSingleNode("cited-patent-literature");
			
			Node nplCiteN = citeNode.selectSingleNode("cited-non-patent-literature");
			
		}

		return citations;
	}


}
