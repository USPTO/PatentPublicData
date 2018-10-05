package gov.uspto.common.text;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Joiner;

public class StringCaseUtil {

	private static Set<String> lowerCaseWords = new HashSet<String>();
	private static Set<String> prefixes = new HashSet<String>();
	private static Set<String> lowerTrailHyphen = new HashSet<String>();
	private static Set<String> prepositions = new HashSet<String>();

	static {
		prepositions.addAll(Arrays.asList(new String[] { "As", "At", "But", "By", "For", "From", "In", "Into", "Of", "Off", "On", "Onto", "To", "Up",
				"Via", "With", "Within", "Without", "Like" }));

		lowerCaseWords.addAll(Arrays.asList(new String[] { "The", "A", "An", "And", "Nor", "Or", "Per", "So", "Yet", "Into" }));
		lowerCaseWords.addAll(prepositions);

		prefixes.addAll(Arrays.asList(new String[] {"Re", "Pre", "Post", "Anti", "Fore", "De", "Non", "Dis", "En", "Em", "In", "Im", "Il", "Ir", "Inter",
				"Mid", "Mis", "Over", "Sub", "Super", "Supra", "Trans", "Un", "Under", "Co", "Pro", "Proto", "Para", "Meta", "Ex", "Semi", "Ultra", "Neo",
				"Mini",	"Micro", "Mega", "Macro", "Intra", "Infra", "Hypo", "Hyper", "Bi", "Tri", "Di", "Multi", "Auto", "Bio", "Geo", "Hydro", "Extra",
				"Pseudo", "Socio", "Electro", "Single", "Dual", "Triple", "Quad"}));

		lowerTrailHyphen.addAll(Arrays.asList(new String[] { "Like", "Type", "Driven" }));
	}

	public static String join(String delimiter, String... elements) {
		// Similar to Java 8's : "String.join(delimiter, elements);"
		return Joiner.on(delimiter).skipNulls().join(elements);
	}

	public static String toNameCase(String text) {
		String[] words = text.split(" ");
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < words.length; i++) {
			sb.append(words[i].substring(0, 1).toUpperCase() + words[i].substring(1).toLowerCase());
		}

		return sb.toString();
	}


	/**
	 * Title Case
	 *
	 *<pre>
	 * Patent Titles originate in one of three forms: all upper-case, sentence case or proper title case; 
	 * and occasionally a double space after punctuation.
	 * 
	 * Best effort is performed to normalized to Title Case.
	 * 
	 * The Style Rules loosely follow the Chicago Simple and Wikipedia Style Manuals; with a preference
	 * to lowercase words when possible but only if it can be done with a light weight manner 
	 * (without a NLP parser) and receives a decent level of accuracy.
	 *
	 * 1) Maintain Uppercase if detect the use of lowercase and word is not in lowerCaseWords (articles, ...)
	 * 2) Quotes or brackets: maintain case of words within
	 * 3) First and Last Word: capitalize
	 * 4) articles (the,a,an): lowercase
	 * 5) coordinating conjunctions (and, but, for, or, nor): lowercase
	 * 6) short prepositions : lowercase
	 * 7) common prepositions: lowercase
	 * 8) Hyphens split into individual words and use rules above.
	 * 9) Hyphenated lead prefix: treat prefix and word as if a single word; capitalize prefix and lowercase trailing word.
	 * 10) Hyphenated trailing word with verb tense suffixes ['ing','ed']: capitalize first word then lowercase trailing word.
	 * 11) Slashes: treat each word as a noun or verb, capitalize each word
	 * 12) Multiple Spaces: compress to a single space
	 *</pre>
	 *
	 * @param text
	 * @return
	 */
	public static String toTitleCase(String text) {
		if (text == null) {
			return null;
		}

		System.out.println("Title Text: " + text);
		
		/*
		 * If text is not all capitals then maintain capitals
		 */
		boolean maintainCapitals = false;
		if (text.matches(".*[a-z]{3,}.*")) { // sequence of 3 at least lowercase chars
			maintainCapitals = true;
		}

		String[] words = text.split("\\s+");

		boolean inBlock = false;
		char wantCloseBlockChar = '?';
		for (int i = 0; i < words.length; i++) {
			
			/*
			 * Block Quoted or Bracketed; maintain original lettercase.
			 */
			char firstChar = words[i].charAt(0);
			char lastChar = words[i].charAt(words[i].length()-1);
			if (('‘' == firstChar && '’' == lastChar) || '(' == firstChar && ')' == lastChar || (')' == lastChar && !inBlock)) {
				if ('‘' == firstChar) {
					words[i] = "'" + words[i].substring(1, words[i].length()-1) + "'";
				}
				continue;
			}
			else if ('‘' == firstChar || '(' == firstChar || words[i].indexOf('(') != -1) {
				if ('‘' == firstChar) {
					words[i] = "'" + words[i].substring(1);
					wantCloseBlockChar = '’';
				} else {
					wantCloseBlockChar = ')';
				}
				inBlock = true;
				continue;
			}
			else if (wantCloseBlockChar == lastChar) {
				if ('’' == lastChar) {
					words[i] = words[i].substring(0, words[i].length()-1) + "'";
				}
				inBlock = false;
				continue;
			}
			else if (inBlock || maintainCapitals && Character.isUpperCase(firstChar) && !lowerCaseWords.contains(words[i])) {
				words[i] = normHyphenWord(words[i]);
				words[i] = normSlashWord(words[i]);
				continue;
			}

			words[i] = capitalizeFirstLetter(words[i]);
			words[i] = normHyphenWord(words[i]);
			words[i] = normSlashWord(words[i]);

			/*
			 * Lowercase Always Words
			 */
			if ((i > 0 || i == words.length-1) && lowerCaseWords.contains(words[i])) {
				words[i] = words[i].toLowerCase();
			}
		}

		return (Joiner.on(" ").join(words));
	}

	
	
	/**
	 * Fix Hyphen Word
	 * 
	 * @param word
	 * @return
	 */
	private static String normHyphenWord(String word) {
		String ret = word;
		String[] hypenwords = word.split("[-\\u2013\\u2014]");
		if (hypenwords.length > 1) {
			for (int j = 0; j < hypenwords.length; j++) {
				hypenwords[j] = capitalizeFirstLetter(hypenwords[j]);
				if (j != 0 && lowerCaseWords.contains(hypenwords[j])) {
					hypenwords[j] = hypenwords[j].toLowerCase();
				}
			}

			if (hypenwords[0].length() == 1 || 
					prefixes.contains(hypenwords[0]) || 
					hypenwords[1].endsWith("ing") || 
					hypenwords[1].endsWith("ed") ||
					// maybe also "en"
					lowerTrailHyphen.contains(hypenwords[1])) {
				
						hypenwords[1] = hypenwords[1].toLowerCase();
			}

			ret = Joiner.on("-").join(hypenwords);
		}
		return ret;
	}

	private static String normSlashWord(String word) {
		String ret = word;
		String[] slashWords = word.split("/");
		if (slashWords.length > 1) {
			slashWords = capitalizeFirstLetter(slashWords);
			ret = Joiner.on("/").join(slashWords);
		}
		return ret;
	}

	public static String[] capitalizeFirstLetter(String[] strArr) {
		for (int j = 0; j < strArr.length; j++) {
			strArr[j] = capitalizeFirstLetter(strArr[j]);
		}
		return strArr;
	}

	public static String capitalizeFirstLetter(String word) {
		if (word.length() >= 2) {
			word = word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
		} else {
			word = word.toUpperCase();
		}
		return word;
	}
}
