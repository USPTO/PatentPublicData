package gov.uspto.patent.doc.sgml;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.nodes.Document.OutputSettings.Syntax;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Parser;
import org.jsoup.parser.Tag;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;

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

	private static final String[] HTML_WHITELIST_TAGS = new String[] { "br", "b", "sub", "sup", "h1", "h2", "h3", "h4",
			"h5", "h6", "p", "table", "tbody", "thead", "th", "tr", "td", "ul", "ol", "li", "dl", "dt", "dd", "a",
			"span", "colgroup", "col" };
	private static final String[] HTML_WHITELIST_ATTRIB = new String[] { "class", "id", "idref", "num", "format",
			"type", "level", "width", "align", "valign", "rowspan" };

	@Override
	public String getPlainText(String rawText, FreetextConfig textConfig) {
		Document jsoupDoc = Jsoup.parse(rawText, "", Parser.xmlParser());

		for (Element paragraph : jsoupDoc.select("PARA")) {
			int level = paragraph.attr("LVL") != null ? Integer.valueOf(paragraph.attr("LVL")) : 0;
			StringBuilder stb = new StringBuilder();
			for (int i = 0; i <= level; i++) {
				stb.append("&nbsp;");
			}
			paragraph.prepend(stb.toString());
		}

		String simpleHtml = getSimpleHtml(jsoupDoc.outerHtml());
		Document simpleDoc = Jsoup.parse(simpleHtml, "", Parser.xmlParser());

		HtmlToPlainText htmlConvert = new HtmlToPlainText(textConfig);
		return htmlConvert.getPlainText(simpleDoc);
	}

	@Override
	public String getSimpleHtml(String rawText) {
        Document jsoupDoc = Jsoup.parse("<body>" + rawText + "</body>", "", Parser.xmlParser());
		jsoupDoc.outputSettings().prettyPrint(false).syntax(Syntax.xml).charset(StandardCharsets.UTF_8).escapeMode(EscapeMode.xhtml);

		jsoupDoc.select("bold").tagName("b");

		Elements figRefEls = jsoupDoc.select("FGREF");
		for (int i = 1; i <= figRefEls.size(); i++) {
			Element element = figRefEls.get(i - 1);
			element.attr("id", "FR-" + Strings.padStart(String.valueOf(i), 4, '0'));
			element.attr("idref", ReferenceTagger.createFigId(element.select("PDAT").text()));
			element.tagName("a");
			element.addClass("figref");
		}

		Elements clmRefEls = jsoupDoc.select("CLREF");
		for (int i = 1; i <= clmRefEls.size(); i++) {
			Element element = clmRefEls.get(i - 1);
			element.attr("idref", element.attr("id"));
			element.attr("id", "CR-" + Strings.padStart(String.valueOf(i), 4, '0'));
			element.tagName("a");
			element.addClass("claim");
		}

		/*
		 * for (Element element : jsoupDoc.select("CLM PARA PTEXT > PDAT")) {
		 * //String text = element.text(); //element.replaceWith(new
		 * Node("claim-text")); element.unwrap();
		 * //element.tagName("claim-text"); }
		 */

		// Paragraph headers.
		// jsoupDoc.select("H").tagName("h2");
		for (Element heading : jsoupDoc.select("H")) {
			heading.attr("level", heading.attr("LVL")).tagName("h2");
			// heading.removeAttr("lvl");
		}

		// Remove any paragraph headers.
		for (Element element : jsoupDoc.select("TBLREF")) {
			element.replaceWith(new TextNode("Table-Reference", null));
		}

        /*
         * Escape MathML math elements, to maintain all xml elements after
         * sending through Cleaner.
         */
        boolean mathFound = false;
        Elements mathEls = jsoupDoc.select("math");
        for (int i = 1; i <= mathEls.size(); i++) {
            Element element = mathEls.get(i - 1);
            mathFound = true;

            //String mathml = MathmlEscaper.escape(element.outerHtml());
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
        }

		jsoupDoc.select("CLM PARA").unwrap();
		// jsoupDoc.select("CLM CLMSTEP").tagName("claim-text");
		jsoupDoc.select("CLM CLMSTEP").tagName("li");

		// Rename all "para" tags to "p".
		// jsoupDoc.select("PARA").tagName("p");
		for (Element par : jsoupDoc.select("PARA")) {
			par.attr("level", par.attr("lvl"));
			par.removeAttr("lvl");
			par.tagName("p");
		}

		/*
		 * Subscript use unicode if able to convert
		 */
		for (Element el : jsoupDoc.select("SB")) {
			try {
				String unicode = UnicodeUtil.toSubscript(el.html());
				el.replaceWith(new TextNode(unicode, null));
			} catch (ParseException e) {
				el.tagName("sub");
			}
		}

		/*
		 * Superscript use unicode if able to convert
		 */
		for (Element el : jsoupDoc.select("SP")) {
			try {
				String unicode = UnicodeUtil.toSuperscript(el.html());
				el.replaceWith(new TextNode(unicode, null));
			} catch (ParseException e) {
				el.tagName("sup");
			}
		}

		/*
		 * Tables: Convert CALS Table to HTML Table
		 */
		Elements tableEls = jsoupDoc.select("table");
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
		
		String textStr = jsoupDoc.html();
		textStr = textStr.replaceAll("\\\\n", "\n");

		Whitelist whitelist = Whitelist.none();
		whitelist.addTags(HTML_WHITELIST_TAGS);
		whitelist.addAttributes(":all", HTML_WHITELIST_ATTRIB);

		OutputSettings outSettings = new Document.OutputSettings();
		outSettings.charset(Charsets.UTF_8);
		outSettings.syntax(Syntax.xml);
		outSettings.outline(true);
		outSettings.prettyPrint(false);
		//outSettings.escapeMode(EscapeMode.extended);
        outSettings.escapeMode(EscapeMode.xhtml);


		String fieldTextCleaned = Jsoup.clean(textStr, "", whitelist, outSettings);
		// fieldTextCleaned = fieldTextCleaned.replaceAll("\\s+(\\r|\\n)\\s+", "
		// ");

        if (mathFound) {
            // Reload document and un-base64 the mathml sections.
            jsoupDoc = Jsoup.parse("<body>" + fieldTextCleaned + "</body>", "", Parser.xmlParser());
            jsoupDoc.outputSettings().prettyPrint(false).syntax(OutputSettings.Syntax.xml).charset(StandardCharsets.UTF_8);

            for (Element el : jsoupDoc.select("span[class=math]")) {
                try {
                    String html = new String(Base64.getDecoder().decode(el.html()), "utf-8");
                    el.text("");
                    el.append(html);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            fieldTextCleaned = jsoupDoc.select("body").html();
        }

		return fieldTextCleaned;
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
