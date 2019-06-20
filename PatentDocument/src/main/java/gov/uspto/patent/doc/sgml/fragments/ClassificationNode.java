package gov.uspto.patent.doc.sgml.fragments;

import java.text.ParseException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.model.classification.PatentClassification;
import gov.uspto.patent.model.classification.IpcClassification;
import gov.uspto.patent.model.classification.LocarnoClassification;
import gov.uspto.patent.model.classification.UspcClassification;

public class ClassificationNode extends DOMFragmentReader<Set<PatentClassification>> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ClassificationNode.class);

	private final String IPC_OR_LOCARNO_SECTION = "/PATDOC/SDOBI/B500/B510";
	private final String USPC_SECTION = "/PATDOC/SDOBI/B500/B520";

	public ClassificationNode(Document document) {
		super(document);
	}

	@Override
	public Set<PatentClassification> read() {
		Set<PatentClassification> classifications = new LinkedHashSet<PatentClassification>();

		Node uspcNode = document.selectSingleNode(USPC_SECTION);
		classifications.addAll(readUSPC(uspcNode));

		Node ipcNode = document.selectSingleNode(IPC_OR_LOCARNO_SECTION);
		classifications.addAll(readIPC(ipcNode));

		return classifications;
	}

	public Set<PatentClassification> readIPC(Node ipcNode) {
		Set<PatentClassification> classifications = new LinkedHashSet<PatentClassification>();

		if (ipcNode == null) {
			return classifications;
		}

		// Node ipcEditionN = ipcClassNode.selectSingleNode("B516/PDAT");

		// Primary IPC classification.
		Node ipcPrimaryN = ipcNode.selectSingleNode("B511/PDAT");
		if (ipcPrimaryN != null) {
			String txt = ipcPrimaryN.getText();
			if (txt != null && txt.matches("^[0-9-]{1,6}$")) {
				LocarnoClassification locarno = new LocarnoClassification(txt, true);
				try {
					locarno.parseText(txt);
				} catch (ParseException e) {
					LOGGER.debug("Failed to Parse Secondary Locarno Classification: '{}' from : {}", txt, ipcNode.asXML());
				}
				classifications.add(locarno);				
			}
			else if (txt != null) {
				IpcClassification ipc = new IpcClassification(txt, true);
				try {
					ipc.parseText(txt);
				} catch (ParseException e) {
					LOGGER.debug("Failed to Parse Primary IPC Classification: '{}' from : {}", txt,
							ipcPrimaryN.asXML());
				}
				classifications.add(ipc);
			}
		}

		// Secondary IPC classifications.
		List<Node> ipcSecondaryNodes = ipcNode.selectNodes("B516/PDAT");
		for (Node ipcN : ipcSecondaryNodes) {
			String txt = ipcN.getText();
			if (txt != null && txt.matches("^[0-9-]$")) {
				LocarnoClassification locarno = new LocarnoClassification(txt, false);
				try {
					locarno.parseText(txt);
				} catch (ParseException e) {
					LOGGER.debug("Failed to Parse Secondary Locarno Classification: '{}' from : {}", txt, ipcNode.asXML());
				}
				classifications.add(locarno);				
			}
			else if (txt != null) {
				IpcClassification ipc = new IpcClassification(txt, false);
				try {
					ipc.parseText(txt);
				} catch (ParseException e) {
					LOGGER.debug("Failed to Parse Primary IPC Classification: '{}' from : {}", txt,
							ipcPrimaryN.asXML());
				}
				classifications.add(ipc);
			}
		}

		return classifications;
	}

	public Set<PatentClassification> readUSPC(Node uspcNode) {
		Set<PatentClassification> classifications = new LinkedHashSet<PatentClassification>();

		if (uspcNode == null) {
			return classifications;
		}

		// Primary USPC classification.
		@SuppressWarnings("unchecked")
		Node uspcPrimaryN = uspcNode.selectSingleNode("B521/PDAT");
		if (uspcPrimaryN != null) {
			String uspcTxt = uspcPrimaryN.getText();
			if (uspcTxt != null) {
				UspcClassification uspc = new UspcClassification(uspcTxt, true);
				try {
					uspc.parseText(uspcTxt);
				} catch (ParseException e) {
					LOGGER.warn("Failed to Parse Primary USPC Classification: '{}' from : {}", uspcPrimaryN.getText(),
							uspcPrimaryN.asXML());
				}
				classifications.add(uspc);
			}
		}

		// Secondary USPC classifications.
		@SuppressWarnings("unchecked")
		List<Node> uspcNodes = uspcNode.selectNodes("B522/PDAT");
		for (Node uspcN : uspcNodes) {
			String uspcTxt = uspcN.getText();
			if (uspcTxt != null) {
				UspcClassification uspc = new UspcClassification(uspcTxt, false);
				try {
					uspc.parseText(uspcTxt);
				} catch (ParseException e) {
					LOGGER.warn("Failed to Parse Secondary USPC Classification: '{}' from : {}", uspcN.getText(),
							uspcN.asXML());
				}
				classifications.add(uspc);
			}
		}

		// Secondary USPC classifications.
		@SuppressWarnings("unchecked")
		List<Node> uspcNodes2 = uspcNode.selectNodes("B522US/PDAT");
		for (Node uspcN : uspcNodes2) {
			String uspcTxt = uspcN.getText();
			UspcClassification uspc = new UspcClassification(uspcTxt, false);
			try {
				uspc.parseText(uspcTxt);
			} catch (ParseException e) {
				LOGGER.warn("Failed to Parse Secondary USPC Classification: '{}' from : {}", uspcN.getText(),
						uspcN.asXML());
			}
			classifications.add(uspc);
		}

		return classifications;
	}

}
