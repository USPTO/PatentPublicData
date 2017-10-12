package gov.uspto.patent.model;

/**
 * Patent Application
 *
 *<h4>Does not have within Public Patent Applications:</h4>
 *<ul>
 * <li>Assignee field</li>
 * <li>Examiner field</li>
 * <li>Citation field</li>
 *</ul>
 *
 *<h4>Changes allowed after Application Submission:</h4>
 *<ul>
 * <li>Claims and Citations can change</li>
 * <li>Abstract and Description fields can only have small changes which do not change the context (grammar, format, spelling, and clarity)</li>
 *</ul>
 */
public class PatentApplication extends Patent {

    public PatentApplication(DocumentId documentId, PatentType patentType) {
        super(PatentCorpus.PGPUB, documentId, patentType);
    }

}
