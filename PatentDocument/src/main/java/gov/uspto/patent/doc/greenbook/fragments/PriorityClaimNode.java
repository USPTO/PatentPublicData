package gov.uspto.patent.doc.greenbook.fragments;

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

	private static final String FRAGMENT_PATH = "/DOCUMENT/PRIR";

	public PriorityClaimNode(Document document) {
		super(document);
	}

	@Override
	public List<DocumentId> read() {
		List<DocumentId> docIds = new ArrayList<DocumentId>();

		Node pctGroupN = document.selectSingleNode(FRAGMENT_PATH);
		if (pctGroupN == null) {
			return docIds;
		}

		Node CntryN = pctGroupN.selectSingleNode("CNT");
		String cntryCodeStr = CntryN != null ? CntryN.getText() : "";
		cntryCodeStr = cntryCodeStr.replaceFirst("X$", "");

		CountryCode countryCode = CountryCode.UNKNOWN;
		try {
			countryCode = CountryCode.fromString(cntryCodeStr);
		} catch (InvalidDataException e) {
			LOGGER.warn("Failed to lookup CountryCode: {}", cntryCodeStr);
		}

		Node priorityIdN = pctGroupN.selectSingleNode("APN");
		if (priorityIdN != null) {
			DocumentId priorityDocId = new DocumentId(countryCode, priorityIdN.getText());

			Node dateN = pctGroupN.selectSingleNode("APD"); // filing date.
			if (dateN != null) {
				String dateStr = dateN.getText().replaceAll("[^0-9]", "");
				try {
					DocumentDate filingDate = new DocumentDate(dateStr);
					priorityDocId.setDate(filingDate);
				} catch (InvalidDataException e) {
					LOGGER.warn("Invalid Priority Filing Date: {}", dateStr);
				}
			}
			docIds.add(priorityDocId);
		}

		return docIds;
	}
}
