package gov.uspto.patent.doc.greenbook.items;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.ItemReader;
import gov.uspto.patent.model.Figure;

/**
 * DescriptionFigures, read Patent figures from Description Drawing Section.
 *
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
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
			String pargraphText = paragraphN.getText();
			DescriptionFigures.findFigures(pargraphText, figures);
		}

		return figures;
	}

	public static void findFigures(String pargraphText, List<Figure> figureList){
		Matcher matchFig = PATENT_FIG.matcher(pargraphText);
		if (matchFig.lookingAt()){
			String id = matchFig.group(1);
			int end = matchFig.end();
			if (!id.equals(pargraphText)){
				String text = pargraphText.substring(end+1);
				Figure fig = new Figure(text, id);
				figureList.add(fig);
			}

		} else {
			Matcher matchFigs = PATENT_FIGS.matcher(pargraphText);
			if (matchFigs.lookingAt()){
				String id = matchFigs.group(1);
				int end = matchFig.end();
				if (!id.equals(pargraphText)){
					String text = pargraphText.substring(end+1);
					Figure fig = new Figure(text, id);
					figureList.add(fig);
				}
			} else {
				if (pargraphText.matches("^FIG")){
					LOGGER.warn("Unable to Parse Patent Figure ID: '" + pargraphText);
				}
			}
		}
	}
}
