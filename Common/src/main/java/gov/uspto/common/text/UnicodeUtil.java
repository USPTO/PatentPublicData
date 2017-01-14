package gov.uspto.common.text;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public class UnicodeUtil {

	public static final Character PARAGRAPH = '\u2029';

	/*
	private static final HashMap<String, Character> FRACTION_TO_UNICODE = new HashMap<String, Character>();
	static {
		FRACTION_TO_UNICODE.put("1/4", '\u00BC'); 
		FRACTION_TO_UNICODE.put("1/2", '\u00BD'); 
		FRACTION_TO_UNICODE.put("3/4", '\u00BE');
		FRACTION_TO_UNICODE.put("1/3", '\u2153');
		FRACTION_TO_UNICODE.put("2/3", '\u2154');
		FRACTION_TO_UNICODE.put("1/5", '\u2155');
		FRACTION_TO_UNICODE.put("2/5", '\u2156'); 
		FRACTION_TO_UNICODE.put("3/5", '\u2157'); 
		FRACTION_TO_UNICODE.put("4/5", '\u2158');
		FRACTION_TO_UNICODE.put("1/6", '\u2159');
		FRACTION_TO_UNICODE.put("5/6", '\u215A');
		FRACTION_TO_UNICODE.put("1/8", '\u215b'); 
		FRACTION_TO_UNICODE.put("3/8", '\u215c'); 
		FRACTION_TO_UNICODE.put("5/8", '\u215d'); 
		FRACTION_TO_UNICODE.put("7/8", '\u215e');
	}
	*/

	private static Map<Character, Character> SUBSCRIPT = new HashMap<Character, Character>();
	static {
		SUBSCRIPT.put('0', '\u2080');
		SUBSCRIPT.put('1', '\u2081');
		SUBSCRIPT.put('2', '\u2082');
		SUBSCRIPT.put('3', '\u2083');
		SUBSCRIPT.put('4', '\u2084');
		SUBSCRIPT.put('5', '\u2085');
		SUBSCRIPT.put('6', '\u2086');
		SUBSCRIPT.put('7', '\u2087');
		SUBSCRIPT.put('8', '\u2088');
		SUBSCRIPT.put('9', '\u2089');
		SUBSCRIPT.put('a', '\u2090');
		SUBSCRIPT.put('e', '\u2091');
		SUBSCRIPT.put('o', '\u2092');
		SUBSCRIPT.put('x', '\u2093');
		SUBSCRIPT.put('h', '\u2095');
		SUBSCRIPT.put('k', '\u2096');
		SUBSCRIPT.put('l', '\u2097');
		SUBSCRIPT.put('m', '\u2098');
		SUBSCRIPT.put('n', '\u2099');
		SUBSCRIPT.put('p', '\u209A');
		SUBSCRIPT.put('S', '\u209B');
		SUBSCRIPT.put('T', '\u209C');
		SUBSCRIPT.put('+', '\u208A');
		SUBSCRIPT.put('-', '\u208B');
		SUBSCRIPT.put('=', '\u208C');
		SUBSCRIPT.put('(', '\u208D');
		SUBSCRIPT.put(')', '\u208E');
		SUBSCRIPT.put(' ', ' ');
	};

	private static Map<Character, Character> SUPERSCRIPT = new HashMap<Character, Character>();
	static {
		SUPERSCRIPT.put('0', '\u2070');
		SUPERSCRIPT.put('1', '\u00B9');
		SUPERSCRIPT.put('2', '\u00B2');
		SUPERSCRIPT.put('3', '\u00B3');
		SUPERSCRIPT.put('4', '\u2074');
		SUPERSCRIPT.put('5', '\u2075');
		SUPERSCRIPT.put('6', '\u2076');
		SUPERSCRIPT.put('7', '\u2077');
		SUPERSCRIPT.put('8', '\u2078');
		SUPERSCRIPT.put('9', '\u2079');
		SUPERSCRIPT.put('+', '\u207A');
		SUPERSCRIPT.put('-', '\u207B');
		SUPERSCRIPT.put('=', '\u207C');
		SUPERSCRIPT.put('(', '\u207D');
		SUPERSCRIPT.put(')', '\u207E');
		SUPERSCRIPT.put('n', '\u207F');
		SUPERSCRIPT.put('a', '\u1d43');
		SUPERSCRIPT.put('b', '\u1d47');
		SUPERSCRIPT.put('c', '\u1d9c');
		SUPERSCRIPT.put('d', '\u1d48');
		SUPERSCRIPT.put('e', '\u1d49');
		SUPERSCRIPT.put('f', '\u1da0');
		SUPERSCRIPT.put('g', '\u1d4d');
		SUPERSCRIPT.put('i', '\u2071');
		SUPERSCRIPT.put('k', '\u1d4d');
		SUPERSCRIPT.put(' ', ' ');
	};

	public static String toSubscript(char[] charArray) throws ParseException {
		char[] outAr = new char[charArray.length];
		for (int k = 0; k < charArray.length; k++) {
			if (SUBSCRIPT.containsKey(charArray[k])){
				outAr[k] = SUBSCRIPT.get(charArray[k]);
			}
			else {
				throw new ParseException("No Unicode Superscript for Character " + charArray[k], k);
			}
		}
		return new String(outAr);
	}

	public static String toSubscript(String value) throws ParseException {
		return toSubscript(value.toCharArray());
	}

	public static String toSubscript(int number) throws ParseException {
		return toSubscript(String.valueOf(number).toCharArray());
	}

	public static String toSuperscript(char[] charArray) throws ParseException {
		char[] outAr = new char[charArray.length];
		for (int k = 0; k < charArray.length; k++) {
			if (SUPERSCRIPT.containsKey(charArray[k])){
				outAr[k] = SUPERSCRIPT.get(charArray[k]);
			}
			else {
				throw new ParseException("No Unicode Superscript for Character " + charArray[k], k);
			}
		}
		return new String(outAr);
	}

	public static String toSuperscript(String value) throws ParseException {
		return toSuperscript(value.toCharArray());
	}

	public static String toSuperscript(int number) throws ParseException {
		return toSuperscript(String.valueOf(number).toCharArray());
	}
}
