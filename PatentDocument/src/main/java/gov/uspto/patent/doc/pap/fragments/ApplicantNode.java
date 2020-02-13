package gov.uspto.patent.doc.pap.fragments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.doc.pap.items.AddressNode;
import gov.uspto.patent.doc.pap.items.NameNode;
import gov.uspto.patent.doc.pap.items.ResidenceNode;
import gov.uspto.patent.model.entity.Address;
import gov.uspto.patent.model.entity.Applicant;
import gov.uspto.patent.model.entity.Name;

/**
 * 
 * Applicants are Inventors with has an "authority-applicant"
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class ApplicantNode extends DOMFragmentReader<List<Applicant>> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicantNode.class);

	private static final XPath INVENTORSXP = DocumentHelper
			.createXPath("/patent-application-publication/subdoc-bibliographic-information/inventors");
	private static final XPath APPLICANTXP = DocumentHelper.createXPath("inventor/authority-applicant/..|first-named-inventor/authority-applicant/..");
	private static final XPath ADDRESS_W_CHILDXP = DocumentHelper.createXPath("address/*[1]");

	public ApplicantNode(Document document) {
		super(document);
	}

	@Override
	public List<Applicant> read() {
		Node parentNode = INVENTORSXP.selectSingleNode(document);
		if (parentNode == null) {
			return Collections.emptyList();
		}

		List<Node> inventors = APPLICANTXP.selectNodes(parentNode);
		return readInventors(inventors);
	}

	private List<Applicant> readInventors(List<Node> inventors) {
		List<Applicant> applicantList = new ArrayList<Applicant>();
		for (Node inventorNode : inventors) {
			Name name = new NameNode(inventorNode).read();
			if (name == null) {
				LOGGER.warn("Inventor-Applicant does not have name : {}", inventorNode.asXML());
				continue;
			}
			try {
				name.validate();
			} catch (InvalidDataException e) {
				LOGGER.warn("{} : {}", e.getMessage(), inventorNode.asXML());
			}

			/*
			 * When Address node is missing, read from ResidenceNode
			 */
			Node addressN = ADDRESS_W_CHILDXP.selectSingleNode(inventorNode);
			Address address;
			if (addressN == null) {
				address = new ResidenceNode(inventorNode).read();
			} else {
				address = new AddressNode(inventorNode).read();
			}

			try {
				address.validate();
			} catch (InvalidDataException e) {
				LOGGER.warn("{} : {}", e.getMessage(), inventorNode.asXML());
			}

			Applicant applicantInventor = new Applicant(name, address);
			applicantList.add(applicantInventor);			
		}

		return applicantList;
	}

}
