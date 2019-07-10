package gov.uspto.patent.doc.sgml.fragments;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;
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

	private static final XPath DESCXP = DocumentHelper.createXPath("/PATDOC/SDODE");
	private static final XPath BRFSUMXP = DocumentHelper.createXPath("BRFSUM");
	private static final XPath DRWDESCXP = DocumentHelper.createXPath("DRWDESC");
	private static final XPath DETDESCXP = DocumentHelper.createXPath("DETDESC");

	public DescriptionNode(Document document, TextProcessor textProcessor) {
		super(document, textProcessor);
	}

	@Override
	public Description read() {
		Description desc = new Description();

		Node descriptionN = DESCXP.selectSingleNode(document);
		if (descriptionN == null) {
			LOGGER.warn("Patent does not have a Description.");
			return desc;
		}

		Node briefSummary = BRFSUMXP.selectSingleNode(descriptionN);
		if (briefSummary != null) {
			desc.addSection(new DescriptionSection(DescSection.BRIEF_SUMMARY, briefSummary.asXML(), textProcessor));
		}

		Node drawingDesc = DRWDESCXP.selectSingleNode(descriptionN);
		if (drawingDesc != null) {
			desc.addSection(new DescriptionSection(DescSection.DRAWING_DESC, drawingDesc.asXML(), textProcessor));

			List<Figure> figures = new DescriptionFigures(drawingDesc).read();
			desc.addFigures(figures);
		}

		Node detailedDesc = DETDESCXP.selectSingleNode(descriptionN);
		if (detailedDesc != null) {
			desc.addSection(new DescriptionSection(DescSection.DETAILED_DESC, detailedDesc.asXML(), textProcessor));
		}

		return desc;
	}

}
