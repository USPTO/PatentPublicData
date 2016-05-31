package gov.uspto.patent.greenbook.fragments;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.greenbook.items.AddressNode;
import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.DocumentDate;
import gov.uspto.patent.model.DocumentId;
import gov.uspto.patent.model.DocumentIdType;

/**
 * Referenced Patent Document Ids
 * 
 *<p><pre>
 *{@code
 *<UREF>
 *   <PNO>3045849</PNO>
 *   <ISD>19620700</ISD>
 *   <NAM>John Doe</NAM>
 *   <OCL>214450</OCL>
 *</UREF>
 *}
 *</pre></p>
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class ReferencedId extends DOMFragmentReader<List<DocumentId>> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ReferencedId.class);

	private static final String US_RELATED = "/DOCUMENT/UREF";
	private static final String FOREIGN_RELATED = "/DOCUMENT/FREF";

	public ReferencedId(Document document) {
		super(document);
	}

	@Override
	public List<DocumentId> read() {
		List<DocumentId> docIds = new ArrayList<DocumentId>();

		@SuppressWarnings("unchecked")
		List<Node> usRels = document.selectNodes(US_RELATED);
		for (Node usRelN : usRels) {
			DocumentId docId = readDocumentId(usRelN, CountryCode.US);
			if (docId != null) {
				docIds.add(docId);
			}
		}

		@SuppressWarnings("unchecked")
		List<Node> foreignRels = document.selectNodes(FOREIGN_RELATED);
		for (Node frelN : foreignRels) {
			CountryCode countryCode = readCountryCode(frelN);

			DocumentId docId = readDocumentId(frelN, countryCode);
			if (docId != null) {
				docIds.add(docId);
			}
		}

		return docIds;
	}

	public CountryCode readCountryCode(Node itemNode) {
		Node countryN = itemNode.selectSingleNode("CNT");
		String country = countryN != null ? countryN.getText() : null;
		CountryCode countryCode = AddressNode.getCountryCode(country);
		return countryCode;
	}

	public DocumentId readDocumentId(Node itemNode, CountryCode countryCode) {

		Node docNumN = itemNode.selectSingleNode("PNO");
		if (docNumN == null) {
			LOGGER.warn("DocNum not found, field 'PNO': {}", itemNode.asXML());
			return null;
		}

		DocumentId documentId = new DocumentId(countryCode, docNumN.getText().trim());
		documentId.setType(DocumentIdType.RELATED_PUBLICATION);

		Node dateN = itemNode.selectSingleNode("ISD");
		if (dateN != null) {
			String dateTxt = dateN.getText();
			try {
				documentId.setDate(new DocumentDate(dateTxt));
			} catch (InvalidDataException e) {
				LOGGER.warn("Failed to parse date: {}", dateTxt, e);
			}
		}

		return documentId;
	}

}
