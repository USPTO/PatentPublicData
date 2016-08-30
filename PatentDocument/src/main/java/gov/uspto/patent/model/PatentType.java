package gov.uspto.patent.model;

/**
 *  Types of Patents
 */
public enum PatentType {
    UTILITY, // over 90% of patents issued by USPTO.
    DESIGN,
    PLANT,
    REISSUE, // reissue to correct error in issued utility, design or plant patent.
    DEFENSIVE_PUBLICATION, // (DEF) used until 1985-86.
    STATUTORY_INVENTION_REGISTRATION // (SIR) from 1985-86 to 2011.
    ;
}
