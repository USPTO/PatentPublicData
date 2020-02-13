package gov.uspto.patent.doc.pap.fragments;

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

public class PriorityClaimNode extends DOMFragmentReader<List<DocumentId>> {
	private static final Logger LOGGER = LoggerFactory.getLogger(PriorityClaimNode.class);

	private static final XPath PCTPRIORITY_XP = DocumentHelper
			.createXPath("/patent-application-publication/subdoc-bibliographic-information/foreign-priority-data");
	private static final XPath DOCIDXP = DocumentHelper.createXPath("priority-application-number/doc-number");
	private static final XPath DATEXP = DocumentHelper.createXPath("filing-date");
	private static final XPath COUNTRYXP = DocumentHelper.createXPath("country-code");

	public PriorityClaimNode(Document document) {
		super(document);
	}

	@Override
	public List<DocumentId> read() {
		List<DocumentId> priorityIds = new ArrayList<DocumentId>();

		List<Node> nodes = PCTPRIORITY_XP.selectNodes(document);
		if (nodes == null) {
			return priorityIds;
		}

		for (Node priorityNode : nodes) {
			Node docIdN = DOCIDXP.selectSingleNode(priorityNode);
			String docidStr = docIdN != null ? docIdN.getText() : "";

			Node filingDateN = DATEXP.selectSingleNode(priorityNode);
			String filingDateStr = filingDateN != null ? filingDateN.getText() : "";

			Node cntryCodeN = COUNTRYXP.selectSingleNode(priorityNode);
			String cntryCodeStr = cntryCodeN != null ? cntryCodeN.getText() : "";
			DocumentId docId = null;

			CountryCode cntryCode = CountryCode.UNDEFINED;
			try {
				cntryCode = CountryCode.fromString(cntryCodeStr);
			} catch (InvalidDataException e) {
				if (docidStr.startsWith("PCT/")) {
					cntryCode = CountryCode.WO;
				} else {
					LOGGER.warn("{} : {}", e.getMessage(), priorityNode.asXML());
				}
			}

			docId = new DocumentId(cntryCode, docidStr);
			if (filingDateN != null && filingDateN.getText().trim().isEmpty()) {
				LOGGER.warn("Invalid Date, Empty or Missing : {}", priorityNode.asXML());
			}
			try {
				DocumentDate docDate = new DocumentDate(filingDateStr);
				docId.setDate(docDate);
			} catch (InvalidDataException e) {
				LOGGER.warn("{} : {}", e.getMessage(), filingDateStr);
			}

			priorityIds.add(docId);
		}

		return priorityIds;
	}

}
