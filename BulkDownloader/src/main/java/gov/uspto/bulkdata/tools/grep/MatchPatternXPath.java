package gov.uspto.bulkdata.tools.grep;

import java.io.IOException;
import java.io.Writer;

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
			if (super.hasMatch(node.getNodeValue())){
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

					if (super.isOnlyMatching()) {
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
}
