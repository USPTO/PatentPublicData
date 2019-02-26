package gov.uspto.parser.dom4j;

import static org.junit.Assert.assertEquals;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.junit.Test;
import org.xml.sax.SAXException;

public class Dom4jUtilTest {

	@Test
	public void getTextOrNull() throws SAXException, DocumentException {

		String xmlString = "<us-patent-grant><us-bibliographic-data-grant>"
				+ "<invention-title id=\"d0e61\"><i>Streptococcus pneumoniae </i>proteins and nucleic acid molecules</invention-title>"
				+ "</us-bibliographic-data-grant></us-patent-grant>";

		Document doc = Dom4jUtil.read(xmlString);

		String actual = Dom4jUtil.getTextOrEmpty(doc, "/us-patent-grant/us-bibliographic-data-grant/invention-title");

		assertEquals("Streptococcus pneumoniae proteins and nucleic acid molecules", actual);
	}
}
