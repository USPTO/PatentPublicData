package gov.uspto.patent.doc.greenbook.fragments;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;

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

	private static final String RELATED = "/DOCUMENT/RLAP";

	public RelatedIdNode(Document document) {
		super(document);
	}

	@Override
	public List<DocumentId> read() {
		List<DocumentId> familyDocIds = new ArrayList<DocumentId>();

		@SuppressWarnings("unchecked")
		List<Node> relNodes = document.selectNodes(RELATED);
		for (Node relN : relNodes) {
			//Node parentCodeN = relN.selectSingleNode("COD");
			//Node parentStatusCodeN = relN.selectSingleNode("PSC");

			Node patNumN = relN.selectSingleNode("PNO");
			DocumentId docId;
			if (patNumN != null) {
				//Node issueDateN = relN.selectSingleNode("ISD");
				docId = new DocumentId(CountryCode.US, patNumN.getText());
			} else {
				Node appNumN = relN.selectSingleNode("APN");
				//Node appFilingDate = relN.selectSingleNode("APD");
				String docNumber = appNumN != null ? appNumN.getText()  : "" ;
				docId = new DocumentId(CountryCode.US, docNumber);
			}

			familyDocIds.add(docId);
		}

		return familyDocIds;
	}

}
