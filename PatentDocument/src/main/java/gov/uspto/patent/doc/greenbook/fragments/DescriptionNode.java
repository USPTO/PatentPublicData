package gov.uspto.patent.doc.greenbook.fragments;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.TextProcessor;
import gov.uspto.patent.doc.greenbook.items.DescriptionFigures;
import gov.uspto.patent.model.DescSection;
import gov.uspto.patent.model.Description;
import gov.uspto.patent.model.DescriptionSection;
import gov.uspto.patent.model.Figure;

public class DescriptionNode extends DOMFragmentReader<Description> {

	private static final XPath PARNXP = DocumentHelper.createXPath("/DOCUMENT/PARN");
	private static final XPath BSUMXP = DocumentHelper.createXPath("/DOCUMENT/BSUM");
	private static final XPath DRWDXP = DocumentHelper.createXPath("/DOCUMENT/DRWD");
	private static final XPath DETDXP = DocumentHelper.createXPath("/DOCUMENT/DETD");

	public DescriptionNode(Document document, TextProcessor textProcessor) {
		super(document, textProcessor);
	}

	@Override
	public Description read() {

		Description desc = new Description();

		Node parentCaseTextN = PARNXP.selectSingleNode(document);
		if (parentCaseTextN != null) {
			desc.addSection(new DescriptionSection(DescSection.REL_APP_DESC, parentCaseTextN.asXML(), textProcessor));
		}

		Node briefSummary = BSUMXP.selectSingleNode(document);
		if (briefSummary != null) {
			desc.addSection(new DescriptionSection(DescSection.BRIEF_SUMMARY, briefSummary.asXML(), textProcessor));
		}

		Node drawingDesc = DRWDXP.selectSingleNode(document);
		if (drawingDesc != null) {
			desc.addSection(new DescriptionSection(DescSection.DRAWING_DESC, drawingDesc.asXML(), textProcessor));

			List<Figure> figures = new DescriptionFigures(drawingDesc).read();
			desc.addFigures(figures);
		}

		Node detailedDesc = DETDXP.selectSingleNode(document);
		if (detailedDesc != null) {
			desc.addSection(new DescriptionSection(DescSection.DETAILED_DESC, detailedDesc.asXML(), textProcessor));
		}

		return desc;
	}

}
