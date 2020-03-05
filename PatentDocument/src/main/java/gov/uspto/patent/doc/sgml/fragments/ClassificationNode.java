package gov.uspto.patent.doc.sgml.fragments;

import java.text.ParseException;
import java.util.LinkedHashSet;
import java.util.List;
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
import gov.uspto.patent.model.classification.LocarnoClassification;
import gov.uspto.patent.model.classification.UspcClassification;

public class ClassificationNode extends DOMFragmentReader<Set<PatentClassification>> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ClassificationNode.class);

	private static final XPath CLASSIFICATIONSXP = DocumentHelper.createXPath("/PATDOC/SDOBI/B500");
	private static final XPath IPC_LOCARNO_XP = DocumentHelper.createXPath("B510");
	private static final XPath IPC_PRIMARY = DocumentHelper.createXPath("B511/PDAT");
	private static final XPath IPC_SECONDARY = DocumentHelper.createXPath("B516/PDAT");

	private static final XPath USPC_XP = DocumentHelper.createXPath("B520");
	private static final XPath USPC_PRIMARY_XP = DocumentHelper.createXPath("B521/PDAT");
	private static final XPath USPC_SECONDARY_XP = DocumentHelper.createXPath("B522US/PDAT|B522/PDAT");

	public ClassificationNode(Document document) {
		super(document);
	}

	@Override
	public Set<PatentClassification> read() {
		Set<PatentClassification> classifications = new LinkedHashSet<PatentClassification>();

		Node parentNode = CLASSIFICATIONSXP.selectSingleNode(document);
		if (parentNode == null) {
			return classifications;
		}
		
		Node uspcNode = USPC_XP.selectSingleNode(parentNode);
		classifications.addAll(readUSPC(uspcNode));

		Node ipcNode = IPC_LOCARNO_XP.selectSingleNode(parentNode);
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
		Node ipcPrimaryN = IPC_PRIMARY.selectSingleNode(ipcNode);
		if (ipcPrimaryN != null) {
			String txt = ipcPrimaryN.getText();
			if (txt != null && txt.matches("^[0-9-]{1,6}$")) {
				LocarnoClassification locarno = new LocarnoClassification(txt, true);
				try {
					locarno.parseText(txt);
				} catch (ParseException e) {
					LOGGER.debug("Failed to Parse Primary Locarno Classification: '{}' from : {}", txt, ipcNode.asXML());
				}
				classifications.add(locarno);				
			}
			else if (txt != null && !txt.isEmpty()) {
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
		List<Node> ipcSecondaryNodes = IPC_SECONDARY.selectNodes(ipcNode);
		for (Node ipcN : ipcSecondaryNodes) {
			String txt = ipcN.getText();
			if (txt != null && txt.matches("^[0-9-]$")) {
				LocarnoClassification locarno = new LocarnoClassification(txt, false);
				try {
					locarno.parseText(txt);
				} catch (ParseException e) {
					LOGGER.warn("Failed to Parse Secondary Locarno Classification: '{}' from : {}", txt, ipcNode.asXML());
				}
				classifications.add(locarno);				
			}
			else if (txt != null && !txt.isEmpty()) {
				IpcClassification ipc = new IpcClassification(txt, false);
				try {
					ipc.parseText(txt);
				} catch (ParseException e) {
					LOGGER.warn("Failed to Parse Secondary IPC Classification: '{}' from : {}", txt,
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
		Node uspcPrimaryN = USPC_PRIMARY_XP.selectSingleNode(uspcNode);
		if (uspcPrimaryN != null) {
			String uspcTxt = uspcPrimaryN.getText();
			if (uspcTxt != null && !uspcTxt.isEmpty()) {
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
		List<Node> uspcNodes = USPC_SECONDARY_XP.selectNodes(uspcNode);
		for (Node uspcN : uspcNodes) {
			String uspcTxt = uspcN.getText();
			if (uspcTxt != null && !uspcTxt.isEmpty()) {
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


		return classifications;
	}

}
