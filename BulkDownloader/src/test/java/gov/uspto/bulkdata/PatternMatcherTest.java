package gov.uspto.bulkdata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.xml.xpath.XPathExpressionException;

import org.junit.Test;

public class PatternMatcherTest {

	@Test
	public void matchXPathAttributeStartsWith() throws XPathExpressionException {
		String xmlString = "<xml><doc id='US123456789'></doc></xml>";

		PatternXPath pattern = new PatternXPath("//doc/@id[starts-with(.,'US1234')]");
		PatternMatcher matcher = new PatternMatcher();
		matcher.add(pattern);
		assertTrue(matcher.match(xmlString));
	}

	@Test
	public void matchXPathStartsWith() throws XPathExpressionException {
		String xmlString = "<xml><doc><id>US123456789</id></doc></xml>";

		PatternXPath pattern = new PatternXPath("//doc/id[starts-with(.,'US1234')]");
		PatternMatcher matcher = new PatternMatcher();
		matcher.add(pattern);
		assertTrue(matcher.match(xmlString));
	}

	@Test
	public void matchXPathMultiple() throws XPathExpressionException {
		String xmlString = "<xml><doc id='US123456789'></doc></xml>";

		PatternXPath pattern = new PatternXPath("//doc/id[starts-with(.,'US1234')]", "//doc/@id[starts-with(.,'US1234')]");
		PatternMatcher matcher = new PatternMatcher();
		matcher.add(pattern);
		assertTrue(matcher.match(xmlString));
	}

	@Test
	public void matchPartialIdTrue() throws XPathExpressionException {
		String xmlString = "<xml><doc><id>US123456789</id></doc></xml>";

		PatternXpathValueRegex pattern = new PatternXpathValueRegex("//doc/id", "^US1234.+");
		PatternMatcher matcher = new PatternMatcher();
		matcher.add(pattern);
		assertTrue(matcher.match(xmlString));
	}

	@Test
	public void matchPartialIdAttributeTrue() throws XPathExpressionException {
		String xmlString = "<xml><doc id='US123456789'></doc></xml>";

		PatternXpathValueRegex pattern = new PatternXpathValueRegex("//doc/@id", "^US1234.+");
		PatternMatcher matcher = new PatternMatcher();
		matcher.add(pattern);
		assertTrue(matcher.match(xmlString));
	}

	@Test
	public void matchPartialIdFalse() throws XPathExpressionException {
		String xmlString = "<xml><doc><id>US123456789</id></doc></xml>";

		PatternXpathValueRegex pattern = new PatternXpathValueRegex("//doc/id", "^ABCBAD.+");
		PatternMatcher matcher = new PatternMatcher();
		matcher.add(pattern);
		assertFalse(matcher.match(xmlString));
	}

	@Test
	public void matchInvalidXPath() throws XPathExpressionException {
		String xmlString = "<xml><doc><id>US123456789</id></doc></xml>";

		PatternXpathValueRegex pattern = new PatternXpathValueRegex("//BAD/LOCATION", "^ABCBAD.+");
		PatternMatcher matcher = new PatternMatcher();
		matcher.add(pattern);
		assertFalse(matcher.match(xmlString));
	}
}
