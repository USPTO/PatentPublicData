package gov.uspto.patent.sgml.fragments;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.model.entity.Address;
import gov.uspto.patent.model.entity.Inventor;
import gov.uspto.patent.model.entity.Name;
import gov.uspto.patent.sgml.items.AddressNode;
import gov.uspto.patent.sgml.items.NameNode;

public class InventorNode extends DOMFragmentReader<List<Inventor>> {
	private static final String FRAGMENT_PATH = "/PATDOC/SDOBI/B700/B720";

	public InventorNode(Document document) {
		super(document);
	}

	@Override
	public List<Inventor> read() {
		List<Inventor> inventorList = new ArrayList<Inventor>();

		@SuppressWarnings("unchecked")
		List<Node> inventors = document.selectNodes(FRAGMENT_PATH);

		for (Node inventorN : inventors) {
			Node dataNode = inventorN.selectSingleNode("B721/PARTY-US");
			Inventor inventor = readInventor(dataNode);
			inventorList.add(inventor);
		}

		return inventorList;
	}

	public Inventor readInventor(Node inventorNode){
		Name name = new NameNode(inventorNode).read();
		Address address = new AddressNode(inventorNode).read();
		return new Inventor(name, address);
	}

}
