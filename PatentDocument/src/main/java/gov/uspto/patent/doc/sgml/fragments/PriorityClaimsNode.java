package gov.uspto.patent.doc.sgml.fragments;

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
import gov.uspto.patent.doc.sgml.items.DocNode;
import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.CountryCodeHistory;
import gov.uspto.patent.model.DocumentDate;
import gov.uspto.patent.model.DocumentId;

/**
 * Foreign Application Priority Claim
 *
 * <p>
 * <li>DNUM PDAT Document number
 * <li>DATE PDAT Document date
 * <li>CTRY PDAT Publishing country or organization (ST.3)
 * </p>
 * <p>
 * 
 * <pre>
 *{@code
 *<B300>
 * <B310><DNUM><PDAT>197 57 896</PDAT></DNUM></B310>
 * <B320><DATE><PDAT>19971224</PDAT></DATE></B320>
 * <B330><CTRY><PDAT>DE</PDAT></CTRY></B330>
 *</B300>
 *}
 * </pre>
 *
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class PriorityClaimsNode extends DOMFragmentReader<List<DocumentId>> {
	private static final Logger LOGGER = LoggerFactory.getLogger(PriorityClaimsNode.class);

	private static final XPath PARENTXP = DocumentHelper.createXPath("/PATDOC/SDOBI/B300");
	private static final XPath IDXP = DocumentHelper.createXPath("B310");
	private static final XPath DATEXP = DocumentHelper.createXPath("B320/DATE/PDAT");
	private static final XPath COUNTRYXP = DocumentHelper.createXPath("B330/CTRY/PDAT");

	public PriorityClaimsNode(Document document) {
		super(document);
	}

	@Override
	public List<DocumentId> read() {
		List<DocumentId> priorityIds = new ArrayList<DocumentId>();

		List<Node> fragmentNodes = PARENTXP.selectNodes(document);
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

		Node idNode = IDXP.selectSingleNode(fragmentNode);
		if (idNode == null) {
			return null;
		}

		Node dateNode = DATEXP.selectSingleNode(fragmentNode);
		String docDateStr = dateNode != null ? dateNode.getText() : null;
		DocumentDate docDate = null;
		if (docDateStr != null) {
			try {
				docDate = new DocumentDate(docDateStr);
			} catch (InvalidDataException e) {
				LOGGER.warn("{} : {}", fragmentNode.asXML());
			}
		}

		Node countryN = COUNTRYXP.selectSingleNode(fragmentNode);
		String countryStr = countryN != null ? countryN.getText() : null;
		CountryCode countryCode = null;
		if (countryStr != null) {
			try {
				countryCode = CountryCode.fromString(countryStr);
			} catch (InvalidDataException e) {
				if (docDate != null) {
					countryCode = CountryCodeHistory.getCurrentCode(countryStr, docDate.getYear());
					if (countryCode == CountryCode.UNKNOWN) {
						LOGGER.warn("{} : {}", e.getMessage(), fragmentNode.asXML());
					} else {
						LOGGER.debug("Historic Country Code Matched '{}' yr {} : '{}'", countryStr, docDate.getYear(),
								countryCode);
					}
				} else {
					LOGGER.warn("{} : {}", e.getMessage(), fragmentNode.asXML());
				}
			}
		}

		DocumentId documentId = new DocNode(idNode, countryCode).read();
		if (documentId != null && docDate != null) {
			documentId.setDate(docDate);
		}
		return documentId;
	}

}
