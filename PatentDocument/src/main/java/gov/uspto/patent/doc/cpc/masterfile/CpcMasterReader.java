package gov.uspto.patent.doc.cpc.masterfile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.PatentDocReader;
import gov.uspto.patent.PatentReaderException;
import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.DocumentDate;
import gov.uspto.patent.model.DocumentId;
import gov.uspto.patent.model.classification.CpcClassification;
import gov.uspto.patent.model.classification.PatentClassification;

public class CpcMasterReader implements PatentDocReader<MasterClassificationRecord> {
	private static final Logger LOGGER = LoggerFactory.getLogger(CpcMasterReader.class);

	private static final String header = "<?xml version=\"1.0\" ?>\n<uspat:CPCMasterClassificationFile xmlns:uspat=\"patent:uspto:doc:us:gov\" xmlns:com=\"http://www.wipo.int/standards/XMLSchema/ST96/Common\" xmlns:pat=\"http://www.wipo.int/standards/XMLSchema/ST96/Patent\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"patent:uspto:doc:us:gov CPCMasterClassificationFile.xsd\">";
	private static final String footer = "</uspat:CPCMasterClassificationFile>";

	private Predicate<PatentClassification> classPredicate;

	public void setClassificationPredicate(Predicate<PatentClassification> predicate) {
		this.classPredicate = predicate;
	}

	public Reader wrap(Reader reader) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(reader);

		StringBuilder stb = new StringBuilder();
		stb.append(header);

		String line;
		String lastLine = "";
		while ((line = bufferedReader.readLine()) != null) {
			stb.append(line).append("\n");
			lastLine = line;
		}
		reader.close();

		if (!lastLine.contains("CPCMasterClassificationFile")) {
			stb.append(footer);
		}

		String rawRecord = stb.toString();
		// LOGGER.info(rawRecord);
		// System.exit(1);

		return new StringReader(rawRecord);
	}

	public MasterClassificationRecord read(Reader reader) throws PatentReaderException, IOException {
		Reader reader2 = wrap(reader);

		try {
			SAXReader sax = new SAXReader(false);
			sax.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			Document document = sax.read(reader2);
			return parse(document);
		} catch (SAXException e) {
			throw new PatentReaderException(e);
		} catch (DocumentException e) {
			throw new PatentReaderException(e);
		}
	}

	public MasterClassificationRecord parse(Document document) {

		Node root = document.selectSingleNode("/uspat:CPCMasterClassificationFile/uspat:CPCMasterClassificationRecord");

		Node cpcN = root.selectSingleNode("pat:CPCClassificationBag");
		List<CpcClassification> cpcClass = readCPC(cpcN);

		if (classPredicate != null && !cpcClass.stream().anyMatch(classPredicate)){
			return null;
		}

		DocumentId appId = null;
		Node appNode = root.selectSingleNode("pat:ApplicationIdentification");
		if (appNode != null) {
			appId = readDocumentId(appNode);
		}

		Node pubNode = root.selectSingleNode("pat:PatentPublicationIdentification|pat:PatentGrantIdentification");
		DocumentId pubId = null;
		if (pubNode != null) {
			pubId = readDocumentId(pubNode);
		}

		return new MasterClassificationRecord(appId, pubId, cpcClass);
	}

	public List<CpcClassification> readCPC(Node node) {
		List<CpcClassification> cpcClasses = new ArrayList<CpcClassification>();
		Node mainN = node.selectSingleNode("pat:MainCPC");

		CpcClassification mainCpc = readClass(mainN);

		if (mainCpc == null) {
			// LOGGER.error("Failed to read 'pat:MainCPC': {}", node.asXML());
			return cpcClasses;
		}

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

		CpcClassification cpcClass = new CpcClassification();
		cpcClass.setSection(cpcSectionN.getText());
		cpcClass.setMainClass(cpcClassN.getText());
		cpcClass.setSubClass(cpcSubClassN.getText());
		cpcClass.setMainGroup(cpcMainGroupN.getText());
		cpcClass.setSubGroup(cpcSubGroupN.getText());
		return cpcClass;
	}

	public DocumentId readDocumentId(Node node) {
		Node countryN = node.selectSingleNode("com:IPOfficeCode");
		Node idN = node.selectSingleNode(
				"pat:PublicationNumber|com:ApplicationNumber/com:ApplicationNumberText|pat:PatentNumber");
		Node kindN = node.selectSingleNode("com:PatentDocumentKindCode");
		Node dateN = node.selectSingleNode("com:PublicationDate");

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

}
