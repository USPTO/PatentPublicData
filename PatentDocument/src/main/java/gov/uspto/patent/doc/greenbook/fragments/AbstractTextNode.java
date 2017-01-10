package gov.uspto.patent.doc.greenbook.fragments;

import org.dom4j.Document;
import org.dom4j.Node;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.doc.greenbook.FormattedText;
import gov.uspto.patent.model.Abstract;

public class AbstractTextNode extends DOMFragmentReader<Abstract> {

	private static final String FRAGMENT_PATH = "/DOCUMENT/ABST";

	public AbstractTextNode(Document document, FormattedText textProcessor) {
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
