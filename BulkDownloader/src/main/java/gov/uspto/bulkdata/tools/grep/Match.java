package gov.uspto.bulkdata.tools.grep;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Set;

public interface Match<MatchPattern> {

	void setMatchPatterns(Set<MatchPattern> patterns);

	boolean match(Reader reader) throws DocumentException;

	/**
	 * Match
	 * 
	 * True/False if match exists
	 * 
	 * @param rawDocStr
	 * @return boolean
	 * @throws DocumentException 
	 * @throws IOException 
	 */
	boolean match(String rawDocStr) throws IOException, DocumentException;

	/**
	 * Write matching lines to Writer
	 * 
	 * @param source - only written in the output to track the source of the data.
	 * @param reader
	 * @param writer
	 * @param stopOnFirstMatch
	 * @return boolean
	 * @throws IOException
	 * @throws DocumentException 
	 */
	boolean match(String source, Reader reader, Writer writer, boolean stopOnFirstMatch) throws IOException, DocumentException;
	
}
