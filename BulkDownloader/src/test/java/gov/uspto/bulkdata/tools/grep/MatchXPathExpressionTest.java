package gov.uspto.bulkdata.tools.grep;

import static org.junit.Assert.*;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Test;

public class MatchXPathExpressionTest {

	@Test
	public void ConstraintNodes() throws XPathExpressionException {
		MatchXPathExpression xpath = new MatchXPathExpression(
				"//description/p[contains(descendant-or-self::text(),'computer')]");
		assertEquals(XPathConstants.NODESET, xpath.getXpathConstraint());
		
		MatchXPathExpression xpath2 = new MatchXPathExpression(
				"//*[text() = 'qwerty']");
		assertEquals(XPathConstants.NODESET, xpath2.getXpathConstraint());

		MatchXPathExpression xpath3 = new MatchXPathExpression(
				"//*[node/text() = 'qwerty']");
		assertEquals(XPathConstants.NODESET, xpath3.getXpathConstraint());
	}

	@Test
	public void ConstraintCountBoolean() throws XPathExpressionException {
		QName expect = XPathConstants.BOOLEAN;

		MatchXPathExpression xpath = new MatchXPathExpression(
				"count(//claim-text/*[contains(text(),' consisting ')]) > 3");
		assertEquals(expect, xpath.getXpathConstraint());

		MatchXPathExpression xpath2 = new MatchXPathExpression(
				"count(//claim-text/*[contains(text(),' consisting ')])>=3");
		assertEquals(expect, xpath2.getXpathConstraint());

		MatchXPathExpression xpath3 = new MatchXPathExpression(
				"//classifications-cpc/main-cpc/classification-cpc[section/text()=A]");
		assertEquals(expect, xpath3.getXpathConstraint());
	}

	@Test
	public void ConstraintNumber() throws XPathExpressionException {
		QName expect = XPathConstants.NUMBER;

		MatchXPathExpression xpath = new MatchXPathExpression(
				"string-length(//addressbook/last-name)");
		assertEquals(expect, xpath.getXpathConstraint());

		MatchXPathExpression xpath2 = new MatchXPathExpression(
				"count(//addressbook/last-name)");
		assertEquals(expect, xpath2.getXpathConstraint());
	}

	@Test
	public void ConstraintString() throws XPathExpressionException {
		QName expect = XPathConstants.STRING;

		MatchXPathExpression xpath = new MatchXPathExpression(
				"concat('CLASS', ' = ', //classification-cpc/class)");
		assertEquals(expect, xpath.getXpathConstraint());
	}

}
