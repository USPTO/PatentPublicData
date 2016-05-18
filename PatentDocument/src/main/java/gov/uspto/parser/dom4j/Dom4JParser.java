package gov.uspto.parser.dom4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;

import com.google.common.base.Preconditions;

import gov.uspto.patent.PatentParserException;
import gov.uspto.patent.model.Patent;

public abstract class Dom4JParser implements Dom4j {

	/**
	 * Parse CharSequence (String, StringBuffer, StringBuilder, CharBuffer)
	 *  
	 * @param xmlString
	 * @return
	 * @throws PatentParserException
	 */
	public Patent parse(CharSequence xmlString) throws PatentParserException {
		Preconditions.checkNotNull(xmlString, "xmlString can not be Null");

		StringReader reader = new StringReader(xmlString.toString());
		return parse(reader);
	}

	/**
	 * Parse File
	 * 
	 * @param file
	 * @return
	 * @throws FileNotFoundException
	 * @throws PatentParserException
	 */
	public Patent parse(File file) throws FileNotFoundException, PatentParserException {
		Preconditions.checkNotNull(file, "File can not be Null");

		FileReader reader = new FileReader(file);
		return parse(reader);
	}

	/**
	 * Parse InputStream (ByteArrayInputStream, FileInputStream, FilterInputStream, InputStream, ObjectInputStream, PipedInputStream, SequenceInputStream, StringBufferInputStream)
	 * 
	 * @param inputStream
	 * @return
	 * @throws PatentParserException
	 */
	public Patent parse(InputStream inputStream) throws PatentParserException {
		try {
			SAXReader sax = new SAXReader(false);
			sax.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			Document document = sax.read(inputStream);
			return parse(document);
		} catch (DocumentException | SAXException e) {
			throw new PatentParserException(e);
		}
	}

	/**
	 * Parse Reader (BufferedReader, CharArrayReader, FilterReader, InputStreamReader, PipedReader, StringReader)
	 * 
	 * @param reader
	 * @return
	 * @throws PatentParserException
	 */
	public Patent parse(Reader reader) throws PatentParserException {
		try {
			SAXReader sax = new SAXReader(false);
			sax.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			Document document = sax.read(reader);
			return parse(document);
		} catch (DocumentException | SAXException e) {
			throw new PatentParserException(e);
		}
	}

}
