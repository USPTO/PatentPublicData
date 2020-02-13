package gov.uspto.patent.doc.greenbook.fragments;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.doc.greenbook.FormattedText;
import gov.uspto.patent.model.Abstract;

public class AbstractTextNode extends DOMFragmentReader<Abstract> {

	private static final XPath ABSTRACTXP = DocumentHelper.createXPath("/DOCUMENT/ABST");

	public AbstractTextNode(Document document, FormattedText textProcessor) {
		super(document, textProcessor);
	}

	@Override
	public Abstract read() {
		Node abstractN = ABSTRACTXP.selectSingleNode(document);
		if (abstractN == null) {
			return new Abstract("", textProcessor);
		}

		return new Abstract(abstractN.asXML(), textProcessor);
	}
}
