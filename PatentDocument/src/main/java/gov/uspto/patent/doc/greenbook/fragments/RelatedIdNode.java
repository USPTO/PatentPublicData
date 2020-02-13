package gov.uspto.patent.doc.greenbook.fragments;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.DocumentId;

/**
 * Related Patents within a Patent Family
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class RelatedIdNode extends DOMFragmentReader<List<DocumentId>> {

	private static final XPath APTXP = DocumentHelper.createXPath("/DOCUMENT/RLAP");
	private static final XPath PNOXP = DocumentHelper.createXPath("PNO");
	private static final XPath APNXP = DocumentHelper.createXPath("APN");

	public RelatedIdNode(Document document) {
		super(document);
	}

	@Override
	public List<DocumentId> read() {
		List<DocumentId> familyDocIds = new ArrayList<DocumentId>();

		List<Node> relNodes = APTXP.selectNodes(document);
		for (Node relN : relNodes) {
			// Node parentCodeN = relN.selectSingleNode("COD");
			// Node parentStatusCodeN = relN.selectSingleNode("PSC");

			Node patNumN = PNOXP.selectSingleNode(relN);
			DocumentId docId;
			if (patNumN != null) {
				// Node issueDateN = relN.selectSingleNode("ISD");
				docId = new DocumentId(CountryCode.US, patNumN.getText().trim());
			} else {
				Node appNumN = APNXP.selectSingleNode(relN);
				// Node appFilingDate = relN.selectSingleNode("APD");
				String docNumber = appNumN != null ? appNumN.getText().trim() : "";
				docId = new DocumentId(CountryCode.US, docNumber);
			}

			familyDocIds.add(docId);
		}

		return familyDocIds;
	}

}
