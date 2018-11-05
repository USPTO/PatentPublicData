package gov.uspto.bulkdata.grep;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import gov.uspto.bulkdata.tools.grep.RegexArguments;

public class RegexArgumentsTest {

	@Test
	public void parseSingleRegexString() {
		String regexesStr = "regex1~i";
		List<RegexArguments> regexes = RegexArguments.parseString(regexesStr);

		RegexArguments regex1 = regexes.get(0);
		assertEquals("regex1", regex1.getRegex());
		assertEquals(true, regex1.isIgnoreCase());
	}

	@Test
	public void parseMultiplRegexString() {
		String regexesStr = "'regex1~i','regex2','regex3~if','regex4~f'";
		List<RegexArguments> regexes = RegexArguments.parseString(regexesStr);

		RegexArguments regex1 = regexes.get(0);
		assertEquals("regex1", regex1.getRegex());
		assertEquals(true, regex1.isIgnoreCase());

		RegexArguments regex3 = regexes.get(2);
		assertEquals("regex3", regex3.getRegex());
		assertEquals(true, regex3.isFullMatch());
		assertEquals(true, regex3.isIgnoreCase());		

		//System.out.println(Arrays.toString(regexes.toArray()));
	}

}
