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
import gov.uspto.patent.model.entity.Inventor;
import gov.uspto.patent.model.entity.Name;

public class InventorNode extends DOMFragmentReader<List<Inventor>> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(InventorNode.class);

	private static final XPath INVENTORXP = DocumentHelper.createXPath("/PATDOC/SDOBI/B700/B720/B721");
	private static final XPath DATAXP = DocumentHelper.createXPath("PARTY-US");

	public InventorNode(Document document) {
		super(document);
	}

	@Override
	public List<Inventor> read() {
		List<Inventor> inventorList = new ArrayList<Inventor>();

		List<Node> inventors = INVENTORXP.selectNodes(document);

		for(int i=0; i < inventors.size(); i++) {
			Node dataNode = DATAXP.selectSingleNode(inventors.get(i));
			Inventor inventor = readInventor(dataNode);
			if (inventor != null) {
				inventor.setSequence(String.valueOf(i+1));
				inventorList.add(inventor);
			}
		}

		return inventorList;
	}

	public Inventor readInventor(Node inventorNode) {
		Name name = new NameNode(inventorNode).read();
		if (name != null) {
			Address address = new AddressNode(inventorNode).read();

			try {
				address.validate();
			} catch (InvalidDataException e) {
				LOGGER.warn("{} : {}", e.getMessage(), inventorNode.asXML());
			}

			return new Inventor(name, address);
		}
		return null;
	}

}
