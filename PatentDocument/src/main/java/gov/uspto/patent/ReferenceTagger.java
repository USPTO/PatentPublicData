package gov.uspto.patent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Strings;

/**
 * Tag Claim and Patent Figure References from Free Text
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class ReferenceTagger {
	private static final Pattern CLAIM_REF = Pattern.compile("\\bclaim (([0-9])(?:( or |-)([0-9]))?)\\b");
	private static final Pattern PATENT_FIG = Pattern
			.compile("\\b([Ff][Ii][Gg][Ss]?\\. )(?:([1-9][0-9]?[()A-z]*)(?:( to | and |-)([1-9][0-9]?[()A-z]*))?)\\b");

	public static String markRefs(final String rawText) {
		StringBuilder stb = new StringBuilder(rawText);
		Matcher clmMatcher = CLAIM_REF.matcher(rawText);
		int additionalChars = 0;
		while (clmMatcher.find()) {
			String fullMatch = clmMatcher.group(0);

			StringBuilder claimStb = new StringBuilder();
			claimStb.append("<a class=\"claim\" idref=\"CLM-").append(Strings.padStart(clmMatcher.group(2), 4, '0'));

			if (" or ".equals(clmMatcher.group(3))) {
				// claim 1 or 2  BECOMES  CLM-1 or CLM-2 
				claimStb.append("\">").append("claim ").append(clmMatcher.group(2)).append("</a>");
				claimStb.append(clmMatcher.group(3)).append("<a class=\"claim\" idref=\"CLM-").append(Strings.padStart(clmMatcher.group(4), 4, '0'));
				claimStb.append("\">").append("claim ").append(clmMatcher.group(4)).append("</a>");
			} else if (clmMatcher.group(3) != null) {
				// claim 1 to 2  OR  claim 1 - 2  BECOMES  CLM-1 - CLM-2 
				claimStb.append(" - ").append("CLM-").append(Strings.padStart(clmMatcher.group(4), 4, '0'));
				claimStb.append("\">").append(fullMatch).append("</a>");
			} else {
				claimStb.append("\">").append("claim ").append(clmMatcher.group(2)).append("</a>");
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
	 * Generate Claim ID from Text
	 * 
	 * claim 1  ==>  CLM-0001
	 * 
	 * @param text
	 * @return
	 */
	public static String createClaimId(final String text){
		Matcher clmMatcher = CLAIM_REF.matcher(text);
		StringBuilder stb = new StringBuilder();
		if (clmMatcher.matches()){
			stb.append("CLM-");
			stb.append(Strings.padStart(clmMatcher.group(2), 4, '0'));
		}
		return stb.toString();
	}

	/**
	 * Generate Fig ID from Text
	 * 
	 * Fig. 1  ==>  FIG-1
	 * 
	 * @param text
	 * @return
	 */
	public static String createFigId(final String text){
		Matcher figMatcher = PATENT_FIG.matcher(text);
		StringBuilder stb = new StringBuilder();
		if (figMatcher.matches()){
			stb.append("FIG-");
			stb.append(figMatcher.group(2));
			if (figMatcher.group(3) != null){
				stb.append(",").append("FIG-").append(figMatcher.group(4));
			}
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
	public static String normRefs(final String text) {
		String updated = CLAIM_REF.matcher(text).replaceAll("Patent-Claim");
		updated = PATENT_FIG.matcher(updated).replaceAll("Patent-Figure");
		return updated;
	}
}
