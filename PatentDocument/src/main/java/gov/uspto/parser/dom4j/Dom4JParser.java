package gov.uspto.parser.dom4j;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.jsoup.Jsoup;
import org.jsoup.parser.ParseSettings;
import org.jsoup.parser.Parser;
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
			return parse(sax.read(reader));
		} catch (DocumentException | SAXException e) {
			try {
				reader.reset();
				return parse(fixTagsJDOM(IOUtils.toString(reader)));
			} catch (IOException e1) {
				throw new PatentReaderException(e1);
			}
		}
	}

	/**
	 * Fix unclosed tags by loading into and out of JSoup
	 * 
	 * @param badXml
	 * @return
	 * @throws IOException
	 * @throws PatentReaderException
	 */
	public static Document fixTagsJDOM(String badXml) throws IOException, PatentReaderException {
		org.jsoup.nodes.Document jsoupDoc = Jsoup.parse("<body>" + badXml + "</body>", "",
				Parser.xmlParser().settings(ParseSettings.preserveCase));
		jsoupDoc.outputSettings().prettyPrint(false);
		String doc = jsoupDoc.select("body").html();
		// Add HTML DTD to ensure HTML entities do not cause any problems.
        doc = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n" + doc; 
		try {
			SAXReader sax = new SAXReader(false);
			sax.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			return sax.read(new StringReader(doc));
		} catch (DocumentException | SAXException e) {
			throw new PatentReaderException("Failed to Fix and Parse Docuemnt", e);
		}
	}
}
