package gov.uspto.patent.doc.greenbook.fragments;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.model.PatentType;

/**
 * Patent Type / Application Type "APT"
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class PatentTypeNode extends DOMFragmentReader<PatentType> {

	private static final XPath APTXP = DocumentHelper.createXPath("/DOCUMENT/PATN/APT");

	public PatentTypeNode(Document document) {
		super(document);
	}

	@Override
	public PatentType read() {
		Node aptN = APTXP.selectSingleNode(document);
		if (aptN != null) {
			switch (aptN.getText()) {
			case "1":
				return PatentType.UTILITY;
			case "2":
				return PatentType.REISSUE;
			case "3":
				return PatentType.UNDEFINED; // FIXME for TVPP Applications
			case "4":
				return PatentType.DESIGN;
			case "5":
				return PatentType.DEF;
			case "6":
				return PatentType.PLANT;
			case "7":
				return PatentType.SIR;
			}
		}
		return PatentType.UNDEFINED;
	}
}
