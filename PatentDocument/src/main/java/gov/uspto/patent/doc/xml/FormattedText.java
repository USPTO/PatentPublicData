package gov.uspto.patent.doc.xml;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.nodes.Document.OutputSettings.Syntax;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Parser;
import org.jsoup.parser.Tag;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;

import gov.uspto.common.text.UnicodeUtil;
import gov.uspto.patent.ReferenceTagger;
import gov.uspto.patent.TextProcessor;
import gov.uspto.patent.doc.simplehtml.FreetextConfig;
import gov.uspto.patent.doc.simplehtml.HtmlToPlainText;
import gov.uspto.patent.mathml.MathmlEscaper;

/**
 * Parse and Clean Formated Text Fields, such as Description, Abstract and
 * Claims.
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class FormattedText implements TextProcessor {

	private static final Pattern TRAILING_REGEX = Pattern.compile("^\\s?(?:[.,;(])?([a-z])([.,;)]|\\b)");

	private static final String[] HTML_WHITELIST_TAGS = new String[] { "br", "b", "sub", "sup", "h1", "h2", "h3", "h4",
			"h5", "h6", "p", "table", "tbody", "thead", "th", "tr", "td", "ul", "ol", "li", "dl", "dt", "dd", "a",
			"span", "colgroup", "col" };
	private static final String[] HTML_WHITELIST_ATTRIB = new String[] { "class", "id", "idref", "num", "format",
			"type", "level", "width", "align", "valign", "rowspan" };

	public static final ImmutableSet<String> HEADER_ELEMENTS = ImmutableSet.of("heading", "p[id^=h-]");
	public static final ImmutableSet<String> TABLE_ELEMENTS = ImmutableSet.of("tr", "entry", "row", "table");
	public static final ImmutableSet<String> LIST_ELEMENTS = ImmutableSet.of("ul", "ol", "li", "dl", "dt", "dd");

	@Override
	public String getPlainText(String rawText, FreetextConfig textConfig) {
		String simpleHtml = getSimpleHtml(rawText);
		Document simpleDoc = Jsoup.parse(simpleHtml, "", Parser.xmlParser());

		HtmlToPlainText htmlConvert = new HtmlToPlainText(textConfig);
		return htmlConvert.getPlainText(simpleDoc);
	}

	@Override
	public String getSimpleHtml(String rawText) {

		/*
		 * Change xml processing instruction "in-line-formulae" to normal xml
		 * node, as it was in the Patent PAP format; also making it searchable
		 * within jsoup.
		 */
		rawText = rawText.replaceAll("<\\?in-line-formulae description=\"In-line Formulae\" end=\"lead\"\\?>",
				"<in-line-formula>");
		rawText = rawText.replaceAll("<\\?in-line-formulae description=\"In-line Formulae\" end=\"end\"\\?>",
				"</in-line-formula>");

		Document document = Jsoup.parse("<body>" + rawText + "</body>", "", Parser.xmlParser());
		document.outputSettings().prettyPrint(false).syntax(Syntax.xml).charset(StandardCharsets.UTF_8)
				.escapeMode(EscapeMode.xhtml);

		document.select("bold").tagName("b");

		Elements forEls = document.select("in-line-formula");
		for (int i = 1; i <= forEls.size(); i++) {
			Element element = forEls.get(i - 1);
			element.attr("id", "FOR-" + Strings.padStart(String.valueOf(i), 4, '0'));
			element.tagName("span");
			element.addClass("formula");
		}

		/*
		 * HEADING tags
		 */
		for (Element heading : document.select("heading")) {
			if (heading.hasAttr("lvl")) {
				heading.attr("level", heading.attr("lvl"));
			}
			heading.tagName("h2");
		}
		// Header Paragraphs which have an id starting with "h-".
		document.select("p[id^=h-]").tagName("h4");

		for (Element par : document.select("p")) {
			if (par.hasAttr("lvl")) {
				par.attr("level", par.attr("lvl"));
			}
		}

		/*
		 * Patent Drawing FIGREF tags not needed in index.
		 * 
		 * <figref idref="DRAWINGS">FIG. 1A</figref>
		 * 
		 * <a id="FR-0001" idref="FIG-1A" class="figref">FIG. 1A</figref>
		 */
		Elements figRefEls = document.select("figref");
		for (int i = 1; i <= figRefEls.size(); i++) {
			Element element = figRefEls.get(i - 1);
			element.attr("id", "FR-" + Strings.padStart(String.valueOf(i), 4, '0'));
			element.attr("idref", ReferenceTagger.createFigId(element.text()));
			element.tagName("a");
			element.addClass("figref");
		}

		/*
		 * Patent Claim Reference
		 * 
		 * <claim-ref idref="CLM-00001">claim 1</claim-ref>
		 * 
		 * <a idref="CLM-00001" class="claim">claim 1</a>
		 */
		Elements clmRefEls = document.select("claim-ref");
		for (int i = 1; i <= clmRefEls.size(); i++) {
			Element element = clmRefEls.get(i - 1);
			element.attr("id", "CR-" + Strings.padStart(String.valueOf(i), 4, '0'));
			element.tagName("a");
			element.addClass("claim");
		}

		/*
		 * Patent Citation Reference
		 * 
		 */
		for (Element element : document.select("patcit")) {
			element.tagName("a");
			element.addClass("patcite");
		}

		/*
		 * Patent Citation Reference
		 */
		for (Element element : document.select("nplcit")) {
			element.tagName("a");
			element.addClass("nplcite");
		}

		/*
		 * crossref tags are internal links to another section.
		 */
		for (Element element : document.select("crossref")) {
			element.tagName("a");
			element.addClass("crossref");
		}

		fixFigrefLists(document);

		for (Element element : document.select("a")) {
			figrefMergeTrailing(element);
		}

		/*
		 * Escape MathML math elements, to maintain all xml elements after
		 * sending through Cleaner.
		 */
		boolean mathFound = false;
		Elements mathEls = document.select("math");
		for (int i = 1; i <= mathEls.size(); i++) {
			Element element = mathEls.get(i - 1);
			mathFound = true;

			// String mathml = MathmlEscaper.escape(element.outerHtml());
			String mathml = "";
			try {
				mathml = Base64.getEncoder().encodeToString(element.outerHtml().getBytes("utf-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			Element newEl = new Element(Tag.valueOf("span"), "");
			newEl.attr("id", "MTH-" + Strings.padStart(String.valueOf(i), 4, '0'));
			newEl.addClass("math");
			newEl.attr("format", "mathml");
			newEl.appendChild(new TextNode(mathml, null));
			element.replaceWith(newEl);
			figrefMergeTrailing(newEl);
		}

		/*
		 * Subscript use unicode if able to convert
		 */
		for (Element el : document.select("sub")) {
			try {
				String unicode = UnicodeUtil.toSubscript(el.html());
				el.replaceWith(new TextNode(unicode, null));
			} catch (ParseException e) {
				// ignore.
			}
		}

		/*
		 * Superscript use unicode if able to convert
		 */
		for (Element el : document.select("sup")) {
			try {
				String unicode = UnicodeUtil.toSuperscript(el.html());
				el.replaceWith(new TextNode(unicode, null));
			} catch (ParseException e) {
				// ignore.
			}
		}

		// document.select("sub2").prepend("_");
		// document.select("sup2").prepend("^");

		/*
		 * List
		 */
		document.select("ol").tagName("ul");

		/*
		 * Tables: Convert CALS Table to HTML Table
		 */
		Elements tableEls = document.select("table");
		for (int i = 1; i <= tableEls.size(); i++) {
			Element element = tableEls.get(i - 1);
			element.attr("id", "TBL-" + Strings.padStart(String.valueOf(i), 4, '0'));

			Element colGroup = element.prependElement("colgroup");
			for (Element spec : element.select("colspec")) {
				colGroup.appendElement("col").attr("width", spec.attr("colwidth")).attr("align", spec.attr("align"));
			}

			for (Element row : element.select("thead row")) {
				for (Element cell : row.select("entry")) {
					cell.tagName("th");
				}
				row.tagName("tr");
			}

			for (Element row : element.select("tbody row")) {
				for (Element cell : row.select("entry")) {
					String rowSpanSt = cell.attr("morerows");
					int rowspan = !rowSpanSt.isEmpty() ? Integer.parseInt(rowSpanSt) + 1 : 1;
					if (rowspan > 1) {
						cell.attr("rowspan", String.valueOf(rowspan));
					}
					cell.tagName("td");
				}
				row.tagName("tr");
			}
		}

		// document.select("p:matchesOwn((?is) +?)").remove(); // remove
		// paragraphs which contain only spaces.

		String docStr = document.html().replaceAll("\\s{2,}", " ");
		docStr = docStr.replaceAll("\\\\n", "\n");

		Whitelist whitelist = Whitelist.none();
		whitelist.addTags(HTML_WHITELIST_TAGS);
		whitelist.addAttributes(":all", HTML_WHITELIST_ATTRIB);

		OutputSettings outSettings = new Document.OutputSettings();
		outSettings.charset(Charsets.UTF_8);
		outSettings.syntax(Syntax.xml);
		outSettings.outline(true);
		outSettings.prettyPrint(false);
		outSettings.escapeMode(EscapeMode.xhtml);
		// outSettings.escapeMode(EscapeMode.extended);

		docStr = Jsoup.clean(docStr, "", whitelist, outSettings);

		if (mathFound) {
			// Reload document and un-base64 the mathml sections.
			document = Jsoup.parse("<body>" + docStr + "</body>", "", Parser.xmlParser());
			document.outputSettings().prettyPrint(false).syntax(OutputSettings.Syntax.xml)
					.charset(StandardCharsets.UTF_8);

			for (Element el : document.select("span[class=math]")) {
				try {
					String html = new String(Base64.getDecoder().decode(el.html()), "utf-8");
					el.text("");
					el.append(html);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
			docStr = document.select("body").html();
		}

		return docStr;
	}

	/*
	 * Capture trailing non-space trailing text back to entity.
	 *
	 * <a idref="DRAWINGS">FIG. 4</a><i>a</i>
	 * (<a idref="DRAWINGS">FIG. 4</a>a)
	 * <a idref="DRAWINGS">FIG. 4</a>a;
	 * <a idref="DRAWINGS">FIG. 4</a> <i>(a)</i>;
	 * 
	 *  ==> <a idref="DRAWINGS">FIG. 4a</a>
	 * 
	 */
	public void figrefMergeTrailing(Element element) {
		Node next = element.nextSibling();

		String trailingTxt;
		if (next != null && next instanceof TextNode) {
			trailingTxt = ((TextNode) next).getWholeText();
			
		} else if (next != null && next instanceof Element) {
			trailingTxt = ((Element) next).text();
		} else {
			return;
		}

		Matcher matcher = TRAILING_REGEX.matcher(trailingTxt);
		if (matcher.matches()) {
			String trailingChar = matcher.group(1);
			String buffer = matcher.group(2);
			String tail = trailingTxt.substring(matcher.end() - buffer.length()).trim();
			if (next instanceof Element) {
				Element nel = (Element) next;
				if (trailingTxt.length() == 1) {
					element.append(nel.outerHtml());
					nel.remove();
				} else {
					nel.text(tail);
					element.text(element.text() + trailingChar);
				}
			}
			else if (next instanceof TextNode) {
				TextNode txtNode = (TextNode) next;
				txtNode.text(tail);
				element.text(element.text() + trailingChar);
			}
		}
	}

	/*
	 * Fix Figref Lists TRAILING
	 * 
	 * <figref idref=\"DRAWINGS\">FIGS. 1</figref>, <b>2</b> and <b>3</b>c
	 *   become 
	 * <figref>FIGS. 1</figref>, <figref>2</figref> and <figref>3c</figref>
	 */
	public void fixFigrefLists(Document document) {
		for (Element element : document.select("a.figref")) {
			fixFigrefListItem(element);
		}
	}

	public void fixFigrefListItem(Element element) {
		Node next = element.nextSibling();
		
		String trailingTxt;
		if (next != null && next instanceof TextNode) {
			trailingTxt = ((TextNode) next).getWholeText();
		} else if (next != null && next instanceof Element) {
			trailingTxt = ((Element) next).text();
		} else {
			return;
		}

		if (trailingTxt.matches("^(, |,? and )")) {
			next = element.nextSibling().nextSibling();
			if (next.nodeName().toLowerCase().equals("b")){
				String containedTxt = ((TextNode) next.childNode(0)).getWholeText();
				if (containedTxt.matches("[0-9]{1,2}[A-z]?")){
					Element newEl = element.clone();
					newEl.attr("id", "FR-" + Strings.padStart(containedTxt, 4, '0'));
					newEl.attr("idref", ReferenceTagger.createFigId(containedTxt));
					newEl.tagName("a");
					newEl.addClass("figref");
					newEl.text(containedTxt);
					next.replaceWith(newEl);
					
					fixFigrefListItem(newEl);
				}
			}
		}
	}
	
	@Override
	public List<String> getParagraphText(String rawText) {
		String textWithPMarks = getSimpleHtml(rawText);
		Document jsoupDoc = Jsoup.parse(textWithPMarks, "", Parser.xmlParser());

		List<String> paragraphs = new ArrayList<String>();
		for (Element element : jsoupDoc.select("p")) {
			paragraphs.add(element.html());
		}

		return paragraphs;
	}

}
