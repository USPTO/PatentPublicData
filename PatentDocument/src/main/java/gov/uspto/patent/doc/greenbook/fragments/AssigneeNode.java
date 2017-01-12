package gov.uspto.patent.doc.greenbook.fragments;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.doc.greenbook.items.AddressNode;
import gov.uspto.patent.doc.greenbook.items.NameNode;
import gov.uspto.patent.model.entity.Address;
import gov.uspto.patent.model.entity.Assignee;
import gov.uspto.patent.model.entity.Name;

public class AssigneeNode extends DOMFragmentReader<List<Assignee>> {
	private static final Logger LOGGER = LoggerFactory.getLogger(AssigneeNode.class);

	private static final String FRAGMENT_PATH = "/DOCUMENT/ASSG";

	public AssigneeNode(Document document) {
		super(document);
	}

	@Override
	public List<Assignee> read() {
		List<Assignee> assigneeList = new ArrayList<Assignee>();

		@SuppressWarnings("unchecked")
		List<Node> assignees = document.selectNodes(FRAGMENT_PATH);
		for (Node assigneeN : assignees) {
			Assignee assignee = readAssignee(assigneeN);
			if (assignee != null){
				assigneeList.add(assignee);
			}
		}

		return assigneeList;
	}

	public Assignee readAssignee(Node assigneeN){
		Name name = new NameNode(assigneeN).read();
		if (name == null){
			return null;
		}

		Address address = new AddressNode(assigneeN).read();

		Node typeCodeN = assigneeN.selectSingleNode("COD");
		Assignee assignee = new Assignee(name, address);
		if (typeCodeN != null){
			try {
				assignee.setRole(typeCodeN.getText());
			} catch (InvalidDataException e) {
				LOGGER.warn("Invalid Assignee Role Type:", e);
			}
		}

		return assignee;
	}
}
