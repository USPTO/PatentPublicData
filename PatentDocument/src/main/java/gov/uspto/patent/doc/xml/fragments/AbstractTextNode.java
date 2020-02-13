package gov.uspto.patent.doc.xml.fragments;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.TextProcessor;
import gov.uspto.patent.model.Abstract;

public class AbstractTextNode extends DOMFragmentReader<Abstract> {

	private static final XPath ABSTRACTX = DocumentHelper.createXPath("/*/abstract");

	public AbstractTextNode(Document document, TextProcessor textProcessor) {
		super(document, textProcessor);
	}

	@Override
	public Abstract read() {
		Node abstractN = ABSTRACTX.selectSingleNode(document);
		if (abstractN == null) {
			return new Abstract("", textProcessor);
		}

		return new Abstract(abstractN.asXML(), textProcessor);
	}

}
