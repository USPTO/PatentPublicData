package gov.uspto.patent.doc.greenbook.fragments;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.model.classification.PatentClassification;
import gov.uspto.patent.model.classification.UspcClassification;

/**
 * Search Classifications
 * <p>
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
public class ClassificationSearchNode extends DOMFragmentReader<Set<PatentClassification>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ClassificationSearchNode.class);

	private static final XPath CLASSXP = DocumentHelper.createXPath("/DOCUMENT/CLAS/FSC");
	private static final XPath CLASSUBXP = DocumentHelper.createXPath("following-sibling::FSS[1]");

	public ClassificationSearchNode(Document document) {
		super(document);
	}

	@Override
	public Set<PatentClassification> read() {
		Set<PatentClassification> classifications = new LinkedHashSet<PatentClassification>();

		List<Node> classNodes = CLASSXP.selectNodes(document);
		for (Node classN : classNodes) {
			if (classN != null) {
				String searchMainClass = classN.getText().trim();
				searchMainClass = Strings.padStart(searchMainClass, 3, '0');

				Node fssN = CLASSUBXP.selectSingleNode(classN);
				if (fssN != null) {
					String[] fss = fssN.getText().trim().split(";");
					for (String subClass : fss) {

						String searchCL = searchMainClass + subClass;

						UspcClassification uspc = new UspcClassification(searchCL, false);
						uspc.setMainClass(searchMainClass);
						uspc.setSubClass(subClass);

						classifications.add(uspc);
					}
				}
			}
		}

		return classifications;
	}

}
