package gov.uspto.patent.xml.fragments;

import java.util.ArrayList;
import java.util.List;

import javax.naming.directory.InvalidAttributesException;

import org.dom4j.Document;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.model.entity.Inventor;
import gov.uspto.patent.model.entity.RelationshipType;
import gov.uspto.patent.xml.items.AddressBookNode;

public class InventorNode extends DOMFragmentReader<List<Inventor>> {
	private static final Logger LOGGER = LoggerFactory.getLogger(InventorNode.class);

	private static final String FRAGMENT_PATH = "//us-parties/inventors/inventor"; // current.

	private static final String FRAGMENT_PATH2 = "//parties/inventors/inventor"; // pre 2012.

	public InventorNode(Document document) {
		super(document);
	}

	@Override
	public List<Inventor> read() {
		@SuppressWarnings("unchecked")
		List<Node> inventors = document.selectNodes(FRAGMENT_PATH);
		List<Inventor> currentLocInventors = readInventors(inventors);
		if (!currentLocInventors.isEmpty()) {
			return currentLocInventors;
		}

		@SuppressWarnings("unchecked")
		List<Node> inventors2 = document.selectNodes(FRAGMENT_PATH2);
		List<Inventor> pre2012Inventors = readInventors(inventors2);
		return pre2012Inventors;
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

		try {
			Inventor inventor = new Inventor(addressBook.getPersonName(), addressBook.getAddress());
			if (addressBook.getOrgName() != null) {
				inventor.addRelationship(addressBook.getOrgName(), RelationshipType.EMPLOYEE);
			}
			return inventor;
		} catch (InvalidAttributesException e) {
			LOGGER.warn("Invalid Inventor: {}", inventorNode.asXML(), e);
		}
		return null;
	}

}
