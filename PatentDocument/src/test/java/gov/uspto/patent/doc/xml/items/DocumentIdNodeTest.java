package gov.uspto.patent.doc.xml.items;

import static org.junit.Assert.*;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.junit.Test;

import gov.uspto.patent.DateTextType;
import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.DocumentId;

public class DocumentIdNodeTest {

	@Test
	public void SimpleAppId() throws DocumentException {
		String xml = "<xml><document-id><country>US</country><doc-number>12345678</doc-number><kind>A1</kind><date>20010101</date></document-id></xml>";
		Document xmlDoc = createDocument(xml);
		DocumentId docId = new DocumentIdNode(xmlDoc.getRootElement()).read();
		assertEquals(CountryCode.US, docId.getCountryCode());
		assertEquals("12345678", docId.getDocNumber());
		assertEquals("A1", docId.getKindCode());
		assertEquals("20010101", docId.getDate().getDateText(DateTextType.RAW));
		assertEquals("US12345678A1", docId.toText());
	}

	@Test
	public void RemoveRepeatCountryCode() throws DocumentException {
		// country code appears again in doc-number.
		String xml = "<xml><document-id><country>WO</country><doc-number>WO 2004/12345678</doc-number><date>20010101</date></document-id></xml>";
		Document xmlDoc = createDocument(xml);
		DocumentId docId = new DocumentIdNode(xmlDoc.getRootElement()).read();
		assertEquals(CountryCode.WO, docId.getCountryCode());
		assertEquals("200412345678", docId.getDocNumber());
		assertEquals("20010101", docId.getDate().getDateText(DateTextType.RAW));
		assertEquals("WO200412345678", docId.toText());
	}

	@Test
	public void WO_PCT() throws DocumentException {
		String xml = "<xml><document-id><country>WO</country><doc-number>PCT/EP/12345678</doc-number><date>20010101</date></document-id></xml>";
		Document xmlDoc = createDocument(xml);
		DocumentId docId = new DocumentIdNode(xmlDoc.getRootElement()).read();
		assertEquals(CountryCode.WO, docId.getCountryCode());
		assertEquals("PCT/EP/12345678", docId.getDocNumber());
		assertEquals("20010101", docId.getDate().getDateText(DateTextType.RAW));
		assertEquals("WOPCT/EP/12345678", docId.toText());
	}

	@Test
	public void FixShortYear() throws DocumentException {
		String xml = "<xml><document-id><country>WO</country><doc-number>WO 04/12345678</doc-number><date>20010101</date></document-id></xml>";
		Document xmlDoc = createDocument(xml);
		DocumentId docId = new DocumentIdNode(xmlDoc.getRootElement()).read();
		assertEquals(CountryCode.WO, docId.getCountryCode());
		assertEquals("200412345678", docId.getDocNumber());
		assertEquals("20010101", docId.getDate().getDateText(DateTextType.RAW));
		assertEquals("WO200412345678", docId.toText());
	}

	@Test
	public void RemoveSpaces() throws DocumentException {
		String xml = "<xml><document-id><country>DE</country><doc-number>299 06 432</doc-number><date>20010101</date><kind>U1</kind></document-id></xml>";
		Document xmlDoc = createDocument(xml);
		DocumentId docId = new DocumentIdNode(xmlDoc.getRootElement()).read();
		assertEquals(CountryCode.DE, docId.getCountryCode());
		assertEquals("29906432", docId.getDocNumber());
		assertEquals("20010101", docId.getDate().getDateText(DateTextType.RAW));
		assertEquals("DE29906432U1", docId.toText());
	}

	private Document createDocument(String xml) throws DocumentException{
        SAXReader reader = new SAXReader();
		return reader.read(new StringReader(xml));
	}
}
