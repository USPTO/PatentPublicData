package gov.uspto.patent.doc.xml.fragments;

import static org.junit.Assert.*;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.junit.Test;

import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.DocumentDate;
import gov.uspto.patent.model.DocumentId;
import gov.uspto.patent.model.DocumentIdType;

public class PublicationIdNodeTest {

	@Test
	public void test() throws DocumentException, InvalidDataException {
		String xml = "<xml><biblio><publication-reference><document-id>\r\n" + 
				"<country>US</country>\r\n" + 
				"<doc-number>09855244</doc-number>\r\n" + 
				"<kind>B2</kind>\r\n" + 
				"<date>20180102</date>\r\n" + 
				"</document-id></publication-reference></biblio></xml>";
		
		Document doc = DocumentHelper.parseText(xml);
		DocumentId docId = new PublicationIdNode(doc).read();

		DocumentId expectId = new DocumentId(CountryCode.US, "09855244", "B2");
		expectId.setType(DocumentIdType.REGIONAL_FILING);
		expectId.setDate(new DocumentDate("20180102"));
		assertEquals(expectId, docId);
	}

}
