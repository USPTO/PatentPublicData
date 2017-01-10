package gov.uspto.patent.doc.pap;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.dom4j.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import gov.uspto.parser.dom4j.Dom4JParser;
import gov.uspto.parser.dom4j.Dom4jUtil;
import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.PatentReaderException;
import gov.uspto.patent.doc.pap.fragments.AbstractTextNode;
import gov.uspto.patent.doc.pap.fragments.AgentNode;
import gov.uspto.patent.doc.pap.fragments.ApplicantNode;
import gov.uspto.patent.doc.pap.fragments.ApplicationIdNode;
import gov.uspto.patent.doc.pap.fragments.AssigneeNode;
import gov.uspto.patent.doc.pap.fragments.ClaimNode;
import gov.uspto.patent.doc.pap.fragments.ClassificationNode;
import gov.uspto.patent.doc.pap.fragments.DescriptionNode;
import gov.uspto.patent.doc.pap.fragments.InventorNode;
import gov.uspto.patent.doc.pap.fragments.PriorityClaimNode;
import gov.uspto.patent.doc.pap.fragments.PublicationIdNode;
import gov.uspto.patent.doc.pap.fragments.RelatedIdNode;
import gov.uspto.patent.model.Abstract;
import gov.uspto.patent.model.Claim;
import gov.uspto.patent.model.ClaimTreeBuilder;
import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.Description;
import gov.uspto.patent.model.DocumentId;
import gov.uspto.patent.model.Patent;
import gov.uspto.patent.model.PatentApplication;
import gov.uspto.patent.model.PatentType;
import gov.uspto.patent.model.UsKindCode2PatentType;
import gov.uspto.patent.model.classification.PatentClassification;
import gov.uspto.patent.model.entity.Agent;
import gov.uspto.patent.model.entity.Applicant;
import gov.uspto.patent.model.entity.Assignee;
import gov.uspto.patent.model.entity.Inventor;

/**
 * 
 * Patent Application Publication Parser
 * 
 * Legacy PAP format from 2001-2004
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class PatentAppPubParser extends Dom4JParser {
	private static final Logger LOGGER = LoggerFactory.getLogger(PatentAppPubParser.class);

	private PatentApplication patent;

	public static final String XML_ROOT = "/patent-application-publication";

	@Override
	public Patent parse(Document document) {
		DocumentId publicationId = new PublicationIdNode(document).read();

		DocumentId applicationId = new ApplicationIdNode(document).read();

		if (CountryCode.UNDEFINED.equals(publicationId.getCountryCode())) {
			publicationId = new DocumentId(applicationId.getCountryCode(), publicationId.getDocNumber(),
					publicationId.getKindCode());
		}

		if (publicationId != null) {
			MDC.put("DOCID", publicationId.toText());
		}

		String title = Dom4jUtil.getTextOrNull(document,
				XML_ROOT + "/subdoc-bibliographic-information/technical-information/title-of-invention");

		String dateProduced = Dom4jUtil.getTextOrNull(document,
				XML_ROOT + "/subdoc-bibliographic-information/domestic-filing-data/filing-date");
		
		/*
		 * Patent Type from field or from kindCode.
		 */
		String patentTypeStr = Dom4jUtil
				.getTextOrEmpty(document, XML_ROOT + "/subdoc-bibliographic-information/publication-filing-type")
				.replaceFirst("^new-", "");
		PatentType patentType = null;
		try {
			patentType = PatentType.fromString(patentTypeStr);
		} catch (InvalidDataException e1) {
			patentType = UsKindCode2PatentType.getInstance().lookupPatentType(publicationId.getKindCode());
		}

		List<DocumentId> priorityIds = new PriorityClaimNode(document).read();
		List<DocumentId> relatedIds = new RelatedIdNode(document).read();

		List<Inventor> inventors = new InventorNode(document).read();
		List<Applicant> applicants = new ApplicantNode(document).read();
		List<Agent> agents = new AgentNode(document).read();

		List<Assignee> assignees = new AssigneeNode(document).read();

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
		//	patent.reset();
		//	patent.setDocumentId(publicationId);
		//	patent.setPatentType(patentType);
		//}

		patent.setApplicationId(applicationId);
		patent.addPriorityId(priorityIds);
		patent.addRelationIds(relatedIds);

        patent.addOtherId(patent.getApplicationId());
		patent.addOtherId(patent.getPriorityIds());
		patent.addRelationIds(patent.getOtherIds());

		patent.setTitle(title);
		patent.setAbstract(abstractText);
		patent.setDescription(description);
		patent.setInventor(inventors);
		patent.setAssignee(assignees);
		patent.setApplicant(applicants);
		patent.setAgent(agents);
		// patent.setCitation(citations); // Applications made public don't
		// contain citations.
		patent.setClaim(claims);
		patent.setClassification(classifications);

		if (dateProduced != null) {
			try {
				patent.setDateProduced(dateProduced);
			} catch (InvalidDataException e) {
				LOGGER.error("Invalid Date: {}", dateProduced, e);
			}
		}

		if (dateProduced != null) {
			try {
				patent.setDateProduced(dateProduced);
			} catch (InvalidDataException e) {
				LOGGER.error("Invalid Date: {}", dateProduced, e);
			}
		}

		if (publicationId != null) {
			patent.setDatePublished(publicationId.getDate());
		}

		LOGGER.trace(patent.toString());

		return patent;
	}

	public static void main(String[] args) throws PatentReaderException, IOException {

		File file = new File(args[0]);

		if (file.isDirectory()) {
			int count = 1;
			for (File subfile : file.listFiles()) {
				System.out.println(count++ + " " + subfile.getAbsolutePath());
				PatentAppPubParser papXml = new PatentAppPubParser();
				Patent patent = papXml.parse(subfile);
				if (patent.getAbstract().getPlainText().length() < 90) {
					System.err.println("Abstract too small.");
				}
				if (patent.getDescription().getAllPlainText().length() < 400) {
					System.err.println("Description to small.");
				}
				// System.out.println(patent.toString());
			}
		} else {
			PatentAppPubParser papXml = new PatentAppPubParser();
			Patent patent = papXml.parse(file);
			System.out.println(patent.toString());
		}

	}

}
