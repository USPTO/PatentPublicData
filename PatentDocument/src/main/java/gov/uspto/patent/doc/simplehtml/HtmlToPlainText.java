package gov.uspto.patent.doc.simplehtml;

import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

/**
 * HTML to Plaintext
 * 
 * Adapted from org.jsoup.examples.HtmlToPlainText
 *
 */
public class HtmlToPlainText implements NodeVisitor {

	private static final int MAX_WIDTH_DEFAULT = 80;
	private final int maxWidth;
	private int width = 0;
	private StringBuilder accum = new StringBuilder();
	private final FreetextConfig config;

	public HtmlToPlainText(FreetextConfig config) {
		this.maxWidth = config.getWrapWidth() != 0 ? config.getWrapWidth() : MAX_WIDTH_DEFAULT;
		this.config = config;
	}

	/**
	 * Format an Element to plain-text
	 * 
	 * @param element
	 *            the root element to format
	 * @return formatted text
	 */
	public String getPlainText(Element element) {
		
		for (String xmlElSelector : config.getRemoveElements()) {
			element.select(xmlElSelector).remove(); //.unwrap();
		}

		for (String xmlElSelector : config.getReplaceElements().keySet()) {
			for (Element matchEl : element.select(xmlElSelector)) {
				matchEl.replaceWith(new TextNode(config.getReplaceElements().get(xmlElSelector), null));
			}
		}

		NodeTraversor traversor = new NodeTraversor(this);
		traversor.traverse(element);
		//NodeTraversor.traverse(this, element);
		
		return this.toString();
	}

	public void head(Node node, int depth) {
		String name = node.nodeName();

		if (node instanceof TextNode) {
			append(((TextNode) node).text());
		} else if (name.equals("li")) {
			append("\n * ");
		} else if (name.equals("dt")) {
			append("  ");
		} else if (StringUtil.in(name, "p", "h1", "h2", "h3", "h4", "h5", "tr", "table")) {
			append("\n");
		}
	}

	public void tail(Node node, int depth) {
		String name = node.nodeName();

		if (StringUtil.in(name, "br", "dd", "dt", "p", "h1", "h2", "h3", "h4", "h5", "table")) {
			append("\n");
		}
	}

	private void append(String text) {
		if (text.startsWith("\n")) {
			width = 0;
		}

		if (text.equals(" ")
				&& (accum.length() == 0 || StringUtil.in(accum.substring(accum.length() - 1), " ", "\n"))) {
			return;
		}

		if (config.isWrapText() && text.length() + width > maxWidth) {
			String words[] = text.split("\\s+");
			for (int i = 0; i < words.length; i++) {
				String word = words[i];
				boolean last = i == words.length - 1;
				if (!last) {
					word = word + " ";
				}
				if (word.length() + width > maxWidth) {
					accum.append("\n").append(word);
					width = word.length();
				} else {
					accum.append(word);
					width += word.length();
				}
			}
		} else {
			accum.append(text);
			width += text.length();
		}
	}

	@Override
	public String toString() {
		return accum.toString();
	}
}
