package gov.uspto.patent.doc.xml.items;

import java.text.ParseException;
import java.util.List;

import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.ItemReader;
import gov.uspto.patent.model.classification.PatentClassification;
import gov.uspto.patent.model.classification.CpcClassification;

/*
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
*/

public class ClassificationCpcNode extends ItemReader<PatentClassification> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ClassificationCpcNode.class);

	public ClassificationCpcNode(Node itemNode){
		super(itemNode);
	}

	@Override
	public PatentClassification read() {

		Node cpcN = itemNode.selectSingleNode("classification-cpc");
		if (cpcN != null){

			String section = cpcN.selectSingleNode("section").getText();
			String mainClass = cpcN.selectSingleNode("class").getText();
			String subclass = cpcN.selectSingleNode("subclass").getText();
			String mainGroup = cpcN.selectSingleNode("main-group").getText();
			String subgroup = cpcN.selectSingleNode("subgroup").getText();

			CpcClassification cpcClass = new CpcClassification();
			cpcClass.setSection(section);
			cpcClass.setMainClass(mainClass);
			cpcClass.setSubClass(subclass);
			cpcClass.setMainGroup(mainGroup);
			cpcClass.setSubGroup(subgroup);

			LOGGER.trace("{}", cpcClass);
			
			return cpcClass;
		}
				
		Node classTxt = itemNode.selectSingleNode("classification-cpc-text");
		if (classTxt != null){
			
			CpcClassification classification = null;
			try {
				classification = new CpcClassification();
				classification.parseText(classTxt.getText());
			} catch (ParseException e) {
				LOGGER.warn("Failed to parse CPC classification: {}", classTxt.asXML());

			}
			
			return classification;
		}

		Node mainClass = itemNode.selectSingleNode("main-classification");
		if (mainClass != null){
			
			CpcClassification classification;
			try {
				classification = new CpcClassification();
				classification.parseText(mainClass.getText());
			} catch (ParseException e1) {
				LOGGER.warn("Failed to parse CPC classification: {}", mainClass.asXML());
				return null;
			}

			@SuppressWarnings("unchecked")
			List<Node> furtherClasses = itemNode.selectNodes("further-classification");
			for (Node subclass: furtherClasses){
				
				try {
					CpcClassification cpcClass = new CpcClassification();
					cpcClass.parseText(subclass.getText());
					classification.addChild( cpcClass  );
				} catch (ParseException e) {
					LOGGER.warn("Failed to parse CPC classification: {}", subclass.asXML());
				}

			}

			return classification;
		}
		

		return null;
	}

}
