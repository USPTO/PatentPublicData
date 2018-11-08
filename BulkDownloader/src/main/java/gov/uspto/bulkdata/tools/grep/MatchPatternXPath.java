package gov.uspto.bulkdata.tools.grep;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.base.Preconditions;

public class MatchPatternXPath extends MatchRegexBase {

	private String XPathNodePath;
	private XPathExpression xpathExpression;

	public MatchPatternXPath(String regex, String XPathNodePath) throws XPathExpressionException{
		super(regex, false); // TODO set ignorecase from config.
		Preconditions.checkNotNull(XPathNodePath);
		this.XPathNodePath = XPathNodePath;
		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();
		this.xpathExpression = xpath.compile(XPathNodePath);
	}
	
	public String getXpathNodePath() {
		return XPathNodePath;
	}

	public boolean hasMatch(Document document) throws XPathExpressionException {	
		NodeList nodes = (NodeList) xpathExpression.evaluate(document, XPathConstants.NODESET);
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (super.hasMatch(node.getTextContent())){
				return true;
			}
		}
		return false;
	}

	public boolean writeMatches(String source, Document document, Writer writer) throws IOException, DocumentException {
		try {
			NodeList nodes = (NodeList) xpathExpression.evaluate(document, XPathConstants.NODESET);

			boolean matched = false;
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				if (node.getNodeValue() != null && super.hasMatch(node.getNodeValue())){

					if (super.isPrintSource()) {
						writer.write(source);
						writer.write(" : ");
						writer.write(node.getParentNode().getNodeName());
						writer.write(node.getNodeName());
						writer.write(":[");
						writer.write(String.valueOf(i));
						writer.write("] - ");
					}

					if (super.isMatchingNode()) {
						writer.write(nodeToString(node));
						writer.write("\n");
					}
					else if (super.isOnlyMatching()) {
						writer.write(super.getMatch());
						writer.write("\n");
						
						while(super.hasNext()) {
							writer.write(super.getMatch());
							writer.write("\n");
						}
					} else {
						writer.write(node.getNodeValue());
						writer.write("\n");
					}
					
					writer.flush();
					matched = true;
				}
			}
			return matched;
		
		} catch (XPathExpressionException e) {
			throw new DocumentException(e);
		}
	}

	private String nodeToString(Node node) {
		StringWriter sw = new StringWriter();
		try {
			Transformer t = TransformerFactory.newInstance().newTransformer();
			t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			t.setOutputProperty(OutputKeys.INDENT, "no");
			if (node.getNodeType() == Node.TEXT_NODE || node.getNodeType() == Node.ATTRIBUTE_NODE) {
				t.transform(new DOMSource(node.getParentNode()), new StreamResult(sw));
			} else {
				t.transform(new DOMSource(node), new StreamResult(sw));
			}
		} catch (TransformerException te) {
			System.out.println("nodeToString Transformer Exception");
		}
		return sw.toString();
	}
}
