package gov.uspto.bulkdata.tools.grep;

import java.io.IOException;
import java.io.Writer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;

public class MatchRegexBase implements MatchPattern<CharSequence> {
	private Matcher matcher;
	private String regex;
	private boolean negate = false;
	private boolean partial = true;
	private boolean printSource = true;
	private boolean onlyMatching = false;

	public MatchRegexBase(String regex, Boolean ignoreCase) {
		Preconditions.checkNotNull(regex);
		this.regex = regex;
		this.matcher = Pattern.compile(regex, ignoreCase ? Pattern.CASE_INSENSITIVE : 0).matcher("");
	}

	/**
	 * Constructor
	 * @param regexOrPatternName - only written in output to track the pattern which matched.
	 * @param pattern
	 */
	public MatchRegexBase(String regexOrPatternName, Pattern pattern) {
		Preconditions.checkNotNull(pattern);
		this.regex = regexOrPatternName;
		this.matcher = pattern.matcher("");
	}

	@Override
	public void negate() {
		this.negate  = true;
	}

	@Override
	public boolean isNegate() {
		return this.negate;
	}

	@Override
	public void doNotPrintSource() {
		this.printSource = false;
	}

	@Override
	public boolean isPrintSource() {
		return printSource;
	}

	@Override
	public void onlyMatching() {
		this.onlyMatching  = true;
	}

	@Override
	public boolean isOnlyMatching() {
		return onlyMatching;
	}
	
	/**
	 * Set Matcher to match pattern against entire region of the text
	 * 
	 * @return
	 */
	public MatchRegexBase entire() {
		this.partial  = false;
		return this;
	}

	public String getRegex() {
		return regex;
	}

	public boolean isNegated() {
		return negate;
	}

	private boolean find(CharSequence text) {
		return this.matcher.reset(text).find();
	}

	/**
	 * Check and move Matcher to next match within content.
	 * @return
	 */
	public boolean hasNext() {
		return this.matcher.find();
	}

	private boolean matches(CharSequence text) {
		return this.matcher.reset(text).matches();
	}

	@Override
	public String getMatch() {
		return matcher.group();
	}

	@Override
	public boolean hasMatch(CharSequence text) {
		boolean matched;

		if (partial) {
			matched = find(text);
		} else {
			matched =  matches(text);
		}

		return negate ? !matched : matched;
	}

	@Override
	public boolean writeMatches(String source, CharSequence text, Writer writer) throws IOException {
		if (hasMatch(text)) {
			//if (negate) {
				//writer.write(source + "negate('" + regex + "') - " + text);
				if (printSource) {
					writer.write(source);
					writer.write(" - ");
					
					/*
					if (negate) {
						writer.write("negate('");
					} else {
						writer.write("'");
					}
					
					writer.write(regex);
					
					if (negate) {
						writer.write("') - ");
					}
					else {
						writer.write("' - ");
					}
					*/
				}

				if (onlyMatching) {
					writer.write(getMatch());
				}
				else {
					writer.write(text.toString());
				}
				writer.write("\n");
			//}
			return true;
		}
		return false;
	}

}
