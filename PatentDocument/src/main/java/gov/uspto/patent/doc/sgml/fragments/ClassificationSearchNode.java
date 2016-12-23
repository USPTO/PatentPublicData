package gov.uspto.patent.doc.sgml.fragments;

import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.Node;
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

	private static final String FRAGMENT_PATH = "/PATDOC/SDOBI/B500/B580";

	private Node parentPath;

	public ClassificationSearchNode(Document document) {
		super(document);

		Node parentPath = document.selectSingleNode(FRAGMENT_PATH);
		if (parentPath != null){
			this.parentPath = parentPath;
		} else {
			this.parentPath = document.getRootElement();
		}
	}

	@Override
	public Set<PatentClassification> read() {
		Set<PatentClassification> classifications = new HashSet<PatentClassification>();

		// IPC classification.
		@SuppressWarnings("unchecked")
		Node ipcN = parentPath.selectSingleNode("B581/PDAT");
		if (ipcN != null) {
			try {
				IpcClassification ipc = new IpcClassification();
				ipc.parseText(ipcN.getText());
				classifications.add(ipc);
			} catch (ParseException e) {
				LOGGER.debug("Failed to Parse IPC Classification: '{}' from : {}", ipcN.getText(),
						ipcN.asXML());
			}
		}

		// USPC classification.
		@SuppressWarnings("unchecked")
		Node uspcN = parentPath.selectSingleNode("B582/PDAT");
		if (uspcN != null) {
			try {
				UspcClassification uspc = new UspcClassification();
				uspc.parseText(uspcN.getText());
				classifications.add(uspc);
			} catch (ParseException e) {
				LOGGER.debug("Failed to Parse USPC Classification: '{}' from : {}", uspcN.getText(), uspcN.asXML());
			}
		}

		return classifications;
	}
}
