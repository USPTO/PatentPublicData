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
import gov.uspto.patent.doc.greenbook.items.AddressNode;
import gov.uspto.patent.model.Citation;
import gov.uspto.patent.model.Citation.CitedBy;
import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.DocumentDate;
import gov.uspto.patent.model.DocumentId;
import gov.uspto.patent.model.NplCitation;
import gov.uspto.patent.model.PatCitation;

/**
 * Citation / Referenced Patent Document Ids
 * 
 * <p>
 * 
 * <pre>
 *{@code
 *<UREF>
 *   <PNO>3045849</PNO>
 *   <ISD>19620700</ISD>
 *   <NAM>John Doe</NAM>
 *   <OCL>214450</OCL>
 *</UREF>
 *<OREF>
 *	<PAL></PAL>
 *</OREF>
 *}
 * </pre>
 * </p>
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class CitationNode extends DOMFragmentReader<List<Citation>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CitationNode.class);

	private static final XPath USPATXP = DocumentHelper.createXPath("/DOCUMENT/UREF");
	private static final XPath FORPATXP = DocumentHelper.createXPath("/DOCUMENT/FREF");
	private static final XPath NPLXP = DocumentHelper.createXPath("/DOCUMENT/OREF/PAL");
	
	private static final XPath CNTRYXP = DocumentHelper.createXPath("CNT");
	private static final XPath PATNUMXP = DocumentHelper.createXPath("PNO");
	private static final XPath ISSUEDATEXP = DocumentHelper.createXPath("ISD");

	public CitationNode(Document document) {
		super(document);
	}

	@Override
	public List<Citation> read() {
		List<Citation> citations = new ArrayList<Citation>();

		List<Node> usRels = USPATXP.selectNodes(document);
		for (int i = 0; i < usRels.size(); i++) {
			Node usRelN = usRels.get(i);
			DocumentId docId = readDocumentId(usRelN, CountryCode.US);
			if (docId != null) {
				Citation patCite = new PatCitation(String.valueOf(i), docId, CitedBy.UNDEFINED);
				citations.add(patCite);
			}
		}

		List<Node> foreignRels = FORPATXP.selectNodes(document);
		for (int i = 0; i < foreignRels.size(); i++) {
			Node frelN = foreignRels.get(i);
			CountryCode countryCode = readCountryCode(frelN);

			DocumentId docId = readDocumentId(frelN, countryCode);
			if (docId != null) {
				Citation patCite = new PatCitation(String.valueOf(i), docId,  CitedBy.UNDEFINED);
				citations.add(patCite);
			}
		}

		List<Node> nplCites = NPLXP.selectNodes(document);
		for (int i = 0; i < nplCites.size(); i++) {
			Node nplCiteN = nplCites.get(i);
			Citation nplCite = new NplCitation(String.valueOf(i), nplCiteN.getText(),  CitedBy.UNDEFINED);
			citations.add(nplCite);
		}

		return citations;
	}

	public CountryCode readCountryCode(Node itemNode) {
		Node countryN = CNTRYXP.selectSingleNode(itemNode);
		CountryCode countryCode = AddressNode.getCountryCode(countryN);
		return countryCode;
	}

	public DocumentId readDocumentId(Node itemNode, CountryCode countryCode) {

		Node docNumN = PATNUMXP.selectSingleNode(itemNode);
		if (docNumN == null) {
			LOGGER.warn("DocNum not found, field 'PNO': {}", itemNode.asXML());
			return null;
		}

		DocumentId documentId = new DocumentId(countryCode, docNumN.getText().trim());

		Node dateN = ISSUEDATEXP.selectSingleNode(itemNode);
		if (dateN != null) {
			String dateTxt = dateN.getText();
			try {
				documentId.setDate(new DocumentDate(dateTxt));
			} catch (InvalidDataException e) {
				LOGGER.warn("{} : {}", e.getMessage(), itemNode.asXML());
			}
		}

		return documentId;
	}

}
