package gov.uspto.patent.doc.greenbook;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import gov.uspto.parser.dom4j.keyvalue.KvParser;
import gov.uspto.patent.PatentReaderException;
import gov.uspto.patent.doc.greenbook.fragments.AbstractTextNode;
import gov.uspto.patent.doc.greenbook.fragments.AgentNode;
import gov.uspto.patent.doc.greenbook.fragments.ApplicationIdNode;
import gov.uspto.patent.doc.greenbook.fragments.AssigneeNode;
import gov.uspto.patent.doc.greenbook.fragments.CitationNode;
import gov.uspto.patent.doc.greenbook.fragments.ClaimNode;
import gov.uspto.patent.doc.greenbook.fragments.ClassificationNode;
import gov.uspto.patent.doc.greenbook.fragments.DescriptionNode;
import gov.uspto.patent.doc.greenbook.fragments.DocumentIdNode;
import gov.uspto.patent.doc.greenbook.fragments.ExaminerNode;
import gov.uspto.patent.doc.greenbook.fragments.InventorNode;
import gov.uspto.patent.doc.greenbook.fragments.PatentTypeNode;
import gov.uspto.patent.doc.greenbook.fragments.PctRegionalIdNode;
import gov.uspto.patent.doc.greenbook.fragments.PriorityClaimNode;
import gov.uspto.patent.doc.greenbook.fragments.RelatedIdNode;
import gov.uspto.patent.model.Abstract;
import gov.uspto.patent.model.Citation;
import gov.uspto.patent.model.Claim;
import gov.uspto.patent.model.ClaimTreeBuilder;
import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.Description;
import gov.uspto.patent.model.DocumentId;
import gov.uspto.patent.model.Patent;
import gov.uspto.patent.model.PatentGranted;
import gov.uspto.patent.model.PatentType;
import gov.uspto.patent.model.classification.PatentClassification;
import gov.uspto.patent.model.entity.Agent;
import gov.uspto.patent.model.entity.Assignee;
import gov.uspto.patent.model.entity.Examiner;
import gov.uspto.patent.model.entity.Inventor;

/**
 * Greenbook (APS) Patent Format
 * 
 *<p>
 * APS was the acronym for the USPTO's Automated Patent System
 *</p>
 * 
 * @see http://www.uspto.gov/sites/default/files/products/PatentFullTextAPSGreenBook-Documentation.pdf
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 */
public class Greenbook extends KvParser {
	private static final Logger LOGGER = LoggerFactory.getLogger(Greenbook.class);

	private static List<String> MAINTAIN_SPACE_FIELDS = Arrays.asList(new String[] { "TBL" });
	private static List<String> PARAGRAPH_FIELDS = Arrays
			.asList(new String[] { "PAR", "PA1", "PA2", "PA3", "PA4", "PA5", "PAL" });
	private static List<String> HEADER_FIELDS = Arrays.asList(new String[] { "PAC" });
	private static List<String> TABLE_FIELDS = Arrays.asList(new String[] { "TBL" });

	public Greenbook() {
		super(MAINTAIN_SPACE_FIELDS, PARAGRAPH_FIELDS, HEADER_FIELDS, TABLE_FIELDS);
	}

	/*
	 * private static final Set<String> SECTIONS = new HashSet<String>(20);
	 * static { SECTIONS.add("PATN"); SECTIONS.add("INVT");
	 * SECTIONS.add("ASSG"); SECTIONS.add("PRIR"); SECTIONS.add("REIS");
	 * SECTIONS.add("RLAP"); SECTIONS.add("CLAS"); SECTIONS.add("UREF");
	 * SECTIONS.add("FREF"); SECTIONS.add("OREF"); SECTIONS.add("LREP");
	 * SECTIONS.add("PCTA"); SECTIONS.add("ABST"); SECTIONS.add("GOVT");
	 * SECTIONS.add("PARN"); SECTIONS.add("BSUM"); SECTIONS.add("DRWD");
	 * SECTIONS.add("DETD"); SECTIONS.add("CLMS"); SECTIONS.add("DCLM"); }
	 */

	@Override
	public Patent parse(Document document) throws PatentReaderException {

		DocumentId publicationId = new DocumentIdNode(document).read();
		PatentType patentType = PatentType.UNDEFINED;
		if (publicationId != null){
			MDC.put("DOCID", publicationId.toText());
			patentType = new PatentTypeNode(document).read();
			publicationId.setPatentType(patentType);
			
			/*
			 * Assign Kind Code from PatentType
			 */
			if (CountryCode.US.equals(publicationId.getCountryCode())) {
				switch (patentType) {
				case UTILITY:
					// Utility Patent Grant issued prior to January 2, 2001.
					publicationId.setKindCode("A");
					break;
				case PLANT:
					// Plant Patent Grant issued prior to January 2, 2001.
					publicationId.setKindCode("P");
					break;
				case DESIGN:
					publicationId.setKindCode("S");
					break;
				case REISSUE:
					publicationId.setKindCode("E");
					break;
				case STATUTORY_INVENTION_REGISTRATION:
					publicationId.setKindCode("H");
					break;
				case DEFENSIVE_PUBLICATION:
					break;
				case UNDEFINED:
					break;
				default:
					break;
				}
			}
		}


		DocumentId applicationId = new ApplicationIdNode(document).read();

		Node titleN = document.selectSingleNode("/DOCUMENT/PATN/TTL");
		String title = titleN != null ? titleN.getText() : null;

		List<Examiner> examiners = new ExaminerNode(document).read();
		List<Inventor> inventors = new InventorNode(document).read();
		List<Assignee> assignees = new AssigneeNode(document).read();
		List<Agent> agents = new AgentNode(document).read();

		Set<PatentClassification> classifications = new ClassificationNode(document).read();

		List<DocumentId> priorityIds = new PriorityClaimNode(document).read();
		List<DocumentId> relatedIds = new RelatedIdNode(document).read();
		List<Citation> citations = new CitationNode(document).read();

		List<DocumentId> pctRegionalIds = new PctRegionalIdNode(document).read();

		/*
		 * Formatted Text.
		 */
		FormattedText textProcessor = new FormattedText();
		Abstract abstractText = new AbstractTextNode(document, textProcessor).read();
		Description description = new DescriptionNode(document, textProcessor).read();
		List<Claim> claims = new ClaimNode(document, textProcessor).read();

		new ClaimTreeBuilder(claims).build();

		/*
		 * Building Patent Object.
		 */
		Patent patent = new PatentGranted(publicationId, patentType);

		if (publicationId != null && publicationId.getDate() != null) {
			patent.setDatePublished(publicationId.getDate());
		}

		if (applicationId != null && applicationId.getDate() != null) {
			patent.setDateProduced(applicationId.getDate());
		}

		patent.setApplicationId(applicationId);
		patent.addPriorityId(priorityIds);
		patent.addOtherId(pctRegionalIds);
		patent.addRelationIds(relatedIds);

		patent.addOtherId(patent.getApplicationId());
		patent.addOtherId(patent.getPriorityIds());
		patent.addRelationIds(patent.getOtherIds());

		patent.setTitle(title);
		patent.setInventor(inventors);
		patent.setAssignee(assignees);
		patent.setExaminer(examiners);
		patent.setAgent(agents);
		patent.setCitation(citations);

		// patent.setReferenceIds(referencedIds);

		patent.setClassification(classifications);
		patent.setAbstract(abstractText);
		patent.setDescription(description);
		patent.setClaim(claims);

		LOGGER.trace(patent.toString());

		return patent;
	}

	public static void main(String[] args) throws PatentReaderException, IOException {

		String filename = args[0];

		File file = new File(filename);

		/*
		 * if(file.getName().endsWith("\\.zip")){
		 * 
		 * ZipFindFile find = new ZipFindFile(); find.setFileSuffix("xml");
		 * find.setParentPath("corpus/patents/ST32-US-Grant-025xml.dtd/");
		 * 
		 * ZipReader fileInZip = new ZipReader(file, find); fileInZip.open();
		 * 
		 * int count = 0; while(fileInZip.hasNext()){ Reader docTxtStr =
		 * fileInZip.next(); if (docTxtStr == null){ break; } count++; Sgml sgml
		 * = new Sgml(); Patent patent = sgml.parse(docTxtStr);
		 * System.out.println(count + " :: " + patent.toString()); }
		 * fileInZip.close(); } else
		 */
		if (file.isDirectory()) {
			int count = 1;
			for (File subfile : file.listFiles()) {
				System.out.println(count++ + " " + subfile.getAbsolutePath());
				Greenbook greenbook = new Greenbook();
				Patent patent = greenbook.parse(subfile);
			}
		} else {
			Greenbook greenbook = new Greenbook();
			Patent patent = greenbook.parse(file);
			System.out.println(patent.toString());
		}
	}

}
