package gov.uspto.patent.model;

import static org.junit.Assert.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.DocumentId;

public class DocumentIdTest {

	@Test
	public void parse_docid_shortkind() throws InvalidDataException {
		DocumentId docId = DocumentId.fromText("US5973683A", 1836);
		assertEquals(CountryCode.US, docId.getCountryCode());
		assertEquals("5973683", docId.getDocNumber());
		assertEquals("A", docId.getKindCode());
		assertEquals("US5973683A", docId.getId());
		assertEquals("US5973683", docId.getIdNoKind());
		assertEquals("US005973683A", docId.toText(9));
	}

	@Test
	public void xpatent_docid() throws InvalidDataException {
		String docIdStr2 = "USRX9876I1";
		DocumentId docId2 = DocumentId.fromText(docIdStr2, 1836);
		assertEquals(CountryCode.US, docId2.getCountryCode());
		assertEquals("RX9876", docId2.getDocNumber());
		assertEquals("I1", docId2.getKindCode());
		assertEquals("USRX9876I1", docId2.getId());
		assertEquals("USRX9876", docId2.getIdNoKind());
	}

	@Test
	public void xpatent_reissue_docid() throws InvalidDataException {
		String docIdStr2 = "USRX9876I1";
		DocumentId docId2 = DocumentId.fromText(docIdStr2, 1836);
		assertEquals(CountryCode.US, docId2.getCountryCode());
		assertEquals(docIdStr2, docId2.getId());
	}

	@Test
	public void white_split() throws InvalidDataException {
		String docIdStr = "DE101 47 913C1";
		DocumentId docId = DocumentId.fromText(docIdStr, 1990);
		assertEquals(docIdStr, docId.getRawText());
		assertEquals(CountryCode.DE, docId.getCountryCode());
		assertEquals("C1", docId.getKindCode());
		assertEquals("DE10147913C1", docId.getId());
		assertEquals("DE10147913", docId.getIdNoKind());
	}

	@Test
	public void white_split_2() throws InvalidDataException {
		String docIdStr = "EP 470 185B1";
		DocumentId docId = DocumentId.fromText(docIdStr, 1990);
		assertEquals(CountryCode.EP, docId.getCountryCode());
		assertEquals(docIdStr, docId.getRawText());
		assertEquals("B1", docId.getKindCode());
		assertEquals("EP470185B1", docId.getId());
		assertEquals("EP470185", docId.getIdNoKind());
	}

	@Test
	public void application_yr_us() throws InvalidDataException {
		String docIdStr = "US2002/0097953A1";
		DocumentId docId = DocumentId.fromText(docIdStr, 1990);
		assertEquals(docIdStr, docId.getRawText());
		assertEquals(CountryCode.US, docId.getCountryCode());
		assertEquals("A1", docId.getKindCode());
		assertEquals("US20020097953A1", docId.getId());
		assertEquals("US20020097953", docId.getIdNoKind());
		assertEquals(new DocumentDate("2002"), docId.getDate());
	}

	@Test
	public void application_yr_other() throws InvalidDataException {
		String docIdStr = "WO2004/021600A1";
		DocumentId docId = DocumentId.fromText("WO2004/021600A1", 1990);
		assertEquals(docIdStr, docId.getRawText());
		assertEquals(CountryCode.WO, docId.getCountryCode());
		assertEquals("A1", docId.getKindCode());
		assertEquals("WO2004021600A1", docId.getId());
		assertEquals("WO2004021600", docId.getIdNoKind());
		assertEquals(new DocumentDate("2004"), docId.getDate());
	}

	@Test
	public void remove_leading_zeros() throws InvalidDataException {
		Map<String, String> check = new LinkedHashMap<String, String>();
		check.put("US00123456A1", "US123456A1");
		check.put("USD0466643S1", "USD466643S1");
		check.put("USRE037914E1", "USRE37914E1");
		check.put("USPP0121231P", "USPP121231P");
		for (Entry<String, String> entry : check.entrySet()) {
			DocumentId docId = DocumentId.fromText(entry.getKey(), 1990);
			assertEquals(entry.getKey(), docId.getRawText());
			assertEquals(entry.getValue(), docId.getId());
		}
	}

	@Test
	public void other_Ids() throws InvalidDataException {
		Map<String, String> check = new LinkedHashMap<String, String>();
		check.put("JP6-302377", "JP6302377");
		for (Entry<String, String> entry : check.entrySet()) {
			DocumentId docId = DocumentId.fromText(entry.getKey(), 1990);
			assertEquals(entry.getKey(), docId.getRawText());
			assertEquals(entry.getValue(), docId.getId());
		}
	}

	public void no_parse_normalize() throws InvalidDataException {
		String docIdStr = "196 34 785";
		String expect = "DE19634785";
		DocumentId docId = new DocumentId(CountryCode.DE, docIdStr);
		docId.setRawText(docIdStr);
		assertEquals(docIdStr, docId.getRawText());
		assertEquals(CountryCode.DE, docId.getCountryCode());
		assertEquals(expect, docId.getDocNumber());
		assertEquals(expect, docId.getId());
		assertEquals(expect, docId.getIdNoKind());
	}

	@Test
	public void pct_no_parse_4digit_yr() throws InvalidDataException {
		String docIdStr = "PCT/US1999/123456";
		DocumentId docId = new DocumentId(CountryCode.WO, docIdStr);
		docId.setRawText(docIdStr);
		assertEquals(docIdStr, docId.getRawText());
		assertEquals(CountryCode.WO, docId.getCountryCode());
		assertEquals(docIdStr, docId.getDocNumber());
		assertEquals(docIdStr, docId.getId());
		assertEquals(docIdStr, docId.getIdNoKind());
	}

	@Test
	public void pct_parse_4digit_yr_US() throws InvalidDataException {
		String docIdStr = "PCT/US1999/123456";
		String expect = "US1999123456";
		DocumentId docId = DocumentId.fromText(docIdStr, 1990);
		assertEquals(docIdStr, docId.getRawText());
		assertEquals(CountryCode.US, docId.getCountryCode());
		assertEquals("123456", docId.getDocNumber());
		assertEquals(expect, docId.getId());
		assertEquals(expect, docId.getIdNoKind());
		assertEquals(new DocumentDate("1999"), docId.getDate());
	}

	@Test
	public void pct_parse_2digit_yr_US() throws InvalidDataException {
		String docIdStr = "PCT/US99/12345";
		String expect = "US9912345";
		DocumentId docId = DocumentId.fromText(docIdStr, 1990);
		assertEquals(docIdStr, docId.getRawText());
		assertEquals(CountryCode.US, docId.getCountryCode());
		assertEquals("12345", docId.getDocNumber());
		assertEquals(expect, docId.getId());
		assertEquals(expect, docId.getIdNoKind());
		assertEquals(new DocumentDate("1999"), docId.getDate());
	}

	@Test
	public void pct_parse_2digit_yr() throws InvalidDataException {
		String docIdStr = "PCT/CA99/123456";
		DocumentId docId = DocumentId.fromText(docIdStr, 1990);
		assertEquals(docIdStr, docId.getRawText());
		assertEquals(CountryCode.WO, docId.getCountryCode());
		assertEquals(docIdStr, docId.getDocNumber());
		assertEquals(docIdStr, docId.getId());
		assertEquals(docIdStr, docId.getIdNoKind());
		assertEquals(new DocumentDate("1999"), docId.getDate());
	}

	@Test
	public void adding_leading_zeros() throws InvalidDataException {
		DocumentId docId = new DocumentId(CountryCode.US, "123456", "A1");
		assertEquals(CountryCode.US, docId.getCountryCode());
		assertEquals("US000123456A1", docId.toText(9));
		assertEquals("US0000123456A1", docId.toText(10));
	}

	@Test
	public void equals() throws InvalidDataException {
		DocumentId docId = new DocumentId(CountryCode.US, "123456", "A1");
		assertEquals(docId, new DocumentId(CountryCode.US, "123456")); // same
		assertEquals(docId, new DocumentId(CountryCode.US, "00123456")); // same, leading zeros
		// assertEquals(docId, new DocumentId(CountryCode.US, "123456", "B1")); //
		// different kindcode
		assertNotEquals(docId, new DocumentId(CountryCode.WO, "123456")); // different country
		assertNotEquals(docId, new DocumentId(CountryCode.US, "888888")); // different number
	}

}
