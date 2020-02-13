package gov.uspto.patent.model.classification;

import java.text.ParseException;
import java.util.List;

import gov.uspto.common.tree.Tree;
import gov.uspto.patent.InvalidDataException;

public interface Classification extends Comparable<Classification> {
	public ClassificationType getType();

	/**
	 * Parse Text
	 * 
	 * @param text
	 * @throws ParseException
	 */
	public void parseText(final String text) throws ParseException;

	public String getTextOriginal();

	/**
	 * Get Normalized String interpretation
	 * 
	 * @return
	 */
	public String getTextNormalized();

	public int getDepth();

	/**
	 * Get Search Tokens
	 * 
	 * @return List<String>
	 */
	public List<String> getSearchTokens();
	
	/**
	 * Text representation
	 * 
	 * normal setup is to call getTextOriginal() if empty calls getTextNormalized();
	 */
	public String toText();

	/**
	 * Classification is contained; either equal or under this classification
	 * 
	 * @param check classification
	 * @return boolean
	 */
	public boolean isContained(PatentClassification check);

	public Tree getTree();

	public boolean validate() throws InvalidDataException;
}
