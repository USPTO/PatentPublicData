package gov.uspto.patent.doc.greenbook.fragments;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
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

/**
 * Classifications
 *<p>
 *<li>OCL - US Classification (USPC)
 *<li>XCL - Cross Reference
 *<li>UCL - Unofficial Reference
 *<li>DCL - Digest Reference
 *<li>EDF - Edition Field
 *<li>UCL - International Classification
 *<li>FSC - Field of Search Class
 *<li>FSS - Field of Search Subclass
 *</p>
 *
 *<p><pre>
 *{@code
 *  <CLAS>
 *   <OCL>2940207</OCL>
 *   <XCL>188 795GE</XCL>
 *   <XCL>2940213</XCL>
 *   <EDF>2</EDF>
 *   <ICL>B23P  700</ICL>
 *   <ICL>B23P 1518</ICL>
 *   <FSC>29</FSC>
 *   <FSS>401 R;401 B;401 D;401 F</FSS>
 *   <FSC>188</FSC>
 *   <FSS>196 B;196 BA;79.5 K;79.5 GE;79.5 GC;79.5 GT;79.5 P;79.5 K</FSS>
 * </CLAS>
 *}
 *</pre></p>
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class ClassificationNode extends DOMFragmentReader<Set<Classification>> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ClassificationNode.class);

	private static final String FRAGMENT_PATH = "/DOCUMENT/CLAS";

	public ClassificationNode(Document document) {
		super(document);
	}

	@Override
	public Set<Classification> read() {
		Set<Classification> classifications = new LinkedHashSet<Classification>();

		@SuppressWarnings("unchecked")
		List<Node> classNodes = document.selectNodes(FRAGMENT_PATH);
		for (Node classN : classNodes) {
			UspcClassification uspc = getUSPC(classN);
			if (uspc != null){
				classifications.add(uspc);
			}

			Set<IpcClassification> ipcClasses = getIPC(classN);
			classifications.addAll(ipcClasses);
		}

		return classifications;
	}

	public UspcClassification getUSPC(Node classN) {
		Node uspcN = classN.selectSingleNode("OCL");
		if (uspcN != null) {
			try {
				String classStr = uspcN.getText().trim();
				UspcClassification uspc = UspcClassification.fromText(classStr);
				uspc.setIsMainClassification(true);
				return uspc;
			} catch (ParseException e) {
				LOGGER.warn("Failed to Parse USPC Classification: '{}' from : {}", uspcN.getText(), classN.asXML());
			}
		}
		return null;
	}

	public Set<IpcClassification> getIPC(Node classN) {
	    Set<IpcClassification> ipcClasses = new HashSet<IpcClassification>();
		List<Node> ipcNs = classN.selectNodes("ICL");
		
		for(Node ipcN: ipcNs){
    		if (ipcN != null) {
    			try {
    				String classStr = ipcN.getText().trim();
    				classStr = classStr.replaceAll("\\s+", " ");
    				IpcClassification ipc = IpcClassification.fromText(classStr);
    				//ipc.setIsMainClassification(true);
    				ipcClasses.add(ipc);
    			} catch (ParseException e) {
    				LOGGER.warn("Failed to Parse IPC Classification: '{}' from : {}", ipcN.getText(), classN.asXML());
    			}
    		}
		}
		return ipcClasses;
	}
}
