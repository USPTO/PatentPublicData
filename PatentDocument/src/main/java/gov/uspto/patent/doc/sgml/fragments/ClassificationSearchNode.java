package gov.uspto.patent.doc.sgml.fragments;

import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.model.classification.PatentClassification;
import gov.uspto.patent.model.classification.IpcClassification;
import gov.uspto.patent.model.classification.UspcClassification;

/**
 * 
 * 
 * <!ELEMENT B580 (B581*,(B582|B583US)+) >
 *
 */
public class ClassificationSearchNode extends DOMFragmentReader<Set<PatentClassification>> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ClassificationSearchNode.class);

	private static final XPath SEARCHXP = DocumentHelper.createXPath("/PATDOC/SDOBI/B500/B580");

	private static final XPath IPCXP = DocumentHelper.createXPath("B581/PDAT");
	private static final XPath USPCXP = DocumentHelper.createXPath("B582/PDAT");

	private Node parentPath;

	public ClassificationSearchNode(Document document) {
		super(document);

		Node parentPath = SEARCHXP.selectSingleNode(document);
		if (parentPath != null) {
			this.parentPath = parentPath;
		} else {
			this.parentPath = document.getRootElement();
		}
	}

	@Override
	public Set<PatentClassification> read() {
		Set<PatentClassification> classifications = new HashSet<PatentClassification>();

		// IPC classification.
		Node ipcN = IPCXP.selectSingleNode(parentPath);
		if (ipcN != null) {
			IpcClassification ipc = new IpcClassification(ipcN.getText(), false);
			try {
				ipc.parseText(ipcN.getText());
			} catch (ParseException e) {
				LOGGER.debug("Failed to Parse IPC Classification: '{}' from : {}", ipcN.getText(), ipcN.asXML());
			}
			classifications.add(ipc);
		}

		// USPC classification.
		Node uspcN = USPCXP.selectSingleNode(parentPath);
		if (uspcN != null) {
			UspcClassification uspc = new UspcClassification(uspcN.getText(), false);
			try {
				uspc.parseText(uspcN.getText());
			} catch (ParseException e) {
				LOGGER.debug("Failed to Parse USPC Classification: '{}' from : {}", uspcN.getText(), uspcN.asXML());
			}
			classifications.add(uspc);
		}

		return classifications;
	}
}
