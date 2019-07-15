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
import gov.uspto.patent.model.entity.Inventor;
import gov.uspto.patent.model.entity.Name;

public class InventorNode extends DOMFragmentReader<List<Inventor>> {
	private static final Logger LOGGER = LoggerFactory.getLogger(InventorNode.class);

	private static final XPath INVENTORXP = DocumentHelper.createXPath("/DOCUMENT/INVT");

	public InventorNode(Document document) {
		super(document);
	}

	@Override
	public List<Inventor> read() {
		List<Inventor> inventorList = new ArrayList<Inventor>();

		List<Node> inventors = INVENTORXP.selectNodes(document);
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
		if (name == null) {
			LOGGER.warn("Inventor Name is missing : {}", inventorN.asXML());
			return null;
		}
		try {
			name.validate();
		} catch (InvalidDataException e) {
			LOGGER.warn("{} : {}", e.getMessage(), inventorN.asXML());
		}

		Address address = new AddressNode(inventorN).read();
		/*try {
			address.validate();
		} catch (InvalidDataException e) {
			LOGGER.warn("{} : {}", e.getMessage(), inventorN.asXML());
		}
		*/

		return new Inventor(name, address);
	}
}
