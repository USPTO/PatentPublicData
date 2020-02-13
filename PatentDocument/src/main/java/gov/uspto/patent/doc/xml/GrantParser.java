/**
 * 
 */
package gov.uspto.patent.doc.xml;

import java.util.List;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import gov.uspto.parser.dom4j.Dom4JParser;
import gov.uspto.parser.dom4j.Dom4jUtil;
import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.doc.xml.fragments.AbstractTextNode;
import gov.uspto.patent.doc.xml.fragments.AgentNode;
import gov.uspto.patent.doc.xml.fragments.ApplicantNode;
import gov.uspto.patent.doc.xml.fragments.ApplicationIdNode;
import gov.uspto.patent.doc.xml.fragments.AssigneeNode;
import gov.uspto.patent.doc.xml.fragments.CitationNode;
import gov.uspto.patent.doc.xml.fragments.ClaimNode;
import gov.uspto.patent.doc.xml.fragments.ClassificationNode;
import gov.uspto.patent.doc.xml.fragments.ClassificationSearchNode;
import gov.uspto.patent.doc.xml.fragments.DescriptionNode;
import gov.uspto.patent.doc.xml.fragments.ExaminerNode;
import gov.uspto.patent.doc.xml.fragments.InventorNode;
import gov.uspto.patent.doc.xml.fragments.PublicationIdNode;
import gov.uspto.patent.doc.xml.fragments.PctRegionalIdNode;
import gov.uspto.patent.doc.xml.fragments.PriorityClaims;
import gov.uspto.patent.doc.xml.fragments.RelatedIdNode;
import gov.uspto.patent.doc.xml.fragments.Relations;
import gov.uspto.patent.model.Abstract;
import gov.uspto.patent.model.Citation;
import gov.uspto.patent.model.Claim;
import gov.uspto.patent.model.ClaimTreeBuilder;
import gov.uspto.patent.model.Description;
import gov.uspto.patent.model.DocumentDate;
import gov.uspto.patent.model.DocumentId;
import gov.uspto.patent.model.DocumentIdType;
import gov.uspto.patent.model.Patent;
import gov.uspto.patent.model.PatentGranted;
import gov.uspto.patent.model.PatentType;
import gov.uspto.patent.model.classification.PatentClassification;
import gov.uspto.patent.model.entity.Agent;
import gov.uspto.patent.model.entity.Applicant;
import gov.uspto.patent.model.entity.Assignee;
import gov.uspto.patent.model.entity.Examiner;
import gov.uspto.patent.model.entity.Inventor;

public class GrantParser extends Dom4JParser {
	private static final Logger LOGGER = LoggerFactory.getLogger(GrantParser.class);

	public static final String XML_ROOT = "/us-patent-grant";

	private static final XPath TITLEXP = DocumentHelper
			.createXPath("/us-patent-grant/us-bibliographic-data-grant/invention-title");
	private static final XPath APPTYPEXP = DocumentHelper
			.createXPath("/us-patent-grant/us-bibliographic-data-grant/application-reference/@appl-type");
	private static final XPath PRODDATEXP = DocumentHelper.createXPath("/us-patent-grant/@date-produced");
	private static final XPath PUBDATEXP = DocumentHelper.createXPath("/us-patent-grant/@date-publ");

	@Override
	public Patent parse(Document document) {

		DocumentId publicationId = new PublicationIdNode(document).read();
		if (publicationId != null) {
			MDC.put("DOCID", publicationId.toText());
		} else {
			LOGGER.warn("Publication ID not read!");
		}

		String title = Dom4jUtil.getTextOrNull(document, TITLEXP);

		String dateProduced = Dom4jUtil.getTextOrNull(document, PRODDATEXP);
		String datePublished = Dom4jUtil.getTextOrNull(document, PUBDATEXP);

		DocumentDate dateProducedDate = null;
		if (dateProduced != null) {
			try {
				dateProducedDate = new DocumentDate(dateProduced);
			} catch (InvalidDataException e) {
				LOGGER.warn("{} : {}", e.getMessage(), "dateProduced");
			}
		}

		DocumentDate datePublishedDate = null;
		if (datePublished != null) {
			try {
				datePublishedDate = new DocumentDate(datePublished);
			} catch (InvalidDataException e) {
				LOGGER.warn("{} : {}", e.getMessage(), "datePublished");
			}
		}

		String patentTypeStr = Dom4jUtil.getTextOrNull(document, APPTYPEXP);
		PatentType patentType = PatentType.UNDEFINED;
		try {
			patentType = PatentType.fromString(patentTypeStr);
		} catch (InvalidDataException e1) {
			LOGGER.warn("{} : {}", e1.getMessage(), "patentTypeStr");
		}

		DocumentId applicationId = new ApplicationIdNode(document).read();

		List<DocumentId> priorityIds = new PriorityClaims(document).read();
		List<DocumentId> pctRegionalIds = new PctRegionalIdNode(document).read();
		DocumentId relatedId = new RelatedIdNode(document).read();
		List<DocumentId> relationIds = new Relations(document).read();

		List<Inventor> inventors = new InventorNode(document).read();
		List<Applicant> applicants = new ApplicantNode(document).read();
		List<Agent> agents = new AgentNode(document).read();
		List<Examiner> examiners = new ExaminerNode(document).read();
		List<Assignee> assignees = new AssigneeNode(document).read();

		List<Citation> citations = new CitationNode(document).read();
		Set<PatentClassification> classifications = new ClassificationNode(document).read();
		Set<PatentClassification> searchClassifications = new ClassificationSearchNode(document).read();

		/*
		 * Formated Text
		 */
		FormattedText textProcessor = new FormattedText();
		Abstract abstractText = new AbstractTextNode(document, textProcessor).read();
		Description description = new DescriptionNode(document, textProcessor).read();
		List<Claim> claims = new ClaimNode(document, textProcessor).read();
		new ClaimTreeBuilder(claims).build();

		/*
		 * Start Building Patent Object.
		 */
		PatentGranted patent = new PatentGranted(publicationId, patentType);
		patent.setSource(getSource());
		patent.setDateProduced(dateProducedDate);
		patent.setDatePublished(datePublishedDate);

		patent.setDocumentId(publicationId);

		if (publicationId != null) {
			DocumentId docIdWithoutKind = new DocumentId(publicationId.getCountryCode(), publicationId.getDocNumber());
			docIdWithoutKind.setType(DocumentIdType.PUBLISHED);
			docIdWithoutKind.setDate(publicationId.getDate());
			patent.addOtherId(docIdWithoutKind);
		}

		patent.setApplicationId(applicationId);
		patent.addPriorityId(priorityIds);
		patent.addOtherId(pctRegionalIds);
		patent.addRelationIds(relationIds);

		patent.addOtherId(patent.getApplicationId());
		patent.addOtherId(patent.getPriorityIds());
		patent.addRelationIds(patent.getOtherIds());

		patent.setTitle(title);
		patent.setAbstract(abstractText);
		patent.setDescription(description);

		patent.setExaminer(examiners);
		patent.setAssignee(assignees);
		patent.setInventor(inventors);
		patent.setApplicant(applicants);
		patent.setAgent(agents);

		if (citations != null) {
			patent.setCitation(citations);
		} else {
			LOGGER.warn("Patent Grant did not read any citations: {}", patent.getDocumentId().toText());
		}

		patent.setClaim(claims);
		patent.setClassification(classifications);
		patent.setSearchClassification(searchClassifications);

		LOGGER.trace(patent.toString());

		return patent;
	}

}
