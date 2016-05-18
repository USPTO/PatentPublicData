package gov.uspto.patent.xml.fragments;

import java.util.ArrayList;
import java.util.List;

import javax.naming.directory.InvalidAttributesException;

import org.dom4j.Document;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.model.entity.Address;
import gov.uspto.patent.model.entity.Assignee;
import gov.uspto.patent.model.entity.Name;
import gov.uspto.patent.xml.items.AddressBookNode;

public class AssigneeNode extends DOMFragmentReader<List<Assignee>> {
	private static final Logger LOGGER = LoggerFactory.getLogger(AssigneeNode.class);

	private static final String FRAGMENT_PATH = "//assignees/assignee";

	public AssigneeNode(Document document) {
		super(document);
	}

	@Override
	public List<Assignee> read() {
		List<Assignee> assigneeList = new ArrayList<Assignee>();

		@SuppressWarnings("unchecked")
		List<Node> assignees = document.selectNodes(FRAGMENT_PATH);

		for (Node node : assignees) {

			AddressBookNode addressBook = new AddressBookNode(node);

			Name assigneeName = null;
			try {
				if (addressBook.getPersonName() != null) {
					assigneeName = addressBook.getPersonName();
				} else {
					assigneeName = addressBook.getOrgName();
				}
			} catch (InvalidAttributesException e) {
				LOGGER.warn("Invalid Assignee: {}", node.asXML(), e);
			}

			Address address = null;
			try {
				address = addressBook.getAddress();
			} catch (InvalidAttributesException e1) {
				LOGGER.warn("Invalid Assignee: {}", node.asXML(), e1);
			}

			Node roleTypeN = node.selectSingleNode("addressbook/role");
			String roleType = roleTypeN != null ? roleTypeN.getText() : "";

			Assignee assignee = new Assignee(assigneeName, address);
			assigneeList.add(assignee);

			try {
				assignee.setRole(roleType);
			} catch (InvalidAttributesException e) {
				LOGGER.warn("Invalid Assignee: {}", node.asXML(), e);
			}
		}

		return assigneeList;
	}

}
