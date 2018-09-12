package gov.uspto.patent.doc.xml.fragments;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.time.DateParser;
import org.apache.commons.lang3.time.FastDateFormat;
import org.dom4j.Document;
import org.dom4j.Node;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.doc.xml.items.ClassificationCpcNode;
import gov.uspto.patent.doc.xml.items.ClassificationIPCNode;
import gov.uspto.patent.doc.xml.items.ClassificationNationalNode;
import gov.uspto.patent.doc.xml.items.DocumentIdNode;
import gov.uspto.patent.model.Citation;
import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.DocumentDate;
import gov.uspto.patent.model.DocumentId;
import gov.uspto.patent.model.DocumentIdType;
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

			DocumentId docId = nplParseUSApp(citeTxt);
			
			NplCitation citation = new NplCitation(num, citeTxt, examinerCited);
			if (docId != null) {
				citation.setPatentId(docId);
			}
			nplCitations.add(citation);
		}

		return nplCitations;
	}

	private static Pattern US_APP = Pattern.compile("(?:^|.+?\\b)(?:U\\.S\\. Appl\\. No\\. |PCT/US/?)(\\d{2,4}/\\d{3},?\\d{2,})\\b.+");
	private static Pattern DATE = Pattern.compile(".+?[,;] [Ff]iled (?:on )?(\\D{3}\\. \\d{1,2}, [12]\\d{3})[,.].+?");
    private static final DateParser NPL_DATE_FORMAT = FastDateFormat.getInstance("MMM. d, yyyy");
    private Matcher usAppMatcher = US_APP.matcher("");
	private Matcher dateMatcher = DATE.matcher("");

	public DocumentId nplParseUSApp(String citeText){
		usAppMatcher.reset(citeText);
		dateMatcher.reset(citeText);
		if (usAppMatcher.matches()) {
			String number = usAppMatcher.group(1);
			number = number.replaceAll("[^0-9]", "");
			DocumentDate docDate = null;
			if (dateMatcher.matches()){
				String dateStr = dateMatcher.group(1);
				 try {
					 Date date = NPL_DATE_FORMAT.parse(dateStr);
					 docDate = new DocumentDate(date);
				 } catch (ParseException e) {
					 //LOGGER.warn("NPL date parse failed.");
				 } catch (InvalidDataException e) {
					//e.printStackTrace();
				}
			}

			DocumentId docId = new DocumentId(CountryCode.US, number);
			docId.setType(DocumentIdType.APPLICATION);
			if (docDate != null) {
				docId.setDate(docDate);
			}
			return docId;
		}

		return null;
	}

	public List<Citation> readPatCitations() {
		List<Citation> patCitations = new ArrayList<Citation>();

		@SuppressWarnings("unchecked")
		List<Node> patcitNodes = citationNode.selectNodes("citation/patcit|us-citation/patcit");
		for (Node patcit : patcitNodes) {
			String num = patcit.selectSingleNode("@num").getText();

			DocumentId documentId = new DocumentIdNode(patcit).read();
			
			//if (documentId.getCountryCode() != CountryCode.US){
			//	System.out.println("PatCite docId: " + documentId);
			//}

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
