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
import gov.uspto.patent.doc.greenbook.items.NameNode;
import gov.uspto.patent.model.entity.Address;
import gov.uspto.patent.model.entity.Assignee;
import gov.uspto.patent.model.entity.Name;

public class AssigneeNode extends DOMFragmentReader<List<Assignee>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(AssigneeNode.class);

	private static final XPath ASSIGNEEXP = DocumentHelper.createXPath("/DOCUMENT/ASSG");
	private static final XPath ATYPEXP = DocumentHelper.createXPath("COD");

	public AssigneeNode(Document document) {
		super(document);
	}

	@Override
	public List<Assignee> read() {
		List<Assignee> assigneeList = new ArrayList<Assignee>();

		List<Node> assignees = ASSIGNEEXP.selectNodes(document);
		for (Node assigneeN : assignees) {
			Assignee assignee = readAssignee(assigneeN);
			if (assignee != null) {
				assigneeList.add(assignee);
			}
		}

		return assigneeList;
	}

	public Assignee readAssignee(Node assigneeN) {
		Name name = new NameNode(assigneeN).read();
		if (name == null) {
			LOGGER.warn("Assignee Name is missing : {}", assigneeN.asXML());
			return null;
		}
		try {
			name.validate();
		} catch (InvalidDataException e) {
			LOGGER.warn("{} : {}", e.getMessage(), assigneeN.asXML());
		}

		Address address = new AddressNode(assigneeN).read();
		/*
		 * try { address.validate(); } catch (InvalidDataException e) {
		 * LOGGER.warn("{} : {}", e.getMessage(), assigneeN.asXML()); }
		 */

		Node typeCodeN = ATYPEXP.selectSingleNode(assigneeN);
		Assignee assignee = new Assignee(name, address);

		String assigneeType = typeCodeN != null ? typeCodeN.getText() : null;
		try {
			assignee.setRole(assigneeType);
		} catch (InvalidDataException e) {
			LOGGER.warn("{} : {}", e.getMessage(), assigneeN.asXML());
		}

		return assignee;
	}
}
