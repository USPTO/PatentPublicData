package gov.uspto.bulkdata.grep;

import java.io.IOException;
import java.io.Writer;

public interface MatchPattern<T> {

	public boolean hasMatch(T doc) throws DocumentException;
	
	public String getMatch();

	public void onlyMatching();
	
	public boolean isOnlyMatching();

	/**
	 * Inverts/Negates Match to return anything not matching
	 */
	public void negate();

	public boolean isNegate();

	public void doNotPrintSource();

	public boolean isPrintSource();

	/**
	 * Write Matches
	 * 
	 * Write output of what matches
	 * 
	 * @param source - written out to help track current document
	 * @param text - text or XML Doc to match against
	 * @param writer - output is written to.
	 * @return
	 * @throws IOException
	 */
	boolean writeMatches(String source, T doc, Writer writer) throws DocumentException, IOException;

}
