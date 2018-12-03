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
 * <p>
 * Adapted from org.jsoup.examples.HtmlToPlainText
 * </p>
 * 
 * <h3>Changes</h3>
 * <ul>
 * <li>Added ul list decorator from list-style-type or class</li>
 * <li>Added text shorthand "_" for SUB and "^" for SUP</li>
 * <li>Added optional Paragraph indenting</li>
 * <li>Added block indenting (blockquote,ul,ol,dl)</li>
 * <li>Added FreetextConfig</li>
 * <li>Added PrettyPrint - when false, created newlines are commented out</li>
 * <li>Copied traverse method and added ability to Filter and Replace Nodes
 * (Brian G. Feldman <brian.feldman@uspto.gov>)</li>
 * </ul>
 *
 */
public class HtmlToPlainText implements NodeVisitor {

	private static final int MAX_WIDTH_DEFAULT = 80;
	private final int maxWidth;
	private int width = 0;
	private StringBuilder accum = new StringBuilder();
	private final FreetextConfig config;
	private boolean insideIndentBlock = false;
	private String listDecorator = "*";

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
				append("\n " + listDecorator + " ");
			} else {
				append("\\n " + listDecorator + " ");
			}
		} else if (name.equals("dt")) {
			append("  ");
		} else if (StringUtil.in(name, "blockquote", "ul", "ol", "dl")) {
			if (name.equals("ul") && node.hasAttr("style")) {
				if (node.attr("style").contains("list-style-type:none")) {
					listDecorator = "";
				}
				else {
					listDecorator = "*";
				}
			}
			else if (node.hasAttr("class") && node.attr("class").equals("ul-dash")) {
				listDecorator = "-";
			}
			else {
				listDecorator = "*";
			}

			insideIndentBlock = true;
			if (config.isPrettyPrint()) {
				append("\n");
			} else {
				append("\\n");
			}
		} else if (name.equals("q")) {
			append("\u201C"); // open quote.
		} else if (name.equals("p")) {
			if (config.isPrettyPrint()) {
				if (config.isIndentParagraphs()) {
					append("\n\t");
				} else {
					append("\n");
				}
			} else {
				if (config.isIndentParagraphs()) {
					append("\\n\t");
				} else {
					append("\\n");
				}
			}
		} else if (StringUtil.in(name, "h1", "h2", "h3", "h4", "h5", "h6", "tr", "table")) {
			if (config.isPrettyPrint()) {
				append("\n");
			} else {
				append("\\n");
			}
		} else if (name.equals("sup")) {
			append("^{"); // Math superscript TeX block
		} else if (name.equals("sub")) {
			append("_{"); // Math subscript TeX block
		}
	}

	public void tail(Node node, int depth) {
		String name = node.nodeName();

		if (name.equals("q")) {
			append("\u201d"); // close quote.
		} else if (StringUtil.in(name, "blockquote", "ul", "ol", "dl")) {
			insideIndentBlock = false;
		} else if (StringUtil.in(name, "br", "dd", "dt", "p", "h1", "h2", "h3", "h4", "h5", "h6", "table")) {
			if (config.isPrettyPrint()) {
				append("\n");
			} else {
				append("\\n");
			}
		} else if (StringUtil.in(name, "sup", "sub")) {
			append("}"); // Math close superscript or subscript block
		}
	}

	private void append(String text) {
		append(text, insideIndentBlock);
	}

	private void append(String text, boolean tabIndentBlock) {
		if (StringUtil.in(text, "\n", "\\n", "\n\t", "\\n\t")) {
			accum.append(text);
			return;
		}

		int maxWidth = this.maxWidth;
		int indentLen = 0;
		if (tabIndentBlock) {
			indentLen = "\t".length();
			maxWidth -= indentLen;
		}

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
						accum.append('\n');
					} else {
						accum.append("\\n");
					}
					if (tabIndentBlock) {
						accum.append('\t');
					}
					accum.append(word);
					width = word.length() + indentLen;
				} else {
					if (tabIndentBlock) {
						accum.append('\t');
					}
					accum.append(word);
					width += word.length() + indentLen;
				}
			}
		} else {
			if (tabIndentBlock) {
				accum.append('\t');
			}
			accum.append(text);
			width += text.length() + indentLen;
		}
	}

	@Override
	public String toString() {
		return accum.toString();
	}
}
