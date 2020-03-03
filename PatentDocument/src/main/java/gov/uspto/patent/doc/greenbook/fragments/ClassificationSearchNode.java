package gov.uspto.patent.doc.greenbook.fragments;

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
			Node fssN =  CLASSUBXP.selectSingleNode(classN);

			String searchClass = classN.getText().trim();
			searchClass = Strings.padStart(searchClass, 3, '0');

			String[] fss = fssN.getText().trim().split(";");
			for(String subClass: fss) {

				String subClass2 = normSubClass(subClass); 
				LOGGER.debug("Normalized Classification: {} -> {}", searchClass+subClass, searchClass+subClass2);

				String searchCL = searchClass + subClass2;
				UspcClassification uspc = new UspcClassification(searchCL.replace(" ", ""), false);
				try {
					uspc.parseText(searchCL);
				} catch (ParseException e) {
					LOGGER.warn("Failed to Parse USPC 'Search' Classification [field: FSC, FSS] : '{}' from : {}", searchCL, classN.asXML() + fssN.asXML());
				}
				classifications.add(uspc);
			}
		}

		return classifications;
	}
	
	protected String normSubClass(String subClass) {
		// 06315-15.8 -> 063015-015.8
		String subClass2 = Strings.padStart(subClass, 3, '0');
		String[] parts = subClass.split("-");
		if (parts.length == 2) {
			parts[0] = Strings.padStart(parts[0], 3, '0');
			if (parts[1].contains(".")) {
				String[] part2P = parts[1].split("\\.");
				part2P[0] = Strings.padStart(part2P[0], 3, '0');
				parts[1] = String.join(".", part2P);
			} else {
				parts[1] = Strings.padStart(parts[1], 3, '0');
			}
			subClass2 = String.join("-", parts);
		}

		// 23C21/00 -> 23C021/00
		String[] parts2 = subClass.split("/");
		if (parts2.length == 2) {
			parts2[0] = Strings.padStart(parts2[0], 3, '0');
			subClass2 = String.join("/", parts2);
		}

		return subClass2;
	}

}
