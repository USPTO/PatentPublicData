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
import gov.uspto.patent.doc.sgml.items.AddressNode;
import gov.uspto.patent.doc.sgml.items.NameNode;
import gov.uspto.patent.model.entity.Address;
import gov.uspto.patent.model.entity.Assignee;
import gov.uspto.patent.model.entity.Name;

public class AssigneeNode extends DOMFragmentReader<List<Assignee>> {
	private static final Logger LOGGER = LoggerFactory.getLogger(AssigneeNode.class);

	private static final XPath ASSIGNEXP = DocumentHelper.createXPath("/PATDOC/SDOBI/B700/B730/B731");
	private static final XPath ASSIGNDATAEXP = DocumentHelper.createXPath("PARTY-US");

	public AssigneeNode(Document document) {
		super(document);
	}

	@Override
	public List<Assignee> read() {
		List<Assignee> assigneeList = new ArrayList<Assignee>();

		List<Node> assignees = ASSIGNEXP.selectNodes(document);
		for (Node assigneeN : assignees) {
			Node dataNode = ASSIGNDATAEXP.selectSingleNode(assigneeN);

			Assignee assignee = readAssignee(dataNode);

			if (assignee != null) {
				try {
					assignee.setRole(null);
				} catch (InvalidDataException e) {
					e.printStackTrace();
				}
				assigneeList.add(assignee);
			}
		}

		return assigneeList;
	}

	public Assignee readAssignee(Node assigneeNode) {
		Name name = new NameNode(assigneeNode).read();
		if (name != null) {
			Address address = new AddressNode(assigneeNode).read();

			try {
				address.validate();
			} catch (InvalidDataException e) {
				LOGGER.warn("{} : {}", e.getMessage(), assigneeNode.asXML());
			}

			return new Assignee(name, address);
		}
		return null;
	}

}
