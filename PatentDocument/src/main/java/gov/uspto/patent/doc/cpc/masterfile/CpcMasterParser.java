package gov.uspto.patent.doc.cpc.masterfile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import gov.uspto.common.file.filter.SuffixFileFilter;
import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.PatentReaderException;
import gov.uspto.patent.bulk.BulkArchive;
import gov.uspto.patent.bulk.DumpFile;
import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.DocumentDate;
import gov.uspto.patent.model.DocumentId;
import gov.uspto.patent.model.classification.CpcClassification;

/**
 * 
 * Build CSV File from CPC Master
 * 
 * <pre>
 * grant docId, grant doc number, application docId, application doc number [main,further], CpcClassification
 * </pre>
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class CpcMasterParser extends BulkArchive {
    private static final Logger LOGGER = LoggerFactory.getLogger(CpcMasterParser.class);

    private static FileFilter fileFilter = new SuffixFileFilter("xml");

    private DumpFile currentDumpFile;

    public CpcMasterParser(File file) {
        super(file, fileFilter);
    }

    public MasterClassificationRecord parse(CharSequence xmlString) throws PatentReaderException {
        StringReader reader = new StringReader(xmlString.toString());
        return parse(reader);
    }

    public MasterClassificationRecord parse(Reader reader) throws PatentReaderException {
        try {
            SAXReader sax = new SAXReader(false);
            sax.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            Document document = sax.read(reader);
            return parse(document);
        } catch (SAXException e) {
            throw new PatentReaderException(e);
        } catch (DocumentException e) {
            throw new PatentReaderException(e);
        }
    }

    public MasterClassificationRecord parse(Document document) {

        Node root = document.selectSingleNode("/uspat:CPCMasterClassificationFile/uspat:CPCMasterClassificationRecord");
        Node appIdN = root.selectSingleNode("pat:ApplicationIdentification");
        DocumentId appId = readDocumentId(appIdN);

        Node grantIdN = root.selectSingleNode("pat:PatentGrantIdentification");
        DocumentId grantId = readDocumentId(grantIdN);

        Node cpcN = root.selectSingleNode("pat:CPCClassificationBag");
        List<CpcClassification> cpcClass = readCPC(cpcN);

        return new MasterClassificationRecord(grantId, appId, cpcClass);
    }

    public List<CpcClassification> readCPC(Node node) {
        List<CpcClassification> cpcClasses = new ArrayList<CpcClassification>();
        Node mainN = node.selectSingleNode("pat:MainCPC");
        CpcClassification mainCpc = readClass(mainN);
        mainCpc.setIsMainClassification(true);
        cpcClasses.add(mainCpc);

        @SuppressWarnings("unchecked")
        List<Node> furtherCpcN = node.selectNodes("pat:FurtherCPC");
        for (Node futherN : furtherCpcN) {
            CpcClassification cpcClass = readClass(futherN);
            if (cpcClass != null) {
                cpcClass.setIsMainClassification(false);
                mainCpc.addChild(cpcClass);
                cpcClasses.add(cpcClass);
                LOGGER.debug("FURTHER CPC: {}", cpcClass.toText());
            }
        }

        return cpcClasses;
    }

    private CpcClassification readClass(Node node) {
        Node classN = node.selectSingleNode("pat:CPCClassification");
        if (classN == null) {
            return null;
        }

        Node dateVersionN = classN.selectSingleNode("pat:ClassificationVersionDate");
        Node cpcSectionN = classN.selectSingleNode("pat:CPCSection");
        Node cpcClassN = classN.selectSingleNode("pat:Class");
        Node cpcSubClassN = classN.selectSingleNode("pat:Subclass");
        Node cpcMainGroupN = classN.selectSingleNode("pat:MainGroup");
        Node cpcSubGroupN = classN.selectSingleNode("pat:Subgroup");

        CpcClassification cpcClass = new CpcClassification("");
        cpcClass.setSection(cpcSectionN.getText());
        cpcClass.setMainClass(cpcClassN.getText());
        cpcClass.setSubClass(cpcSubClassN.getText());
        cpcClass.setMainGroup(cpcMainGroupN.getText());
        cpcClass.setSubGroup(cpcSubGroupN.getText());
        return cpcClass;
    }

    public DocumentId readDocumentId(Node node) {
        Node countryN = node.selectSingleNode("com:IPOfficeCode");
        Node idN = node.selectSingleNode("pat:PatentNumber|ApplicationNumber/ApplicationNumberText");
        Node kindN = node.selectSingleNode("com:PatentDocumentKindCode");
        Node dateN = node.selectSingleNode("pat:GrantDate");

        String countryTxt = countryN != null ? countryN.getText() : "";
        String idTxt = idN != null ? idN.getText() : "";
        String kindTxt = kindN != null ? kindN.getText() : "";
        String dateTxt = dateN != null ? dateN.getText() : "";
        dateTxt = dateTxt.replaceAll("-", "");

        DocumentDate docDate = null;
        if (!dateTxt.isEmpty()) {
            try {
                docDate = new DocumentDate(dateTxt);
            } catch (InvalidDataException e1) {
                LOGGER.error("Failed to parse date: {}", dateTxt, e1);
            }
        }

        try {
            CountryCode countryCode = CountryCode.fromString(countryTxt);
            DocumentId docId = new DocumentId(countryCode, idTxt, kindTxt);
            docId.setDate(docDate);
            return docId;
        } catch (InvalidDataException e) {
            LOGGER.error("Invalid CountryCode: {}", countryTxt, e);
        }
        return null;
    }

    public static void main(String[] args) throws IOException {
        String filePath = args[0];

        String header = "<?xml version=\"1.0\" ?>\n<uspat:CPCMasterClassificationFile xmlns:uspat=\"patent:uspto:doc:us:gov\" xmlns:com=\"http://www.wipo.int/standards/XMLSchema/ST96/Common\" xmlns:pat=\"http://www.wipo.int/standards/XMLSchema/ST96/Patent\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"patent:uspto:doc:us:gov CPCMasterClassificationFile.xsd\">";
        String footer = "</uspat:CPCMasterClassificationFile>";

        CpcMasterParser cpcMaster = new CpcMasterParser(new File(filePath));
        cpcMaster.open();

        File outFile = new File("cpc_master.csv");
        Writer writer = new BufferedWriter(new FileWriter(outFile));

        while (cpcMaster.hasNext()) {
            try (DumpFile dumpFile = cpcMaster.next()) {
                dumpFile.open();
                while (dumpFile.hasNext()) {
                    LOGGER.info("Processing: {}:{}", dumpFile.getFile(), dumpFile.getCurrentRecCount());
                    String rawRecord = dumpFile.next();
                    if (rawRecord == null){
                        break;
                    }
                    try {
                        MasterClassificationRecord record = cpcMaster.parse(header + rawRecord + footer);

                        writer.write(record.getGrantId().toText(7));
                        writer.write(",");
                        writer.write(record.getGrantId().getDocNumber());
                        writer.write(",");
                        writer.write(record.getAppId().toText());
                        writer.write(",");
                        writer.write(record.getAppId().getDocNumber());
                        writer.write(",");
                        writer.write("main");
                        writer.write(",");
                        writer.write(record.getMainCPC().toText());
                        writer.write("\n");

                        List<CpcClassification> furtherCpcClasses = record.getFutherCPC();
                        if (!furtherCpcClasses.isEmpty()) {
                            for (CpcClassification cpcClass : furtherCpcClasses) {
                                writer.write(record.getGrantId().toText(7));
                                writer.write(",");
                                writer.write(record.getGrantId().getDocNumber());
                                writer.write(",");
                                writer.write(record.getAppId().toText());
                                writer.write(",");
                                writer.write(record.getAppId().getDocNumber());
                                writer.write(",");
                                writer.write("further");
                                writer.write(",");
                                writer.write(cpcClass.toText());
                                writer.write("\n");
                            }
                        }

                        LOGGER.trace("Record: {}", record);

                    } catch (PatentReaderException e) {
                        LOGGER.error("Failed on: {}:{}", dumpFile.getFile(), dumpFile.getCurrentRecCount(), e);
                    }
                }
            }
        }

        writer.close();
        cpcMaster.close();
    }

}
