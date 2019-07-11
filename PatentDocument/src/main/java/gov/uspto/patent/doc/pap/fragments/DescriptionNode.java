package gov.uspto.patent.doc.pap.fragments;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;
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

	private static final XPath DESCXP = DocumentHelper.createXPath("/patent-application-publication/subdoc-description");
	private static final XPath CROSSDESCXP = DocumentHelper.createXPath("cross-reference-to-related-applications");
	private static final XPath BACKDESCXP = DocumentHelper.createXPath("background-of-invention");
	private static final XPath SUMDESCXP = DocumentHelper.createXPath("summary-of-invention");
	private static final XPath DRAWDESCXP = DocumentHelper.createXPath("brief-description-of-drawings");
	private static final XPath SEQDESCXP = DocumentHelper.createXPath("brief-description-of-sequences");
	private static final XPath DETAILDESCXP = DocumentHelper.createXPath("detailed-description");

	public DescriptionNode(Document document, TextProcessor textProcessor) {
		super(document, textProcessor);
	}

	@Override
	public Description read() {
		Description desc = new Description();

		Node descN = DESCXP.selectSingleNode(document);
		if (descN == null) {
			LOGGER.warn("Patent missing Description.");
			return desc;
		}

		Node relAppDesc = CROSSDESCXP.selectSingleNode(descN);
		if (relAppDesc != null) {
			desc.addSection(new DescriptionSection(DescSection.REL_APP_DESC, relAppDesc.asXML(), textProcessor));
		}

		Node backgroundOfInvention = BACKDESCXP.selectSingleNode(descN);
		if (backgroundOfInvention != null) {
			desc.addSection(new DescriptionSection(DescSection.BRIEF_SUMMARY, backgroundOfInvention.asXML(), textProcessor));
		}

		Node briefSummary = SUMDESCXP.selectSingleNode(descN);
		if (briefSummary != null) {
			desc.addSection(new DescriptionSection(DescSection.BRIEF_SUMMARY, briefSummary.asXML(), textProcessor));
		}

		Node drawingDesc = DRAWDESCXP.selectSingleNode(descN);
		if (drawingDesc != null) {
			desc.addSection(new DescriptionSection(DescSection.DRAWING_DESC, drawingDesc.asXML(), textProcessor));
			
			List<Figure> figures = new DescriptionFigures(drawingDesc).read();
			desc.addFigures(figures);			
		}

		Node sequenceDesc = SEQDESCXP.selectSingleNode(descN);
		if (sequenceDesc != null) {
			//LOGGER.warn("brief-description-of-sequences");
			desc.addSection(new DescriptionSection(DescSection.DRAWING_DESC, sequenceDesc.asXML(), textProcessor));
		}

		Node detailedDesc = DETAILDESCXP.selectSingleNode(descN);
		if (detailedDesc != null) {
			desc.addSection(new DescriptionSection(DescSection.DETAILED_DESC, detailedDesc.asXML(), textProcessor));
		}

		return desc;
	}

}
