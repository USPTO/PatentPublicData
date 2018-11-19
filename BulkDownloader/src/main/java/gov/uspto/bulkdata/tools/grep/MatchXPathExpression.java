package gov.uspto.bulkdata.tools.grep;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.namespace.QName;
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

public class MatchXPathExpression implements MatchPattern<Document> {
	private final XPath xpath;
	private String XPathExpression;
	private XPathExpression xpathExpression;
	private boolean printSource = true;
	private boolean onlyMatchingNode = false;
	private QName XpathConstaint = XPathConstants.NODESET;

	public MatchXPathExpression(final String XPathExpression) throws XPathExpressionException {
		Preconditions.checkNotNull(XPathExpression);
		this.XPathExpression = XPathExpression;
		XPathFactory xpathFactory = XPathFactory.newInstance();
		this.xpath = xpathFactory.newXPath();
		this.xpathExpression = xpath.compile(XPathExpression);
		this.XpathConstaint = getXpathConstraint();
	}

	/**
	 * Attempt detection of {@link javax.xml.xpath.XPathConstants XPathConstants} by
	 * parsing XPath Expression
	 * 
	 * @return
	 */
	public QName getXpathConstraint() {
		if (XPathExpression.matches("/+\\*\\[[^\\]]+\\]$")) { // xPath("//*[text() = 'qwerty']")
			return XPathConstants.NODESET;
		}
		// else if
		// (XPathExpression.matches("/(child|descendant|descendant-or-self|parent|ancestor|ancestor-or-self|preceding-sibling|following-sibling|preceding|following|self|attribute)(:?::)?"))
		// { // xPath("//*[text() = 'qwerty']")
		// return XPathConstants.NODESET;
		// }
		else if (XPathExpression
				.matches("^(count|sum|min|max|avg|number|string-length|round|floor|ceiling)\\(.+\\)$")) {
			return XPathConstants.NUMBER;
		} else if (XPathExpression
				.matches("(.+[!=><]{1,2}\\s?['A-z0-9]+$|^(not|nilled|boolean)\\(.+\\)$|=\\s?[A-z0-9]$)")) {
			return XPathConstants.BOOLEAN;
		} else if (XPathExpression.matches("(.+ /(text|name|local-name)\\(\\)$|^(string|concat)\\(.+\\)$)")) {
			return XPathConstants.STRING;
		}
		return XPathConstants.NODESET;
	}

	@Override
	public void negate() {
		// not used... negate should be defined within xpath expression with the xpath
		// "not" function.
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
	public void onlyMatchingNode() {
		this.onlyMatchingNode = true;
	}

	@Override
	public boolean isMatchingNode() {
		return onlyMatchingNode;
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
		try {
			// NodeList nodes = (NodeList) xpathExpression.evaluate(document,
			// XPathConstants.NODESET);
			// return (nodes.getLength() > 0);

			Object evelObj = xpathExpression.evaluate(document, XpathConstaint);
			if (XpathConstaint == XPathConstants.NUMBER) {
				Double num = (Double) evelObj;
				return num > 0;
			} else if (XpathConstaint == XPathConstants.BOOLEAN) {
				Boolean bool = (Boolean) evelObj;
				return bool;
			} else if (XpathConstaint == XPathConstants.STRING) {
				String str = (String) evelObj;
				return str != null && str.length() > 0;
			} else {
				NodeList nodes = (NodeList) evelObj;
				return (nodes.getLength() > 0);
			}

		} catch (XPathExpressionException e) {
			throw new DocumentException(e);
		}
	}

	@Override
	public String getMatch() {
		// not used.
		return "";
	}

	private boolean writeNodes(String source, Writer writer, NodeList nodes) throws IOException {
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

			if (onlyMatchingNode) {
				writer.write(nodeToString(node));
			} else {
				writer.write(node.getTextContent());
			}
			writer.write("\n");

			// writer.write(source + " : " + node.getParentNode().getNodeName() + "/" +
			// node.getLocalName() + ":[" + i + "] - " + node.getTextContent() + "\n");
			writer.flush();
			matched = true;
		}
		return matched;
	}

	private void writeSingleValue(String source, Writer writer, String value) throws IOException {
		if (printSource) {
			writer.write(source);
			writer.write(" : ");
		}
		writer.write(value);
		writer.write("\n");
		writer.flush();

	}

	@Override
	public boolean writeMatches(String source, Document document, Writer writer) throws DocumentException, IOException {
		try {
			// If boolean query and want matching node, then change query to get matching
			// node.
			if (onlyMatchingNode && XpathConstaint == XPathConstants.BOOLEAN) {
				XpathConstaint = XPathConstants.NODESET;
				xpathExpression = xpath.compile(XPathExpression + "/.");
			}

			Object evelObj = xpathExpression.evaluate(document, XpathConstaint);

			if (XpathConstaint == XPathConstants.NUMBER) {
				Double num = (Double) evelObj;
				if (num > 0) {
					writeSingleValue(source, writer, String.valueOf(num));
				}
				return num > 0;
			} else if (XpathConstaint == XPathConstants.BOOLEAN) {
				Boolean bool = (Boolean) evelObj;
				if (bool) {
					writeSingleValue(source, writer, String.valueOf(bool));
				}
				return bool;
			} else if (XpathConstaint == XPathConstants.STRING) {
				String str = (String) evelObj;
				if (str.length() > 0) {
					writeSingleValue(source, writer, str);
				}
				return str.length() > 0;
			} else {
				return writeNodes(source, writer, (NodeList) evelObj);
			}
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
