package gov.uspto.patent.pap;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Set;

import org.dom4j.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import gov.uspto.parser.dom4j.Dom4JParser;
import gov.uspto.parser.dom4j.Dom4jUtil;
import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.PatentParserException;
import gov.uspto.patent.model.Abstract;
import gov.uspto.patent.model.Claim;
import gov.uspto.patent.model.Description;
import gov.uspto.patent.model.DocumentId;
import gov.uspto.patent.model.Patent;
import gov.uspto.patent.model.PatentApplication;
import gov.uspto.patent.model.classification.Classification;
import gov.uspto.patent.model.entity.Assignee;
import gov.uspto.patent.model.entity.Inventor;
import gov.uspto.patent.pap.fragments.AbstractTextNode;
import gov.uspto.patent.pap.fragments.ApplicationIdNode;
import gov.uspto.patent.pap.fragments.AssigneeNode;
//import gov.uspto.patent.pap.fragments.RelatedIdNode;
import gov.uspto.patent.pap.fragments.ClaimNode;
import gov.uspto.patent.pap.fragments.ClassificationNode;
import gov.uspto.patent.pap.fragments.DescriptionNode;
import gov.uspto.patent.pap.fragments.InventorNode;
import gov.uspto.patent.pap.fragments.PublicationIdNode;

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
		String title = Dom4jUtil.getTextOrNull(document,
				XML_ROOT + "/subdoc-bibliographic-information/technical-information/title-of-invention");

		String dateProduced = Dom4jUtil.getTextOrNull(document,
				XML_ROOT + "/subdoc-bibliographic-information/domestic-filing-data/filing-date");

		DocumentId publicationId = new PublicationIdNode(document).read();
		if (publicationId != null){
			MDC.put("DOCID", publicationId.toText());
		}

		DocumentId applicationId = new ApplicationIdNode(document).read();
		//DocumentId relatedId = new RelatedIdNode(document).read();

		List<Inventor> inventors = new InventorNode(document).read();
		List<Assignee> assignees = new AssigneeNode(document).read();

		Set<Classification> classifications = new ClassificationNode(document).read();

		/*
		 * Formated Text
		 */
		FormattedText textProcessor = new FormattedText();
		List<Claim> claims = new ClaimNode(document, textProcessor).read();
		Abstract abstractText = new AbstractTextNode(document, textProcessor).read();
		Description description = new DescriptionNode(document, textProcessor).read();

		/*
		 * Start Building Patent Object.
		 */
		if (patent == null) {
			patent = new PatentApplication(publicationId);
		} else {
			patent.reset();
		}

		patent.setApplicationId(applicationId);
		//patent.addOtherId(applicationId);
		//atent.addOtherId(regionId);
		//patent.addOtherId(relatedId);
		//patent.setRelationIds(relationIds);
		patent.setTitle(title);
		patent.setAbstract(abstractText);
		patent.setDescription(description);
		patent.setInventor(inventors);
		patent.setAssignee(assignees);
		//patent.setApplicant(applicants);
		//patent.setAgent(agents);
		//patent.setCitation(citations);
		patent.setClaim(claims);
		patent.setClassification(classifications);

		if (dateProduced != null) {
			try {
				patent.setDateProduced(dateProduced);
			} catch (InvalidDataException e) {
				LOGGER.error("Invalid Date: {}", dateProduced, e);
			}
		}

		LOGGER.trace(patent.toString());

		return patent;
	}

	public static void main(String[] args) throws FileNotFoundException, PatentParserException {

		File file = new File(args[0]);

		if (file.isDirectory()) {
			int count = 1;
			for (File subfile : file.listFiles()) {
				System.out.println(count++ + " " + subfile.getAbsolutePath());
				PatentAppPubParser papXml = new PatentAppPubParser();
				Patent patent = papXml.parse(subfile);
				if (patent.getAbstract().getProcessedText().length() < 90) {
					System.err.println("Abstract too small.");
				}
				if (patent.getDescription().getAllProcessedText().length() < 400) {
					System.err.println("Description to small.");
				}
				//System.out.println(patent.toString());
			}
		} else {
			PatentAppPubParser papXml = new PatentAppPubParser();
			Patent patent = papXml.parse(file);
			System.out.println(patent.toString());
		}

	}

}
