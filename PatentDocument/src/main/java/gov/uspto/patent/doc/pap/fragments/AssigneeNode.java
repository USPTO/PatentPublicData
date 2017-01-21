package gov.uspto.patent.doc.pap.fragments;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.doc.pap.items.AddressNode;
import gov.uspto.patent.doc.pap.items.NameNode;
import gov.uspto.patent.model.entity.Address;
import gov.uspto.patent.model.entity.Assignee;
import gov.uspto.patent.model.entity.Name;

public class AssigneeNode extends DOMFragmentReader<List<Assignee>> {
	private static final Logger LOGGER = LoggerFactory.getLogger(AssigneeNode.class);

	private static final String FRAGMENT_PATH = "//assignee";
	// private static final String ADDRESS_PATH = "//correspondence-address";

	public AssigneeNode(Document document) {
		super(document);
	}

	@Override
	public List<Assignee> read() {
		List<Assignee> assigneeList = new ArrayList<Assignee>();

		@SuppressWarnings("unchecked")
		List<Node> assignees = document.selectNodes(FRAGMENT_PATH);
		for (Node assigneeNode : assignees) {
			Assignee assignee = readAssignee(assigneeNode);
			if (assignee != null) {
				assigneeList.add(assignee);
			}
		}

		return assigneeList;
	}

	public Assignee readAssignee(Node assigneeNode) {
		Name name = new NameNode(assigneeNode).read();
		if (name == null) {
			return null;
		}

		Address address = new AddressNode(assigneeNode).read();

		// Node residenceN = assigneeNode.selectSingleNode(ADDRESS_PATH);
		// Address resident = new ResidenceNode(residenceN).read();

		Assignee assignee = new Assignee(name, address);
		String roleType = assigneeNode.selectSingleNode("assignee-type").getText();
		try {
			assignee.setRole(roleType);
		} catch (InvalidDataException e) {
			LOGGER.warn("Invalid Assignee 'assignee-type': {}", assigneeNode.asXML(), e);
		}

		return assignee;
	}

}
