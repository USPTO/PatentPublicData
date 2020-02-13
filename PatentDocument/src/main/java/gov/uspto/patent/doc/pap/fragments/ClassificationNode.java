package gov.uspto.patent.doc.pap.fragments;

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
import gov.uspto.patent.model.classification.UspcClassification;

public class ClassificationNode extends DOMFragmentReader<Set<PatentClassification>> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ClassificationNode.class);

	private static final XPath CLASSIFICATION_XP = DocumentHelper.createXPath("/patent-application-publication/subdoc-bibliographic-information/technical-information");
	private static final XPath IPC_XP = DocumentHelper.createXPath("classification-ipc");
	private static final XPath USPC_XP = DocumentHelper.createXPath("classification-us");

	private Set<PatentClassification> classifications = new LinkedHashSet<PatentClassification>();

	public ClassificationNode(Document document) {
		super(document);
	}

	@Override
	public Set<PatentClassification> read() {
		Node parentNode = CLASSIFICATION_XP.selectSingleNode(document);
		readUSPC(parentNode);
		readIPC(parentNode);
		return classifications;
	}

	public void readUSPC(Node parentNode){
		Node uspcN = USPC_XP.selectSingleNode(parentNode);
		if (uspcN != null){
			Node uspcPrimaryClassN = uspcN.selectSingleNode("classification-us-primary/uspc/class");
			Node uspcPrimarySubClassN = uspcN.selectSingleNode("classification-us-primary/uspc/subclass");

			if (uspcPrimaryClassN != null && uspcPrimarySubClassN != null){
				String mainClass = uspcPrimaryClassN.getText().trim();
				String subClass = uspcPrimarySubClassN.getText().trim();

				UspcClassification uspc = new UspcClassification("", true);
				uspc.setMainClass(mainClass);
				uspc.setSubClass(subClass);
				classifications.add(uspc);
			}

			List<Node> uspcSecondaries = uspcN.selectNodes("classification-us-secondary");
			for(Node uspcSecoundary: uspcSecondaries){

				Node mainClassN = uspcSecoundary.selectSingleNode("uspc/class");
				Node subClassN = uspcSecoundary.selectSingleNode("uspc/subclass");

				if (mainClassN != null && subClassN != null){
					String mainClass = uspcPrimaryClassN.getText().trim();
					String subClass = subClassN.getText().trim();
					
					UspcClassification uspc = new UspcClassification("", false);
					uspc.setMainClass(mainClass);
					uspc.setSubClass(subClass);
					classifications.add(uspc);
				}
			}
		}
	}

	public void readIPC(Node parentNode){
		Node ipcN = IPC_XP.selectSingleNode(parentNode);
		if (ipcN != null){
			Node ipcPrimaryClassN = ipcN.selectSingleNode("classification-ipc-primary/ipc");
			if (ipcPrimaryClassN != null){
				String ipcPrimaryClassStr = ipcPrimaryClassN != null ? ipcPrimaryClassN.getText().trim() : null;
				IpcClassification ipcPrimaryClass = new IpcClassification(ipcPrimaryClassStr, true);
				try {
					ipcPrimaryClass.parseText(ipcPrimaryClassStr);
				} catch (ParseException e) {
					LOGGER.error("Invalid classification-ipc-primary", ipcPrimaryClassN.asXML(), e);
				}
				classifications.add(ipcPrimaryClass);
			}

			List<Node> ipcSecondaries = ipcN.selectNodes("classification-ipc-secondary/ipc");
			for(Node ipcSecoundary: ipcSecondaries){
				String ipcSecondaryClassStr = ipcSecoundary != null ? ipcSecoundary.getText().trim() : null;
				IpcClassification ipcSecondaryClass = new IpcClassification(ipcSecondaryClassStr, true);
				try {
					ipcSecondaryClass.parseText(ipcSecondaryClassStr);
				} catch (ParseException e) {
					LOGGER.error("Invalid classification-ipc-secondary", ipcSecoundary.asXML(), e);
				}
				classifications.add(ipcSecondaryClass);
			}

			// ipcN.selectSingleNode("classification-ipc-edition");
		}
	}
}
