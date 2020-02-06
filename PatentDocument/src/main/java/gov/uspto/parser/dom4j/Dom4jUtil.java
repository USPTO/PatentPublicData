package gov.uspto.parser.dom4j;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class Dom4jUtil {

	public static Document read(String xmlString) throws SAXException, DocumentException {
		SAXReader sax = new SAXReader(false);
		sax.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		return sax.read(new InputSource(new StringReader(xmlString)));
	}

	public static String getTextOrNull(Node document, XPath xPath) {
		Node node = xPath.selectSingleNode(document);
		return node != null ? node.getStringValue() : null;
	}

	public static String getTextOrNull(Node document, String xPath) {
		Node node = document.selectSingleNode(xPath);
		return node != null ? node.getStringValue() : null;
	}

	/**
	 * Get Text or Empty if Null
	 * 
	 * @param document
	 * @param xPath
	 * @return value or empty instead of null
	 */
	public static String getTextOrEmpty(Node document, XPath xPath) {
		String value = getTextOrNull(document, xPath);
		return value != null ? value : "";
	}

	/**
	 * Get Text or Empty if Null
	 * 
	 * @param document
	 * @param xPath
	 * @return value or empty instead of null
	 */
	public static String getTextOrEmpty(Node document, String xPath) {
		String value = getTextOrNull(document, xPath);
		return value != null ? value : "";
	}

	/**
	 * Pretty Print XML of Dom4j Document
	 * 
	 * @param document
	 * @return
	 */
	public static String toText(Document document) {
		StringWriter sw = new StringWriter();
		OutputFormat format = OutputFormat.createPrettyPrint();
		XMLWriter xw = new XMLWriter(sw, format);

		try {
			xw.write(document);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return sw.toString();
	}

	public static void serializeDom(Writer writer, Document document, Charset outCharset, boolean prettyPrint) throws IOException {
		//Charset outCharset = StandardCharsets.UTF_8;
		//new FileWriter(outDir.resolve(outFileName).toFile()
		OutputFormat outFormat;
		if (prettyPrint) {
			outFormat = OutputFormat.createPrettyPrint();
		} else {
			outFormat = OutputFormat.createCompactFormat();
		}
		outFormat.setEncoding(outCharset.name());
		XMLWriter xmlWriter = new XMLWriter(writer, outFormat);
		xmlWriter.write(document);
	}
}
