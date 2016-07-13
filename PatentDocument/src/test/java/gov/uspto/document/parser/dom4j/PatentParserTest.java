package gov.uspto.document.parser.dom4j;

import java.io.IOException;

import org.junit.Test;

import gov.uspto.patent.PatentReader;
import gov.uspto.patent.PatentReaderException;
import gov.uspto.patent.PatentType;

public class PatentParserTest {

	@Test(expected = PatentReaderException.class)
	public void parseFailUnknownXMLType() throws PatentReaderException, IOException {
		String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><application></application>";

		try(PatentReader patentReader = new PatentReader(xmlString, PatentType.Pap)){
			patentReader.read();
		}
	}

	@Test
	public void parsePatentApplication() throws PatentReaderException, IOException {
		String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><us-patent-application><us-bibliographic-data-application></us-bibliographic-data-application></us-patent-application>";

		try(PatentReader patentReader = new PatentReader(xmlString, PatentType.RedbookApplication)){
			patentReader.read();
		}
	}

	@Test
	public void parsePatentGrant() throws PatentReaderException, IOException {
		String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><us-patent-grant></us-patent-grant>";

		try(PatentReader patentReader = new PatentReader(xmlString, PatentType.RedbookGrant)){
			patentReader.read();
		}
	}

	@Test
	public void parsePatentSGML() throws PatentReaderException, IOException {
		String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><PATDOC><SDOBI></SDOBI></PATDOC>";

		try(PatentReader patentReader = new PatentReader(xmlString, PatentType.Sgml)){
			patentReader.read();
		}
	}

	@Test
	public void parsePatentPAP() throws PatentReaderException, IOException {
		String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><patent-application-publication><subdoc-bibliographic-information></subdoc-bibliographic-information></patent-application-publication>";

		try(PatentReader patentReader = new PatentReader(xmlString, PatentType.Pap)){
			patentReader.read();
		}
	}

	@Test
	public void parsePatentGreenbook() throws PatentReaderException, IOException {
		String xmlString = "PATN\nWKU  039305848\n";

		try(PatentReader patentReader = new PatentReader(xmlString, PatentType.Greenbook)){
			patentReader.read();
		}
		//(patent.getDocumentId(), "039305848");
	}
}
