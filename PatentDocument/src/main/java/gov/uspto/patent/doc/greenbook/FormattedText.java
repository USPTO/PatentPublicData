package gov.uspto.patent.doc.greenbook;

import java.util.ArrayList;
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
import org.jsoup.parser.Parser;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import com.google.common.base.Charsets;

import gov.uspto.patent.TextProcessor;
import gov.uspto.patent.doc.simplehtml.FreetextConfig;
import gov.uspto.patent.doc.simplehtml.HtmlToPlainText;

/**
 * Parse and Clean Formated Text Fields, such as Description, Abstract and
 * Claims.
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class FormattedText implements TextProcessor {

	private static final Pattern CLAIM_REF = Pattern.compile("\\bclaim (([0-9])(?:( or |-)([0-9]))?)\\b");
	private static final Pattern PATENT_FIG = Pattern
			.compile("\\b(FIGS?\\. )(?:([1-9][0-9]?[()A-z]*)(?:( to | and |-)([1-9][0-9]?[()A-z]*))?)\\b");

	private static final String[] HTML_WHITELIST = new String[] { "p", "h2", "pre", "table", "tr", "td", "a", "li",
			"ul", "span" };
	private static final String[] HTML_WHITELIST_ATTRIB = new String[] { "class", "id", "num", "idref", "level" };

	@Override
	public String getPlainText(String rawText, FreetextConfig textConfig) {
		Document jsoupDoc = Jsoup.parse(rawText, "", Parser.xmlParser());

		for (int j = 0; j < 4; j++) {
			for (Element paragraph : jsoupDoc.select("PA" + j)) {
				int level = j;
				StringBuilder stb = new StringBuilder();
				for (int i = 0; i <= level; i++) {
					stb.append("&nbsp;");
				}
				paragraph.prepend(stb.toString());
			}
		}

		String simpleHtml = getSimpleHtml(jsoupDoc.outerHtml());
		Document simpleDoc = Jsoup.parse(simpleHtml, "", Parser.xmlParser());

		HtmlToPlainText htmlConvert = new HtmlToPlainText(textConfig);
		return htmlConvert.getPlainText(simpleDoc);
	}

	@Override
	public String getSimpleHtml(String rawText) {	
		rawText = markRefs(rawText);
		Document jsoupDoc = Jsoup.parse("<body>" + rawText + "</body>", "", Parser.xmlParser());

		// rename header to "h2"
		jsoupDoc.select("PAC").tagName("h2");

		// Rename all "para" tags to "p".
		jsoupDoc.select("PAR").tagName("p");

		for (int j = 0; j < 4; j++) {
			jsoupDoc.select("PA" + j).tagName("p").attr("level", String.valueOf(j));
		}

		jsoupDoc.select("PAL").tagName("p");

		/*
		 * Greenbook tables are just freetext 
		 * with newline and white space formating
		 * maintain by using a html "pre" tag.
		 */
		//jsoupDoc.select("TBL").tagName("table");
		for (Element tbl : jsoupDoc.select("TBL")) {
			tbl.tagName("pre");
			tbl.attr("class", "freetext-table");

			Element par = jsoupDoc.createElement("p");
			tbl.replaceWith(par);
			par.appendChild(tbl);
		}

		String textStr = jsoupDoc.outerHtml();
		textStr = textStr.replaceAll("\\\\n", "\n");

		Whitelist whitelist = Whitelist.none();
		whitelist.addTags(HTML_WHITELIST);
		whitelist.addAttributes(":all", HTML_WHITELIST_ATTRIB);

		OutputSettings outSettings = new Document.OutputSettings();
		outSettings.charset(Charsets.UTF_8);
		outSettings.prettyPrint(false);
		outSettings.escapeMode(EscapeMode.extended);

        Cleaner cleaner = new Cleaner(whitelist);
        Document clean = cleaner.clean(jsoupDoc);
        clean.outputSettings(outSettings);
        return clean.body().html();
	}

	public String markRefs(String rawText) {
		StringBuilder stb = new StringBuilder(rawText);
		Matcher clmMatcher = CLAIM_REF.matcher(rawText);
		int additionalChars = 0;
		while (clmMatcher.find()) {
			String fullMatch = clmMatcher.group(0);

			StringBuilder claimStb = new StringBuilder();
			claimStb.append("<a class=\"claim\" idref=\"CLM-").append(clmMatcher.group(2));

			if (" or ".equals(clmMatcher.group(3))) {
				// claim 1 or 2  BECOMES  CLM-1 or CLM-2 
				claimStb.append("\">").append("CLM-").append(clmMatcher.group(2)).append("</a>");
				claimStb.append(clmMatcher.group(3)).append("<a class=\"claim\" idref=\"CLM-").append(clmMatcher.group(4));
				claimStb.append("\">").append("CLM-").append(clmMatcher.group(4)).append("</a>");
			} else if (clmMatcher.group(3) != null) {
				// claim 1 to 2  OR  claim 1 - 2  BECOMES  CLM-1 - CLM-2 
				claimStb.append(" - ").append("CLM-").append(clmMatcher.group(4));
				claimStb.append("\">").append(fullMatch).append("</a>");
			} else {
				claimStb.append("\">").append("CLM-").append(clmMatcher.group(2)).append("</a>");
			}
			String newStr = claimStb.toString();

			stb.replace(clmMatcher.start() + additionalChars, clmMatcher.end() + additionalChars, newStr);
			additionalChars = additionalChars + (newStr.length() - fullMatch.length());
		}

		String htmlText = stb.toString();
		stb = new StringBuilder(htmlText);
		Matcher figMatcher = PATENT_FIG.matcher(htmlText);
		additionalChars = 0;
		while (figMatcher.find()) {
			String fullMatch = figMatcher.group(0);

			StringBuilder figStb = new StringBuilder();
			figStb.append("<a class=\"figref\" idref=\"FIG-").append(figMatcher.group(2));

			if (" and ".equals(figMatcher.group(3))) {
				// FIGS. 1 and 2  BECOMES  FIG-1 and FIG-2 
				figStb.append("\">").append("FIG-").append(figMatcher.group(2)).append("</a>");
				figStb.append(figMatcher.group(3)).append("<a class=\"figref\" idref=\"FIG-").append(figMatcher.group(4));
				figStb.append("\">").append("FIG-").append(figMatcher.group(4)).append("</a>");
			} else if (figMatcher.group(3) != null) {
				// FIGS. 1 to 2  OR  FIGS. 1 - 2  BECOMES  FIG-1 - FIG-2 
				figStb.append(" - ").append("FIG-").append(figMatcher.group(4));
				figStb.append("\">").append(fullMatch).append("</a>");
			} else {
				figStb.append("\">").append("FIG-").append(figMatcher.group(2)).append("</a>");
			}

			String newStr = figStb.toString();

			stb.replace(figMatcher.start() + additionalChars, figMatcher.end() + additionalChars, newStr);
			additionalChars = additionalChars + (newStr.length() - fullMatch.length());
		}
		return stb.toString();
	}

	/**
	 * Transform Patent Figure and Patent Claims and their accompanied number to
	 * simply "Patent-Claim" or "Patent-Figure"
	 * 
	 * @param text
	 * @return
	 */
	protected String cleanText(String text) {
		text = text.replaceAll("\\\\n", "\n");
		text = CLAIM_REF.matcher(text).replaceAll("Patent-Claim");
		text = PATENT_FIG.matcher(text).replaceAll("Patent-Figure");
		return text;
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
