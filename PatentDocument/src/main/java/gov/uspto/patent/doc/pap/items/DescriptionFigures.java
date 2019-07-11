package gov.uspto.patent.doc.pap.items;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;

import gov.uspto.parser.dom4j.ItemReader;
import gov.uspto.patent.model.Figure;

public class DescriptionFigures extends ItemReader<List<Figure>> {

	private static final XPath PARAGRAPH_XP = DocumentHelper.createXPath("section/paragraph|paragraph");
	private static final XPath CROSSREF_XP = DocumentHelper.createXPath("cross-reference");

	public DescriptionFigures(Node itemNode) {
		super(itemNode);
	}

	@Override
	public List<Figure> read() {
		List<Figure> figures = new ArrayList<Figure>();

		List<Node> paragraphNodes = PARAGRAPH_XP.selectNodes(itemNode);

		for (Node paragraphN : paragraphNodes) {
			List<Node> figN = CROSSREF_XP.selectNodes(paragraphN);
			if (figN != null && !figN.isEmpty()) {
				String id = figN.get(0).getText();
				String text = paragraphN.getText().trim();
				Figure fig = new Figure(text, id);
				figures.add(fig);
			}
		}

		return figures;
	}
}
