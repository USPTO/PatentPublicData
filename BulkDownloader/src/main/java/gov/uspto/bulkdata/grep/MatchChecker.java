package gov.uspto.bulkdata.grep;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.Set;

import com.google.common.base.Preconditions;

public class MatchChecker implements Match<MatchPattern> {

	protected Set<MatchPattern> matchPatterns;

	public MatchChecker(Set<MatchPattern> matchPatterns) {
		setMatchPatterns(matchPatterns);
	}

	@Override
	public void setMatchPatterns(Set<MatchPattern> patterns) {
		Preconditions.checkNotNull(patterns);
		Preconditions.checkArgument(!patterns.isEmpty());
		this.matchPatterns = patterns;
	}

	public boolean match(String source, CharSequence string, Writer writer, boolean stopOnFirstMatch) throws IOException, DocumentException {
		return match(source, new StringReader(string.toString()), writer, stopOnFirstMatch);
	}


	@Override
	public boolean match(Reader reader) throws DocumentException {
		BufferedReader reader2 = new BufferedReader(reader);		
		try {
			while(reader2.ready()) {
				if (match(reader2.readLine())) {
					return true;
				}
			}
		} catch (IOException e) {
			throw new DocumentException(e);
		}
		return false;
	}

	@Override
	public boolean match(String rawStr) throws DocumentException {
		for(MatchPattern matchPattern: matchPatterns) {
			if (matchPattern.hasMatch(rawStr)) {
				//LOGGER.debug("Matched {} : {}", matchPattern.getRegex(), rawStr);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean match(String source, Reader reader, Writer writer, boolean stopOnFirstMatch) throws IOException, DocumentException {
		int matchCount = 0;
		BufferedReader reader2 = new BufferedReader(reader);

		while(reader2.ready()) {
			String line = reader2.readLine();
			
			for(MatchPattern matchPattern: matchPatterns) {
				if (matchPattern.writeMatches(source, line, writer)) {
					matchCount++;
					if (stopOnFirstMatch) {
						return true;
					}
				}
			}
		}

		return matchCount != 0;
	}
}
