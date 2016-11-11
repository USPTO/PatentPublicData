package gov.uspto.patent.doc.sgml.items;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.ItemReader;
import gov.uspto.patent.model.Figure;

public class DescriptionFigures extends ItemReader<List<Figure>> {
	private static final Logger LOGGER = LoggerFactory.getLogger(DescriptionFigures.class);

	public DescriptionFigures(Node itemNode) {
		super(itemNode);
	}

	@Override
	public List<Figure> read() {
		List<Figure> figures = new ArrayList<Figure>();

		@SuppressWarnings("unchecked")
		List<Node> paragraphNodes = itemNode.selectNodes("BTEXT/PARA");

		for (Node paragraphN : paragraphNodes) {

			@SuppressWarnings("unchecked")
			List<Node> figN = paragraphN.selectNodes("PTEXT/FGREF/PDAT");
			if (figN != null && figN.size() > 0) {
				Node firstNode = figN.get(0);
				List<Node> parentNode = paragraphN.selectNodes("descendant::PDAT[not(ancestor::FGREF)]/text()");
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
