package gov.uspto.patent.pap.fragments;

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
import gov.uspto.patent.pap.items.AddressNode;
import gov.uspto.patent.pap.items.NameNode;
import gov.uspto.patent.pap.items.ResidenceNode;

public class AssigneeNode extends DOMFragmentReader<List<Assignee>> {
	private static final Logger LOGGER = LoggerFactory.getLogger(AssigneeNode.class);

	private static final String FRAGMENT_PATH = "//assignee";
	private static final String ADDRESS_PATH = "//correspondence-address";

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
			assigneeList.add(assignee);
		}

		return assigneeList;
	}

	public Assignee readAssignee(Node assigneeNode){
		Name name = new NameNode(assigneeNode).read();

		Node residenceN = assigneeNode.selectSingleNode(ADDRESS_PATH);
		//Address address = new AddressNode(residenceN).read();
		Address resident = new ResidenceNode(residenceN).read();

		Assignee assignee = new Assignee(name, resident);
		String roleType = assigneeNode.selectSingleNode("assignee-type").getText();
		try {
			assignee.setRole(roleType);
		} catch (InvalidAttributesException e) {
			LOGGER.warn("Invalid Assignee: {}", assigneeNode.asXML(), e);
		}
		return assignee;
	}

}
