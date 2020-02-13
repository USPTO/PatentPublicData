package gov.uspto.bulkdata.tools.grep;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Set;

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

import com.google.common.base.Preconditions;

/**
 * Match XML node value against list of values
 * 
 * @author Brian G. Feldman<brian.feldman@uspto.gov>
 *
 */
public class MatchXPathNodeValues implements MatchPattern<Document> {

	private final String XPathNodePath;
	private final Set<String> values;
	private final XPathExpression xpathExpression;

	private boolean printSource = true;
	private boolean onlyMatchingNode = false;

	public MatchXPathNodeValues(String XPathNodePath, Set<String> values) throws XPathExpressionException {
		Preconditions.checkNotNull(XPathNodePath);
		this.XPathNodePath = XPathNodePath;
		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();
		this.xpathExpression = xpath.compile(XPathNodePath);
		this.values = values;
	}

	public String getXpathNodePath() {
		return XPathNodePath;
	}

	public boolean hasMatch(Document document) {
		try {
			String value = (String) xpathExpression.evaluate(document, XPathConstants.STRING);
			if (values.contains(value)) {
				return true;
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		return false;
	}

	@Override
	public String getMatch() {
		// not used
		return "";
	}

	@Override
	public void onlyMatchingNode() {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean isMatchingNode() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onlyMatching() {
		onlyMatchingNode = true;
	}

	@Override
	public boolean isOnlyMatching() {
		return onlyMatchingNode;
	}

	@Override
	public void negate() {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean isNegate() {
		return false;
	}

	@Override
	public void doNotPrintSource() {
		printSource = false;
	}

	@Override
	public boolean isPrintSource() {
		return printSource;
	}

	@Override
	public boolean writeMatches(String source, Document doc, Writer writer) throws DocumentException, IOException {
		if (hasMatch(doc)) {
			writeSingleValue(source, writer, nodeToString(doc));
			return true;
		}
		return false;
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
