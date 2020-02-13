package gov.uspto.patent.doc.sgml.fragments;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.doc.sgml.items.DocNode;
import gov.uspto.patent.model.Citation;
import gov.uspto.patent.model.DocumentId;
import gov.uspto.patent.model.NplCitation;
import gov.uspto.patent.model.PatCitation;
import gov.uspto.patent.model.Citation.CitedBy;

/**
 * CitationNode
 * 
 * <pre>
 * <code>
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
 *</code>
 * 
 * <pre>
 *
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class CitationNode extends DOMFragmentReader<List<Citation>> {

	private static final XPath PARENTXP = DocumentHelper.createXPath("/PATDOC/SDOBI/B500/B560");
	private static final XPath PATCITEXP = DocumentHelper.createXPath("B561");
	private static final XPath NPLCITEXP = DocumentHelper.createXPath("B562");
	private static final XPath CITEBYXP = DocumentHelper.createXPath("CITED-BY-EXAMINER");
	private static final XPath PATNUMXP = DocumentHelper.createXPath("PCIT/DOC");
	private static final XPath NPLTXTXP = DocumentHelper.createXPath("NCIT/STEXT/PDAT");
	
	public CitationNode(Document document) {
		super(document);
	}

	@Override
	public List<Citation> read() {
		List<Citation> citations = new ArrayList<Citation>();

		Node citationNode = PARENTXP.selectSingleNode(document);
		if (citationNode == null) {
			return citations;
		}

		List<Node> patCiteNodes = PATCITEXP.selectNodes(citationNode);
		for (int i = 0; i < patCiteNodes.size(); i++) {

			Node citeNode = patCiteNodes.get(i);

			CitedBy citedBy = getCitedBy(CITEBYXP.selectSingleNode(citeNode));

			Node citeDoc = PATNUMXP.selectSingleNode(citeNode);

			DocumentId docId = new DocNode(citeDoc).read();

			citations.add(new PatCitation(String.valueOf(i), docId, citedBy));
		}

		List<Node> nplCiteNodes = NPLCITEXP.selectNodes(citationNode);
		for (int i = 0; i < nplCiteNodes.size(); i++) {

			Node citeNode = nplCiteNodes.get(i);

			CitedBy citedBy = getCitedBy(CITEBYXP.selectSingleNode(citeNode));

			Node textN = NPLTXTXP.selectSingleNode(citeNode);
			String citeText = textN != null ? textN.getText() : "";

			Citation citation = new NplCitation(String.valueOf(i), citeText, citedBy);

			citations.add(citation);
		}

		return citations;
	}

	private Citation.CitedBy getCitedBy(Node citedByExaminerN) {
		if (citedByExaminerN != null) {
			return CitedBy.EXAMINER;
		} else {
			return CitedBy.APPLICANT;
		}
	}

}
