package gov.uspto.patent.doc.xml.items;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.ItemReader;
import gov.uspto.patent.model.classification.LocarnoClassification;
import gov.uspto.patent.model.classification.PatentClassification;

public class ClassificationLocarnoNode extends ItemReader<List<PatentClassification>> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ClassificationLocarnoNode.class);

	private final static String ITEM_NODE_NAME = "classification-locarno";

	public ClassificationLocarnoNode(Node itemNode) {
		super(itemNode, ITEM_NODE_NAME);
	}

	@Override
	public List<PatentClassification> read() {
		List<PatentClassification> classes = new ArrayList<PatentClassification>();
		
		Node mainClass = itemNode.selectSingleNode("main-classification");
		if (mainClass == null) {
			return classes;
		}

		String mainClassTxt = mainClass.getText();
		if ("None".equalsIgnoreCase(mainClassTxt)) {
			LOGGER.trace("Invalid Locarno classification 'main-classification': 'None'");
			return classes;
		}

		LocarnoClassification classification = new LocarnoClassification(mainClass.getText(), true);
		try {
			classification.parseText(mainClass.getText());
			classes.add(classification);
		} catch (ParseException e1) {
			LOGGER.warn("Failed to parse Locarno classification 'main-classification': {}", mainClass.asXML());
		}

		List<Node> furtherClasses = itemNode.selectNodes("further-classification");
		for (Node subclass : furtherClasses) {
			LocarnoClassification locarnoClass = new LocarnoClassification(subclass.getText(), false);
			try {
				locarnoClass.parseText(subclass.getText());
			} catch (ParseException e) {
				LOGGER.warn("Failed to parse Locarno classification 'further-classification': {}", subclass.asXML());
			}
			classes.add(locarnoClass);
		}

		return classes;
	}

}
