package gov.uspto.patent.doc.simplehtml;

import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Evaluator;
import org.jsoup.select.NodeVisitor;

/**
 * HTML to Plaintext
 * 
 *<p>Adapted from org.jsoup.examples.HtmlToPlainText</p>
 * 
 *<h3>Changes</h3>
 *<ul>
 *<li>Added FreetextConfig</li>
 *<li>Added PrettyPrint - when false, created newlines are commented out</li>
 *<li>Copied traverse method and added ability to Filter and Replace Nodes (Brian G. Feldman <brian.feldman@uspto.gov>)</li>
 *</ul>
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
	 * @param element the root element to format
	 * @return formatted text
	 */
	public String getPlainText(Element element) {

		traverse(element);

		return this.toString();
	}

	public void traverse(Node root) {
		Node node = root;
		int depth = 0;

		while (node != null) {
			if (filter(node) || replace(node)) {
				node = node.nextSibling();
				continue;
			}

			head(node, depth);

			if (node.childNodeSize() > 0) {
				node = node.childNode(0);
				depth++;
			} else {
				while (node.nextSibling() == null && depth > 0) {
					tail(node, depth);
					node = node.parentNode();
					depth--;
				}
				tail(node, depth);
				if (node == root) {
					break;
				}
				node = node.nextSibling();
			}
		}
	}

	public boolean filter(Node node) {
		if (config.getRemoveElements().contains(node.nodeName().toLowerCase())) {
			return true;
		}
		return false;
	}

	public boolean replace(Node node) {
		try {
			Element el = (Element) node;

			for (Evaluator cssSelector : config.getReplaceElements().keySet()) {
				if (el.is(cssSelector)) {
					append(config.getReplaceElements().get(cssSelector));
					return true;
				}
			}
		} catch (ClassCastException e) {
			// ignore.
		}

		return false;
	}

	public void head(Node node, int depth) {
		String name = node.nodeName();

		if (node instanceof TextNode) {
			append(((TextNode) node).text());
		} else if (name.equals("li")) {
			if (config.isPrettyPrint()) {
				append("\n * ");
			} else {
				append("\\n *");
			}
		} else if (name.equals("dt")) {
			append("  ");
		} else if (StringUtil.in(name, "p", "h1", "h2", "h3", "h4", "h5", "tr", "table")) {
			if (config.isPrettyPrint()) {
				append("\n");
			} else {
				append("\\n");
			}
		}
	}

	public void tail(Node node, int depth) {
		String name = node.nodeName();

		if (StringUtil.in(name, "br", "dd", "dt", "p", "h1", "h2", "h3", "h4", "h5", "table")) {
			if (config.isPrettyPrint()) {
				append("\n");
			} else {
				append("\\n");
			}
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
					if (config.isPrettyPrint()) {
						accum.append("\n");
					} else {
						accum.append("\\n");
					}
					accum.append(word);
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
