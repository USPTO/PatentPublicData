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

    private Map<Evaluator, String> replacements = new HashMap<Evaluator , String>();
    private Collection<String> remove = new HashSet<String>();
    private Collection<HtmlFieldType> removeTypes = new HashSet<HtmlFieldType>();
    private boolean wrapText = false;
    private int wrapWidth = 0;
	private final Boolean prettyPrint;

	/**
	 * 
	 * @param prettyPrint -- print newlines, else print commented-out newlines.
	 */
    public FreetextConfig(Boolean prettyPrint) {
    	this.prettyPrint = prettyPrint;
    }

    public Boolean isPrettyPrint() {
    	return this.prettyPrint;
    }

    /**
     * Replace XML/HTML Element with Text
     * 
     * @param xmlElementName - element name or jsoup element selector
     * @param replacementText
     * @return
     */
    public FreetextConfig replace(String xmlElementName, String replacementText) {
    	replacements.put(QueryParser.parse(xmlElementName), replacementText);
        //replacements.put(xmlElementName, replacementText);
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
    	for(String key: fieldType.getNodeNames()){
    		replacements.put(QueryParser.parse(key), replacementText);
    	}
        return this;
    }

    /**
     * Replace XML/HTML Element with Text
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
        for(HtmlFieldType fieldType: fieldTypes){
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
	 * Default Configuration
	 *
     *<ul>
     *<li>Pretty Print true, newlines between sections</li>
     *<li>remove CROSSREF field</li>
     *<li>replace FIGREF field with "Patent-Figure" text</li>
	 *<li>replace CLAIMREF field with "Patent-Claim"</li>
	 *<li>replace PATCITE field with "Patent-Citation"</li>
     *<li>replace NPLCITE field with "Patent-Citation"</li>
	 *<li>replace NPLCITE field with "Patent-Citation"</li>
     *</ul> 
	 * 
	 * @return FreetextConfig
	 */
	public static FreetextConfig getDefault(){
		FreetextConfig config = new FreetextConfig(true);

		config.remove(HtmlFieldType.CROSSREF);

		config.replace(HtmlFieldType.FIGREF, "Patent-Figure");
		config.replace(HtmlFieldType.CLAIMREF, "Patent-Claim");
		config.replace(HtmlFieldType.PATCITE, "Patent-Citation");
		config.replace(HtmlFieldType.NPLCITE, "Patent-Citation");
		config.replace(HtmlFieldType.NPLCITE, "Patent-Citation");

		return config;
	}

}
