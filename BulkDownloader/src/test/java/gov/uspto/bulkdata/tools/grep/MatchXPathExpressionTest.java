package gov.uspto.bulkdata.tools.grep;

import static org.junit.Assert.*;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Test;

public class MatchXPathExpressionTest {

	@Test
	public void ConstraintNodes() throws XPathExpressionException {
		QName expect = XPathConstants.NODESET;

		MatchXPathExpression xpath = new MatchXPathExpression(
				"//description/p[contains(descendant-or-self::text(),'computer')]");
		assertEquals(expect, xpath.getXpathConstraint());
		
		MatchXPathExpression xpath2 = new MatchXPathExpression(
				"//*[text() = 'qwerty']");
		assertEquals(expect, xpath2.getXpathConstraint());

		MatchXPathExpression xpath3 = new MatchXPathExpression(
				"//*[node/text() = 'qwerty']");
		assertEquals(expect, xpath3.getXpathConstraint());

		MatchXPathExpression xpath6 = new MatchXPathExpression(
				"*/session[contains(comments, 'strong')]");
		assertEquals(expect, xpath6.getXpathConstraint());

		MatchXPathExpression xpath7 = new MatchXPathExpression(
				"//a[not(@id='XX')]");
		assertEquals(expect, xpath7.getXpathConstraint());

		MatchXPathExpression xpath8 = new MatchXPathExpression(
				"//a[not(contains(@id, 'xx'))]");
		assertEquals(expect, xpath8.getXpathConstraint());
		
		MatchXPathExpression xpath9 = new MatchXPathExpression(
				"//a[not(@class)]");
		assertEquals(expect, xpath9.getXpathConstraint());		
		
		MatchXPathExpression xpath5 = new MatchXPathExpression(
				"//child::item[position()=last()]");
		assertEquals(expect, xpath5.getXpathConstraint());

		MatchXPathExpression xpath4 = new MatchXPathExpression(
				"//child::item[position()=3]/.");
		assertEquals(expect, xpath4.getXpathConstraint());
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

		MatchXPathExpression xpath3 = new MatchXPathExpression(
				"count(*/distance[@units='miles'])");
		assertEquals(expect, xpath3.getXpathConstraint());
		
		MatchXPathExpression xpath4 = new MatchXPathExpression(
				"sum(cart/item/@price)");
		assertEquals(expect, xpath4.getXpathConstraint());
		
		MatchXPathExpression xpath5 = new MatchXPathExpression(
				"round(sum(cart/item/@price))");
		assertEquals(expect, xpath5.getXpathConstraint());
	}

	@Test
	public void ConstraintString() throws XPathExpressionException {
		QName expect = XPathConstants.STRING;

		MatchXPathExpression xpath = new MatchXPathExpression(
				"concat('CLASS', ' = ', //classification-cpc/class)");
		assertEquals(expect, xpath.getXpathConstraint());
	}

}
