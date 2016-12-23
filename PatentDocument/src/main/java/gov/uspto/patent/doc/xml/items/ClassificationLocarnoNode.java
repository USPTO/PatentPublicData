package gov.uspto.patent.doc.xml.items;

import java.text.ParseException;
import java.util.List;

import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.ItemReader;
import gov.uspto.patent.model.classification.PatentClassification;
import gov.uspto.patent.model.classification.LocarnoClassification;

public class ClassificationLocarnoNode extends ItemReader<PatentClassification>{
	private static final Logger LOGGER = LoggerFactory.getLogger(ClassificationLocarnoNode.class);

	private final static String ITEM_NODE_NAME = "classification-locarno";

	public ClassificationLocarnoNode(Node itemNode){
		super(itemNode, ITEM_NODE_NAME);
	}

	@Override
	public PatentClassification read() {
		
		Node mainClass = itemNode.selectSingleNode("main-classification");
		if (mainClass == null){
			return null;
		}

		String mainClassTxt = mainClass.getText();
		if ("None".equalsIgnoreCase(mainClassTxt)){
			LOGGER.trace("Invalid Locarno classification 'main-classification': 'None'");
			return null;
		}

		LocarnoClassification classification;
		try {
			classification = new LocarnoClassification();
			classification.parseText(mainClass.getText());
			classification.setIsMainClassification(true);
		} catch (ParseException e1) {
			LOGGER.warn("Failed to parse Locarno classification 'main-classification': {}", mainClass.asXML(), e1);
			return null;
		}

		@SuppressWarnings("unchecked")
		List<Node> furtherClasses = itemNode.selectNodes("further-classification");
		for (Node subclass: furtherClasses){

			try {
				LocarnoClassification locarnoClass = new LocarnoClassification();
				locarnoClass.parseText(subclass.getText());
				classification.addChild( locarnoClass );
			} catch (ParseException e) {
				LOGGER.warn("Failed to parse Locarno classification 'further-classification': {}", subclass.asXML(), e);
			}

		}

		return classification;
	}
	
}
