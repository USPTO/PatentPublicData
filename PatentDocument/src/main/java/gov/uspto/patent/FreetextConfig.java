package gov.uspto.patent;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * XML/HTML to Freetext Configuration
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class FreetextConfig {

    private Map<String, String> replacements = new HashMap<String, String>();
    private Collection<String> remove = new HashSet<String>();
    private Collection<FieldType> removeTypes = new HashSet<FieldType>();

    public enum FieldType {
        HEADER, TABLE, LIST, MATHML
    }

    public FreetextConfig() {
        // empty.
    }

    /**
     * Replace XML/HTML Element with Text
     * 
     * @param xmlElementName - element name or jsoup element selector
     * @param replacementText
     * @return
     */
    public FreetextConfig replace(String xmlElementName, String replacementText) {
        replacements.put(xmlElementName, replacementText);
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

    public boolean keepType(FieldType fieldType) {
        return !removeTypes.contains(fieldType);
    }

    /**
     * Remove XML/HTML Element by FieldType
     * 
     * @param fieldType
     * @return
     */
    public FreetextConfig remove(FieldType... fieldTypes) {
        for(FieldType fieldType: fieldTypes){
            switch (fieldType) {
            case MATHML:
                remove.add("math");
                break;
            }
            
            removeTypes.add(fieldType);
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

    public Map<String, String> getReplaceElements() {
        return replacements;
    }
}
