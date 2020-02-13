package gov.uspto.patent.doc.sgml.items;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;

import gov.uspto.parser.dom4j.ItemReader;
import gov.uspto.patent.model.Figure;

public class DescriptionFigures extends ItemReader<List<Figure>> {

	private static final XPath PARAXP = DocumentHelper.createXPath("BTEXT/PARA");
	private static final XPath PFIGXP = DocumentHelper.createXPath("PTEXT/FGREF/PDAT");
	private static final XPath PFIGTXTXP = DocumentHelper.createXPath("descendant::PDAT[not(ancestor::FGREF)]/text()");

	public DescriptionFigures(Node itemNode) {
		super(itemNode);
	}

	@Override
	public List<Figure> read() {
		List<Figure> figures = new ArrayList<Figure>();

		List<Node> paragraphNodes = PARAXP.selectNodes(itemNode);

		for (Node paragraphN : paragraphNodes) {

			List<Node> figN = PFIGXP.selectNodes(paragraphN);
			if (figN != null && figN.size() > 0) {
				Node firstNode = figN.get(0);
				List<Node> parentNode = PFIGTXTXP.selectNodes(paragraphN);
				StringBuilder stb = new StringBuilder();
				for (Node textNode : parentNode) {
					stb.append(textNode.getText().trim()).append(" ");
				}

				String id = firstNode.getText();
				String text = stb.toString().trim();

				Figure fig = new Figure(text, id);
				figures.add(fig);
			}
		}

		return figures;
	}
}
