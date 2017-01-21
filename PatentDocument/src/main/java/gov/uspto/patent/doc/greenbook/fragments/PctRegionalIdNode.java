package gov.uspto.patent.doc.greenbook.fragments;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.DocumentDate;
import gov.uspto.patent.model.DocumentId;

public class PctRegionalIdNode extends DOMFragmentReader<List<DocumentId>> {
	private static final Logger LOGGER = LoggerFactory.getLogger(PctRegionalIdNode.class);

	private static final String FRAGMENT_PATH = "/DOCUMENT/PCTA";

	public PctRegionalIdNode(Document document) {
		super(document);
	}

	@Override
	public List<DocumentId> read() {
		List<DocumentId> docIds = new ArrayList<DocumentId>();

		Node pctGroupN = document.selectSingleNode(FRAGMENT_PATH);
		if (pctGroupN == null) {
			return docIds;
		}

		DocumentId pctFilingId = filingId(pctGroupN);
		if (pctFilingId != null){
			docIds.add(pctFilingId);
		}

		DocumentId pctPubId = filingId(pctGroupN);
		if (pctPubId != null){
			docIds.add(pctPubId);
		}

		return docIds;
	}

	public DocumentId publicationId(Node pctGroupN){
		Node pctPubIdN = pctGroupN.selectSingleNode("PCP");
		if (pctPubIdN != null) {
			DocumentId pubDocId = buildDocId(pctPubIdN.getText());

			if (pubDocId != null) {
				Node pctPubDateN = pctGroupN.selectSingleNode("PCD");
				if (pctPubDateN != null) {
					String pubDateStr = pctPubDateN.getText().replaceAll("[^0-9]", "");
					try {
						DocumentDate pubDate = new DocumentDate(pubDateStr);
						pubDocId.setDate(pubDate);
					} catch (InvalidDataException e) {
						LOGGER.warn("Invalid PCT Publication Date: {}", pubDateStr);
					}
				}

				return pubDocId;
			}
		}

		return null;
	}

	public DocumentId filingId(Node pctGroupN){
		Node pctFilingIdN = pctGroupN.selectSingleNode("PCN");
		if (pctFilingIdN != null) {
			DocumentId filindDocId = buildDocId(pctFilingIdN.getText());

			if (filindDocId != null) {
				Node pctFilingDateN = pctGroupN.selectSingleNode("PD3");
				if (pctFilingDateN != null) {
					String filingDateStr = pctFilingDateN.getText().replaceAll("[^0-9]", "");
					try {
						DocumentDate filingDate = new DocumentDate(filingDateStr);
						filindDocId.setDate(filingDate);
					} catch (InvalidDataException e) {
						LOGGER.warn("Invalid PCT Filing Date: '{}'", pctFilingDateN.getText());
					}
				}

				return filindDocId;
			}
		}

		return null;
	}
	
	private static final Pattern PCT_ID_PATTERN = Pattern.compile("^(?:PCT/)?([A-Z]{2})([0-9]{2}/[0-9]{4,})$");

	private DocumentId buildDocId(String pctDocIdString) {
		Matcher matcher = PCT_ID_PATTERN.matcher(pctDocIdString);

		if (matcher.matches()) {
			String countryCodeStr = matcher.group(1);
			String docId = matcher.group(2).replaceAll("/", "");

			CountryCode countryCode = CountryCode.UNKNOWN;

			try {
				countryCode = CountryCode.fromString(countryCodeStr);
			} catch (InvalidDataException e) {
				LOGGER.warn("Failed to lookup CountryCode: {}", countryCodeStr);
			}

			return new DocumentId(countryCode, docId);
		}

		LOGGER.warn("PCT DocID did not match pattern: {}", pctDocIdString);

		return null;
	}

}
