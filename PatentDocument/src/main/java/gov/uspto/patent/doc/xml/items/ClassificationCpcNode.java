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
import gov.uspto.patent.model.classification.CpcClassification;
import gov.uspto.patent.model.classification.PatentClassification;

/**
 * Classification Cpc Node
 * 
 * <pre>
 * {@code
	<classifications-cpc>
		<main-cpc>
			<classification-cpc>
				<cpc-version-indicator><date>20130101</date></cpc-version-indicator>
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
				<scheme-origination-code>C</scheme-origination-code>
			</classification-cpc>
		</main-cpc>
		<further-cpc>
			<classification-cpc>
			   ...
			</classification-cpc>
		</further-cpc>
	</classifications-cpc>
	}
 * </pre>
 */
public class ClassificationCpcNode extends ItemReader<List<PatentClassification>> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ClassificationCpcNode.class);
	
	private static final XPath CPCXP = DocumentHelper.createXPath("self::classification-cpc|classification-cpc");
	private static final XPath SECTIONXP = DocumentHelper.createXPath("section");
	private static final XPath MCLASSXP = DocumentHelper.createXPath("class");
	private static final XPath SCLASSXP = DocumentHelper.createXPath("subclass");
	private static final XPath MGROUPXP = DocumentHelper.createXPath("main-group");
	private static final XPath SGROUPXP = DocumentHelper.createXPath("subgroup");
	private static final XPath TYPEXP = DocumentHelper.createXPath("classification-value");

	private static final XPath CPCTXTXP = DocumentHelper.createXPath("self::classification-cpc-text|classification-cpc-text");
	private static final XPath MAINCLASSXP = DocumentHelper.createXPath("main-classification");
	private static final XPath FURTHERCLASSXP = DocumentHelper.createXPath("further-classification");

	private boolean isInventive;
	
	public ClassificationCpcNode(Node itemNode, boolean isInventive) {
		super(itemNode);
		this.isInventive = isInventive;
	}

	@Override
	public List<PatentClassification> read() {
		
		Node cpcN = CPCXP.selectSingleNode(itemNode);
		if (cpcN != null) {
			return readSectionedFormat(cpcN);
		}
		else {
			Node cpcTxtN = CPCTXTXP.selectSingleNode(itemNode);
			if (cpcTxtN != null) {
				return readFlatFormat(cpcTxtN);
			}
		}

		return Collections.emptyList();
	}

	public List<PatentClassification> readSectionedFormat(Node cpcN) {
		List<PatentClassification> cpcClasses = new ArrayList<PatentClassification>();
		if (cpcN == null) {
			return cpcClasses;
		}

		String section = SECTIONXP.selectSingleNode(cpcN).getText();
		String mainClass = MCLASSXP.selectSingleNode(cpcN).getText();
		String subclass = SCLASSXP.selectSingleNode(cpcN).getText();
		String mainGroup = MGROUPXP.selectSingleNode(cpcN).getText();
		String subgroup = SGROUPXP.selectSingleNode(cpcN).getText();

		// classification-value: I = inventive, A = additional
		String type = TYPEXP.selectSingleNode(cpcN).getText();
		boolean inventive = false;
		if ("I".equals(type)) {
			inventive = true;
		}

		CpcClassification cpcClass = new CpcClassification("", inventive);
		cpcClass.setSection(section);
		cpcClass.setMainClass(mainClass);
		cpcClass.setSubClass(subclass);
		cpcClass.setMainGroup(new String[] {mainGroup});
		cpcClass.setSubGroup(new String[] {subgroup});
		cpcClasses.add(cpcClass);

		return cpcClasses;
	}

	public List<PatentClassification> readFlatFormat(Node cpcTxtN) {
		
		List<PatentClassification> cpcClasses = new ArrayList<PatentClassification>();
		if (cpcTxtN == null) {
			return cpcClasses;
		}
		
		Node classTxt = CPCTXTXP.selectSingleNode(cpcTxtN);
		if (classTxt != null) {

			CpcClassification classification = new CpcClassification(classTxt.getText(), true);
			try {
				classification.parseText(classTxt.getText());
			} catch (ParseException e) {
				LOGGER.warn("Failed to parse CPC classification: {}", classTxt.asXML());
			}
			cpcClasses.add(classification);
			
			return cpcClasses;
		}

		Node mainClass = MAINCLASSXP.selectSingleNode(cpcTxtN);
		if (mainClass != null) {

			CpcClassification classification = new CpcClassification(mainClass.getText(), true);
			try {
				classification.parseText(mainClass.getText());
			} catch (ParseException e1) {
				LOGGER.warn("Failed to parse CPC classification: {}", mainClass.asXML());
			}
			cpcClasses.add(classification);

			List<Node> furtherClasses = FURTHERCLASSXP.selectNodes(cpcTxtN);
			for (Node subclass : furtherClasses) {
				CpcClassification cpcClass = new CpcClassification(subclass.getText(), false);
				try {
					cpcClass.parseText(subclass.getText());
				} catch (ParseException e) {
					LOGGER.warn("Failed to parse CPC classification: {}", subclass.asXML());
				}
				cpcClasses.add(cpcClass);
			}
		}
		
		return cpcClasses;

	}

}
