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
		assertEquals("PCT/EP/12345678", docId.toText());
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
	public void JP_JAPAN() throws DocumentException {
		String xml = "<xml><document-id><country>JP</country><doc-number>99-123456</doc-number></document-id></xml>";
		Document xmlDoc = createDocument(xml);
		DocumentId docId = new DocumentIdNode(xmlDoc.getRootElement()).read();
		assertEquals(CountryCode.JP, docId.getCountryCode());
		assertEquals("1999123456", docId.getDocNumber());
		assertEquals("JP1999123456", docId.toText());
		
		String xml2 = "<xml><document-id><country>JP</country><doc-number>2000-123456</doc-number></document-id></xml>";
		Document xmlDoc2 = createDocument(xml2);
		DocumentId docId2 = new DocumentIdNode(xmlDoc2.getRootElement()).read();
		assertEquals(CountryCode.JP, docId2.getCountryCode());
		assertEquals("2000123456", docId2.getDocNumber());
		assertEquals("JP2000123456", docId2.toText());
	}

	@Test
	public void KR_S_KOREA() throws DocumentException {
		// yy-nnnnnn  yy-nnnnnnn  1948-1993
		String xml = "<xml><document-id><country>KR</country><doc-number>93-1234567</doc-number></document-id></xml>";
		Document xmlDoc = createDocument(xml);
		DocumentId docId = new DocumentIdNode(xmlDoc.getRootElement()).read();
		assertEquals(CountryCode.KR, docId.getCountryCode());
		assertEquals("19931234567", docId.getDocNumber());
		assertEquals("KR19931234567", docId.toText());

		// yyyy-nnnnnn  yyyy-nnnnnnn 1994-2004
		String xml2 = "<xml><document-id><country>KR</country><doc-number>1997-1234567</doc-number></document-id></xml>";
		Document xmlDoc2 = createDocument(xml2);
		DocumentId docId2 = new DocumentIdNode(xmlDoc2.getRootElement()).read();
		assertEquals(CountryCode.KR, docId2.getCountryCode());
		assertEquals("19971234567", docId2.getDocNumber());
		assertEquals("KR19971234567", docId2.toText());

		// tt-yyyy-nnnnnnn 2004-current   tt=[10 patent, 20 utility model, 30 design 40 trademark]
		String xml3 = "<xml><document-id><country>KR</country><doc-number>10-2004-1234567</doc-number></document-id></xml>";
		Document xmlDoc3 = createDocument(xml3);
		DocumentId docId3 = new DocumentIdNode(xmlDoc3.getRootElement()).read();
		assertEquals(CountryCode.KR, docId3.getCountryCode());
		assertEquals("1020041234567", docId3.getDocNumber());
		assertEquals("KR1020041234567", docId3.toText());
	}

	@Test
	public void RU_RUSSIA() throws DocumentException {
		// nnnnnnn patent 2000-current
		// yyyynnnnnn application 2000-current
		// yynnnnnn application 1992-2000
		String xml2 = "<xml><document-id><country>RU</country><doc-number>2000123456</doc-number><kind>U1</kind></document-id></xml>";
		Document xmlDoc2 = createDocument(xml2);
		DocumentId docId2 = new DocumentIdNode(xmlDoc2.getRootElement()).read();
		assertEquals(CountryCode.RU, docId2.getCountryCode());
		assertEquals("2000123456", docId2.getDocNumber());
		assertEquals("RU2000123456U1", docId2.toText());
	}

	@Test
	public void IN_INDIA() throws DocumentException {
		// nnnnnn patent 1912-current
		// nnnnn/LLL/yyyy application 1972-2016
		// yyyyJTnnnnnn application 2016-current 
		// 		J=jurisdictions of Indian Patent Offices:[1 Delhi, 2 Mumbai, 3 Kolkata, 4 Chennai] 
		//		T=type of application [1-9]
		// yynnnnnn application 1992-2000

		String xml = "<xml><document-id><country>IN</country><doc-number>12345/LLL/1997</doc-number></document-id></xml>";
		Document xmlDoc = createDocument(xml);
		DocumentId docId = new DocumentIdNode(xmlDoc.getRootElement()).read();
		assertEquals(CountryCode.IN, docId.getCountryCode());
		assertEquals("12345LLL1997", docId.getDocNumber());
		assertEquals("IN12345LLL1997", docId.toText());

		String xml2 = "<xml><document-id><country>IN</country><doc-number>201611123456</doc-number></document-id></xml>";
		Document xmlDoc2 = createDocument(xml2);
		DocumentId docId2 = new DocumentIdNode(xmlDoc2.getRootElement()).read();
		assertEquals(CountryCode.IN, docId2.getCountryCode());
		assertEquals("201611123456", docId2.getDocNumber());
		assertEquals("IN201611123456", docId2.toText());
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
