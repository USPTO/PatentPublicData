package gov.uspto.patent.doc.xml.fragments;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.junit.Test;

import gov.uspto.patent.doc.xml.fragments.Relations;
import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.DocumentId;
import gov.uspto.patent.model.DocumentIdType;

public class RelationsTest {

	@Test
	public void continuation() throws DocumentException {
		String xml = "<xml><biblio><us-related-documents><continuation><relation>\r\n" + 
				"	<parent-doc>\r\n" + 
				"		<document-id>\r\n" + 
				"		<country>US</country>\r\n" + 
				"		<doc-number>15311805</doc-number>\r\n" + 
				"		</document-id>\r\n" + 
				"		<parent-status>PENDING</parent-status>\r\n" + 
				"		<parent-pct-document>\r\n" + 
				"			<document-id>\r\n" + 
				"			<country>WO</country>\r\n" + 
				"			<doc-number>PCT/EP2015/060757</doc-number>\r\n" + 
				"			<date>20150515</date>\r\n" + 
				"			</document-id>\r\n" + 
				"		</parent-pct-document>\r\n" + 
				"	</parent-doc>\r\n" + 
				"	<child-doc>\r\n" + 
				"		<document-id>\r\n" + 
				"		<country>US</country>\r\n" + 
				"		<doc-number>29606059</doc-number>\r\n" + 
				"		</document-id>\r\n" + 
				"	</child-doc>\r\n" + 
				"</relation></continuation></us-related-documents></biblio></xml>";

		Document doc = DocumentHelper.parseText(xml);

		List<DocumentId> docIds = new Relations(doc).read();

		//docIds.forEach(System.out::println);

		DocumentIdType docIdType = DocumentIdType.CONTINUATION;
		DocumentId expectId1 = new DocumentId(CountryCode.US, "15311805");
		DocumentId expectId2 = new DocumentId(CountryCode.WO, "PCT/EP2015/060757");
		DocumentId expectId3 = new DocumentId(CountryCode.US, "29606059");

		assertEquals("Expect 3 docids", 3, docIds.size());

		assertEquals(docIdType, docIds.get(0).getType());
		assertEquals(expectId1, docIds.get(0));
		
		assertEquals(docIdType, docIds.get(1).getType());
		assertEquals(expectId2, docIds.get(1));
		
		assertEquals(docIdType, docIds.get(2).getType());
		assertEquals(expectId3, docIds.get(2));
	}

}
