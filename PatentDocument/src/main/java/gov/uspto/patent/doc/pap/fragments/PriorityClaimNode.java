package gov.uspto.patent.doc.pap.fragments;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.DocumentDate;
import gov.uspto.patent.model.DocumentId;

public class PriorityClaimNode extends DOMFragmentReader<List<DocumentId>> {
	private static final Logger LOGGER = LoggerFactory.getLogger(PriorityClaimNode.class);

	private static final String FRAGMENT_PATH = "//foreign-priority-data";

	public PriorityClaimNode(Document document) {
		super(document);
	}

	@Override
	public List<DocumentId> read() {
		List<DocumentId> priorityIds = new ArrayList<DocumentId>();

		List<Node> nodes = document.selectNodes(FRAGMENT_PATH);
		if (nodes == null) {
			return priorityIds;
		}

		for (Node priorityNode : nodes) {
			Node docIdN = priorityNode.selectSingleNode("priority-application-number/doc-number");
			String docidStr = docIdN != null ? docIdN.getText() : "";

			Node filingDateN = priorityNode.selectSingleNode("filing-date");
			String filingDateStr = filingDateN != null ? filingDateN.getText() : "";

			Node cntryCodeN = priorityNode.selectSingleNode("country-code");
			String cntryCodeStr = cntryCodeN != null ? cntryCodeN.getText() : "";
			DocumentId docId = null;
			try {
				CountryCode cntryCode = CountryCode.fromString(cntryCodeStr);
				docId = new DocumentId(cntryCode, docidStr);
			} catch (InvalidDataException e) {
				LOGGER.warn("Invalid CountryCode: {}", cntryCodeStr, e);
			}

			if (docId != null) {
				try {
					DocumentDate docDate = new DocumentDate(filingDateStr);
					docId.setDate(docDate);
				} catch (InvalidDataException e) {
					LOGGER.warn("Invalid Document Date: {}", filingDateStr, e);
				}

				priorityIds.add(docId);
			}
		}

		return priorityIds;
	}

}
