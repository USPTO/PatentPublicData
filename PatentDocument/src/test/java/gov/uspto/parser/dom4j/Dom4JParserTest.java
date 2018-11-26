package gov.uspto.parser.dom4j;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.junit.Test;

import gov.uspto.patent.PatentReaderException;
import gov.uspto.patent.model.Patent;

public class Dom4JParserTest {

	@Test
	public void largeDocumentSkipElements() throws PatentReaderException, DocumentException, IOException {
		String xml="<xml>"
				+ "<patent>"
				+ "<biblio>"
				+ "<invention-title>Widget</invention-title>"
				+ "<inventor>John Doe</inventor>"
				+ "</biblio>"
				+ "<abstract>Abstract paragraph here.</abstract>"
				+ "<description>Large content may be here.</description>"
				+ "<claims>Large content may also be here.</claims>"
				+ "</patent>"
				+ "</xml>";

		String[] skipPaths = new String[] {"/xml/patent/description", "/xml/patent/claims"};

		// Wrap the Dom4JParser abstract class.
		class LargeParser extends Dom4JParser {
			@Override
			public Patent parse(Document document) throws PatentReaderException {
				return null;
			}
		}

		Document doc = ((Dom4JParser) new LargeParser()).readLarge(new StringReader(xml), Arrays.asList(skipPaths));
		//System.out.println(doc.asXML());

		String expect = "Note: This field was truncated from the Large XML Document.";

		Node descN = doc.selectSingleNode("/*/*/description");
		assertEquals(expect, descN.getText());

		Node claimsN = doc.selectSingleNode("/*/*/claims");
		assertEquals(expect, claimsN.getText());
	}

}
