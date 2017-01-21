package gov.uspto.patent.doc.xml.fragments;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.doc.xml.items.AddressBookNode;
import gov.uspto.patent.model.entity.Address;
import gov.uspto.patent.model.entity.Assignee;
import gov.uspto.patent.model.entity.Name;

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

			AddressBookNode addressBook;
			if (node.selectSingleNode("addressbook") != null) {
				addressBook = new AddressBookNode(node);
			} else {
				// Fix for assignee without addressbook, wrap assignee child nodes with addressbook.
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

			Address address = addressBook.getAddress();

			Node roleTypeN = node.selectSingleNode("addressbook/role");
			String roleType = roleTypeN != null ? roleTypeN.getText() : "";

			Assignee assignee = new Assignee(assigneeName, address);
			assigneeList.add(assignee);

			try {
				assignee.setRole(roleType);
			} catch (InvalidDataException e) {
				LOGGER.warn("Invalid Assignee RoleType: {}", node.asXML(), e);
			}
		}

		return assigneeList;
	}

}
