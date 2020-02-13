package gov.uspto.patent.doc.xml.items;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.ItemReader;
import gov.uspto.patent.model.classification.PatentClassification;
import gov.uspto.patent.model.classification.IpcClassification;

public class ClassificationIPCNode extends ItemReader<List<PatentClassification>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ClassificationIPCNode.class);

	private static final XPath IPCXP = DocumentHelper.createXPath("classifications-ipcr|self::classifications-ipcr");
	private static final XPath SECTIONXP = DocumentHelper.createXPath("section");
	private static final XPath MCLASSXP = DocumentHelper.createXPath("class");
	private static final XPath SCLASSXP = DocumentHelper.createXPath("subclass");
	private static final XPath MGROUPXP = DocumentHelper.createXPath("main-group");
	private static final XPath SGROUPXP = DocumentHelper.createXPath("subgroup");
	private static final XPath TYPEXP = DocumentHelper.createXPath("classification-value");

	private static final XPath IPCTXTXP = DocumentHelper.createXPath("classification-ipc|self::classification-ipc");
	private static final XPath MAINCLASSXP = DocumentHelper.createXPath("main-classification");
	private static final XPath FURTHERCLASSXP = DocumentHelper.createXPath("further-classification");

	public ClassificationIPCNode(Node itemNode) {
		super(itemNode);
	}

	@Override
	public List<PatentClassification> read() {

		Node ipcN = IPCXP.selectSingleNode(itemNode);
		if (ipcN != null) {
			return readSectionedFormat(ipcN);
		} else {
			Node ipcTxtN = IPCTXTXP.selectSingleNode(itemNode);
			if (ipcTxtN != null) {
				LOGGER.info(ipcTxtN.getName());
				return readFlatFormat(ipcTxtN);
			}
		}
		
		return Collections.emptyList();
	}

	/**
	 * readSectionedFormat
	 *
	 * <pre>
	 * {@code
	 *<classifications-ipcr>
	 *	<classification-ipcr>
	 *	   <ipc-version-indicator><date>20060101</date></ipc-version-indicator>
	 *     <classification-level>A</classification-level>
	 *     <section>F</section>
	 *     <class>02</class>
	 *     <subclass>K</subclass>
	 *     <main-group>3</main-group>
	 *     <subgroup>12</subgroup>
	 *     <symbol-position>F</symbol-position>
	 *     <classification-value>I</classification-value>
	 *     <action-date><date>20150101</date></action-date>
	 *     <generating-office><country>US</country></generating-office>
	 *     <classification-status>B</classification-status>
	 *     <classification-data-source>H</classification-data-source>
	 *  </classification-ipcr>
	 *<classifications-ipcr>
	 * }
	 * </pre>
	 * 
	 * @return PatentClassification
	 */
	public List<PatentClassification> readSectionedFormat(Node ipcN) {
		List<PatentClassification> ipcClasses = new ArrayList<PatentClassification>();
		if (ipcN == null) {
			return ipcClasses;
		}

		String section = SECTIONXP.selectSingleNode(ipcN).getText();
		String mainClass = MCLASSXP.selectSingleNode(ipcN).getText();
		String subclass = SCLASSXP.selectSingleNode(ipcN).getText();
		String mainGroup = MGROUPXP.selectSingleNode(ipcN).getText();
		String subgroup = SGROUPXP.selectSingleNode(ipcN).getText();

		// classification-value: I = inventive, A = additional
		String type = TYPEXP.selectSingleNode(ipcN).getText();
		boolean inventive = false;
		if ("I".equals(type)) {
			inventive = true;
		}

		IpcClassification ipcClass = new IpcClassification("", inventive);
		ipcClass.setSection(section);
		ipcClass.setMainClass(mainClass);
		ipcClass.setSubClass(subclass);
		ipcClass.setMainGroup(mainGroup);
		ipcClass.setSubGroup(subgroup);

		LOGGER.info(ipcClasses.toString());

		ipcClasses.add(ipcClass);
		return ipcClasses;
	}

	/**
	 * readFlatFormat
	 * 
	 * <pre>
	 * {@code
	 * <classification-ipc> 
	 * 	<edition>07</edition>
	 * 	<main-classification>H04N007/167</main-classification>
	 * 	<further-classification>H04K001/00</further-classification>
	 * </classification-ipc>
	 * }
	 * </pre>
	 * 
	 * @return PatentClassification
	 */
	public List<PatentClassification> readFlatFormat(Node ipcTxtN) {
		List<PatentClassification> ipcClasses = new ArrayList<PatentClassification>();
		if (ipcTxtN == null) {
			return ipcClasses;
		}

		Node mainClass = MAINCLASSXP.selectSingleNode(ipcTxtN);
		String mainClassTxt = mainClass.getText();
		if ("None".equalsIgnoreCase(mainClassTxt)) {
			LOGGER.trace("Invalid IPC classification 'main-classification': 'None'");
			return ipcClasses;
		}

		IpcClassification classification = new IpcClassification(mainClass.getText(), true);
		try {
			classification.parseText(mainClass.getText());
		} catch (ParseException e1) {
			LOGGER.warn("Failed to parse IPC classification 'main-classification': {}", mainClass.asXML());
			return ipcClasses;
		}
		ipcClasses.add(classification);

		List<Node> furtherClasses = FURTHERCLASSXP.selectNodes(ipcTxtN);
		for (Node subclass : furtherClasses) {
			IpcClassification ipcClass = new IpcClassification(subclass.getText(), false);
			try {
				ipcClass.parseText(subclass.getText());
			} catch (ParseException e) {
				LOGGER.warn("Failed to parse IPC classification 'further-classification': {}", subclass.asXML());
			}
			ipcClasses.add(ipcClass);
		}

		LOGGER.info(ipcClasses.toString());
		
		return ipcClasses;
	}
}
