package gov.uspto.patent.model.classification;

import java.text.ParseException;
import java.util.List;

public interface Classification extends Comparable<Classification> {
    public ClassificationType getType();

    /**
     * Classification symbol/parts/sections depth.
     */
    public int getDepth();

    /**
     * Parse Text
     * 
     * @param text
     * @throws ParseException
     */
    public void parseText(final String text) throws ParseException;

    /**
     * Original Text
     * @param originalText
     */
    public void setTextOriginal(final String originalText);

    public String getTextOriginal();

    /**
     * Get Normalized String interpretation
     * @return
     */
    public String getTextNormalized();

    /**
     * Text representation
     * 
     * normal setup is to call getTextOriginal() if empty calls getTextNormalized();
     */
    public String toText();

    /**
     * Classification is contained; either equal or under this classification
     * @param check classification
     * @return boolean
     */
    public boolean isContained(PatentClassification check);

    /**
     * Get Classification parts/sections in string array.
     * @return
     */
    public String[] getParts();

    public String[] toFacet();

    public <T extends PatentClassification> List<T> fromFacets(final List<String> classificationFacets,
            Class<T> clazz);

    public String[] getTree();
}
