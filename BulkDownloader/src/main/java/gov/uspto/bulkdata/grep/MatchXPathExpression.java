package gov.uspto.bulkdata.grep;

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

// /*/cat[matches(@name, '^cat$', 'i')]

public class MatchXPathExpression implements MatchPattern<Document> {
	private String XPathExpression;
	private XPathExpression xpathExpression;
	private boolean printSource = true;
	
	public MatchXPathExpression(String XPathExpression) throws XPathExpressionException{
		Preconditions.checkNotNull(XPathExpression);
		this.XPathExpression = XPathExpression;
		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();
		this.xpathExpression = xpath.compile(XPathExpression);
	}

	@Override
	public void negate() {
		// not used... negate should be defined within xpath expression with the xpath "not" function.
	}
	
	@Override
	public void onlyMatching() {
		// not used...	
	}

	@Override
	public boolean isOnlyMatching() {
		// not used...
		return false;
	}

	@Override
	public boolean isNegate() {
		return false;
	}
	
	@Override
	public void doNotPrintSource() {
		this.printSource = false;
	}

	@Override
	public boolean isPrintSource() {
		return printSource;
	}

	public String getXPathExpression() {
		return this.XPathExpression;
	}

	@Override
	public boolean hasMatch(Document document) throws DocumentException {	
		NodeList nodes;
		try {
			nodes = (NodeList) xpathExpression.evaluate(document, XPathConstants.NODESET);
			return (nodes.getLength() > 0);
		} catch (XPathExpressionException e) {
			throw new DocumentException(e);
		}
	}

	@Override
	public String getMatch() {
		// not used.
		return "";
	}
	
	@Override
	public boolean writeMatches(String source, Document document, Writer writer) throws DocumentException, IOException {
		try {
			NodeList nodes = (NodeList) xpathExpression.evaluate(document, XPathConstants.NODESET);

			boolean matched = false;
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				
				if (printSource) {
					writer.write(source);
					writer.write(" : ");
					writer.write(node.getParentNode().getNodeName());
					writer.write("/");
					writer.write(node.getNodeName());
					writer.write(":[");
					writer.write(String.valueOf(i));
					writer.write("] -- ");
				}
				writer.write(node.getTextContent());
				writer.write("\n");
				
				//writer.write(source + " : " + node.getParentNode().getNodeName() + "/" + node.getLocalName() + ":[" + i + "] - " + node.getTextContent() + "\n");
				writer.flush();
				matched = true;
			}
			return matched;
		} catch (XPathExpressionException e) {
			throw new DocumentException(e);
		}
	}


	
}
