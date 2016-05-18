package gov.uspto.patent.xml.items;

import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.ItemReader;
import gov.uspto.patent.model.classification.Classification;
import gov.uspto.patent.model.classification.IpcClassification;

/*
<classifications-ipcr>
	<classification-ipcr>
		<ipc-version-indicator><date>20060101</date></ipc-version-indicator>
		<classification-level>A</classification-level>
		<section>F</section>
		<class>02</class>
		<subclass>K</subclass>
		<main-group>3</main-group>
		<subgroup>12</subgroup>
		<symbol-position>F</symbol-position>
		<classification-value>I</classification-value>
		<action-date><date>20150101</date></action-date>
		<generating-office><country>US</country></generating-office>
		<classification-status>B</classification-status>
		<classification-data-source>H</classification-data-source>
	</classification-ipcr>
<classifications-ipcr>

*/

public class ClassificationIPCNode extends ItemReader<Classification>{
	private static final Logger LOGGER = LoggerFactory.getLogger(ClassificationIPCNode.class);

	public ClassificationIPCNode(Node itemNode){
		super(itemNode);
	}

	@Override
	public Classification read() {
		String section = itemNode.selectSingleNode("section").getText();
		String mainClass = itemNode.selectSingleNode("class").getText();
		String subclass = itemNode.selectSingleNode("subclass").getText();
		String mainGroup = itemNode.selectSingleNode("main-group").getText();
		String subgroup = itemNode.selectSingleNode("subgroup").getText();

		IpcClassification ipcClass = new IpcClassification(null);
		ipcClass.setSection(section);
		ipcClass.setMainClass(mainClass);
		ipcClass.setSubClass(subclass);
		ipcClass.setMainGroup(mainGroup);
		ipcClass.setSubGroup(subgroup);

		LOGGER.trace("{}", ipcClass);
		
		return ipcClass;
	}

}
