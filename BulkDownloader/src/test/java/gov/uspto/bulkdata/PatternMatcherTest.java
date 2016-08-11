package gov.uspto.bulkdata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.xml.xpath.XPathExpressionException;

import org.junit.Test;

import gov.uspto.bulkdata.find.PatternMatcher;
import gov.uspto.bulkdata.find.PatternXPath;
import gov.uspto.bulkdata.find.PatternXpathValueRegex;

public class PatternMatcherTest {

	
	//classifications-cpc/main-cpc/classification-cpc[section/text()='H' and class/text()='04' and subclass/text()='N' and main-group[starts-with(.,'21')]]
	
	@Test
	public void matchXpathMutiDocs() throws XPathExpressionException {

		String uspcClass = "333";
		StringBuilder stb = new StringBuilder();
		stb.append("//classification-national/main-classification");
		stb.append("[starts-with(.,'").append(uspcClass).append("')]");
		String patternTxt = stb.toString();
		
		PatternXPath pattern = new PatternXPath(patternTxt);
		PatternMatcher matcher = new PatternMatcher();
		matcher.add(pattern);
		
		String xmlString = "<xml><classification-national><country>US</country><main-classification>333101</main-classification><further-classification>333131</further-classification></classification-national></xml>";
		assertTrue(matcher.match(xmlString));

		String xmlString2 = "<xml><classification-national><country>US</country><main-classification>66000</main-classification><further-classification>333131</further-classification></classification-national></xml>";
		assertFalse(matcher.match(xmlString2));
	}

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

		PatternXPath pattern = new PatternXPath("//doc/id[starts-with(.,'US1234')]",
				"//doc/@id[starts-with(.,'US1234')]");
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
