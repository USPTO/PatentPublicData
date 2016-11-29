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

	private static final Pattern CLAIM_REF = Pattern.compile("\\bclaim ([0-9](?:(?: or |-)[0-9])?)\\b");
	private static final Pattern PATENT_FIG = Pattern
			.compile("\\bFIGS?\\. ([1-9][0-9]?[()A-z]*(?:(?: to | and |-)[1-9][0-9]?[()A-z]*)?)\\b");

	private static final String[] HTML_WHITELIST = new String[] { "p", "h2", "pre", "table", "tr", "td", "a", "li", "ul", "span" };
	private static final String[] HTML_WHITELIST_ATTRIB = new String[] { "class", "id", "num", "idref" };

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
		rawText = createRefs(rawText);
		Document jsoupDoc = Jsoup.parse(rawText, "", Parser.xmlParser());

		// rename header to "h2"
		jsoupDoc.select("PAC").tagName("h2");

		// Rename all "para" tags to "p".
		jsoupDoc.select("PAR").tagName("p");

		for (int j = 0; j < 4; j++) {
			jsoupDoc.select("PA" + j).tagName("p");
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

		String fieldTextCleaned = Jsoup.clean(textStr, "", whitelist, outSettings);

		return fieldTextCleaned;
	}

	public String createRefs(String rawText) {
		StringBuilder stb = new StringBuilder(rawText);
		Matcher clmMatcher = CLAIM_REF.matcher(rawText);
		int additionalChars = 0;
		while (clmMatcher.find()) {
			String fullMatch = clmMatcher.group(0);
			String newStr = "<a class=\"claim\">" + fullMatch + "</a>";
			stb.replace(clmMatcher.start() + additionalChars, clmMatcher.end() + additionalChars, newStr);
			additionalChars = additionalChars + (newStr.length() - fullMatch.length());
		}
		String htmlText = stb.toString();

		stb = new StringBuilder(htmlText);
		Matcher figMatcher = PATENT_FIG.matcher(htmlText);
		additionalChars = 0;
		while (figMatcher.find()) {
			String fullMatch = figMatcher.group(0);
			String newStr = "<a class=\"figref\">" + fullMatch + "</a>";
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
