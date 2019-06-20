package gov.uspto.patent.doc.xml.items;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.ItemReader;
import gov.uspto.patent.model.classification.PatentClassification;
import gov.uspto.patent.model.classification.IpcClassification;

public class ClassificationIPCNode extends ItemReader<List<PatentClassification>> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ClassificationIPCNode.class);

	public ClassificationIPCNode(Node itemNode) {
		super(itemNode);
	}

	@Override
	public List<PatentClassification> read() {
		if ("classification-ipcr".equals(itemNode.getName())) {
			return readSectionedFormat();
		} else if ("classification-ipc".equals(itemNode.getName())) {
			return readFlatFormat();
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
	public List<PatentClassification> readSectionedFormat() {
		List<PatentClassification> ipcClasses = new ArrayList<PatentClassification>();

		String section = itemNode.selectSingleNode("section").getText();
		String mainClass = itemNode.selectSingleNode("class").getText();
		String subclass = itemNode.selectSingleNode("subclass").getText();
		String mainGroup = itemNode.selectSingleNode("main-group").getText();
		String subgroup = itemNode.selectSingleNode("subgroup").getText();
		// classification-value: I = inventive, A = additional
		String type = itemNode.selectSingleNode("classification-value").getText();

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

		LOGGER.trace("{}", ipcClass);

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
	public List<PatentClassification> readFlatFormat() {
		List<PatentClassification> ipcClasses = new ArrayList<PatentClassification>();

		Node mainClass = itemNode.selectSingleNode("main-classification");
		if (mainClass == null) {
			return ipcClasses;
		}

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

		List<Node> furtherClasses = itemNode.selectNodes("further-classification");
		for (Node subclass : furtherClasses) {
			IpcClassification ipcClass = new IpcClassification(subclass.getText(), false);
			try {
				ipcClass.parseText(subclass.getText());
			} catch (ParseException e) {
				LOGGER.warn("Failed to parse IPC classification 'further-classification': {}", subclass.asXML());
			}
			ipcClasses.add(ipcClass);
		}

		return ipcClasses;
	}
}
