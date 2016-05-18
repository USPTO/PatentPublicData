package gov.uspto.patent.greenbook.fragments;

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
 *   <OCL>214450</OCL>
 *   <EDF>2</EDF>
 *   <ICL>B60R 904</ICL>
 *   <FSC>214</FSC>
 *   <FSS>450;517</FSS>
 *   <FSC>224</FSC>
 *   <FSS>42.03 R;42.1 R;42.1 H;42.1 F</FSS>
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
			
			IpcClassification ipc = getIPC(classN);
			if (ipc != null){
				classifications.add(ipc);
			}
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

	public IpcClassification getIPC(Node classN) {
		Node ipcN = classN.selectSingleNode("ICL");
		if (ipcN != null) {
			try {
				String classStr = ipcN.getText().trim();
				IpcClassification ipc = IpcClassification.fromText(classStr);
				ipc.setIsMainClassification(true);
				return ipc;
			} catch (ParseException e) {
				LOGGER.warn("Failed to Parse IPC Classification: '{}' from : {}", ipcN.getText(), classN.asXML());
			}
		}
		return null;
	}
}
