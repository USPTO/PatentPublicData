package gov.uspto.patent.doc.pap.fragments;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.doc.pap.items.AddressNode;
import gov.uspto.patent.doc.pap.items.NameNode;
import gov.uspto.patent.doc.pap.items.ResidenceNode;
import gov.uspto.patent.model.entity.Address;
import gov.uspto.patent.model.entity.Inventor;
import gov.uspto.patent.model.entity.Name;

public class InventorNode extends DOMFragmentReader<List<Inventor>> {
	private static final Logger LOGGER = LoggerFactory.getLogger(InventorNode.class);

	private static final String FRAGMENT_PATH = "//inventors/inventor";
	private static final String FRAGMENT_PATH1 = "//inventors/first-named-inventor";

	private List<Inventor> inventorList;

	public InventorNode(Document document) {
		super(document);
	}

	@Override
	public List<Inventor> read() {
		inventorList = new ArrayList<Inventor>();

		@SuppressWarnings("unchecked")
		List<Node> inventors = document.selectNodes(FRAGMENT_PATH1);
		readInventors(inventors);

		@SuppressWarnings("unchecked")
		List<Node> inventors2 = document.selectNodes(FRAGMENT_PATH);
		readInventors(inventors2);

		return inventorList;
	}

	private void readInventors(List<Node> inventors) {
		for (Node inventorNode : inventors) {
			Name name = new NameNode(inventorNode).read();
			if (name == null) {
				LOGGER.warn("Inventor does not have name : {}", inventorNode.asXML());
				continue;
			}

			Address residenceAddress = new ResidenceNode(inventorNode).read();

			Address address = new AddressNode(inventorNode).read();
			if (address == null && residenceAddress != null) {
				address = residenceAddress;
			}

			Inventor inventor = new Inventor(name, address);
			inventor.setResidency(residenceAddress.getCountry());
			inventorList.add(inventor);
		}
	}

}
