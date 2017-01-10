package gov.uspto.patent.doc.css_brs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import gov.uspto.parser.dom4j.keyvalue.KeyValue;
import gov.uspto.parser.dom4j.keyvalue.KvParser;
import gov.uspto.parser.dom4j.keyvalue.KvReader;
import gov.uspto.parser.dom4j.keyvalue.config.FieldGroup;
import gov.uspto.patent.PatentReaderException;
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
import gov.uspto.patent.doc.greenbook.fragments.RelatedIdNode;
import gov.uspto.patent.model.Citation;
import gov.uspto.patent.model.Claim;
import gov.uspto.patent.model.Description;
import gov.uspto.patent.model.DocumentId;
import gov.uspto.patent.model.Patent;
import gov.uspto.patent.model.PatentGranted;
import gov.uspto.patent.model.PatentType;
import gov.uspto.patent.model.UsKindCode2PatentType;
import gov.uspto.patent.model.classification.PatentClassification;
import gov.uspto.patent.model.entity.Agent;
import gov.uspto.patent.model.entity.Assignee;
import gov.uspto.patent.model.entity.Examiner;
import gov.uspto.patent.model.entity.Inventor;

/**
 * 
 * @see http://www.uspto.gov/sites/default/files/products/
 *      PatentFullTextAPSGreenBook-Documentation.pdf
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 */
public class CssBrs extends KvParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(CssBrs.class);

    @Override
    public Patent parse(Document document) throws PatentReaderException {
        
        DocumentId documentId = new DocumentIdNode(document).read();
        MDC.put("DOCID", documentId.toText());

        PatentType patentType = UsKindCode2PatentType.getInstance().lookupPatentType(documentId.getKindCode());
        
        DocumentId applicationId = new ApplicationIdNode(document).read();

        Node titleN = document.selectSingleNode("/DOCUMENT/TTL");
        String title = titleN != null ? titleN.getText() : null;

        List<Examiner> examiners = new ExaminerNode(document).read();
        List<Inventor> inventors = new InventorNode(document).read();
        List<Assignee> assignees = new AssigneeNode(document).read();
        List<Agent> agents = new AgentNode(document).read();

        Set<PatentClassification> classifications = new ClassificationNode(document).read();

        List<DocumentId> relatedIds = new RelatedIdNode(document).read();
        List<Citation> citations = new CitationNode(document).read();

        /*
         * Formatted Text.
         */
        FormattedText textProcessor = new FormattedText();
        //Abstract abstractText = new AbstractTextNode(document, textProcessor).read();
        Description description = new DescriptionNode(document, textProcessor).read();
        List<Claim> claims = new ClaimNode(document, textProcessor).read();

        /*
         * Building Patent Object.
         */
        Patent patent = new PatentGranted(documentId, patentType);

        if (documentId != null && documentId.getDate() != null) {
            patent.setDatePublished(documentId.getDate());
        }

        if (applicationId != null && applicationId.getDate() != null) {
            patent.setDateProduced(applicationId.getDate());
        }

		patent.setApplicationId(applicationId);
		//patent.addPriorityId(priorityIds);
		//patent.addOtherId(pctRegionalIds);
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

        patent.setClassification(classifications);
        //patent.setAbstract(abstractText);
        patent.setDescription(description);
        patent.setClaim(claims);

        LOGGER.trace(patent.toString());

        return patent;
    }

    public static void main(String[] args) throws PatentReaderException, IOException {

        File inputFile = new File(args[0]);

        List<FieldGroup> fieldGroups = new ArrayList<FieldGroup>();
        fieldGroups.add(new FieldGroup("APPLICANT").addField("AANM", true).addField("AACI", "AAST", "AAZP", "AACO",
                "AATX", "AAGP", "AAAT"));
        fieldGroups.add(new FieldGroup("INVENTOR").addField("INNM", true).addField("INSA", "INCI", "INST", "INZP",
                "INCO", "INTX", "INGP"));
        fieldGroups.add(new FieldGroup("ASSIGNEE").addField("ASNM", true).addField("ASNP", true).addField("ASSA",
                "ASCI", "ASST", "ASCO", "ASPC", "ASTC", "ASZP", "ASTX", "ASGP"));

        //fieldGroups.add(new FieldGroup("AGENT").addField("FIRM", true).addField("PATT", "AATT", "ATTY", "ATTN", "LRNM",
        //        "LRSA", "LRCI", "LRST", "LRZC", "LREA", "LRTX", ""));

        fieldGroups.add(
                new FieldGroup("US_REFERENCE").addField("URPN", true).addField("URPD", "URNM", "URCL", "URCP", "URGP"));

        fieldGroups.add(new FieldGroup("FOREIGN_REFERENCE").addField("FRCO", true).addField("FRPN", "FRPD", "FRCL",
                "FRCP", "FRGP"));

        fieldGroups.add(new FieldGroup("CORRESPONDENCE").addField("COCN", true).addField("CODR"));

        fieldGroups.add(new FieldGroup("PRIOR").addField("PDID", true).addField("PPNR", "PPPD", "PPKC", "PPCC"));

        fieldGroups.add(new FieldGroup("CLAIMS").addField("CLST", true).addField("CLPR"));
        fieldGroups.add(new FieldGroup("ABSTRACT").addField("ABPR", true));
        fieldGroups.add(new FieldGroup("EXAMINERS").addField("ART", true).addField("EXP", "EXA"));

        //fieldGroups.add(new FieldGroup("PCT").addField("PCAN", true).addField("PCAC", "PCAK", "PCAD", "PCDV",
        //       "PCCO", "PCPN", "PCKC", "PCPD", "TDID"));

        //fieldGroups.add(new FieldGroup("CITATION").addField("ASNM", true).addField("RFKC", "RFPD", "RFCO", "RFNM",
        //        "RFNR", "RFON", "RFAD", "RFIP", "RFRS", "RFNP"));
        fieldGroups.add(new FieldGroup("CLAIM").addField("CLTX", true));
        Reader reader = new InputStreamReader(new FileInputStream(inputFile), "UTF-8");

        KvReader kvf = new KvReader();
        // kvf.setSectionSequence(sections);

        List<KeyValue> keyValues = kvf.parse(reader);

        for (KeyValue kv : keyValues) {
            System.out.println(kv.toString());
        }

        Document xmlDoc = kvf.genXml(keyValues, fieldGroups);

        System.out.println(xmlDoc.asXML());
    }

}
