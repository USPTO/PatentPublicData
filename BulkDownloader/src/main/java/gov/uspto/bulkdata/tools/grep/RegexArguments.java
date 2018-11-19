package gov.uspto.bulkdata.tools.grep;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexArguments {
	private final String regex;
	private boolean ignoreCase = false;
	private boolean fullMatch = false;

	public RegexArguments(String regex) {
		this.regex = regex;
	}

	public String getRegex() {
		return regex;
	}

	public void setIgnoreCase(boolean bool) {
		this.ignoreCase = bool;
	}

	public Boolean isIgnoreCase() {
		return this.ignoreCase;
	}

	public void setFullMatch(boolean bool) {
		this.fullMatch = bool;
	}

	public Boolean isFullMatch() {
		return this.fullMatch;
	}

	@Override
	public String toString() {
		return "RegexArguments [regex=" + regex + ", ignoreCase=" + ignoreCase + ", fullMatch=" + fullMatch + "]";
	}

	/**
	 * Parse String optionally containing list of regexs
	 * 
	 * Regex modifiers: i = ignore case f = full match
	 * 
	 * regexs="'regex1~if','regex2'"
	 */
	public static List<RegexArguments> parseString(String regexesStr) {
		String[] regexes = regexesStr.split("'\\s*,\\s*'");
		Matcher flagMatcher = Pattern.compile("^\'?(.+)~([if]+)\'?$").matcher("");
		List<RegexArguments> regexs = new ArrayList<RegexArguments>(regexes.length + 1);
		for (String regex : regexes) {
			String flags = null;
			if (flagMatcher.reset(regex).matches()) {
				regex = flagMatcher.group(1);
				flags = flagMatcher.group(2);
			}
			RegexArguments regexArg = new RegexArguments(regex);
			if (flags != null) {
				regexArg.setFullMatch(flags.contains("f"));
				regexArg.setIgnoreCase(flags.contains("i"));
			}
			regexs.add(regexArg);
		}
		return regexs;
	}

	public static Pattern getPattern(RegexArguments regexArg) {
		return Pattern.compile(regexArg.getRegex(), regexArg.isIgnoreCase() ? Pattern.CASE_INSENSITIVE : 0);
	}
}
