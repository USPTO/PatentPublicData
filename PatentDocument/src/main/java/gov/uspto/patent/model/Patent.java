package gov.uspto.patent.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;

import gov.uspto.common.DateRange;
import gov.uspto.patent.DateTextType;
import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.model.classification.PatentClassification;
import gov.uspto.patent.model.entity.Agent;
import gov.uspto.patent.model.entity.Applicant;
import gov.uspto.patent.model.entity.Assignee;
import gov.uspto.patent.model.entity.ChemicalFormula;
import gov.uspto.patent.model.entity.Examiner;
import gov.uspto.patent.model.entity.Inventor;
import gov.uspto.patent.model.entity.MathFormula;

/**
 *
 * Patent - Right granted by a Patent Office to prevent others from making,
 * using, or selling an invention of the grantee.
 *
 * <h4>Changes allowed to Patents after being Granted:</h4>
 * <ul>
 * <li>Related Ids, patent family may continue to grow after being Granted</li>
 * <li>Assignee; Update available daily within Patent Assignment XML Dump files
 * </li>
 * <li>Classifications; Updates available monthly within Master Classification
 * File Dump files</li>
 * <ul>
 * The original public patent and application bulk dumps are not updated once
 * they are created and made public. Updates are made available within
 * additional dump files listed above.
 *
 */
public abstract class Patent {

    private PatentCorpus patentCorpus;
    private DocumentId documentId;
    private Set<DocumentId> priorityIds = new TreeSet<DocumentId>();
    private Set<DocumentId> otherIds = new TreeSet<DocumentId>();
    private Set<DocumentId> relationIds = new TreeSet<DocumentId>();
    private Set<DocumentId> referenceIds = new TreeSet<DocumentId>();

    private DocumentDate datePublished;
    private DocumentDate dateProduced;

    private String title; // invention-title
    private Abstract abstractText;
    private Description description;

    private List<Citation> citations = new ArrayList<Citation>();
    private Set<PatentClassification> classifications = new HashSet<PatentClassification>();
    private List<Claim> claims = new ArrayList<Claim>();

    private List<Inventor> inventors = new ArrayList<Inventor>();
    private List<Assignee> assignees = new ArrayList<Assignee>();
    private List<Applicant> applicants = new ArrayList<Applicant>();
    private List<Agent> agents = new ArrayList<Agent>();
    private List<Examiner> examiners = new ArrayList<Examiner>();
    private DocumentId applicationId;

    private List<ChemicalFormula> chemFomulas;
    private List<MathFormula> mathFormulas;
    private PatentType patentType;

    public Patent(PatentCorpus patentCorpus, DocumentId documentId, PatentType patentType) {
        this.patentCorpus = patentCorpus;
        this.documentId = documentId;
        this.patentType = patentType;
    }

    public void reset() {
        // patentCorpus = null;
        // patentType = null;
        priorityIds = new TreeSet<DocumentId>();
        otherIds = new TreeSet<DocumentId>();
        relationIds = new TreeSet<DocumentId>();
        referenceIds = new TreeSet<DocumentId>();
        datePublished = null;
        dateProduced = null;
        applicationId = null;
        title = null;
        abstractText = null;
        description = null;
        citations = new ArrayList<Citation>();
        classifications = new HashSet<PatentClassification>();
        claims = new ArrayList<Claim>();
        inventors = new ArrayList<Inventor>();
        assignees = new ArrayList<Assignee>();
        applicants = new ArrayList<Applicant>();
        agents = new ArrayList<Agent>();
        examiners = new ArrayList<Examiner>();
        chemFomulas = null;
        mathFormulas = null;
    }

    public void setApplicationId(DocumentId documentId) {
        this.applicationId = documentId;
    }

    public DocumentId getApplicationId() {
        return applicationId;
    }

    public DocumentDate getApplicationDate() {
        if (applicationId != null) {
            return applicationId.getDate();
        } else {
            return null;
        }
    }

    public void setDocumentId(DocumentId documentId) {
        this.documentId = documentId;
    }

    public DocumentId getDocumentId() {
        return documentId;
    }

    public DocumentDate getDocumentDate() {
        if (documentId != null) {
            return documentId.getDate();
        } else {
            return null;
        }
    }

    public PatentType getPatentType() {
        return patentType;
    }

    public void setPatentType(PatentType patentType) {
        this.patentType = patentType;
    }

    public void setTitle(String title) {
        this.title = title != null ? title : "";
    }

    public String getTitle() {
        return title;
    }

    public List<Claim> getClaims() {
        return claims;
    }

    public void addClaim(Claim claim) {
        claims.add(claim);
    }

    public void setClaim(Iterable<Claim> claims) {
        for (Claim claim : claims) {
            addClaim(claim);
        }
    }

    public List<Citation> getCitations() {
        return citations;
    }

    public void addCitation(Citation citation) {
        citations.add(citation);
    }

    public void setCitation(Iterable<Citation> citations) {
        for (Citation citation : citations) {
            addCitation(citation);
        }
    }

    public Description getDescription() {
        return description;
    }

    public void setDescription(Description description) {
        this.description = description;
    }

    public Set<PatentClassification> getClassification() {
        return classifications;
    }

    public void setClassification(Iterable<PatentClassification> classifications) {
        for (PatentClassification clazz : classifications) {
            addClassification(clazz);
        }
    }

    public void addClassification(PatentClassification classification) {
        if (classification != null) {
            classifications.add(classification);
        }
    }

    public PatentCorpus getPatentCorpus() {
        return patentCorpus;
    }

    public void setPatentCorpus(PatentCorpus patentCorpus) {
        this.patentCorpus = patentCorpus;
    }

    public void setDatePublished(DocumentDate datePublished) {
        this.datePublished = datePublished;
    }

    public void setDatePublished(String datePublished) throws InvalidDataException {
        this.datePublished = new DocumentDate(datePublished);
    }

    public DocumentDate getDatePublished() {
        return datePublished;
    }

    public DocumentDate getDateProduced() {
        return dateProduced;
    }

    public void setDateProduced(DocumentDate dateProduced) {
        this.dateProduced = dateProduced;
    }

    public void setDateProduced(String dateProduced) throws InvalidDataException {
        this.dateProduced = new DocumentDate(dateProduced);
    }

    public Abstract getAbstract() {
        return abstractText;
    }

    public void setAbstract(Abstract patentAbstract) {
        this.abstractText = patentAbstract;
    }

    public List<Inventor> getInventors() {
        return inventors;
    }

    public void addInventor(Inventor inventor) {
        inventors.add(inventor);
    }

    public void setInventor(Iterable<Inventor> inventors) {
        for (Inventor inventor : inventors) {
            addInventor(inventor);
        }
    }

    public List<Applicant> getApplicants() {
        return applicants;
    }

    public void addApplicant(Applicant applicant) {
        applicants.add(applicant);
    }

    public void setApplicant(Iterable<Applicant> applicants) {
        for (Applicant applicant : applicants) {
            addApplicant(applicant);
        }
    }

    public List<Assignee> getAssignee() {
        return assignees;
    }

    public void addAssignee(Assignee assignee) {
        assignees.add(assignee);
    }

    public void setAssignee(Iterable<Assignee> assignees) {
        for (Assignee assignee : assignees) {
            addAssignee(assignee);
        }
    }

    /**
     * Get Legal Agent
     * 
     * @param agents
     * @return
     */
    public List<Agent> getAgent() {
        return agents;
    }

    public void setAgent(List<Agent> agents) {
        if (agents != null) {
            this.agents = agents;
        }
    }

    public List<Examiner> getExaminers() {
        if (examiners == null) {
            return Collections.emptyList();
        }
        return examiners;
    }

    public void setExaminer(List<Examiner> examiners) {
        if (examiners != null) {
            this.examiners = examiners;
        }
    }

    /**
     * Priority IDs, Patent Application this application claims as priority.
     */
    public Set<DocumentId> getPriorityIds() {
        return priorityIds;
    }

    public void addPriorityId(DocumentId priorityId) {
        if (priorityId != null) {
            this.priorityIds.add(priorityId);
        }
    }

    public void addPriorityId(Iterable<DocumentId> priorityIds) {
        for (DocumentId id : priorityIds) {
            addPriorityId(id);
        }
    }

    /**
     * Alias Ids or Other Ids for the same Patent Application
     *
     * <ul>
     * <li>Application "Filed" Id</li>
     * <li>Application "Pre-Grant" Publication Id</li>
     * <li>Grant Publication Id</li>
     * <li>Cross filed application PCT Regional Patent ID</li>
     * </ul>
     */
    public Set<DocumentId> getOtherIds() {
        return otherIds;
    }

    public void addOtherId(DocumentId otherId) {
        if (otherId != null) {
            this.otherIds.add(otherId);
        }
    }

    public void addOtherId(Iterable<DocumentId> otherIds) {
        for (DocumentId id : otherIds) {
            addOtherId(id);
        }
    }

    /**
     * Get Related Patent Ids
     * <p>
     * Patents within same patent family (continuations)
     * </p>
     * 
     * @return related DocumentIds
     */
    public Set<DocumentId> getRelationIds() {
        return relationIds;
    }

    public void addRelationIds(Iterable<DocumentId> relationIds) {
        for (DocumentId id : relationIds) {
            addRelationId(id);
        }
    }

    public void addRelationId(DocumentId relationId) {
        if (relationId != null) {
            this.relationIds.add(relationId);
        }
    }

    /**
     * Get Referenced Patent Ids, references to non-related patents.
     * 
     * @return
     */
    public Set<DocumentId> getReferenceIds() {
        return this.referenceIds;
    }

    public void addReferenceId(DocumentId referenceId) {
        if (referenceId != null) {
            this.referenceIds.add(referenceId);
        }
    }

    public void setReferenceIds(Iterable<DocumentId> referenceIds) {
        for (DocumentId referenceId : referenceIds) {
            addReferenceId(referenceId);
        }
    }

    public List<ChemicalFormula> getChemFomulas() {
        if (chemFomulas == null) {
            return Collections.emptyList();
        }
        return chemFomulas;
    }

    public void setChemFomulas(List<ChemicalFormula> chemFomulas) {
        this.chemFomulas = chemFomulas;
    }

    public List<MathFormula> getMathFormulas() {
        if (mathFormulas == null) {
            return Collections.emptyList();
        }
        return mathFormulas;
    }

    public void setMathFormulas(List<MathFormula> mathFormulas) {
        this.mathFormulas = mathFormulas;
    }

    /**
     * Check classifications against Predicate
     * 
     * @param predicate
     * @return
     */
    public boolean match(Predicate<PatentClassification> predicate) {
        if (getClassification() == null) {
            return false;
        }
        return getClassification().parallelStream().anyMatch(predicate);
    }

    /**
     * Check publication date against DateRange
     * 
     * @param dateRange
     * @return
     */
    public boolean match(DateRange dateRange) {
        if (datePublished.getDate() == null) {
            return false;
        }
        return dateRange.between(datePublished.getDate());
    }

    @Override
    public String toString() {
        return "Patent [patentCorpus=" + patentCorpus + ",\n\t documentId=" + documentId + ",\n\t priorityIds="
                + priorityIds + ",\n\t otherIds=" + otherIds + ",\n\t relationIds=" + relationIds
                + ",\n\t datePublished=" + datePublished + ",\n\t dateProduced=" + dateProduced
                + ",\n\t applicationDate=" + getApplicationDate() + ",\n\t documentDate=" + getDocumentDate()
                + ",\n\t title=" + title + ",\n\t abstractText=" + abstractText + ",\n\t description=" + description
                + ",\n\t citations=" + citations + ",\n\t classifications=" + classifications + ",\n\t claims=" + claims
                + ",\n\t inventors=" + inventors + ",\n\t assignees=" + assignees + ",\n\t applicants=" + applicants
                + ",\n\t agents=" + agents + ",\n\t examiners=" + examiners + ",\n\t applicationId=" + applicationId
                + ",\n\t referenceIds=" + referenceIds + ",\n\t chemFomulas=" + chemFomulas + ",\n\t mathFormulas="
                + mathFormulas + ",\n\t patentType=" + patentType + "]";
    }
}
