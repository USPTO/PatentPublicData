package gov.uspto.patent.doc.xml.fragments;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.doc.xml.items.ClassificationCpcNode;
import gov.uspto.patent.doc.xml.items.ClassificationIPCNode;
import gov.uspto.patent.doc.xml.items.ClassificationNationalNode;
import gov.uspto.patent.doc.xml.items.DocumentIdNode;
import gov.uspto.patent.model.Citation;
import gov.uspto.patent.model.DocumentId;
import gov.uspto.patent.model.NplCitation;
import gov.uspto.patent.model.PatCitation;
import gov.uspto.patent.model.classification.PatentClassification;

/**
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 *<p>Note 1: Citations are only available to the public in Grants.</p>
 *<p>Note 2: classification-national are sometimes missing from citations when cited by applicant.</p>
 *
 */
public class CitationNode extends DOMFragmentReader<List<Citation>> {

	private static final String FRAGMENT_PATH = "//us-references-cited|//references-cited"; // current us-references-cited. 

	private Node citationNode;

	public CitationNode(Document document) {
		super(document);
	}

	@Override
	public List<Citation> read() {
		List<Citation> citations = new ArrayList<Citation>();

		citationNode = document.selectSingleNode(FRAGMENT_PATH);
		if (citationNode == null) {
			return citations;
		}

		List<Citation> patCitations = readPatCitations();
		List<Citation> nplCitations = readNplCitations();

		citations.addAll(patCitations);
		citations.addAll(nplCitations);

		return citations;
	}

	public List<Citation> readNplCitations() {
		List<Citation> nplCitations = new ArrayList<Citation>();

		@SuppressWarnings("unchecked")
		List<Node> nlpcitNodes = citationNode.selectNodes("us-citation/nplcite|citation/nplcit");

		for (Node nplcit : nlpcitNodes) {

			String num = nplcit.selectSingleNode("@num").getText();
			Node citeTxtN = nplcit.selectSingleNode("othercit");

			String citeTxt = citeTxtN != null ? citeTxtN.getText() : "";

			// <category>cited by examiner</category>
			Node categoryN = nplcit.getParent().selectSingleNode("category");
			String categoryTxt = categoryN != null ? categoryN.getText() : "";
			boolean examinerCited = (categoryTxt.equals("cited by examiner"));

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

			/*
			 * Applications appear as US2004123455 CountryDate/Number
			 *
			String[] parts = documentId.getId().split("/");
			String date;
			if (parts.length == 2){
				String country = parts[0].substring(0, 2);
				date = parts[0].substring(1);
				String id2 = country + parts[1];
				
				System.out.println("Application ID: " + documentId + " " + id2);
			}
			*/

			// <category>cited by examiner</category>
			Node category = patcit.getParent().selectSingleNode("category");
			boolean examinerCited = (category.getText().equals("cited by examiner")); // else "cited by applicant"

			PatCitation citation = new PatCitation(num, documentId, examinerCited);

			PatentClassification mainClassNational = new ClassificationNationalNode(patcit.getParent()).read();
			if (mainClassNational != null){
			    citation.setClassification(mainClassNational);
			}

	        PatentClassification mainClassCpc = new ClassificationCpcNode(patcit.getParent()).read();
	        if (mainClassCpc != null){
	            citation.setClassification(mainClassCpc);
	        }
	        
            PatentClassification mainClassIpc = new ClassificationIPCNode(patcit.getParent()).read();
            if (mainClassCpc != null){
                citation.setClassification(mainClassIpc);
            }
	        
	        patCitations.add(citation);
		}

		return patCitations;
	}
}
