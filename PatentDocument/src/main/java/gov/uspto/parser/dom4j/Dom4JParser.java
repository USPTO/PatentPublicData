package gov.uspto.parser.dom4j;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;

import com.google.common.base.Preconditions;

import gov.uspto.patent.PatentReaderException;
import gov.uspto.patent.model.Patent;

public abstract class Dom4JParser implements Dom4j {

	/**
	 * Parse CharSequence (String, StringBuffer, StringBuilder, CharBuffer)
	 *  
	 * @param xmlString
	 * @return
	 * @throws PatentReaderException
	 */
	public Patent parse(CharSequence xmlString) throws PatentReaderException {
		Preconditions.checkNotNull(xmlString, "xmlString can not be Null");

		try (StringReader reader = new StringReader(xmlString.toString())){
			return parse(reader);
		}
	}

	/**
	 * Parse File
	 * 
	 * @param file
	 * @return
	 * @throws PatentReaderException
	 * @throws IOException 
	 */
	public Patent parse(File file) throws PatentReaderException, IOException {
		Preconditions.checkNotNull(file, "File can not be Null");

		try(FileReader reader = new FileReader(file)){
			return parse(reader);
		}
	}

	/**
	 * Parse InputStream (ByteArrayInputStream, FileInputStream, FilterInputStream, InputStream, ObjectInputStream, PipedInputStream, SequenceInputStream, StringBufferInputStream)
	 * 
	 * @param inputStream
	 * @return
	 * @throws PatentReaderException
	 */
	public Patent parse(InputStream inputStream) throws PatentReaderException {
		try {
			SAXReader sax = new SAXReader(false);
			sax.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			Document document = sax.read(inputStream);
			return parse(document);
		} catch (DocumentException | SAXException e) {
			throw new PatentReaderException(e);
		}
	}

	/**
	 * Parse Reader (BufferedReader, CharArrayReader, FilterReader, InputStreamReader, PipedReader, StringReader)
	 * 
	 * @param reader
	 * @return
	 * @throws PatentReaderException
	 */
	public Patent parse(Reader reader) throws PatentReaderException {
		try {
			SAXReader sax = new SAXReader(false);
			sax.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			//sax.setEntityResolver(new SystemEntityResolver());
			Document document = sax.read(reader);
			return parse(document);
		} catch (DocumentException | SAXException e) {
		//} catch (DocumentException e){
			throw new PatentReaderException(e);
		}
	}

}
