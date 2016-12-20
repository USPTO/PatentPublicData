package gov.uspto.patent.doc.sgml.fragments;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.doc.sgml.items.DocNode;
import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.DocumentDate;
import gov.uspto.patent.model.DocumentId;

public class PriorityClaimsNode extends DOMFragmentReader<List<DocumentId>> {
	private static final Logger LOGGER = LoggerFactory.getLogger(PriorityClaimsNode.class);

	private static final String FRAGMENT_PATH = "/PATDOC/SDOBI/B300";
	private static final String ID_PATH = "B310";
	private static final String DATE_PATH = "B320/DATE/PDAT";
	private static final String CTRY_PATH = "B330/CTRY/PDAT";

	public PriorityClaimsNode(Document document) {
		super(document);
	}

	@Override
	public List<DocumentId> read() {
		List<DocumentId> priorityIds = new ArrayList<DocumentId>();

		List<Node> fragmentNodes = document.selectNodes(FRAGMENT_PATH);
		if (fragmentNodes == null) {
			return priorityIds;
		}

		for (Node priorityNode : fragmentNodes) {
			DocumentId documentId = readDocId(priorityNode);
			if (documentId != null) {
				priorityIds.add(documentId);
			}
		}

		return priorityIds;
	}

	public DocumentId readDocId(Node fragmentNode) {
		Node fragmentCtryNode = fragmentNode.selectSingleNode(CTRY_PATH);
		String docCtryStr = fragmentCtryNode != null ? fragmentCtryNode.getText() : null;
		CountryCode countryCode = null;
		if (docCtryStr != null) {
			try {
				countryCode = CountryCode.fromString(docCtryStr);
			} catch (InvalidDataException e) {
				LOGGER.warn("Failed to parse CountryCode {}", docCtryStr);
			}
		}

		Node idNode = fragmentNode.selectSingleNode(ID_PATH);
		if (idNode != null) {
			DocumentId documentId = new DocNode(idNode, countryCode).read();

			if (documentId != null) {
				// documentId.setType(DocumentIdType.REGIONAL_FILING);

				Node dateNode = fragmentNode.selectSingleNode(DATE_PATH);
				String docDateStr = dateNode != null ? dateNode.getText() : null;
				if (docDateStr != null) {
					DocumentDate docDate;
					try {
						docDate = new DocumentDate(docDateStr);
						documentId.setDate(docDate);
					} catch (InvalidDataException e) {
						LOGGER.warn("Failed to parse document date: {}", docDateStr);
					}
				}
			}

			return documentId;
		}

		return null;
	}

}
