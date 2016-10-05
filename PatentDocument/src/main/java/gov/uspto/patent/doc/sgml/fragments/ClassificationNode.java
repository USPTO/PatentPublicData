package gov.uspto.patent.doc.sgml.fragments;

import java.text.ParseException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.model.classification.Classification;
import gov.uspto.patent.model.classification.IpcClassification;
import gov.uspto.patent.model.classification.UspcClassification;

public class ClassificationNode extends DOMFragmentReader<Set<Classification>> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ClassificationNode.class);

	private final String IPC_SECTION = "/PATDOC/SDOBI/B500/B510";
	private final String USPC_SECTION = "/PATDOC/SDOBI/B500/B520";

	public ClassificationNode(Document document) {
		super(document);
	}

	@Override
	public Set<Classification> read() {
		Node ipcNode = document.selectSingleNode(IPC_SECTION);
		Set<Classification> ipcClasses = readIPC(ipcNode);

		Node uspcNode = document.selectSingleNode(USPC_SECTION);
		Set<Classification> uspcClasses = readUSPC(uspcNode);

		Set<Classification> classifications = new LinkedHashSet<Classification>();
		if (ipcClasses != null) {
			classifications.addAll(ipcClasses);
		}

		if (uspcClasses != null) {
			classifications.addAll(uspcClasses);
		}

		return classifications;
	}

	public Set<Classification> readIPC(Node ipcNode) {
		if (ipcNode == null) {
			return null;
		}

		Set<Classification> classifications = new LinkedHashSet<Classification>();

		//Node ipcEditionN = ipcClassNode.selectSingleNode("B516/PDAT");

		// Primary IPC classification.
		@SuppressWarnings("unchecked")
		Node ipcPrimaryN = ipcNode.selectSingleNode("B511/PDAT");
		if (ipcPrimaryN != null) {
			try {
				IpcClassification ipc = IpcClassification.fromText(ipcPrimaryN.getText());
				ipc.setIsMainClassification(true);
				classifications.add(ipc);
			} catch (ParseException e) {
				LOGGER.debug("Failed to Parse Primary IPC Classification: '{}' from : {}", ipcPrimaryN.getText(),
						ipcPrimaryN.asXML());
			}
		}

		// Secondary IPC classifications.
		@SuppressWarnings("unchecked")
		List<Node> ipcSecondaryNodes = ipcNode.selectNodes("B516/PDAT");
		for (Node ipcN : ipcSecondaryNodes) {
			try {
				IpcClassification ipc = IpcClassification.fromText(ipcN.getText());
				classifications.add(ipc);
			} catch (ParseException e) {
				LOGGER.debug("Failed to Parse Secondary IPC Classification: '{}' from : {}", ipcN.getText(),
						ipcN.asXML());
			}
		}

		return classifications;
	}

	public Set<Classification> readUSPC(Node uspcNode) {
		if (uspcNode == null) {
			return null;
		}

		Set<Classification> classifications = new LinkedHashSet<Classification>();

		// Primary USPC classification.
		@SuppressWarnings("unchecked")
		Node uspcPrimaryN = uspcNode.selectSingleNode("B521/PDAT");
		if (uspcPrimaryN != null) {
			try {
				UspcClassification uspc = UspcClassification.fromText(uspcPrimaryN.getText());
				uspc.setIsMainClassification(true);
				classifications.add(uspc);
			} catch (ParseException e) {
				LOGGER.warn("Failed to Parse Primary USPC Classification: '{}' from : {}", uspcPrimaryN.getText(),
						uspcPrimaryN.asXML());
			}
		}

		// Secondary USPC classifications.
		@SuppressWarnings("unchecked")
		List<Node> uspcNodes = uspcNode.selectNodes("B522/PDAT");
		for (Node uspcN : uspcNodes) {
			try {
				UspcClassification uspc = UspcClassification.fromText(uspcN.getText());
				classifications.add(uspc);
			} catch (ParseException e) {
				LOGGER.warn("Failed to Parse Secondary USPC Classification: '{}' from : {}", uspcN.getText(),
						uspcN.asXML());
			}
		}
		
		return classifications;
	}

}
