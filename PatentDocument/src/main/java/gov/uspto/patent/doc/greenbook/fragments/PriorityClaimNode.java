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
import gov.uspto.patent.model.CountryCodeHistory;
import gov.uspto.patent.model.DocumentDate;
import gov.uspto.patent.model.DocumentId;
import gov.uspto.patent.model.DocumentIdType;

public class PriorityClaimNode extends DOMFragmentReader<List<DocumentId>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(PriorityClaimNode.class);

	private static final XPath PRIRXP = DocumentHelper.createXPath("/DOCUMENT/PRIR");
	private static final XPath CNTRYXP = DocumentHelper.createXPath("CNT");
	private static final XPath APPIDXP = DocumentHelper.createXPath("APN");
	private static final XPath APPDATEXP = DocumentHelper.createXPath("APD");

	public PriorityClaimNode(Document document) {
		super(document);
	}

	@Override
	public List<DocumentId> read() {
		List<DocumentId> docIds = new ArrayList<DocumentId>();

		Node pctGroupN = PRIRXP.selectSingleNode(document);
		if (pctGroupN == null) {
			return docIds;
		}

		Node CntryN = CNTRYXP.selectSingleNode(pctGroupN);
		String cntryCodeStr = CntryN != null ? CntryN.getText().trim() : "";

		if (cntryCodeStr.length() == 3) {
			cntryCodeStr = cntryCodeStr.replaceFirst("(?:X|[0-9])$", "");
		}

		CountryCode countryCode = CountryCode.UNKNOWN;
		try {
			countryCode = CountryCode.fromString(cntryCodeStr);
		} catch (InvalidDataException e) {
			countryCode = CountryCodeHistory.getCurrentCode(cntryCodeStr);
			LOGGER.debug("Historic Country Code: '{}' maps to '{}'", cntryCodeStr, countryCode);
			// LOGGER.warn("{} : {}", e.getMessage(), pctGroupN.asXML());
		}

		Node priorityIdN = APPIDXP.selectSingleNode(pctGroupN);
		if (priorityIdN != null) {
			DocumentId priorityDocId = new DocumentId(countryCode, priorityIdN.getText().trim());
			priorityDocId.setType(DocumentIdType.NATIONAL_FILING);

			Node dateN = APPDATEXP.selectSingleNode(pctGroupN); // filing date.
			if (dateN != null) {
				String dateStr = dateN.getText().replaceAll("[^0-9]", "");
				try {
					DocumentDate filingDate = new DocumentDate(dateStr);
					priorityDocId.setDate(filingDate);
				} catch (InvalidDataException e) {
					LOGGER.warn("{} : {}", e.getMessage(), priorityIdN.asXML());
				}
			}
			docIds.add(priorityDocId);
		}

		return docIds;
	}
}
