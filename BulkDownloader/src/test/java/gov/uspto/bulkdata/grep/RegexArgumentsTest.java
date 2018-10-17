package gov.uspto.bulkdata.grep;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class RegexArgumentsTest {

	//@Test
	public void parseString() {
		String regexesStr = "'regex1~i','regex2','regex3~if','regex4~f'";
		List<RegexArguments> regexes = RegexArguments.parseString(regexesStr);
		//System.out.println(Arrays.toString(regexes.toArray()));
	}

	//@Test
	public void parseStringSingle() {
		String regexesStr = "regex1~i";
		List<RegexArguments> regexes = RegexArguments.parseString(regexesStr);
		//System.out.println(Arrays.toString(regexes.toArray()));
	}
}
