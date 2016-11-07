package gov.uspto.patent.model;

/**
 * Description Sub-Sections
 *
 *<p>
 * Each Sub Section is marked in all documents, but an applicant can provide a custom header text for each sub-section.
 * Often occurring header text is documented within each subsection.
 * Present day header text seem to be somewhat standard, though older patents appear to be more variable especially those in Greenbook format.
 *</p>
 */
public enum DescSection {
    /**
     * Single paragraph describing related applications in the same patent family. Paragraph is not present if no prior patent family exists.
     * Related Application Ids are already available in other fields within the application.
     *
     *<p><pre>
     * RELATED APPLICATIONS
     * CROSS REFERENCE TO RELATED APPLICATION
     * CROSS REFERENCE TO RELATED APPLICATIONS
     * CROSS REFERENCE TO RELATED APPLICATIONS AND PATENTS
     * CROSS-REFERENCE TO THE RELATED APPLICATION
     * REFERENCE TO PRIOR ART
     * CROSS REFERENCE
     * CROSS-REFERENCE
     * CROSS-REFERENCES
     *</pre></p>
     */
    REL_APP_DESC,

    /**
     * Short description of the invention, providing a more detail than the Abstract; usually a few paragraphs.
     * 
     *<p><pre>
     * BRIEF SUMMARY
     * BRIEF DESCRIPTION
     * BACKGROUND OF THE INVENTION
     * BACKGROUND AND DESCRIPTION OF THE INVENTION
     * BACKGROUND AND OBJECTS OF THE INVENTION
     * BACKGROUND, OBJECTIVES AND ADVANTAGES OF THE INVENTION
     * BACKGROUND AND SUMMARY OF THE INVENTION
     * BACKGROUND AND SUMMARY
     * DESCRIPTION OF THE INVENTION
     * OBJECT OF THE INVENTION
     * OBJECTS AND SUMMARY OF THE INVENTION
     * SUMMARY OF ADVANTAGES
     * SUMMARY OF ADVANTAGES OF THE INVENTION
     * SUMMARY, OBJECTS, AND BACKGROUND OF THE INVENTION
     * SUMMARY AND OBJECTIVES OF THE INVENTION
     * SUMMARY OF THE INVENTION
     * SUMMARY
     * TECHNICAL FIELD, BACKGROUND ART, THE INVENTION
     *</pre></p>
     */
    BRIEF_SUMMARY,

    /**
     * Contains short descriptive title for each drawing, a single sentence or fragment for each drawing; sometimes as a list.
     * 
     *<p><pre>
     * DESCRIPTION OF THE DRAWINGS
     * BRIEF DESCRIPTION OF DRAWINGS
     * BRIEF DESCRIPTION OF THE DRAWING
     * BRIEF DESCRIPTION OF THE DRAWINGS
     * BRIEF DESCRIPTION OF THE ILLUSTRATIONS
     * BRIEF DESCRIPTION OF THE SEVERAL VIEWS OF THE DRAWINGS
     * GENERAL DESCRIPTION OF DRAWINGS
     * BRIEF DESCRIPTION OF THE ACCOMPANYING DRAWINGS
     * SURVEY OF THE DRAWINGS
     * DRAWINGS OF PREFERRED EMBODIMENTS
     * THE DRAWINGS
     * DRAWINGS
     *</pre></p>
     * 
     */
    DRAWING_DESC,

    /**
     * Detailed description of patent figures and their interactions contained within the drawings. 
     * Largest sub-section of the description and largest body of text of the entire patent or application.
     * May also contain large tables.
     * 
     *<p><pre>
     * DETAILED DESCRIPTION
     * DETAILED DESCRIPTION OF DRAWINGS
     * DETAILED DESCRIPTION OF OPERATION 
     * DETAILED DESCRIPTION OF THE INVENTION
     * DETAILED DESCRIPTION OF THE PREFERRED EMBODIMENT
     * DETAILED DESCRIPTION OF THE PREFERRED EMBODIMENTS
     * DETAILED DESCRIPTION OF THE ILLUSTRATED EMBODIMENTS
     * DETAILED DESCRIPTION OF THE INVENTION AND A PERFERRED EMBODIMENT
     * DETAILED DESCRIPTION OF THE INVENTION AND THE PREFERRED EMBODIMENTS
     * DETAILED DESCRIPTION OF THE INVENTION AND ITS PREFERRED EMBODIMENTS
     * DETAILED DESCRIPTION OF SPECIFIC EMBODIMENT
     * DETAILED DESCRIPTION OF THE DISCLOSED EMBODIMENT
     * DETAILED DESCRIPTION OF AN EXEMPLARY EMBODIMENT
     * DETAILED DESCRIPTION OF EXAMPLE EMBODIMENTS
     * DETAILED DESCRIPTION OF ONE PREFERRED EMBODIMENT OF THE INVENTION
     * DESCRIPTION OF THE PREFERRED EMBODIMENTS
     * DESCRIPTION OF SPECIFIC EMBODIMENTS
     * DESCRIPTION OF PREFERRED EMBODIMENT
     * DESCRIPTION OF PREFERRED EMBODIMENTS
     * DESCRIPTION OF A PREFERRED EMBODIMENT OF THE INVENTION
     * DESCRIPTION OF THE ILLUSTRATIVE EMBODIMENTS
     * DESCRIPTION OF THE SHOWN EMBODIMENT
     *</pre></p>
     */
    DETAILED_DESC;
}
