package gov.uspto.patent.doc.greenbook.fragments;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.DocumentDate;
import gov.uspto.patent.model.DocumentId;
import gov.uspto.patent.model.DocumentIdType;

public class PctRegionalIdNode extends DOMFragmentReader<List<DocumentId>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(PctRegionalIdNode.class);

	private static final XPath PCTXP = DocumentHelper.createXPath("/DOCUMENT/PCTA");
	private static final XPath PCPXP = DocumentHelper.createXPath("PCP");
	private static final XPath PCDXP = DocumentHelper.createXPath("PCD");
	private static final XPath PCNXP = DocumentHelper.createXPath("PCN");
	private static final XPath PD3XP = DocumentHelper.createXPath("PD3");

	public PctRegionalIdNode(Document document) {
		super(document);
	}

	@Override
	public List<DocumentId> read() {
		List<DocumentId> docIds = new ArrayList<DocumentId>();

		Node pctGroupN = PCTXP.selectSingleNode(document);
		if (pctGroupN == null) {
			return docIds;
		}

		DocumentId pctFilingId = filingId(pctGroupN);
		if (pctFilingId != null) {
			docIds.add(pctFilingId);
		}

		DocumentId pctPubId = filingId(pctGroupN);
		if (pctPubId != null) {
			docIds.add(pctPubId);
		}

		return docIds;
	}

	public DocumentId publicationId(Node pctGroupN) {
		Node pctPubIdN = PCPXP.selectSingleNode(pctGroupN);
		if (pctPubIdN != null) {
			DocumentId pubDocId = new DocumentId(CountryCode.WO, pctPubIdN.getText().trim());
			pubDocId.setType(DocumentIdType.INTERNATIONAL_FILING);

			if (pubDocId != null) {
				Node pctPubDateN = PCDXP.selectSingleNode(pctGroupN);
				if (pctPubDateN != null) {
					String pubDateStr = pctPubDateN.getText().replaceAll("[^0-9]", "");
					try {
						DocumentDate pubDate = new DocumentDate(pubDateStr);
						pubDocId.setDate(pubDate);
					} catch (InvalidDataException e) {
						LOGGER.warn("{} : {}", e.getMessage(), pctGroupN.asXML());
					}
				}

				return pubDocId;
			}
		}

		return null;
	}

	public DocumentId filingId(Node pctGroupN) {
		Node pctFilingIdN = PCNXP.selectSingleNode(pctGroupN);
		if (pctFilingIdN != null) {
			DocumentId filindDocId = new DocumentId(CountryCode.WO, pctFilingIdN.getText().trim());
			filindDocId.setType(DocumentIdType.INTERNATIONAL_FILING);

			if (filindDocId != null) {
				Node pctFilingDateN = PD3XP.selectSingleNode(pctGroupN);
				if (pctFilingDateN != null) {
					String filingDateStr = pctFilingDateN.getText().replaceAll("[^0-9]", "");
					try {
						DocumentDate filingDate = new DocumentDate(filingDateStr);
						filindDocId.setDate(filingDate);
					} catch (InvalidDataException e) {
						LOGGER.warn("{} : {}", e.getMessage(), pctGroupN.asXML());
					}
				}

				return filindDocId;
			}
		}

		return null;
	}

}
