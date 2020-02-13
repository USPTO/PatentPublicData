package gov.uspto.patent.doc.sgml.fragments;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.doc.sgml.items.DocNode;
import gov.uspto.patent.model.DocumentId;
import gov.uspto.patent.model.DocumentIdType;

public class PctRegionalIdNode extends DOMFragmentReader<List<DocumentId>> {

	private static final XPath PARENTXP = DocumentHelper.createXPath("/PATDOC/SDOBI/B800");

	// PCT or regional authority application filing
	private static final XPath PCTAPPXP = DocumentHelper.createXPath("B860/B861/DOC");
	// PCT or regional authority publication
	private static final XPath PCTPUBXP = DocumentHelper.createXPath("B870/B871/DOC");

	public PctRegionalIdNode(Document document) {
		super(document);
	}

	@Override
	public List<DocumentId> read() {
		List<DocumentId> pctDocIds = new ArrayList<DocumentId>();

		Node parentNode = PARENTXP.selectSingleNode(document);
		if (parentNode == null) {
			return pctDocIds;
		}

		Node fragmentNode = PCTAPPXP.selectSingleNode(parentNode);
		if (fragmentNode != null) {
			DocumentId documentId = new DocNode(fragmentNode).read();
			if (documentId != null) {
				documentId.setType(DocumentIdType.REGIONAL_FILING);
			}

			pctDocIds.add(documentId);
		}

		Node fragmentNode2 = PCTPUBXP.selectSingleNode(parentNode);
		if (fragmentNode2 != null) {
			DocumentId documentId = new DocNode(fragmentNode2).read();
			if (documentId != null) {
				documentId.setType(DocumentIdType.REGIONAL_PUBLICATION);
			}
			pctDocIds.add(documentId);
		}

		return pctDocIds;
	}

}
