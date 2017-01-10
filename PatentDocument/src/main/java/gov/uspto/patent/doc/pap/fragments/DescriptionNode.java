package gov.uspto.patent.doc.pap.fragments;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.TextProcessor;
import gov.uspto.patent.doc.pap.items.DescriptionFigures;
import gov.uspto.patent.model.DescSection;
import gov.uspto.patent.model.Description;
import gov.uspto.patent.model.DescriptionSection;
import gov.uspto.patent.model.Figure;

public class DescriptionNode extends DOMFragmentReader<Description> {
	private static final Logger LOGGER = LoggerFactory.getLogger(DescriptionNode.class);

	private static final String FRAGMENT_PATH = "//subdoc-description";

	public DescriptionNode(Document document, TextProcessor textProcessor) {
		super(document, textProcessor);
	}

	@Override
	public Description read() {
		Description desc = new Description();

		Node descN = document.selectSingleNode(FRAGMENT_PATH);
		if (descN == null) {
			LOGGER.warn("Patent does not have a Description.");
			return desc;
		}

		Node relAppDesc = descN.selectSingleNode("cross-reference-to-related-applications");
		if (relAppDesc != null) {
			desc.addSection(new DescriptionSection(DescSection.REL_APP_DESC, relAppDesc.asXML(), textProcessor));
		}

		Node briefSummary = descN.selectSingleNode("summary-of-invention/section");
		if (briefSummary != null) {
			desc.addSection(new DescriptionSection(DescSection.BRIEF_SUMMARY, briefSummary.getParent().asXML(), textProcessor));
		}

		Node drawingDesc = descN.selectSingleNode("brief-description-of-drawings/section");
		if (drawingDesc != null) {
			desc.addSection(new DescriptionSection(DescSection.DRAWING_DESC, drawingDesc.getParent().asXML(), textProcessor));
			
			List<Figure> figures = new DescriptionFigures(drawingDesc).read();
			desc.addFigures(figures);			
		}

		Node detailedDesc = descN.selectSingleNode("detailed-description/section");
		if (detailedDesc != null) {
			desc.addSection(new DescriptionSection(DescSection.DETAILED_DESC, detailedDesc.getParent().asXML(), textProcessor));
		}

		return desc;
	}

}
