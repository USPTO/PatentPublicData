package gov.uspto.patent.greenbook.items;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.ItemReader;
import gov.uspto.patent.model.Figure;

public class DescriptionFigures extends ItemReader<List<Figure>>{
	private static final Logger LOGGER = LoggerFactory.getLogger(DescriptionFigures.class);

	private static final Pattern PATENT_FIG = Pattern.compile("^(FIG\\.? \\(?\\d{1,3}[A-Za-z]?\\)?)\\b");
	private static final Pattern PATENT_FIGS = Pattern.compile("^(FIGS\\.? \\d{1,3}\\s?\\(?[A-Za-z]?\\)?(?:(?:\\s?\\-\\s?|, | and | to | through )\\d{0,3}\\(?[A-Za-z]?\\)?)+)\\b");

	public DescriptionFigures(Node itemNode) {
		super(itemNode);
	}

	@Override
	public List<Figure> read() {
		List<Figure> figures = new ArrayList<Figure>();

		@SuppressWarnings("unchecked")
		List<Node> paragraphNodes = itemNode.selectNodes("PAR");

		for(Node paragraphN : paragraphNodes){
			String ptext = paragraphN.getText();
			
			Matcher matchFig = PATENT_FIG.matcher(ptext);
			if (matchFig.lookingAt()){
				String id = matchFig.group(1);
				String text = paragraphN.getText().substring(matchFig.end()+1);
				Figure fig = new Figure(id, text);
				figures.add(fig);
			} else {
				Matcher matchFigs = PATENT_FIGS.matcher(ptext);
				if (matchFigs.lookingAt()){
					String id = matchFigs.group(1);
					String text = paragraphN.getText().substring(matchFigs.end()+1);
					Figure fig = new Figure(id, text);
					figures.add(fig);
				} else {
					if (ptext.matches("^FIG")){
						LOGGER.warn("Unable to Parse Patent Figure ID: '" + paragraphN.getText());
					}
				}
			}
		}

		return figures;
	}
}
