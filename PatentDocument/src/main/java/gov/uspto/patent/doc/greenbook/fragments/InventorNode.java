package gov.uspto.patent.doc.greenbook.fragments;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.doc.greenbook.items.AddressNode;
import gov.uspto.patent.doc.greenbook.items.NameNode;
import gov.uspto.patent.model.entity.Address;
import gov.uspto.patent.model.entity.Inventor;
import gov.uspto.patent.model.entity.Name;

public class InventorNode extends DOMFragmentReader<List<Inventor>> {

	private static final String FRAGMENT_PATH = "/DOCUMENT/INVT";

	public InventorNode(Document document) {
		super(document);
	}

	@Override
	public List<Inventor> read() {
		List<Inventor> inventorList = new ArrayList<Inventor>();

		@SuppressWarnings("unchecked")
		List<Node> inventors = document.selectNodes(FRAGMENT_PATH);
		for (Node inventorN : inventors) {
			Inventor inventor = readInventor(inventorN);
			if (inventor != null) {
				inventorList.add(inventor);
			}
		}

		return inventorList;
	}

	public Inventor readInventor(Node inventorN) {
		Name name = new NameNode(inventorN).read();

		if (name != null) {
			Address address = new AddressNode(inventorN).read();
			return new Inventor(name, address);
		}

		return null;
	}
}
