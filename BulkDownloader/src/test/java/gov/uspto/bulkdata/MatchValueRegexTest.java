package gov.uspto.bulkdata;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MatchValueRegexTest {

	@Test
	public void matchPartialIdTrue() {
		String xmlString = "<xml><doc><id>US123456789</id></doc></xml>";

		Match matcher = new MatchValueRegex("//doc/id", "^US1234.+");
		assertEquals(true, matcher.match(xmlString));
	}

	@Test
	public void matchPartialIdAttributeTrue() {
		String xmlString = "<xml><doc id='US123456789'></doc></xml>";

		Match matcher = new MatchValueRegex("//doc/@id", "^US1234.+");
		assertEquals(true, matcher.match(xmlString));
	}

	public void matchPartialIdFalse() {
		String xmlString = "<xml><doc><id>US123456789</id></doc></xml>";

		Match matcher = new MatchValueRegex("//doc/id", "^ABCBAD.+");
		assertEquals(false, matcher.match(xmlString));
	}

	public void matchInvalidXPath() {
		String xmlString = "<xml><doc><id>US123456789</id></doc></xml>";

		Match matcher = new MatchValueRegex("//BAD/LOCATION", "^ABCBAD.+");
		assertEquals(false, matcher.match(xmlString));
	}
}
