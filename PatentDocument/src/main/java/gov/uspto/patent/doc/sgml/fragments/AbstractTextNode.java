package gov.uspto.patent.doc.sgml.fragments;

import org.dom4j.Document;
import org.dom4j.Node;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.TextProcessor;
import gov.uspto.patent.model.Abstract;

public class AbstractTextNode extends DOMFragmentReader<Abstract> {
	private static final String FRAGMENT_PATH = "/PATDOC/SDOAB";

	public AbstractTextNode(Document document, TextProcessor textProcessor) {
		super(document, textProcessor);
	}

	@Override
	public Abstract read() {
		Node abstractN = document.selectSingleNode(FRAGMENT_PATH);
		if (abstractN == null) {
			return new Abstract("", textProcessor);
		}

		return new Abstract(abstractN.asXML(), textProcessor);
	}
}
