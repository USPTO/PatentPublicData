package gov.uspto.patent.sgml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import gov.uspto.parser.dom4j.Dom4JParser;
import gov.uspto.patent.PatentReaderException;
import gov.uspto.patent.model.Abstract;
import gov.uspto.patent.model.Citation;
import gov.uspto.patent.model.Claim;
import gov.uspto.patent.model.ClaimTreeBuilder;
import gov.uspto.patent.model.Description;
import gov.uspto.patent.model.DocumentId;
import gov.uspto.patent.model.Patent;
import gov.uspto.patent.model.PatentGranted;
import gov.uspto.patent.model.PatentType;
import gov.uspto.patent.model.UsKindCode2PatentType;
import gov.uspto.patent.model.classification.Classification;
import gov.uspto.patent.model.entity.Agent;
import gov.uspto.patent.model.entity.Assignee;
import gov.uspto.patent.model.entity.Examiner;
import gov.uspto.patent.model.entity.Inventor;
import gov.uspto.patent.sgml.fragments.AbstractTextNode;
import gov.uspto.patent.sgml.fragments.AgentNode;
import gov.uspto.patent.sgml.fragments.ApplicationIdNode;
import gov.uspto.patent.sgml.fragments.AssigneeNode;
import gov.uspto.patent.sgml.fragments.CitationNode;
import gov.uspto.patent.sgml.fragments.ClaimNode;
import gov.uspto.patent.sgml.fragments.ClassificationNode;
import gov.uspto.patent.sgml.fragments.DescriptionNode;
import gov.uspto.patent.sgml.fragments.DocumentIdNode;
import gov.uspto.patent.sgml.fragments.ExaminerNode;
import gov.uspto.patent.sgml.fragments.InventorNode;

/**
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 * @see http://www.uspto.gov/sites/default/files/products/PatentGrantSGMLv19-Documentation.pdf
 * @see ST32-US-Grant-025xml.dtd
 */
public class Sgml extends Dom4JParser {
	private static final Logger LOGGER = LoggerFactory.getLogger(Sgml.class);

	public static final String SGML_ROOT = "/PATDOC";

	@Override
	public Patent parse(Document document) throws PatentReaderException {

		DocumentId documentId = new DocumentIdNode(document).read();
		if (documentId != null){
			MDC.put("DOCID", documentId.toText());
		}

		PatentType patentType = UsKindCode2PatentType.getInstance().lookupPatentType(documentId.getKindCode());
		
		DocumentId applicationId = new ApplicationIdNode(document).read();

		Node titleN = document.selectSingleNode("/PATDOC/SDOBI/B500/B540/STEXT/PDAT");
		String title = null;
		if (titleN != null) {
			title = titleN.getText();
		}

		Set<Classification> classifications = new ClassificationNode(document).read();
		List<Inventor> inventors = new InventorNode(document).read();
		List<Assignee> assignees = new AssigneeNode(document).read();
		List<Agent> agents = new AgentNode(document).read();
		List<Examiner> examiners = new ExaminerNode(document).read();
		List<Citation> citations = new CitationNode(document).read();

		/*
		 * Read Formatted Text Fields
		 */
		FormattedText textProcessor = new FormattedText();
		Abstract abstractText = new AbstractTextNode(document, textProcessor).read();
		Description description = new DescriptionNode(document, textProcessor).read();
        List<Claim> claims = new ClaimNode(document, textProcessor).read();
        new ClaimTreeBuilder(claims).build();

		/*
		 * Start Building Patent Object.
		 */
		Patent patent = new PatentGranted(documentId, patentType);

		if (documentId != null && documentId.getDate() != null) {
			patent.setDatePublished(documentId.getDate());
		}


		if (applicationId != null && applicationId.getDate() != null) {
				patent.setDateProduced(applicationId.getDate());
		}

		patent.setApplicationId(applicationId);
		patent.setTitle(title);
		if (classifications != null) {
			patent.addClassification(new ArrayList<Classification>(classifications));
		}
		patent.setInventor(inventors);
		patent.setAssignee(assignees);
		patent.setAgent(agents);
		patent.setExaminer(examiners);
		patent.setAbstract(abstractText);
		patent.setDescription(description);
		patent.setClaim(claims);
		patent.setCitation(citations);

		LOGGER.trace(patent.toString());

		return patent;
	}

	public static void main(String[] args) throws PatentReaderException, IOException {

		File file = new File(args[0]);

		if (file.isDirectory()) {
			int count = 1;
			for (File subfile : file.listFiles()) {
				System.out.println(count++ + " " + subfile.getAbsolutePath());
				Sgml sgml = new Sgml();
				Patent patent = sgml.parse(subfile);
				if (patent.getAbstract().getPlainText().length() < 90) {
					System.err.println("Abstract too small.");
				}
				if (patent.getDescription().getAllPlainText().length() < 400) {
					System.err.println("Description to small.");
				}
				//System.out.println(patent.toString());
			}
		} else {
			Sgml sgml = new Sgml();
			Patent patent = sgml.parse(file);
			System.out.println(patent.toString());
		}

	}

}
