package gov.uspto.common.text;

import java.util.Arrays;
import java.util.List;

import com.google.common.base.Joiner;

public class StringCaseUtil {

	private static List<String> lowerCaseWords = Arrays.asList(new String[] { "A", "An", "And", "As", "At", "But", "By",
			"For", "In", "Nor", "Of", "Off", "On", "Or", "Per", "So", "The", "To", "Up", "Via", "With", "Yet" });

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

	public static String toTitleCase(String text) {
		if (text == null) {
			return null;
		}

		String[] words = text.split("\\s");

		for (int i = 0; i < words.length; i++) {
			words[i] = capitalizeFirstLetter(words[i]);

			String[] hypenwords = words[i].split("-");
			if (hypenwords.length > 1) {
				hypenwords = capitalizeFirstLetter(hypenwords);
				words[i] = Joiner.on("-").join(hypenwords);
			}

			String[] slashWords = words[i].split("/");
			if (slashWords.length > 1) {
				slashWords = capitalizeFirstLetter(slashWords);
				words[i] = Joiner.on("/").join(slashWords);
			}

			if (i > 0 && lowerCaseWords.contains(words[i])) {
				words[i] = words[i].toLowerCase();
			}
		}

		return (Joiner.on(" ").join(words));
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
