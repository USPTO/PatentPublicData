package gov.uspto.patent.doc.xml.fragments;

import static org.junit.Assert.*;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.junit.Test;

import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.DocumentDate;
import gov.uspto.patent.model.DocumentId;
import gov.uspto.patent.model.DocumentIdType;

public class PriorityClaimsTest {

	@Test
	public void test() throws DocumentException, InvalidDataException {
		String xml = "<xml><priority-claims>\r\n" + 
				"<priority-claim kind=\"regional\" sequence=\"01\">\r\n" + 
				"<country>EM</country>\r\n" + 
				"<doc-number>002705756-0001</doc-number>\r\n" + 
				"<date>20150522</date>\r\n" + 
				"</priority-claim>\r\n" + 
				"<priority-claim kind=\"regional\" sequence=\"02\">\r\n" + 
				"<country>EM</country>\r\n" + 
				"<doc-number>002705756-0002</doc-number>\r\n" + 
				"<date>20150522</date>\r\n" + 
				"</priority-claim>\r\n" + 
				"<priority-claim kind=\"regional\" sequence=\"03\">\r\n" + 
				"<country>EM</country>\r\n" + 
				"<doc-number>002705756-0003</doc-number>\r\n" + 
				"<date>20150522</date>\r\n" + 
				"</priority-claim>\r\n" + 
				"</priority-claims></xml>";
		
		Document doc = DocumentHelper.parseText(xml);
		List<DocumentId> docIds = new PriorityClaims(doc).read();

		DocumentId id1 = new DocumentId(CountryCode.EM, "0027057560001");
		id1.setType(DocumentIdType.REGIONAL_FILING);
		id1.setDate(new DocumentDate("20150522"));
		assertEquals(id1, docIds.get(0));

		DocumentId id2 = new DocumentId(CountryCode.EM, "0027057560002");
		id2.setType(DocumentIdType.REGIONAL_FILING);
		id2.setDate(new DocumentDate("20150522"));
		assertEquals(id2, docIds.get(1));

		DocumentId id3 = new DocumentId(CountryCode.EM, "0027057560003");
		id3.setType(DocumentIdType.REGIONAL_FILING);
		id3.setDate(new DocumentDate("20150522"));		
		assertEquals(id3, docIds.get(2));
	}

}
