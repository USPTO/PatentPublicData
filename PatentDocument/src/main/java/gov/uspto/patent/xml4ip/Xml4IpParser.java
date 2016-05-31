package gov.uspto.patent.xml4ip;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;

import gov.uspto.patent.PatentParserException;
import gov.uspto.patent.cpc.ClassificationItem;

public class Xml4IpParser {

	/**
	 * Parse CharSequence (String, StringBuffer, StringBuilder, CharBuffer)
	 *  
	 * @param xmlString
	 * @return
	 * @throws PatentParserException
	 */	
	public Document parse(CharSequence xmlString) throws PatentParserException {
		StringReader reader = new StringReader(xmlString.toString());
		return parse(reader);
	}

	public Document parse(File file) throws PatentParserException, FileNotFoundException {
		FileReader reader = new FileReader(file);
		return parse(reader);
	}

	public Document parse(Reader reader) throws PatentParserException {
		try {
			SAXReader sax = new SAXReader(false);
			sax.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			return sax.read(reader);
		} catch (SAXException e) {
			throw new PatentParserException(e);
		} catch (DocumentException e) {
			throw new PatentParserException(e);
		}
	}
	
	public static void main(String[] args) throws FileNotFoundException, PatentParserException {
		
	}
}
