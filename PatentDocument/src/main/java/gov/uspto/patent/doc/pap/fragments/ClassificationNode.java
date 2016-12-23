package gov.uspto.patent.doc.pap.fragments;

import java.text.ParseException;
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
import gov.uspto.patent.model.classification.UspcClassification;

public class ClassificationNode extends DOMFragmentReader<Set<PatentClassification>> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ClassificationNode.class);

	private static final String IPC_PATH = "/patent-application-publication/subdoc-bibliographic-information/technical-information/classification-ipc";
	private static final String USPC_PATH = "/patent-application-publication/subdoc-bibliographic-information/technical-information/classification-us";

	private Set<PatentClassification> classifications = new LinkedHashSet<PatentClassification>();

	public ClassificationNode(Document document) {
		super(document);
	}

	@Override
	public Set<PatentClassification> read() {
		readUSPC();
		readIPC();
		return classifications;
	}

	public void readUSPC(){
		Node uspcN = document.selectSingleNode(USPC_PATH);
		if (uspcN != null){
			Node uspcPrimaryClassN = uspcN.selectSingleNode("classification-us-primary/uspc/class");
			Node uspcPrimarySubClassN = uspcN.selectSingleNode("classification-us-primary/uspc/subclass");

			if (uspcPrimaryClassN != null && uspcPrimarySubClassN != null){
				String mainClass = uspcPrimaryClassN.getText();
				String subClass = uspcPrimarySubClassN.getText();

				UspcClassification uspc = new UspcClassification();
				uspc.setTextOriginal(mainClass + subClass);
				uspc.setMainClass(mainClass);
				uspc.setSubClass(subClass);
				uspc.setIsMainClassification(true);
				classifications.add(uspc);
			}

			@SuppressWarnings("unchecked")
			List<Node> uspcSecondaries = uspcN.selectNodes("classification-us-secondary");
			for(Node uspcSecoundary: uspcSecondaries){

				Node mainClassN = uspcSecoundary.selectSingleNode("uspc/class");
				Node subClassN = uspcSecoundary.selectSingleNode("uspc/subclass");

				if (mainClassN != null && mainClassN != null){
					String mainClass = uspcPrimaryClassN.getText();
					String subClass = subClassN.getText();
					
					UspcClassification uspc = new UspcClassification();
					uspc.setTextOriginal(mainClass + subClass);
					uspc.setMainClass(mainClass);
					uspc.setSubClass(subClass);
					classifications.add(uspc);
				}
			}
		}
	}

	public void readIPC(){
		Node ipcN = document.selectSingleNode(IPC_PATH);
		if (ipcN != null){
			Node ipcPrimaryClassN = ipcN.selectSingleNode("classification-ipc-primary/ipc");
			if (ipcPrimaryClassN != null){
				String ipcPrimaryClassStr = ipcPrimaryClassN != null ? ipcPrimaryClassN.getText() : null;
				try {
					IpcClassification ipcPrimaryClass = new IpcClassification();
					ipcPrimaryClass.parseText(ipcPrimaryClassStr);
					ipcPrimaryClass.setIsMainClassification(true);
					classifications.add(ipcPrimaryClass);
				} catch (ParseException e) {
					LOGGER.error("Invalid classification-ipc-primary", ipcPrimaryClassN.asXML(), e);
				}
			}

			@SuppressWarnings("unchecked")
			List<Node> ipcSecondaries = ipcN.selectNodes("classification-ipc-secondary/ipc");
			for(Node ipcSecoundary: ipcSecondaries){
				String ipcSecondaryClassStr = ipcSecoundary != null ? ipcSecoundary.getText() : null;
				try {
					IpcClassification ipcSecondaryClass = new IpcClassification();
					ipcSecondaryClass.parseText(ipcSecondaryClassStr);
					classifications.add(ipcSecondaryClass);
				} catch (ParseException e) {
					LOGGER.error("Invalid classification-ipc-secondary", ipcSecoundary.asXML(), e);
				}
			}

			// ipcN.selectSingleNode("classification-ipc-edition");
		}
	}
}
