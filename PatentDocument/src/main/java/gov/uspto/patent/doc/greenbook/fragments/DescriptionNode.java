package gov.uspto.patent.doc.greenbook.fragments;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.TextProcessor;
import gov.uspto.patent.doc.greenbook.items.DescriptionFigures;
import gov.uspto.patent.model.DescSection;
import gov.uspto.patent.model.Description;
import gov.uspto.patent.model.DescriptionSection;
import gov.uspto.patent.model.Figure;

public class DescriptionNode extends DOMFragmentReader<Description> {

    public DescriptionNode(Document document, TextProcessor textProcessor) {
        super(document, textProcessor);
    }

    @Override
    public Description read() {

        Description desc = new Description();

        Node parentCaseTextN = document.selectSingleNode("/DOCUMENT/PARN");
        if (parentCaseTextN != null) {
            desc.addSection(new DescriptionSection(DescSection.REL_APP_DESC, parentCaseTextN.asXML(), textProcessor));
        }

        Node briefSummary = document.selectSingleNode("/DOCUMENT/BSUM");
        if (briefSummary != null) {
            desc.addSection(new DescriptionSection(DescSection.BRIEF_SUMMARY, briefSummary.asXML(), textProcessor));
        }

        Node drawingDesc = document.selectSingleNode("/DOCUMENT/DRWD");
        if (drawingDesc != null) {
            desc.addSection(new DescriptionSection(DescSection.DRAWING_DESC, drawingDesc.asXML(), textProcessor));

            List<Figure> figures = new DescriptionFigures(drawingDesc).read();
            desc.addFigures(figures);
        }

        Node detailedDesc = document.selectSingleNode("/DOCUMENT/DETD");
        if (detailedDesc != null) {
            desc.addSection(new DescriptionSection(DescSection.DETAILED_DESC, detailedDesc.asXML(), textProcessor));
        }

        return desc;
    }

}
