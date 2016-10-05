package gov.uspto.document.parser.dom4j;

import java.io.IOException;

import org.junit.Test;

import gov.uspto.patent.PatentDocFormat;
import gov.uspto.patent.PatentReader;
import gov.uspto.patent.PatentReaderException;

public class PatentParserTest {

	@Test(expected = PatentReaderException.class)
	public void parseFailUnknownXMLType() throws PatentReaderException, IOException {
		String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><application><BAD></application>";

		try (PatentReader patentReader = new PatentReader(xmlString, PatentDocFormat.Pap)) {
			patentReader.read();
		}
	}

	@Test
	public void parsePatentApplication() throws PatentReaderException, IOException {
		String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><us-patent-application><us-bibliographic-data-application><publication-reference><document-id><country-code>US</country-code><doc-number>1234567</doc-number></document-id></publication-reference><application-reference><document-id><doc-number>7654321</doc-number></document-id></application-reference></us-bibliographic-data-application></us-patent-application>";

		try (PatentReader patentReader = new PatentReader(xmlString, PatentDocFormat.RedbookApplication)) {
			patentReader.read();
		}
	}

	@Test
	public void parsePatentGrant() throws PatentReaderException, IOException {
		String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><us-patent-grant><us-bibliographic-data-application><publication-reference><document-id><country-code>US</country-code><doc-number>1234567</doc-number></document-id></publication-reference><application-reference><document-id><doc-number>7654321</doc-number></document-id></application-reference></us-bibliographic-data-application></us-patent-grant>";

		try (PatentReader patentReader = new PatentReader(xmlString, PatentDocFormat.RedbookGrant)) {
			patentReader.read();
		}
	}

	@Test
	public void parsePatentSGML() throws PatentReaderException, IOException {
		String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><PATDOC><SDOBI><B100><B110><DNUM><PDAT>1234567</PDAT></DNUM></B110></B100><B200><B210><DNUM><PDAT>7654321</PDAT></DNUM></B210></B200></SDOBI></PATDOC>";

		try (PatentReader patentReader = new PatentReader(xmlString, PatentDocFormat.Sgml)) {
			patentReader.read();
		}
	}

	@Test
	public void parsePatentPAP() throws PatentReaderException, IOException {
		String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><patent-application-publication><subdoc-bibliographic-information><document-id><country-code>US</country-code><doc-number>7654321</doc-number></document-id><domestic-filing-data><application-number><doc-number>1234567</doc-number></application-number></domestic-filing-data></subdoc-bibliographic-information></patent-application-publication>";

		try (PatentReader patentReader = new PatentReader(xmlString, PatentDocFormat.Pap)) {
			patentReader.read();
		}
	}

	@Test
	public void parsePatentGreenbook() throws PatentReaderException, IOException {
		String xmlString = "PATN\nWKU  039305848\n";

		try (PatentReader patentReader = new PatentReader(xmlString, PatentDocFormat.Greenbook)) {
			patentReader.read();
		}
		// (patent.getDocumentId(), "039305848");
	}
}
