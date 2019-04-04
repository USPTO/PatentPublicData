package gov.uspto.bulkdata.tools.grep;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathConstants;

public class XPathExpressionParser {
	
	
	
	public void parseFunction() {
		
	}
	
	/**
	 * Attempt detection of {@link javax.xml.xpath.XPathConstants XPathConstants} by parsing XPath Expression
	 * 
	 * @return
	 */
	public QName getXpathConstraint(String XPathExpression) {
		if (XPathExpression.matches("/+\\*\\[[^\\]]+\\]$")) { // xPath("//*[text() = 'qwerty']")
			return XPathConstants.NODESET;
		}
		else if (XPathExpression.matches("^(count|sum|min|max|avg|number|string-length)\\(.+\\)$")) {
			return XPathConstants.NUMBER;
		} else if (XPathExpression.matches("(.+[!=><]{1,2}\\s?['A-z0-9]+$|^(not|nilled|boolean)\\(.+\\)$|=\\s?[A-z0-9]$)")) {
			return XPathConstants.BOOLEAN;
		} else if (XPathExpression.matches("(.+ /(text|name|local-name)\\(\\)$|^(string|concat)\\(.+\\)$)")) {
			return XPathConstants.STRING;
		}
		return XPathConstants.NODESET;
	}
	
}
