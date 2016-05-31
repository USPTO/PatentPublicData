package gov.uspto.patent.xml4ip.fragments;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.TextProcessor;
import gov.uspto.patent.model.DescSection;
import gov.uspto.patent.model.Description;
import gov.uspto.patent.model.DescriptionSection;
import gov.uspto.patent.model.Figure;
import gov.uspto.patent.xml.items.DescriptionFigures;

public class DescriptionNode extends DOMFragmentReader<Description> {
	private static final Logger LOGGER = LoggerFactory.getLogger(DescriptionNode.class);

	private static final String FRAGMENT_PATH = "//pat:Description";

	public DescriptionNode(Document document, TextProcessor textProcessor) {
		super(document, textProcessor);
	}

	@Override
	public Description read() {
		Node descN = document.selectSingleNode(FRAGMENT_PATH);
		if (descN == null) {
			LOGGER.warn("Patent does not have a Description.");
			return null;
		}

		Description desc = new Description();

		String relAppDesc = getSectionText(descN, "RELAPP");
		if (relAppDesc != null) {
			desc.addSection(new DescriptionSection(DescSection.REL_APP_DESC, relAppDesc, textProcessor));
		}

		String briefSummary = getSectionText(descN, "BRFSUM");
		if (briefSummary != null) {
			desc.addSection(new DescriptionSection(DescSection.BRIEF_SUMMARY, briefSummary, textProcessor));
		}

		String drawingDesc = getSectionText(descN, "brief-description-of-drawings");
		if (drawingDesc != null) {
			desc.addSection(new DescriptionSection(DescSection.DRAWING_DESC, drawingDesc, textProcessor));

			List<Figure> figures = new DescriptionFigures(descN).read();
			desc.addFigures(figures);
		}

		String detailedDesc = getSectionText(descN, "DETDESC");
		if (detailedDesc != null) {
			desc.addSection(new DescriptionSection(DescSection.DETAILED_DESC, detailedDesc, textProcessor));
		}

		return desc;
	}

	/**
	 * Get all node between two XML Processing Instructions nodes
	 *
	 *<p><pre>
	 *{@code
	 * <?RELAPP description="Other Patent Relations" end="lead"?>
	 * <p id="p-0002" num="0001">This application claims priority To Provisional Application File ...</p>
	 * <?RELAPP description="Other Patent Relations" end="tail"?>
	 *}
	 *</pre></p> 
	 * @param parentNode
	 * @param name
	 * @return
	 */
	public static String getSectionText(Node parentNode, String name) {
		List<Node> nodeLst = getSectionNodes(parentNode, name);
		if (nodeLst != null) {
			StringBuilder stb = new StringBuilder();
			for (Node node : nodeLst) {
				stb.append(node.asXML());
			}
			return stb.toString();
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	public static List<Node> getSectionNodes(Node parentNode, String name){
		return parentNode.selectNodes(getXPATHStatement(name));
	}

	/**
	 * XPATH to select all nodes between two processing-instructions.
	 *
	 *<p><pre>
	 *{@code
	 * //node()[
	 *   preceding-sibling::processing-instruction()[1][
	 *      self::processing-instruction('RELAPP')
	 *      and contains(., 'contains="lead"')
	 *   ]
	 *      and following-sibling::processing-instruction()[1][
	 *      self::processing-instruction('RELAPP')
	 *      and contains(., 'contains="tail"')
	 *   ]
	 * ]
	 *}
	 *</pre></p> 
	 * 
	 * @param name
	 * @return
	 */
	public static String getXPATHStatement(String name) {
		String xpath = "node()[ preceding-sibling::processing-instruction()[1][self::processing-instruction('" + name
				+ "') and contains(., 'end=\"lead\"') ] and following-sibling::processing-instruction()[1][ self::processing-instruction('"
				+ name + "') and contains(., 'end=\"tail\"') ]]";
		return xpath;
	}

}
