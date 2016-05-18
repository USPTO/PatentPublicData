package gov.uspto.document.parser.dom4j;

import org.junit.Test;

import gov.uspto.patent.PatentParser;
import gov.uspto.patent.PatentParserException;
import gov.uspto.patent.model.Patent;

public class PatentParserTest {

	@Test(expected = PatentParserException.class)
	public void parseFailUnknownXMLType() throws PatentParserException {
		String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><application></application>";

		PatentParser parser = new PatentParser();
		parser.parse(xmlString);
	}

	@Test
	public void parsePatentApplication() throws PatentParserException {
		String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><us-patent-application><us-bibliographic-data-application></us-bibliographic-data-application></us-patent-application>";

		PatentParser parser = new PatentParser();
		parser.parse(xmlString);
	}

	@Test
	public void parsePatentGrant() throws PatentParserException {
		String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><us-patent-grant></us-patent-grant>";

		PatentParser parser = new PatentParser();
		parser.parse(xmlString);
	}

	@Test
	public void parsePatentSGML() throws PatentParserException {
		String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><PATDOC><SDOBI></SDOBI></PATDOC>";

		PatentParser parser = new PatentParser();
		parser.parse(xmlString);
	}

	@Test
	public void parsePatentPAP() throws PatentParserException {
		String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><patent-application-publication><subdoc-bibliographic-information></subdoc-bibliographic-information></patent-application-publication>";

		PatentParser parser = new PatentParser();
		parser.parse(xmlString);
	}

	@Test
	public void parsePatentGreenbook() throws PatentParserException {
		String xmlString = "PATN\nWKU  039305848\n";

		PatentParser parser = new PatentParser();
		Patent patent = parser.parse(xmlString);
		//(patent.getDocumentId(), "039305848");
	}
}
