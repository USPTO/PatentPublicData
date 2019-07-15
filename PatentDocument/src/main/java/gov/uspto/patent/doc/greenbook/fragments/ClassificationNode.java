package gov.uspto.patent.doc.greenbook.fragments;

import java.text.ParseException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.model.classification.IpcClassification;
import gov.uspto.patent.model.classification.LocarnoClassification;
import gov.uspto.patent.model.classification.PatentClassification;
import gov.uspto.patent.model.classification.UspcClassification;

/**
 * Classifications
 * <p>
 * <li>OCL - US Classification (USPC)
 * <li>XCL - Cross Reference
 * <li>UCL - Unofficial Reference
 * <li>DCL - Digest Reference
 * <li>EDF - Edition Field
 * <li>UCL - International Classification
 * <li>FSC - Field of Search Class
 * <li>FSS - Field of Search Subclass
 * </p>
 *
 * <p>
 * 
 * <pre>
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
 * </pre>
 * </p>
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class ClassificationNode extends DOMFragmentReader<Set<PatentClassification>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ClassificationNode.class);

	private static final XPath CLASSXP = DocumentHelper.createXPath("/DOCUMENT/CLAS");
	private static final XPath USPCXP = DocumentHelper.createXPath("OCL");
	private static final XPath IPCXP = DocumentHelper.createXPath("ICL");

	/*
	 * IPC Classification for Design Patents: Jan 5, 1971 through March 6, 1984 with
	 * leading "D" then 4 numeric. Mar 13, 1984 through Apil 29, 1997 no
	 * classification. May 6, 1997 use of LOCARNO Classification
	 */
	private static final Pattern DESIGN_ICL_PATTERN = Pattern.compile("^D[0-9]{4}$");

	public ClassificationNode(Document document) {
		super(document);
	}

	@Override
	public Set<PatentClassification> read() {
		Set<PatentClassification> classifications = new LinkedHashSet<PatentClassification>();

		List<Node> classNodes = CLASSXP.selectNodes(document);
		for (Node classN : classNodes) {
			UspcClassification uspc = getUSPC(classN);
			if (uspc != null) {
				classifications.add(uspc);
			}

			Set<PatentClassification> ipcClasses = getIPC(classN);
			classifications.addAll(ipcClasses);
		}

		return classifications;
	}

	public UspcClassification getUSPC(Node classN) {
		Node uspcN = USPCXP.selectSingleNode(classN);
		if (uspcN != null) {
			String classStr = uspcN.getText().trim();
			UspcClassification uspc = new UspcClassification(classStr, true);
			try {
				classStr = Strings.padStart(classStr, 6, '0');
				uspc.parseText(classStr);
			} catch (ParseException e) {
				LOGGER.warn("Failed to Parse USPC Classification: '{}' from : {}", uspcN.getText(), classN.asXML());
			}
			return uspc;
		}
		return null;
	}

	public Set<PatentClassification> getIPC(Node classN) {
		Set<PatentClassification> ipcClasses = new HashSet<PatentClassification>();
		List<Node> ipcNs = IPCXP.selectNodes(classN);

		for (Node ipcN : ipcNs) {
			if (ipcN != null) {
				String classStr = ipcN.getText().trim();
				try {
					IpcClassification ipc = new IpcClassification(classStr, true);
					classStr = classStr.replaceAll("\\s+", " ");
					ipc.parseText(classStr);
					// ipc.setIsMainClassification(true);
					ipcClasses.add(ipc);
				} catch (ParseException e) {
					if (DESIGN_ICL_PATTERN.matcher(classStr).matches()) {
						// FIXME.. implement.
						LOGGER.warn("IPC DESIGN CLASS: {}", classStr);
					} else {
						/*
						 * USPTO Design Patents started LocarnoClassification for International
						 * Classification May 6, 1997; only 1 per design patent. US Design Patents are
						 * also assigned USPC Classifications.
						 */
						LocarnoClassification locarno = new LocarnoClassification(classStr, true);
						try {
							locarno.parseText(classStr);
						} catch (ParseException e1) {
							LOGGER.warn("Failed to Parse locarno IPC Classification: '{}' from : {}", ipcN.getText(),
									classN.asXML());
						}
						ipcClasses.add(locarno);
					}
				}
			}
		}

		return ipcClasses;
	}
}
