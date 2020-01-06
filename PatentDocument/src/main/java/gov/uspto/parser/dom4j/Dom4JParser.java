package gov.uspto.parser.dom4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.ElementHandler;
import org.dom4j.ElementPath;
import org.dom4j.io.SAXReader;
import org.jsoup.Jsoup;
import org.jsoup.parser.ParseSettings;
import org.jsoup.parser.Parser;
import org.slf4j.MDC;
import org.xml.sax.SAXException;

import com.google.common.base.Preconditions;

import gov.uspto.patent.PatentReaderException;
import gov.uspto.patent.model.Patent;

public abstract class Dom4JParser implements Dom4j {

	public String getSource() {
		// %X{SOURCE}:%X{RECNUM}:%X{DOCID}
		StringBuilder stb = new StringBuilder();
		stb.append(MDC.get("SOURCE"));
		stb.append(":");
		stb.append(MDC.get("RECNUM"));
		stb.append(":");
		stb.append(MDC.get("DOCID"));
		return stb.toString();
	}

	/**
	 * Parse CharSequence (String, StringBuffer, StringBuilder, CharBuffer)
	 * 
	 * @param xmlString
	 * @return
	 * @throws PatentReaderException
	 */
	public Patent parse(CharSequence xmlString) throws PatentReaderException {
		Preconditions.checkNotNull(xmlString, "xmlString can not be Null");

		try (StringReader reader = new StringReader(xmlString.toString())) {
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

		String content = FileUtils.readFileToString(file, "UTF-8");
		content = content.replaceFirst("<\\?xml version=\"1.0\" encoding=\"UTF-8\"\\?>\n", "");
		content = content.replaceFirst("<!DOCTYPE .+>\n", "");
		//System.out.println(file.getAbsolutePath());
		return parse(content);
	}

	/**
	 * Parse InputStream (ByteArrayInputStream, FileInputStream, FilterInputStream,
	 * InputStream, ObjectInputStream, PipedInputStream, SequenceInputStream,
	 * StringBufferInputStream)
	 * 
	 * @param inputStream
	 * @return
	 * @throws PatentReaderException
	 */
	public Patent parse(InputStream inputStream) throws PatentReaderException {
		try {
			SAXReader sax = new SAXReader(false);
			//sax.setValidation(false);
			sax.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			sax.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false);
			Document document = sax.read(inputStream);
			return parse(document);
		} catch (DocumentException | SAXException e) {
			throw new PatentReaderException(e);
		}
	}

	/**
	 * Parse Reader (BufferedReader, CharArrayReader, FilterReader,
	 * InputStreamReader, PipedReader, StringReader)
	 * 
	 * @param reader
	 * @return
	 * @throws PatentReaderException
	 */
	public Patent parse(Reader reader) throws PatentReaderException {
		try {
			SAXReader sax = new SAXReader(false);
			sax.setIncludeExternalDTDDeclarations(false);
			sax.setEncoding("UTF-8");
			sax.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			sax.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false);
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
	 * Parse Reader (BufferedReader, CharArrayReader, FilterReader,
	 * InputStreamReader, PipedReader, StringReader)
	 * 
	 * @param reader
	 * @param skipPaths, XPATH paths to skip when creating the document.
	 * @return
	 * @throws PatentReaderException
	 */
	public Patent parse(Reader reader, Iterable<String> skipPaths) throws PatentReaderException {
		try {
			return parse(readLarge(reader, skipPaths));
		} catch (IOException e) {
			throw new PatentReaderException(e);
		}
	}

	/**
	 * Read Large XML using SAX parser, purge xml nodes matching provided paths.
	 * 
	 * @param reader
	 * @param skipExactPaths - Sax paths are literal and does not support xpath,
	 *                       wildcards or tree path transversal.
	 * @return Document
	 * @throws PatentReaderException
	 * @throws IOException
	 */
	public Document readLarge(Reader reader, Iterable<String> skipExactPaths)
			throws PatentReaderException, IOException {

		SAXReader sax = new SAXReader(false);

		ElementHandler skipElementHandler = new ElementHandler() {
			public void onStart(ElementPath path) {
			}

			public void onEnd(ElementPath path) {
				Element el = path.getCurrent();
				Element parent = el.getParent();
				parent.addElement(el.getName()).setText("Note: This field was truncated from the Large XML Document.");
				// System.err.println("Large Field truncated '"+ el.getName() +"' which has
				// content node(s) -> " + el.content().size());
				el.detach();
			}
		};

		for (String path : skipExactPaths) {
			sax.addHandler(path, skipElementHandler);
		}

		try {
			sax.setIncludeExternalDTDDeclarations(false);
			sax.setEncoding("UTF-8");
			// sax.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",
			// false);
			return sax.read(reader);
		} catch (DocumentException e) {
			reader.reset();
			return fixTagsJDOM(IOUtils.toString(reader));
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
		
		System.out.println(doc);
		
		// Add HTML DTD to ensure HTML entities do not cause any problems.
		//doc = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n"
		//		+ doc;
		try {
			SAXReader sax = new SAXReader(false);
			sax.setIncludeExternalDTDDeclarations(false);
			sax.setEncoding("UTF-8");
			sax.setFeature("http://xml.org/sax/features/validation", false);
			sax.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			sax.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false);
			return sax.read(new StringReader(doc));
		} catch (DocumentException | SAXException e) {
			throw new PatentReaderException("Failed to Fix and Parse Docuemnt", e);
		}
	}
}
