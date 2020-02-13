package gov.uspto.patent.doc.greenbook.fragments;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;

import gov.uspto.common.text.WordUtil;
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
	private static final XPath PATXP = DocumentHelper.createXPath("/DOCUMENT/PATN/WKU");

	public PatentTypeNode(Document document) {
		super(document);
	}

	@Override
	public PatentType read() {
		Node aptN = APTXP.selectSingleNode(document);
		if (aptN != null) {
			switch (aptN.getText().trim()) {
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
		} else {
			Node patIdN = PATXP.selectSingleNode(document);
			if (patIdN != null) {
				String patNumStr = patIdN.getText().trim();
				if (WordUtil.startsWithCapital(patNumStr)) {
					String type = WordUtil.getCapital(patNumStr);
					switch (type) {
					case "RE":
						return PatentType.REISSUE;
					case "D":
						return PatentType.DESIGN;
					case "PP":
						return PatentType.PLANT;
					}
				} else {
					return PatentType.UTILITY;
				}
			}

		}

		return PatentType.UNDEFINED;
	}
}
