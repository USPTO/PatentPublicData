package gov.uspto.bulkdata.tools.extractfields;

import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import gov.uspto.bulkdata.RecordProcessor;
import gov.uspto.patent.PatentDocFormat;

public class ExtractFieldsProcessor implements RecordProcessor {

	private final ExtractFieldsConfig config;

	public ExtractFieldsProcessor(ExtractFieldsConfig config) {
		this.config = config;
	}

	@Override
	public void setPatentDocFormat(PatentDocFormat docFormat) {
		// this.patentReader = new PatentReader(docFormat);
	}

	@Override
	public void initialize(Writer writer) throws IOException {
		StringBuilder stbHeader = new StringBuilder();
		for (String key : config.getWantedFields().keySet()) {
			stbHeader.append(key);
			stbHeader.append(",");
		}
		// stbHeader.deleteCharAt(stbHeader.length()-1);
		writer.write("#");
		writer.write(stbHeader.toString());
		writer.write("\n");
	}

	@Override
	public Boolean process(String sourceTxt, String rawRecord, Writer writer) throws IOException {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Document doc = null;
		try {
			javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
			doc = builder.parse(new InputSource(new StringReader(rawRecord)));
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}

		if (config.getMatchExpression() != null) {
			try {
				if (!(boolean) config.getMatchExpression().evaluate(doc, XPathConstants.BOOLEAN)) {
					return false;
				}
			} catch (XPathExpressionException e) {
				e.printStackTrace();
			}
		}

		for (Entry<String, List<XPathExpression>> field : config.getWantedFields().entrySet()) {
			NodeList nodes;
			for (XPathExpression xpath : field.getValue()) {
				try {
					nodes = (NodeList) xpath.evaluate(doc, XPathConstants.NODESET);

					for (int i = 0; i < nodes.getLength(); i++) {
						Node node = nodes.item(i);
						writer.write(node.getTextContent());
						if (i < nodes.getLength() - 1) {
							writer.write("|");
						}
					}

				} catch (XPathExpressionException e) {
					System.err.println("XPathExpressionException: " + field.getKey());
					e.printStackTrace();
				}

			}
			writer.write(",");
		}
		writer.write("\n");
		writer.flush();
		return true;
	}

	@Override
	public void finish(Writer writer) throws IOException {
		// empty.
	}

}
