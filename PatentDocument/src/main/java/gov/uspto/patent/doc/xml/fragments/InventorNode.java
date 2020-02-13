package gov.uspto.patent.doc.xml.fragments;

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
import gov.uspto.patent.doc.xml.items.AddressBookNode;
import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.entity.Address;
import gov.uspto.patent.model.entity.Inventor;
import gov.uspto.patent.model.entity.Name;
import gov.uspto.patent.model.entity.RelationshipType;

public class InventorNode extends DOMFragmentReader<List<Inventor>> {
	private static final Logger LOGGER = LoggerFactory.getLogger(InventorNode.class);

	private static final XPath FRAG_PATHX = DocumentHelper.createXPath("//us-parties/inventors/inventor"); // current.
	private static final XPath FRAG_PATH2X = DocumentHelper.createXPath("//parties/inventors/inventor"); // pre-2012.

	private static final XPath FRAG_PATH_APPLICANTX = DocumentHelper.createXPath("//us-parties/us-applicants/us-applicant[lower-case(@app-type)='applicant-inventor']"); // current.
	private static final XPath FRAG_PATH_APPLICANT2X = DocumentHelper.createXPath("//parties/applicants/applicant[lower-case(@app-type)='applicant-inventor']"); // pre-2012.

	public InventorNode(Document document) {
		super(document);
	}

	@Override
	public List<Inventor> read() {
		List<Inventor> inventorList = new ArrayList<Inventor>();
	
		List<Node> applicantInventNodes = FRAG_PATH_APPLICANTX.selectNodes(document); // document.selectNodes(FRAG_PATH_APPLICANT);
		if (applicantInventNodes.isEmpty()) {
			applicantInventNodes = FRAG_PATH_APPLICANT2X.selectNodes(document); // document.selectNodes(FRAG_PATH_APPLICANT2);
		}

		List<Inventor> applicantInventors = readInventors(applicantInventNodes);
		inventorList.addAll(applicantInventors);

		List<Node> inventors = FRAG_PATHX.selectNodes(document); // document.selectNodes(FRAG_PATH);
		if (inventors.isEmpty()) {
			inventors = FRAG_PATH2X.selectNodes(document); // document.selectNodes(FRAG_PATH2);
		}

		List<Inventor> currentLocInventors = readInventors(inventors);
		inventorList.addAll(currentLocInventors);
		
		return inventorList;
	}

	private List<Inventor> readInventors(List<Node> inventors) {
		List<Inventor> inventorList = new ArrayList<Inventor>();

		for (Node node : inventors) {
			Inventor inventor = readInventor(node);
			if (inventor != null) {
				inventorList.add(inventor);
			}
		}

		return inventorList;
	}

	private Inventor readInventor(Node inventorNode) {
		AddressBookNode addressBook = new AddressBookNode(inventorNode);

		Name personName = addressBook.getPersonName();
		Name orgName = addressBook.getPersonName();
		Name name = personName != null ? personName : orgName;

		if (name == null) {
			return null;
		}

		if (personName != null) {
			try {
				personName.validate();
			} catch (InvalidDataException e) {
				LOGGER.warn("{} : {}", e.getMessage(), inventorNode.asXML());
			}
		}

		Address address = addressBook.getAddress();
		if (address == null) {
			LOGGER.warn("Missing Address : {}", inventorNode.asXML());
			address = new Address("", "", CountryCode.UNDEFINED);
		} else {
			try {
				address.validate();
			} catch (InvalidDataException e) {
				LOGGER.warn("{} : {}", e.getMessage(), inventorNode.asXML());
			}
		}

		Inventor inventor = new Inventor(name, address);
		if (orgName != null) {
			inventor.addRelationship(orgName, RelationshipType.EMPLOYEE);
		}
		return inventor;
	}

}
