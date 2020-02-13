package gov.uspto.patent.doc.xml.items;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.ItemReader;
import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.model.classification.PatentClassification;
import gov.uspto.patent.model.classification.UspcClassification;

/**
 * <pre>
* {@code
*	<classification-national>
*		<country>US</country>
*		<main-classification> 602031</main-classification>
*	</classification-national>
* }
 * </pre>
 *
 * <p>
 * Notes: classification-national are sometimes missing from citations when
 * cited by applicant.
 * </p>
 */
public class ClassificationNationalNode extends ItemReader<List<PatentClassification>> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ClassificationNationalNode.class);

	private final static String ITEM_NODE_NAME = "classification-national";

	public ClassificationNationalNode(Node itemNode) {
		super(itemNode, ITEM_NODE_NAME);
	}

	@Override
	public List<PatentClassification> read() {
		List<PatentClassification> patClasses = new ArrayList<PatentClassification>();

		// Node countryNode = parentNode.selectSingleNode("country");
		Node mainClass = itemNode.selectSingleNode("main-classification");
		if (mainClass == null) {
			return patClasses;
		}

		String mainClassTxt = mainClass.getText();
		if ("None".equalsIgnoreCase(mainClassTxt)) {
			LOGGER.trace("Invalid USPC classification 'main-classification': 'None'");
			return patClasses;
		}

		UspcClassification classification = new UspcClassification(mainClass.getText(), true);
		try {
			classification.parseText(mainClass.getText());
		} catch (ParseException e1) {
			LOGGER.warn("{} : {}", e1.getMessage(), mainClass.asXML());
		}
		patClasses.add(classification);

		try {
			classification.validate();
		} catch (InvalidDataException e1) {
			LOGGER.warn("{} : {}", e1.getMessage(), mainClass.asXML());
		}

		List<Node> furtherClasses = itemNode.selectNodes("further-classification");
		for (Node furtherClass : furtherClasses) {
			UspcClassification usClass = new UspcClassification(furtherClass.getText(), false);
			try {		
				usClass.parseText(furtherClass.getText());
			} catch (ParseException e) {
				LOGGER.warn("Failed to parse USPC classification 'further-classification': {}", furtherClass.asXML());
			}
			patClasses.add(usClass);
		}

		return patClasses;
	}

}
