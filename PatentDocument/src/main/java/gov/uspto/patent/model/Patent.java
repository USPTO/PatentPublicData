package gov.uspto.patent.model;

import java.util.ArrayList;
import java.util.HashSet;
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
 */
public abstract class Patent {

	private PatentType type;
	private DocumentId documentId;
	private List<DocumentId> otherIds = new ArrayList<DocumentId>(); // store regional filing id, or other ids referencing this unique Patent.
	private List<DocumentId> relationIds = new ArrayList<DocumentId>(); // Cross Reference to related Applications or Publications.

	private DocumentDate datePublished;
	private DocumentDate dateProduced;

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
	private List<DocumentId> referenceIds;

	private List<ChemicalFormula> chemFomulas;

	private List<MathFormula> mathFormulas;

	public Patent(PatentType type, DocumentId documentId) {
		this.type = type;
		this.documentId = documentId;
	}

	public void reset() {
		type = null;
		otherIds.clear();
		relationIds.clear();
		referenceIds = null;
		datePublished = null;
		dateProduced = null;
		applicationId = null;
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
		chemFomulas.clear();
		mathFormulas.clear();
	}

	public void setApplicationId(DocumentId documentId) {
		this.applicationId = documentId;
	}

	public DocumentId getApplicationId() {
		return applicationId;
	}

	public void setDocumentId(DocumentId documentId) {
		this.documentId = documentId;
	}

	public DocumentId getDocumentId() {
		return documentId;
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

	public PatentType getType() {
		return type;
	}

	public void setType(PatentType type) {
		this.type = type;
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
		this.inventors = inventors;
	}

	public List<Applicant> getApplicants() {
		return applicants;
	}

	public void setApplicant(List<Applicant> applicants) {
		this.applicants = applicants;
	}

	public List<Assignee> getAssignee() {
		return assignees;
	}

	public void setAssignee(List<Assignee> assignees) {
		this.assignees = assignees;
	}

	/**
	 * Get Legal Agent
	 * @param agents
	 * @return 
	 */
	public List<Agent> getAgent(List<Agent> agents) {
		return agents;
	}

	public void setAgent(List<Agent> agents) {
		this.agents = agents;
	}

	public List<Examiner> getExaminers() {
		return examiners;
	}

	public void setExaminer(List<Examiner> examiners) {
		this.examiners = examiners;
	}

	public List<DocumentId> getOtherIds() {
		return otherIds;
	}

	public void addOtherId(DocumentId otherId) {
		this.otherIds.add(otherId);
	}

	/**
	 * Get Related Patent Ids, patents within same patent family (continuations, ...)
	 * @return
	 */
	public List<DocumentId> getRelationIds() {
		return relationIds;
	}

	public void setRelationIds(List<DocumentId> relationIds) {
		this.relationIds = relationIds;
	}

	public void addRelationIds(List<DocumentId> relationIds) {
		this.relationIds.addAll(relationIds);
	}

	public void addRelationId(DocumentId relationId) {
		this.relationIds.add(relationId);
	}

	public void setReferenceIds(List<DocumentId> referenceIds) {
		this.referenceIds = referenceIds;
	}

	/**
	 * Get Referenced Patent Ids, references to non-related patents.
	 * @return
	 */
	public List<DocumentId> setReferenceIds() {
		return this.referenceIds;
	}

	public List<ChemicalFormula> getChemFomulas() {
		return chemFomulas;
	}

	public void setChemFomulas(List<ChemicalFormula> chemFomulas) {
		this.chemFomulas = chemFomulas;
	}

	public List<MathFormula> getMathFormulas() {
		return mathFormulas;
	}

	public void setMathFormulas(List<MathFormula> mathFormulas) {
		this.mathFormulas = mathFormulas;
	}

	@Override
	public String toString() {
		return "Patent [\n\tdocumentId=" + documentId + ",\n\t applicationId=" + applicationId + ",\n\t otherIds="
				+ otherIds + ",\n\t relationIds=" + relationIds + ",\n\t referenceIds=" + referenceIds
				+ ",\n\t datePublished=" + datePublished + ",\n\t dateProduced=" + dateProduced + ",\n\t title=" + title
				+ ",\n\t abstractText=" + abstractText.getRawText() + ",\n\t description=" + description.getSections()
				+ ",\n\t figures=" + description.getFigures() + ",\n\t type=" + type + ",\n\t citations=" + citations
				+ ",\n\t classifications=" + classifications + ",\n\t claims=" + claims + ",\n\t inventors=" + inventors
				+ ",\n\t assignees=" + assignees + ",\n\t applicant=" + applicants + ",\n\t agent=" + agents
				+ ",\n\t examiners=" + examiners + ",\n\t chemFomulas=" + chemFomulas + ",\n\t mathFormulas="
				+ mathFormulas + "\n\t]";
	}
}
