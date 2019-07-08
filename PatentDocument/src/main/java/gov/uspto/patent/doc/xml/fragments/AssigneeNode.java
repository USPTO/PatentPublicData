package gov.uspto.patent.doc.xml.fragments;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.doc.xml.items.AddressBookNode;
import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.entity.Address;
import gov.uspto.patent.model.entity.Assignee;
import gov.uspto.patent.model.entity.Name;

public class AssigneeNode extends DOMFragmentReader<List<Assignee>> {
	private static final Logger LOGGER = LoggerFactory.getLogger(AssigneeNode.class);

	private static final String FRAGMENT_PATH = "/*/assignees/assignee";
	private static final String APPLICANT_ASSIGNEE_PATH = "/*/*/us-parties/us-applicants/us-applicant[@applicant-authority-category='assignee']";

	public AssigneeNode(Document document) {
		super(document);
	}

	@Override
	public List<Assignee> read() {
		Map<String, Assignee> assigneeList = new LinkedHashMap<String, Assignee>();

		List<Node> applicantAssignees = document.selectNodes(APPLICANT_ASSIGNEE_PATH);
		if (!applicantAssignees.isEmpty()) {
			for (Assignee assign : readEntityNodes(applicantAssignees)) {
				assigneeList.put(assign.getName().getName().toLowerCase(), assign);
			}
		}

		List<Node> assignees = document.selectNodes(FRAGMENT_PATH);
		if (!assignees.isEmpty()) {
			for (Assignee assign : readEntityNodes(assignees)) {
				assigneeList.put(assign.getName().getName().toLowerCase(), assign);
			}
		}

		return new ArrayList<Assignee>(assigneeList.values());
	}

	private List<Assignee> readEntityNodes(List<Node> nodes) {
		List<Assignee> assigneeList = new ArrayList<Assignee>();

		for (Node node : nodes) {

			AddressBookNode addressBook;
			if (node.selectSingleNode("addressbook") != null) {
				addressBook = new AddressBookNode(node);
			} else {
				// Fix for assignee without addressbook, wrap assignee child nodes with
				// addressbook.
				Element addressBookNode = DocumentHelper.createElement("addressbook");

				Iterator<Element> it = ((Element) node).elementIterator();
				while (it.hasNext()) {
					Element el = it.next();
					el.detach();
					addressBookNode.add(el);
				}
				((Element) node).add(addressBookNode);

				addressBook = new AddressBookNode(addressBookNode);
			}

			Name assigneeName = null;
			if (addressBook.getPersonName() != null) {
				assigneeName = addressBook.getPersonName();
			} else {
				assigneeName = addressBook.getOrgName();
			}

			if (assigneeName == null) {
				LOGGER.warn("Invalid Assignee Name: {}", node.asXML());
				continue;
			}

			try {
				assigneeName.validate();
			} catch (InvalidDataException e) {
				LOGGER.warn("{} : {}", e.getMessage(), node.asXML());
			}

			Address address = addressBook.getAddress();
			if (address == null) {
				address = new Address("", "", CountryCode.UNDEFINED);
			}

			Node roleTypeN = node.selectSingleNode("addressbook/role");
			String roleType = roleTypeN != null ? roleTypeN.getText() : "";

			Assignee assignee = new Assignee(assigneeName, address);
			assigneeList.add(assignee);

			try {
				assignee.setRole(roleType);
			} catch (InvalidDataException e) {
				LOGGER.warn("{} : {}", e.getMessage(), node.asXML());
			}
		}

		return assigneeList;
	}

}
