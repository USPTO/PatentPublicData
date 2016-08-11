package gov.uspto.bulkdata.find;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.google.common.base.Preconditions;

public class PatternXPath extends MatchXPath {
	private static final Logger LOGGER = LoggerFactory.getLogger(PatternXPath.class);
	
	private final List<XPathExpression> xPaths = new ArrayList<XPathExpression>();
	private String[] xPathExpressions;

	public PatternXPath(String... xPathExpressions) throws XPathExpressionException {
		Preconditions.checkNotNull(xPathExpressions, "xPathExpression can not be null");

		this.xPathExpressions = xPathExpressions;

		XPathFactory fact = XPathFactory.newInstance();
		XPath xpath = fact.newXPath();

		for (String xpathStr : xPathExpressions) {
			XPathExpression xpathExp = xpath.compile(xpathStr);
			xPaths.add(xpathExp);
		}
	}

	@Override
	public boolean matchAll(Document document) throws XPathExpressionException {
		for (XPathExpression xPath : xPaths) {
			String value = xPath.evaluate(document);
			if (value == null || value.length() == 0) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean matchAny(Document document) throws XPathExpressionException {
		for (XPathExpression xPath : xPaths) {
			String value = xPath.evaluate(document);
			if (value != null && value.length() != 0) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return "PatternXPath [xPathExpressions=" + Arrays.toString(xPathExpressions) + "]";
	}
}
