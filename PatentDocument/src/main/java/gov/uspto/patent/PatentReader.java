package gov.uspto.patent;

import java.io.IOException;
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

import gov.uspto.patent.doc.greenbook.Greenbook;
import gov.uspto.patent.doc.pap.PatentAppPubParser;
import gov.uspto.patent.doc.sgml.Sgml;
import gov.uspto.patent.doc.xml.ApplicationParser;
import gov.uspto.patent.doc.xml.GrantParser;
import gov.uspto.patent.model.Patent;

/**
 * Detect Patent Document Type and/or Parse Document into Patent Object
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class PatentReader implements PatentDocReader<Patent> {
	private static final long DEFAULT_MAX_BYTES = 100000000; // 100 MB.

	private PatentDocFormat patentDocFormat;
	private long maxByteSize = DEFAULT_MAX_BYTES;

	/**
	 * Load Reader
	 * 
	 * @param reader
	 * @param PatentType
	 */
	public PatentReader(final PatentDocFormat patentDocFormat) {
		Preconditions.checkNotNull(patentDocFormat, "patentType can not be Null");
		this.patentDocFormat = patentDocFormat;
	}

	public void setMaxByteSize(long maxByteSize){
		this.maxByteSize = maxByteSize;
	}

	/**
	 * Parse Dom4j Document
	 * 
	 * @param document
	 */
	/*
	 * public PatentReader(Document document) { this.document = document;
	 * this.reader = new StringReader(document.asXML()); }
	 */

	/**
	 * Parse Document and Return Patent Object.
	 * 
	 * @param reader
	 * @return
	 * @throws PatentReaderException
	 * @throws IOException
	 */
	@Override
	public Patent read(Reader reader) throws PatentReaderException, IOException {
		Preconditions.checkNotNull(reader, "reader can not be Null");

		if (!checkSize(reader)){
			throw new PatentReaderException("Patent too Large");
		}

		switch (patentDocFormat) {
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
			try {
				reader.reset();
				return fixTagsJDOM(IOUtils.toString(reader));
			} catch (IOException e1) {
				throw new PatentReaderException(e1);
			}
		}
	}

	public boolean checkSize(Reader reader) throws IOException{
		int c;
		long charCount = 0;
		while ( -1 != (c = reader.read()) ){ 
			charCount++;
		}
		reader.reset();

		return (charCount * 2 < maxByteSize);
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
