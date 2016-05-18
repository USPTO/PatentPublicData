package gov.uspto.parser.dom4j;

import java.io.IOException;
import java.io.StringWriter;

import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

public class Dom4jUtil {

	public static String getTextOrNull(Node document, String xPath) {
		Node node = document.selectSingleNode(xPath);
		return node != null ? node.getText() : null;
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
}
