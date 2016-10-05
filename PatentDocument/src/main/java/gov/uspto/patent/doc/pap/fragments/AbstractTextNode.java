package gov.uspto.patent.doc.pap.fragments;

import org.dom4j.Document;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.TextProcessor;
import gov.uspto.patent.model.Abstract;

public class AbstractTextNode extends DOMFragmentReader<Abstract> {
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTextNode.class);

	private static final String FRAGMENT_PATH = "//subdoc-abstract";

	public AbstractTextNode(Document document, TextProcessor textProcessor) {
		super(document, textProcessor);
	}

	@Override
	public Abstract read() {
		Node abstractN = document.selectSingleNode(FRAGMENT_PATH);
		if (abstractN == null) {
			LOGGER.warn("Patent does not have an Abstract");
			return null;
		}

		return new Abstract(abstractN.asXML(), textProcessor);
	}

}
