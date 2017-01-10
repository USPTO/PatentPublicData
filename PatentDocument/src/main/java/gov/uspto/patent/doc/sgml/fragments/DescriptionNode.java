package gov.uspto.patent.doc.sgml.fragments;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.TextProcessor;
import gov.uspto.patent.doc.sgml.items.DescriptionFigures;
import gov.uspto.patent.model.DescSection;
import gov.uspto.patent.model.Description;
import gov.uspto.patent.model.DescriptionSection;
import gov.uspto.patent.model.Figure;

public class DescriptionNode extends DOMFragmentReader<Description> {
	private static final Logger LOGGER = LoggerFactory.getLogger(DescriptionNode.class);

	private static final String FRAGMENT_PATH = "/PATDOC/SDODE";

	public DescriptionNode(Document document, TextProcessor textProcessor) {
		super(document, textProcessor);
	}

	@Override
	public Description read() {
		Description desc = new Description();

		Node descriptionN = document.selectSingleNode(FRAGMENT_PATH);
		if (descriptionN == null) {
			LOGGER.warn("Patent does not have a Description.");
			return desc;
		}

		Node briefSummary = descriptionN.selectSingleNode("BRFSUM");
		if (briefSummary != null) {
			desc.addSection(new DescriptionSection(DescSection.BRIEF_SUMMARY, briefSummary.asXML(), textProcessor));
		}

		Node drawingDesc = descriptionN.selectSingleNode("DRWDESC");
		if (drawingDesc != null) {
			desc.addSection(new DescriptionSection(DescSection.DRAWING_DESC, drawingDesc.asXML(), textProcessor));
			
			List<Figure> figures = new DescriptionFigures(drawingDesc).read();
			desc.addFigures(figures);
		}

		Node detailedDesc = descriptionN.selectSingleNode("DETDESC");
		if (detailedDesc != null) {
			desc.addSection(new DescriptionSection(DescSection.DETAILED_DESC, detailedDesc.asXML(), textProcessor));
		}

		return desc;
	}

}
