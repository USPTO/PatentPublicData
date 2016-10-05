package gov.uspto.patent.doc.sgml.fragments;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.doc.sgml.items.DocNode;
import gov.uspto.patent.model.Citation;
import gov.uspto.patent.model.DocumentId;
import gov.uspto.patent.model.NplCitation;
import gov.uspto.patent.model.PatCitation;

/**
 * CitationNode
 * 
 *<pre><code>
 * <!--Non-patent literature citation (include patent applications within the DOC model) -->
 * <!ELEMENT NCIT   (DOC?,STEXT+) >
 * <!--Patent citation-->
 * <!ELEMENT PCIT   (DOC,PARTY-US*,PIC*,PNC*,REL?) >
 * <!--Citations-->
 * <!ELEMENT B560   (B561 | B562)+ >
 * <!ATTLIST B560 INID  CDATA #FIXED "[56]" >
 * <!--Citing a patent document-->
 * <!ELEMENT B561   (PCIT,(CITED-BY-EXAMINER | CITED-BY-OTHER)?) >
 *  <!--Citing non-patent literature-->
 * <!ELEMENT B562   (NCIT,(CITED-BY-EXAMINER | CITED-BY-OTHER)?) >
 *</code><pre>
 *
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class CitationNode extends DOMFragmentReader<List<Citation>> {

	private static final String FRAGMENT_PATH = "/PATDOC/SDOBI/B500/B560";

	public CitationNode(Document document) {
		super(document);
	}

	@Override
	public List<Citation> read() {
		List<Citation> citations = new ArrayList<Citation>();

		Node citationNode = document.selectSingleNode(FRAGMENT_PATH);
		if (citationNode == null){
			return citations;
		}

		@SuppressWarnings("unchecked")
		List<Node> patCiteNodes = citationNode.selectNodes("B561");
		for (int i = 0; i < patCiteNodes.size(); i++) {

			Node citeNode = patCiteNodes.get(i);

			boolean examinerCited = (citeNode.selectSingleNode("CITED-BY-EXAMINER") != null);

			Node citeDoc = citeNode.selectSingleNode("PCIT/DOC");

			DocumentId docId = new DocNode(citeDoc).read();

			citations.add(new PatCitation(String.valueOf(i), docId, examinerCited));
		}

		
		@SuppressWarnings("unchecked")
		List<Node> nplCiteNodes = citationNode.selectNodes("B562");
		for (int i = 0; i < nplCiteNodes.size(); i++) {

			Node citeNode = nplCiteNodes.get(i);

			boolean examinerCited = (citeNode.selectSingleNode("CITED-BY-EXAMINER") != null);

			Node textN = citeNode.selectSingleNode("NCIT/STEXT/PDAT");
			String citeText = textN != null ? textN.getText() : "";
			
			Citation citation = new NplCitation(String.valueOf(i), citeText, examinerCited);
			
			citations.add(citation);
		}

		return citations;
	}

}
