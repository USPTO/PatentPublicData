/**
 * 
 */
package gov.uspto.patent.doc.xml;

import java.util.List;
import java.util.Set;

import org.dom4j.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import gov.uspto.common.text.StringCaseUtil;
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
import gov.uspto.patent.doc.xml.fragments.DescriptionNode;
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
import gov.uspto.patent.model.Patent;
import gov.uspto.patent.model.PatentApplication;
import gov.uspto.patent.model.PatentType;
import gov.uspto.patent.model.classification.PatentClassification;
import gov.uspto.patent.model.entity.Agent;
import gov.uspto.patent.model.entity.Applicant;
import gov.uspto.patent.model.entity.Assignee;
import gov.uspto.patent.model.entity.Inventor;

public class ApplicationParser extends Dom4JParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationParser.class);

    private PatentApplication patent;

    public static final String XML_ROOT = "/us-patent-application";

    @Override
    public Patent parse(Document document) {

        DocumentId publicationId = new PublicationIdNode(document).read();
        if (publicationId != null) {
            MDC.put("DOCID", publicationId.toText());
        }
        
        String title = Dom4jUtil.getTextOrNull(document,
                XML_ROOT + "/us-bibliographic-data-application/invention-title");
        title = StringCaseUtil.toTitleCase(title);

        String dateProduced = Dom4jUtil.getTextOrNull(document, XML_ROOT + "/@date-produced");
        String datePublished = Dom4jUtil.getTextOrNull(document, XML_ROOT + "/@date-publ");

        DocumentDate dateProducedDate = null;
        if (dateProduced != null) {
            try {
                dateProducedDate = new DocumentDate(dateProduced);
            } catch (InvalidDataException e) {
                LOGGER.warn("Invalid Date Produced: '{}'", dateProduced, e);
            }
        }

        DocumentDate datePublishedDate = null;
        if (datePublished != null) {
            try {
                datePublishedDate = new DocumentDate(datePublished);
            } catch (InvalidDataException e) {
                LOGGER.warn("Invalid Date Published: '{}'", datePublished, e);
            }
        }

        String patentTypeStr = Dom4jUtil.getTextOrNull(document, XML_ROOT + "/us-bibliographic-data-application/application-reference/@appl-type");
        PatentType patentType = PatentType.UNDEFINED;
        try {
            patentType = PatentType.fromString(patentTypeStr);
        } catch (InvalidDataException e1) {
            LOGGER.warn("Invalid Patent Type: '{}'", patentTypeStr, e1);
        }

        DocumentId applicationId = new ApplicationIdNode(document).read();

        List<DocumentId> priorityIds = new PriorityClaims(document).read();
        List<DocumentId> pctRegionalIds = new PctRegionalIdNode(document).read();
        DocumentId relatedId = new RelatedIdNode(document).read();
        List<DocumentId> relationIds = new Relations(document).read();

        List<Inventor> inventors = new InventorNode(document).read();
        List<Applicant> applicants = new ApplicantNode(document).read();
        List<Agent> agents = new AgentNode(document).read();
        List<Assignee> assignees = new AssigneeNode(document).read();
        
        List<Citation> citations = new CitationNode(document).read();
        Set<PatentClassification> classifications = new ClassificationNode(document).read();

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
        //if (patent == null) {
            patent = new PatentApplication(publicationId, patentType);
        //} else {
        //    patent.reset();
		//	  patent.setDocumentId(publicationId);
		//	  patent.setPatentType(patentType);
        //}

        patent.setDateProduced(dateProducedDate);
        patent.setDatePublished(datePublishedDate);

        patent.setDocumentId(publicationId);
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
        patent.setInventor(inventors);
        patent.setApplicant(applicants);
        patent.setAgent(agents);
        patent.setAssignee(assignees);
        patent.setCitation(citations);
        patent.setClaim(claims);
        patent.setClassification(classifications);

		LOGGER.trace(patent.toString());
        
        return patent;
    }

}
