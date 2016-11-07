package gov.uspto.patent.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.model.classification.Classification;
import gov.uspto.patent.model.entity.Agent;
import gov.uspto.patent.model.entity.Applicant;
import gov.uspto.patent.model.entity.Assignee;
import gov.uspto.patent.model.entity.ChemicalFormula;
import gov.uspto.patent.model.entity.Examiner;
import gov.uspto.patent.model.entity.Inventor;
import gov.uspto.patent.model.entity.MathFormula;

/**
 *
 * Patent - Right granted by a Patent Office to prevent others from making, using, or selling an invention of the grantee.
 *
 *<h4>Changes allowed to Patents after being Granted:</h4>
 *<ul>
 * <li>Related Ids, patent family may continue to grow after being Granted</li>
 * <li>Assignee; Update available daily within Patent Assignment XML Dump files</li>
 * <li>Classifications; Updates available monthly within Master Classification File Dump files</li>
 *<ul>
 * The original public patent and application bulk dumps are not updated once they are created and made public.
 * Updates are made available within additional dump files listed above. 
 *
 */
public abstract class Patent {

    private PatentCorpus patentCorpus;
    private DocumentId documentId;
    private Set<DocumentId> otherIds = new LinkedHashSet<DocumentId>(); // store regional filing id, or other ids referencing this unique Patent.
    private Set<DocumentId> relationIds = new LinkedHashSet<DocumentId>(); // Cross Reference to related Applications or Publications.
    private Set<DocumentId> referenceIds = new LinkedHashSet<DocumentId>();

    private DocumentDate datePublished;
    private DocumentDate dateProduced;
    private DocumentDate applicationDate;
    private DocumentDate documentDate;

    private String title; // invention-title
    private Abstract abstractText;
    private Description description;

    private List<Citation> citations = new ArrayList<Citation>();
    private Set<Classification> classifications = new HashSet<Classification>();
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
        if (documentId != null){
        	this.documentDate = documentId.getDate();
        }
        this.patentType = patentType;
    }

    public void reset() {
        //patentCorpus = null;
        //patentType = null;
        otherIds.clear();
        relationIds.clear();
        referenceIds.clear();
        datePublished = null;
        dateProduced = null;
        applicationId = null;
        applicationDate = null;
        documentDate = null;
        title = null;
        abstractText = null;
        description = null;
        citations.clear();
        classifications.clear();
        claims.clear();
        inventors.clear();
        assignees.clear();
        applicants.clear();
        agents.clear();
        examiners.clear();
        chemFomulas = null;
        mathFormulas = null;
    }

    public void setApplicationId(DocumentId documentId) {
        this.applicationId = documentId;
        if (documentId != null){
        	this.applicationDate = documentId.getDate();
        }
    }

    public DocumentId getApplicationId() {
        return applicationId;
    }

    public DocumentDate getApplicationDate() {
        return applicationDate;
    }

    public void setDocumentId(DocumentId documentId) {
        this.documentId = documentId;
        this.documentDate = documentId.getDate();
    }

    public DocumentId getDocumentId() {
        return documentId;
    }

    public DocumentDate getDocumentDate() {
        return documentDate;
    }

    public PatentType getPatentType() {
        return patentType;
    }

    public void setPatentType(PatentType patentType) {
        this.patentType = patentType;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public void setClaim(List<Claim> claims) {
        this.claims = claims;
    }

    public List<Citation> getCitations() {
        return citations;
    }

    public void addCitation(Citation citation) {
        citations.add(citation);
    }

    public void setCitation(List<Citation> citations) {
        this.citations.addAll(citations);
    }

    public Description getDescription() {
        return description;
    }

    public void setDescription(Description description) {
        this.description = description;
    }

    public Set<Classification> getClassification() {
        return classifications;
    }

    public void setClassification(Set<Classification> classifications) {
        this.classifications = classifications;
    }

    public void addClassification(List<Classification> classification) {
        classifications.addAll(classification);
    }

    public void addClassification(Classification classification) {
        classifications.add(classification);
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

    public void setInventor(List<Inventor> inventors) {
        if (inventors != null){
            this.inventors = inventors;
        }
    }

    public List<Applicant> getApplicants() {
        return applicants;
    }

    public void setApplicant(List<Applicant> applicants) {
        if (applicants != null){
            this.applicants = applicants;
        }
    }

    public List<Assignee> getAssignee() {
        return assignees;
    }

    public void setAssignee(List<Assignee> assignees) {
        if (assignees != null){
            this.assignees = assignees;
        }
    }

    /**
     * Get Legal Agent
     * @param agents
     * @return 
     */
    public List<Agent> getAgent() {
        return agents;
    }

    public void setAgent(List<Agent> agents) {
        if (agents != null){
            this.agents = agents;
        }
    }

    public List<Examiner> getExaminers() {
        return examiners;
    }

    public void setExaminer(List<Examiner> examiners) {
        if (examiners != null){
            this.examiners = examiners;
        }
    }

    /**
     * Other IDs for Same Patent (Application Id, PCT Regional Patent IDs, Related Patent IDs)
     * 
     * @param otherId
     */
    public Set<DocumentId> getOtherIds() {
        return otherIds;
    }

    public void addOtherId(DocumentId otherId) {
        this.otherIds.add(otherId);
    }

    public void addOtherId(Collection<DocumentId> otherIds) {
        this.otherIds.addAll(otherIds);
    }

    /**
     * Get Related Patent Ids, patents within same patent family (continuations, ...)
     * @return
     */
    public Set<DocumentId> getRelationIds() {
        return relationIds;
    }

    public void addRelationIds(Collection<DocumentId> relationIds) {
        this.relationIds.addAll(relationIds);
    }

    public void addRelationId(DocumentId relationId) {
        this.relationIds.add(relationId);
    }

    /**
     * Get Referenced Patent Ids, references to non-related patents.
     * @return
     */
    public Set<DocumentId> getReferenceIds() {
        return this.referenceIds;
    }

    public void addReferenceIds(Collection<DocumentId> referenceIds) {
        this.referenceIds.addAll(referenceIds);
    }

    public List<ChemicalFormula> getChemFomulas() {
        if (chemFomulas == null){
           return Collections.emptyList();
        }
        return chemFomulas;
    }

    public void setChemFomulas(List<ChemicalFormula> chemFomulas) {
        this.chemFomulas = chemFomulas;
    }

    public List<MathFormula> getMathFormulas() {
        if (mathFormulas == null){
            return Collections.emptyList();
        }
        return mathFormulas;
    }

    public void setMathFormulas(List<MathFormula> mathFormulas) {
        this.mathFormulas = mathFormulas;
    }

    @Override
    public String toString() {
        return "Patent [patentCorpus=" + patentCorpus + ",\n\t documentId=" + documentId + ",\n\t otherIds=" + otherIds
                + ",\n\t relationIds=" + relationIds + ",\n\t datePublished=" + datePublished + ",\n\t dateProduced=" + dateProduced
                + ",\n\t applicationDate=" + applicationDate + ",\n\t documentDate=" + documentDate + ",\n\t title=" + title
                + ",\n\t abstractText=" + abstractText + ",\n\t description=" + description + ",\n\t citations=" + citations
                + ",\n\t classifications=" + classifications + ",\n\t claims=" + claims + ",\n\t inventors=" + inventors
                + ",\n\t assignees=" + assignees + ",\n\t applicants=" + applicants + ",\n\t agents=" + agents + ",\n\t examiners="
                + examiners + ",\n\t applicationId=" + applicationId + ",\n\t referenceIds=" + referenceIds + ",\n\t chemFomulas="
                + chemFomulas + ",\n\t mathFormulas=" + mathFormulas + ",\n\t patentType=" + patentType + "]";
    }
}
