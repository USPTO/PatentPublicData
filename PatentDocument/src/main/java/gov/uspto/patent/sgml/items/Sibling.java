package gov.uspto.patent.sgml.items;

import org.dom4j.Node;

import gov.uspto.parser.dom4j.ItemReader;

public class Sibling extends ItemReader<String> {

	public Sibling(Node itemNode) {
		super(itemNode);
	}

	@Override
	public String read() {
		return null;
	}
}
