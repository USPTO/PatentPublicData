package gov.uspto.bulkdata;

import static org.junit.Assert.assertEquals;

import javax.xml.xpath.XPathExpressionException;

import org.junit.Test;

import gov.uspto.bulkdata.find.PatternMatcher;
import gov.uspto.bulkdata.find.PatternXpathValueRegex;

public class MatchValueRegexTest {

	@Test
	public void matchPartialIdTrue() throws XPathExpressionException {
		String xmlString = "<xml><doc><id>US123456789</id></doc></xml>";

		PatternXpathValueRegex pattern = new PatternXpathValueRegex("//doc/id", "^US1234.+");
		PatternMatcher matcher = new PatternMatcher();
		matcher.add(pattern);
		assertEquals(true, matcher.match(xmlString));
	}

	@Test
	public void matchPartialIdAttributeTrue() throws XPathExpressionException {
		String xmlString = "<xml><doc id='US123456789'></doc></xml>";

		PatternXpathValueRegex pattern = new PatternXpathValueRegex("//doc/@id", "^US1234.+");
		PatternMatcher matcher = new PatternMatcher();
		matcher.add(pattern);
		assertEquals(true, matcher.match(xmlString));
	}

	public void matchPartialIdFalse() throws XPathExpressionException {
		String xmlString = "<xml><doc><id>US123456789</id></doc></xml>";

		PatternXpathValueRegex pattern = new PatternXpathValueRegex("//doc/id", "^ABCBAD.+");
		PatternMatcher matcher = new PatternMatcher();
		matcher.add(pattern);
		assertEquals(false, matcher.match(xmlString));
	}

	public void matchInvalidXPath() throws XPathExpressionException {
		String xmlString = "<xml><doc><id>US123456789</id></doc></xml>";

		PatternXpathValueRegex pattern = new PatternXpathValueRegex("//BAD/LOCATION", "^ABCBAD.+");
		PatternMatcher matcher = new PatternMatcher();
		matcher.add(pattern);
		assertEquals(false, matcher.match(xmlString));
	}
}
