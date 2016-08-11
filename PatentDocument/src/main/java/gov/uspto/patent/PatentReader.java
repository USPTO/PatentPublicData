package gov.uspto.patent;

import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;

import com.google.common.base.Preconditions;

import gov.uspto.patent.greenbook.Greenbook;
import gov.uspto.patent.model.Patent;
import gov.uspto.patent.pap.PatentAppPubParser;
import gov.uspto.patent.sgml.Sgml;
import gov.uspto.patent.xml.ApplicationParser;
import gov.uspto.patent.xml.GrantParser;

/**
 * Detect Patent Document Type and/or Parse Document into Patent Object
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class PatentReader implements Closeable {

	private final Reader reader;
	private PatentType patentType;

	/**
	 * Load Reader
	 *  
	 * @param reader
	 * @param PatentType
	 */
	public PatentReader(Reader reader, PatentType patentType) {
		Preconditions.checkNotNull(reader, "reader can not be Null");
		Preconditions.checkNotNull(patentType, "patentType can not be Null");
		this.reader = reader;
		this.patentType = patentType;
	}

	/**
	 * Load CharSequence (String, StringBuffer, StringBuilder, CharBuffer)
	 *  
	 * @param xmlString
 	 * @param PatentType
	 */
	public PatentReader(CharSequence rawDocString, PatentType patentType) {
		this(new StringReader(rawDocString.toString()), patentType);
	}

	/**
	 * Load File
	 * 
	 * @param file
 	 * @param PatentType 
	 * @return
	 * @throws IOException 
	 */
	public PatentReader(File file, PatentType patentType) throws IOException {
		this(new FileReader(file), patentType);
	}

	/**
	 * Load InputStream (ByteArrayInputStream, FileInputStream, FilterInputStream, InputStream, ObjectInputStream, PipedInputStream, SequenceInputStream, StringBufferInputStream)
	 * 
	 * @param inputStream
 	 * @param PatentType 
	 * @return
	 * @throws IOException 
	 */
	public PatentReader(InputStream inputStream, PatentType patentType) throws IOException {
		this(new InputStreamReader(inputStream), patentType);
	}

	/**
	 * Parse Dom4j Document
	 * 
	 * @param document
	 */
	/*
	public PatentReader(Document document) {
		this.document = document;
		this.reader = new StringReader(document.asXML());
	}
	*/

	/**
	 * Parse Document and Return Patent Object.
	 * 
	 * @param reader
	 * @return
	 * @throws PatentReaderException
	 * @throws IOException 
	 */
	public Patent read() throws PatentReaderException, IOException {
		switch (patentType) {
		case Greenbook:
			return new Greenbook().parse(reader);
		case RedbookApplication:
			return new ApplicationParser().parse(getJDOM(reader));
		case RedbookGrant:
			return new GrantParser().parse(getJDOM(reader));
		case Sgml:
			return new Sgml().parse(getJDOM(reader));
		case Pap:
			return new PatentAppPubParser().parse(getJDOM(reader));
		default:
			throw new PatentReaderException("Invalid or Unknown Document Type");
		}
	}

	@Override
	public void close() throws IOException {
		if (reader != null) {
			reader.close();
		}
	}

	/**
	 * Load XML Document
	 * 
	 * @param reader
	 * @return
	 * @throws PatentReaderException
	 */
	public static Document getJDOM(Reader reader) throws PatentReaderException {
		try {	
			SAXReader sax = new SAXReader(false);
			sax.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			return sax.read(reader);
		} catch (DocumentException | SAXException e) {
			throw new PatentReaderException("Failed to load XML", e);
		}
	}
	
}
