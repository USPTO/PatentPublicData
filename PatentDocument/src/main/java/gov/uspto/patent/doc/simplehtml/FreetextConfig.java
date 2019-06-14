package gov.uspto.patent.doc.simplehtml;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.jsoup.select.Evaluator;
import org.jsoup.select.QueryParser;

/**
 * XML/HTML to Freetext Configuration
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class FreetextConfig {

	private static int DEFAULT_TEXT_INDENT_SIZE = 3;

	private Map<Evaluator, String> replacements = new HashMap<Evaluator, String>();
	private Collection<String> remove = new HashSet<String>();
	private Collection<HtmlFieldType> removeTypes = new HashSet<HtmlFieldType>();
	private boolean wrapText = false;
	private int wrapWidth = 0;
	private final boolean prettyPrint;
	private final boolean indentParagraphs;
	private int textIndentSize = DEFAULT_TEXT_INDENT_SIZE;

	/**
	 * 
	 * @param prettyPrint -- print newlines, else print commented-out newlines.
	 */
	public FreetextConfig(boolean prettyPrint, boolean indentParagraphs) {
		this.prettyPrint = prettyPrint;
		this.indentParagraphs = indentParagraphs;
	}

	public void setTextIndentSize(int size) {
		this.textIndentSize = size;
	}

	public int getTextIndentSize() {
		return textIndentSize;
	}

	public boolean isPrettyPrint() {
		return prettyPrint;
	}

	public boolean isIndentParagraphs() {
		return indentParagraphs;
	}

	/**
	 * Replace XML/HTML Element with Text
	 * 
	 * @param xmlElementName  - element name or jsoup element selector
	 * @param replacementText
	 * @return
	 */
	public FreetextConfig replace(String xmlElementName, String replacementText) {
		replacements.put(QueryParser.parse(xmlElementName), replacementText);
		// replacements.put(xmlElementName, replacementText);
		return this;
	}

	/**
	 * Replace XML/HTML Element with Text
	 * 
	 * @param fieldType
	 * @param replacementText
	 * @return
	 */
	public FreetextConfig replace(HtmlFieldType fieldType, String replacementText) {
		for (String key : fieldType.getNodeNames()) {
			replacements.put(QueryParser.parse(key), replacementText);
		}
		return this;
	}

	/**
	 * Replace entire XML/HTML Element with Text
	 * 
	 * <p>
	 * <code>
	 *  Example, when calling replace("figref"): 
	 *  <p>
	 *  &lt;figref&gt;Fig 1.&lt;figref&gt;  become  PATENT-FIGURE
	 *  </p>
	 * </code>
	 * <p>
	 *
	 * <p>
	 * Use Case: Within an search index "Fig 10B." is not very useful and
	 * PATENT-FIGURE is easily ignored. This may also enhance number searches.
	 * </p>
	 *
	 * @param replacements
	 * @return
	 */
	public FreetextConfig replace(Map<String, String> replacements) {
		replacements.putAll(replacements);
		return this;
	}

	public boolean keepType(HtmlFieldType fieldType) {
		return !removeTypes.contains(fieldType);
	}

	/**
	 * Remove XML/HTML Element by FieldType
	 * 
	 * @param fieldType
	 * @return
	 */
	public FreetextConfig remove(HtmlFieldType... fieldTypes) {
		for (HtmlFieldType fieldType : fieldTypes) {
			removeTypes.add(fieldType);
			remove(Arrays.asList(fieldType.getNodeNames()));
		}

		return this;
	}

	/**
	 * Remove XML/HTML Element
	 * 
	 * @param xmlElementName - element name or jsoup element selector
	 * @return
	 */
	public FreetextConfig remove(String xmlElementName) {
		remove.add(xmlElementName);
		return this;
	}

	/**
	 * Remove XML/HTML Element
	 * 
	 * @param xmlElementName - element name or jsoup element selector
	 * @return
	 */
	public FreetextConfig remove(Collection<String> xmlElementNames) {
		remove.addAll(xmlElementNames);
		return this;
	}

	public Collection<String> getRemoveElements() {
		return remove;
	}

	public Map<Evaluator, String> getReplaceElements() {
		return replacements;
	}

	public boolean isWrapText() {
		return wrapText;
	}

	public void setWrapText(boolean wrapText) {
		this.wrapText = wrapText;
	}

	public int getWrapWidth() {
		return wrapWidth;
	}

	public void setWrapWidth(int wrapWidth) {
		this.wrapWidth = wrapWidth;
	}

	/**
	 * Reduced Text Configuration, useful for Solr Index.
	 *
	 * <ul>
	 * <li>Pretty Print "true", output has newlines ; "false" has commented newline
	 * characters</li>
	 * <li>remove ERROR_ANNOTATED html5 fields: [Del,S,Strike]</li>
	 * <li>remove CROSSREF field</li>
	 * <li>replace FIGREF field with "Patent-Figure" text</li>
	 * <li>replace CLAIMREF field with "Patent-Claim"</li>
	 * <li>replace PATCITE field with "Patent-Citation"</li>
	 * <li>replace NPLCITE field with "Patent-Citation"</li>
	 * <li>replace NPLCITE field with "Patent-Citation"</li>
	 * <li>replace new line "BR" tag with five spaces</li>
	 * </ul>
	 * 
	 * @return FreetextConfig
	 */
	public static FreetextConfig getSolrDefault() {
		FreetextConfig config = new FreetextConfig(true, true);
		config.remove(HtmlFieldType.ERROR_ANNOTATED);
		config.remove(HtmlFieldType.CROSSREF);

		config.replace(HtmlFieldType.FIGREF, "Patent-Figure");
		config.replace(HtmlFieldType.CLAIMREF, "Patent-Claim");
		config.replace(HtmlFieldType.PATCITE, "Patent-Citation");
		config.replace(HtmlFieldType.NPLCITE, "Patent-Citation");
		config.replace(HtmlFieldType.NPLCITE, "Patent-Citation");

		return config;
	}

	/**
	 * Default Configuration
	 * 
	 * @return
	 */
	public static FreetextConfig getDefault() {
		return new FreetextConfig(true, true);
	}

}
