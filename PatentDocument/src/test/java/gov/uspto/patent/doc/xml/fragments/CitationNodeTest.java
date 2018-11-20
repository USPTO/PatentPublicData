package gov.uspto.patent.doc.xml.fragments;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.junit.Test;

import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.model.Citation;
import gov.uspto.patent.model.CitationType;
import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.DocumentDate;
import gov.uspto.patent.model.DocumentId;
import gov.uspto.patent.model.NplCitation;
import gov.uspto.patent.model.PatCitation;

public class CitationNodeTest {

	private static Map<String, List<String>> NPL_US_APPS = new LinkedHashMap<String, List<String>>();
	static {
		NPL_US_APPS.put("U.S. Appl. No. 12/163,684, filed Jun. 27, 2008, by Peter Sebastian Slusarczyk et al., entitled “Dimpled Food Product”.", Arrays.asList("12163684","20080627"));
		NPL_US_APPS.put("U.S. Appl. No. 29/308,915, filed on Jul. 14, 2008—First named inventor Alvaro Gonzalez.", Arrays.asList("29308915","20080714"));
		NPL_US_APPS.put("U.S. Appl. No. 29/327,632; entitled Doughnut Product With Six Appendages and Without a Hole; filed Nov. 10, 2008.", Arrays.asList("29327632","20081110"));
		NPL_US_APPS.put("U.S. Appl. No. 29/327,634; entitled Doughnut Product With Six Appendages; filed Nov. 10, 2008.", Arrays.asList("29327634","20081110"));	
		NPL_US_APPS.put("U.S. Appl. No. 29/294,234, filed Dec. 28, 2007, Pidgeon et al.", Arrays.asList("29294234","20071228"));
		NPL_US_APPS.put("U.S. Appl. No. 29/294,704, filed Jan. 24, 2008.", Arrays.asList("29294704","20080124"));
		NPL_US_APPS.put("U.S. Appl. No. 29/294,097, Applicant: Hee-Yul Kim, et al., filed Dec. 21, 2007 entitled Laser Printer.", Arrays.asList("29294097","20071221"));
		NPL_US_APPS.put("U.S. Appl. No. 29/336,975, filed May 13, 2009; Inventor: Ingo Schmitz.", Arrays.asList("29336975","20090513"));
		NPL_US_APPS.put("Ojini, Eziamara Anthony, Office Action, mailed Aug. 25, 2008, in U.S. Appl. No. 11/391,100, U.S. Patent and Trademark Office."
				, Arrays.asList("11391100",""));
		NPL_US_APPS.put("Luke et al., U.S. Appl. No. 29/321,870, filed Jul. 24, 2008,7 pages.", Arrays.asList("29321870","20080724"));
		NPL_US_APPS.put("U.S. Appl. No. 29/283859, filed Aug. 24, 2007, Conforzi.", Arrays.asList("29283859","20070824"));
		NPL_US_APPS.put("U.S. Appl. No. 10/956,613, Fung-jou Chen et al., entitled Foam-Based Fastners, Filed Sep. 30, 2004.",
				 Arrays.asList("10956613","20040930"));

		NPL_US_APPS.put("Transmittal of International Search Report and Written Opinion for PCT/US06/22969 (related to the present application), publication date Aug. 27, 2008, Earthlite Massage Tables, Inc.",
				 Arrays.asList("0622969",""));
		NPL_US_APPS.put("International Search Report and Written Opinion dated Nov. 28, 2006 for PCT Application No. PCT/US2006/016845.",
				 Arrays.asList("2006016845",""));
		NPL_US_APPS.put("International Search Report in corresponding PCT Application No. PCT/US/2006/030219, mailed Dec. 23, 2005.",
				Arrays.asList("2006030219",""));

		// European Search Report dated Jul. 22, 2004; Ref. 3187-001PCT/EP; Application No./Pat. No. 99964066-7-1243-US9928595.
		// German Search Report for DE 102 60 212.3.
		// International Search Report for PCT/EP2003/014099.
		
	}

	@Test
	public void NPL_PAT_US_APP() throws InvalidDataException {
		CitationNode citeNode = new CitationNode(null);

		for (Entry<String, List<String>> entry : NPL_US_APPS.entrySet()) {
			
			DocumentId foundId = citeNode.nplParseUSApp(entry.getKey());
			
			DocumentId expectId = new DocumentId(CountryCode.US, entry.getValue().get(0));
			if (entry.getValue().get(1).length() > 0) {
				expectId.setDate(new DocumentDate(entry.getValue().get(1)));
			}
			
			assertEquals(expectId, foundId);
		}
	}

	@Test
	public void Citations() throws DocumentException {	
	String xml = "<xml><us-bibliographic-data-grant><us-references-cited><us-citation>\r\n" + 
			"<patcit num=\"00001\">\r\n" + 
			"<document-id>\r\n" + 
			"<country>US</country>\r\n" + 
			"<doc-number>414090</doc-number>\r\n" + 
			"<kind>A</kind>\r\n" + 
			"<name>Taylor</name>\r\n" + 
			"<date>18891000</date>\r\n" + 
			"</document-id>\r\n" + 
			"</patcit>\r\n" + 
			"<category>cited by applicant</category>\r\n" + 
			"</us-citation>\r\n" + 
			"<us-citation>\r\n" + 
			"<patcit num=\"00002\">\r\n" + 
			"<document-id>\r\n" + 
			"<country>US</country>\r\n" + 
			"<doc-number>D28864</doc-number>\r\n" + 
			"<kind>S</kind>\r\n" + 
			"<name>Baker</name>\r\n" + 
			"<date>18980600</date>\r\n" + 
			"</document-id>\r\n" + 
			"</patcit>\r\n" + 
			"<category>cited by applicant</category>\r\n" + 
			"</us-citation>\r\n" + 
			"<us-citation>\r\n" + 
			"<nplcit num=\"00003\">\r\n" + 
			"<othercit>“Cool Cite”, Internet: http://www.website.com, Mar. 2003, PDF p. 23.</othercit>\r\n" + 
			"</nplcit>\r\n" + 
			"<category>cited by applicant</category>\r\n" + 
			"</us-citation>\r\n" + 
			"<us-citation>\r\n" + 
			"<nplcit num=\"00004\">\r\n" + 
			"<othercit>“Office Supplies, Jan. 2, 2001, p. 70, Office Supplies Shop, Apr. 29, 2000, p. 75”.</othercit>\r\n" + 
			"</nplcit>\r\n" + 
			"<category>cited by applicant</category>\r\n" + 
			"</us-citation></us-references-cited></us-bibliographic-data-grant></xml>";

		Document doc = DocumentHelper.parseText(xml);

		List<Citation> citations = new CitationNode(doc).read();
		//citations.forEach(System.out::println);

		assertEquals("00001", citations.get(0).getNum());
		assertEquals(CitationType.PATCIT, citations.get(0).getCitType());
		assertEquals(new DocumentId(CountryCode.US, "414090", "A"), ((PatCitation) citations.get(0)).getDocumentId());

		assertEquals("00002", citations.get(1).getNum());
		assertEquals(CitationType.PATCIT, citations.get(1).getCitType());
		assertEquals(new DocumentId(CountryCode.US, "D28864", "S"), ((PatCitation) citations.get(1)).getDocumentId());

		assertEquals("00003", citations.get(2).getNum());
		assertEquals(CitationType.NPLCIT, citations.get(2).getCitType());
		assertEquals("“Cool Cite”, Internet: http://www.website.com, Mar. 2003, PDF p. 23.", ((NplCitation) citations.get(2)).getCiteText());

		assertEquals("00004", citations.get(3).getNum());
		assertEquals(CitationType.NPLCIT, citations.get(3).getCitType());
		assertEquals("“Office Supplies, Jan. 2, 2001, p. 70, Office Supplies Shop, Apr. 29, 2000, p. 75”.", ((NplCitation) citations.get(3)).getCiteText());

	}
}
