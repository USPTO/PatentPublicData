package gov.uspto.patent.doc.pap.fragments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.doc.pap.items.AddressNode;
import gov.uspto.patent.doc.pap.items.NameNode;
import gov.uspto.patent.doc.pap.items.ResidenceNode;
import gov.uspto.patent.model.entity.Address;
import gov.uspto.patent.model.entity.Inventor;
import gov.uspto.patent.model.entity.Name;

public class InventorNode extends DOMFragmentReader<List<Inventor>> {
	private static final Logger LOGGER = LoggerFactory.getLogger(InventorNode.class);

	private static final XPath INVENTORSXP = DocumentHelper
			.createXPath("/patent-application-publication/subdoc-bibliographic-information/inventors");
	private static final XPath INVENTORXP = DocumentHelper.createXPath("inventor|first-named-inventor");
	private static final XPath ADDRESS_W_CHILDXP = DocumentHelper.createXPath("address/*[1]");

	public InventorNode(Document document) {
		super(document);
	}

	public List<Node> getInventorNodes(){
		Node parentNode = INVENTORSXP.selectSingleNode(document);
		if (parentNode == null) {
			return Collections.emptyList();
		}
		return INVENTORXP.selectNodes(parentNode);
	}

	@Override
	public List<Inventor> read() {
		return readInventors(getInventorNodes());
	}

	private List<Inventor> readInventors(List<Node> inventors) {
		List<Inventor> inventorList = new ArrayList<Inventor>();

		for(int i=0; i < inventors.size(); i++) {
			Node inventorNode = inventors.get(i);
			Name name = new NameNode(inventorNode).read();
			if (name == null) {
				LOGGER.warn("Inventor does not have name : {}", inventorNode.asXML());
				continue;
			}
			try {
				name.validate();
			} catch (InvalidDataException e) {
				LOGGER.warn("{} : {}", e.getMessage(), inventorNode.asXML());
			}

			/*
			 * When Address node is missing, read from ResidenceNode
			 */
			Node addressN = ADDRESS_W_CHILDXP.selectSingleNode(inventorNode);
			Address address;
			if (addressN == null) {
				address = new ResidenceNode(inventorNode).read();
			} else {
				address = new AddressNode(inventorNode).read();
			}

			try {
				address.validate();
			} catch (InvalidDataException e) {
				LOGGER.warn("{} : {}", e.getMessage(), inventorNode.asXML());
			}

			Inventor inventor = new Inventor(name, address);
			inventor.setSequence(String.valueOf(i+1));
			inventorList.add(inventor);
		}
		return inventorList;
	}

}
