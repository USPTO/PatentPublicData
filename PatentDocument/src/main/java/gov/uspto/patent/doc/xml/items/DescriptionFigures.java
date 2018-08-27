package gov.uspto.patent.doc.xml.items;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.ItemReader;
import gov.uspto.patent.doc.xml.fragments.DescriptionNode;
import gov.uspto.patent.model.Figure;

public class DescriptionFigures extends ItemReader<List<Figure>> {
	private static final Logger LOGGER = LoggerFactory.getLogger(DescriptionFigures.class);

	public DescriptionFigures(Node itemNode) {
		super(itemNode);
	}

	@Override
	public List<Figure> read() {
		String id =null;
		List<Figure> figures = new ArrayList<Figure>();
		List<Node> childNodes = DescriptionNode.getSectionNodes(itemNode, "brief-description-of-drawings");
		for(Node childN: childNodes){
			if ("description-of-drawings".equals(childN.getName())){
				List<Node> figNodes = childN.selectNodes("p[figref]");
				for (Node pWithFigN: figNodes){
					id = pWithFigN.selectSingleNode("figref").getText();
					if (null != pWithFigN.selectSingleNode("i")) {
						id = id + pWithFigN.selectSingleNode("i").getText();
					}
					String text = pWithFigN.getText().trim();
					Figure fig = new Figure(text, id);
					figures.add(fig);
				}

			}
		}
		return figures;
	}
}
