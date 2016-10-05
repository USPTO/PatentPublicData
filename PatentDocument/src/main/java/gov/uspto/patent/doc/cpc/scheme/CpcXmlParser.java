package gov.uspto.patent.doc.cpc.scheme;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import gov.uspto.patent.PatentReaderException;

public class CpcXmlParser {
	private static final Logger LOGGER = LoggerFactory.getLogger(CpcXmlParser.class);

	private static final String ITEMS = "/class-scheme/classification-item";

	/**
	 * Parse CharSequence (String, StringBuffer, StringBuilder, CharBuffer)
	 *  
	 * @param xmlString
	 * @return
	 * @throws PatentReaderException
	 */	
	public ClassificationItem parse(CharSequence xmlString) throws PatentReaderException {
		StringReader reader = new StringReader(xmlString.toString());
		return parse(reader);
	}

	public ClassificationItem parse(File file) throws PatentReaderException, FileNotFoundException {
		FileReader reader = new FileReader(file);
		return parse(reader);
	}

	public ClassificationItem parse(Reader reader) throws PatentReaderException {
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

	public ClassificationItem parse(Document document) {

		Node parentClassItemN = document.selectSingleNode("/class-scheme/classification-item");
		ClassificationItem parentClassItem = readClassificationItem(parentClassItemN, null);

		List<Node> childClassItems = parentClassItemN.selectNodes("classification-item");
		for (Node childClass: childClassItems){
			readClassificationItem(childClass, parentClassItem);
		}
		
		return parentClassItem;
	}

	private ClassificationItem readClassificationItem(Node classItemNode, ClassificationItem parentClassItem) {

		String symbol = classItemNode.selectSingleNode("classification-symbol").getText();

		List<String> titlePartsText = new LinkedList<String>();

		List<Node> titleParts = classItemNode.selectNodes("class-title/title-part");
		
		for (Node titlePart : titleParts) {
			boolean foundText = false;
			
			Node textN = titlePart.selectSingleNode("text");
			if (textN != null){
				titlePartsText.add(textN.getText());
				foundText = true;
			}
			
			Node sptextN = titlePart.selectSingleNode("CPC-specific-text/text");
			if (sptextN != null){
				titlePartsText.add(sptextN.getText());
				foundText = true;
			}

			if (! foundText){
				LOGGER.error("Symbol '{}' does not have text: {}", symbol, titlePart.asXML());
			}
		}

		ClassificationItem classItem = new ClassificationItem(symbol);
		classItem.addTitlePart(titlePartsText);

		if (parentClassItem == null){
			return classItem;
		} else {
			parentClassItem.addSubClassificationItem(classItem);
		}

		List<Node> childClassItems = classItemNode.selectNodes("classification-item");
		for(Node child: childClassItems){
			readClassificationItem(child, classItem);
		}

		return parentClassItem;
	}

}
