package gov.uspto.common.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 *
 * Stop Words, many different ways.
 *
 * Match:
 * 	1) Identical Match (isStopWord)
 * 	2) Phrase Contains (contains)
 * 	3) Phrase Starts With (hasLeading)
 * 	4) Phrase Ends With (hasTrailing)
 * 	5) Phrase Edge, ends with or start with (hasEdge)
 * 
 * Remove:
 * 	1) Removal All (remove)
 * 	2) Remove Starts With (removeLeading)
 * 	3) Remove Ends With (removeTrailing)
 * 	4) Remove Edge, ends with or start with (removeEdge)
 *
 * @FIXME Phrases containing a stopword with accompanying punctuation will not match, since splitting on whitespace.
 *
 * {@code
 * 	stopword.has(string, StopWord.LOCATION.ANY);
 * 	stopword.remove(string, StopWord.LOCATION.ANY);
 * }
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class StopWord {

	public enum LOCATION {
		CONTAINS, // on remove: entire phrase is killed/removed if stopword is found.
		EQUAL, // on remove: entire phrase is removed if exact match. 
		ANY, // on remove: remove word from phrase.
		EDGE, // on remove: remove word from edge of phrase.
		TRAILING, // on remove: remove word from end of phrase.
		LEADING // on remove: remove word from beginning of phrase.
	};

	private final Path file;
	private Set<String> stopwords = new HashSet<String>();

	public StopWord(Path file) {
		Preconditions.checkArgument(Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS),
				"StopWord file does not exist: " + file);
		this.file = file;
	}

	/**
	 * Read stopword file.
	 * 
	 * @throws IOException
	 */
	public void load() throws IOException {

		BufferedReader reader = null;
		try {
			reader = Files.newBufferedReader(file, StandardCharsets.US_ASCII);

			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();

				String[] stripComments = line.split("#+", 2);

				if (line.startsWith("#")) {
					continue;
				} else if (stripComments.length > 0) {
					stopwords.add(stripComments[0].toLowerCase().trim());
				} else if (line.length() > 0) {
					stopwords.add(line.toLowerCase().trim());
				}
			}

		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	/**
	 * Is Stop Word, String
	 * 
	 * @param stop_word
	 * @return
	 */
	public boolean isStopWord(String token) {
		return stopwords.contains(token.toLowerCase());
	}

	/**
	 * Is Stop Word, String Array
	 * 
	 * @param stop_word
	 * @return
	 */
	public boolean isStopWord(String... token) {
		return stopwords.contains(Arrays.toString(token).toLowerCase());
	}

	/**
	 * Is Stop Word, String
	 * 
	 * @param stop_word
	 * @return
	 */
	public boolean isStopWord(List<String> token) {
		String tokenStr = Joiner.on(" ").join(token);
		return stopwords.contains(tokenStr.toLowerCase());
	}

	/**
	 * Has Stop Words from Phrase, String.
	 * 
	 * @param text
	 * @param location
	 * @return
	 */
	public boolean has(String text, LOCATION location) {
		switch (location) {
		case CONTAINS:
			return contains(text);
		case EQUAL:
			return isStopWord(text);
		case ANY:
			return contains(text);
		case EDGE:
			return hasEdge(text);
		case LEADING:
			return hasLeading(text);
		case TRAILING:
			return hasTrailing(text);
		}

		return contains(text);
	}

	/**
	 * Has Stop Words from Phrase, String.
	 * 
	 * @param text
	 * @param location
	 * @return
	 */
	public boolean has(String[] text, LOCATION location) {
		switch (location) {
		case CONTAINS:
			return contains(text);
		case EQUAL:
			return isStopWord(text);
		case ANY:
			return contains(text);
		case EDGE:
			return hasEdge(text);
		case LEADING:
			return hasLeading(text);
		case TRAILING:
			return hasTrailing(text);
		}

		return contains(text);
	}

	/**
	 * Has Stop Words from Phrase, String.
	 * 
	 * @param text
	 * @param location
	 * @return
	 */
	public boolean has(List<String> text, LOCATION location) {
		switch (location) {
		case CONTAINS:
			return contains(text);
		case EQUAL:
			return isStopWord(text);
		case ANY:
			return contains(text);
		case EDGE:
			return hasEdge(text);
		case LEADING:
			return hasLeading(text);
		case TRAILING:
			return hasTrailing(text);
		}

		return contains(text);
	}

	/**
	 * Starts with stop words.
	 * 
	 * @param text
	 * @return
	 */
	public boolean hasLeading(String text) {
		for (String word : text.split("\\s")) {
			if (stopwords.contains(word)) {
				return true;
			}
			return false;
		}

		return false;
	}

	/**
	 * Starts with stop words, String Array
	 * 
	 * @param text
	 * @return
	 */
	public boolean hasLeading(String... text) {
		for (String word : text) {
			if (stopwords.contains(word)) {
				return true;
			}
			return false;
		}

		return false;
	}

	/**
	 * Starts with stop words, Collection: List, Set
	 * 
	 * @param text
	 * @return
	 */
	public boolean hasLeading(Collection<String> text) {
		for (String word : text) {
			if (stopwords.contains(word)) {
				return true;
			}
			return false;
		}

		return false;
	}

	/**
	 * Trailing StopWord
	 * 
	 * @param text
	 * @return
	 */
	public boolean hasTrailing(String text) {
		List<String> textList = Arrays.asList(text.split("\\s"));
		Collections.reverse(textList);

		for (String word : textList) {
			if (stopwords.contains(word)) {
				return true;
			}
			return false;
		}

		return false;
	}

	/**
	 * Trailing StopWord, String Array
	 * 
	 * @param text
	 * @return
	 */
	public boolean hasTrailing(String... text) {
		for (int i = text.length - 1; i > 0; i--) {
			if (stopwords.contains(text[i])) {
				return true;
			}
			return false;
		}

		return false;
	}

	/**
	 * Trailing StopWord, List
	 * 
	 * @param text
	 * @return
	 */
	public boolean hasTrailing(List<String> text) {
		for (int i = text.size() - 1; i == 0; i--) {
			if (stopwords.contains(text.get(i))) {
				return true;
			}
			return false;
		}

		return false;
	}

	/**
	 * Edge StopWord, Leading or Trailing; String
	 * 
	 * @param text
	 * @return
	 */
	public boolean hasEdge(String text) {
		boolean leading = hasLeading(text);
		if (leading) {
			return leading;
		}

		return hasTrailing(text);
	}

	/**
	 * Edge StopWord, Leading or Trailing; List
	 * 
	 * @param text
	 * @return
	 */
	public boolean hasEdge(List<String> text) {
		boolean leading = hasLeading(text);
		if (leading) {
			return leading;
		}

		return hasTrailing(text);
	}

	/**
	 * Edge StopWord, Leading or Trailing; String Array
	 * 
	 * @param text
	 * @return
	 */
	public boolean hasEdge(String[] text) {
		boolean leading = hasLeading(text);
		if (leading) {
			return leading;
		}

		return hasTrailing(text);
	}

	/**
	 * Contains a Stop Word, text
	 * 
	 * @param text
	 * @return
	 */
	public boolean contains(String text) {
		for (String word : text.split("\\s")) {
			if (stopwords.contains(word)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Contains a Stop Word, String Array.
	 * 
	 * @param text
	 * @return
	 */
	public boolean contains(String... text) {
		for (String word : text) {
			if (stopwords.contains(word)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Contains a Stop Word, Collection: List, Set
	 * 
	 * @param text
	 * @return
	 */
	public boolean contains(Collection<String> text) {
		for (String word : text) {
			if (stopwords.contains(word)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Remove Stop Words from text.
	 * 
	 * @return
	 */
	public String remove(String text) {
		StringBuilder result = new StringBuilder(text.length());

		for (String word : text.split("\\s")) {
			if (!stopwords.contains(word)) {
				result.append(word).append(" ");
			}
		}

		return result.toString().trim();
	}

	/**
	 * Remove Stop Words from String Array text
	 * 
	 * @return
	 */
	public String[] remove(String... text) {
		List<String> result = new ArrayList<String>();
		for (String word : text) {
			if (!stopwords.contains(word)) {
				result.add(word);
			}
		}

		return result.toArray(new String[result.size()]);
	}

	/**
	 * Remove Stop Words from Collection: List or Set.
	 * 
	 * @return
	 */
	public List<String> remove(Collection<String> text) {
		List<String> result = new ArrayList<String>();
		for (String word : text) {
			if (!stopwords.contains(word)) {
				result.add(word);
			}
		}

		return result;
	}

	/**
	 * Remove Stop Words from Phrase, String.
	 * 
	 * @param text
	 * @param location
	 * @return
	 */
	public String remove(char[] text, LOCATION location) {
		return remove(String.valueOf(text), location);
	}

	/**
	 * Remove Stop Words from Phrase, String.
	 * 
	 * @param text
	 * @param location
	 * @return
	 */
	public String remove(String text, LOCATION location) {
		switch (location) {
		case CONTAINS:
			if (contains(text)) {
				return "";
			}
			return text;
		case ANY:
			return remove(text);
		case EDGE:
			return removeEdge(text);
		case LEADING:
			return removeLeading(text);
		case TRAILING:
			return removeTrailing(text);
		}

		return text;
	}

	/**
	 * Remove Stop Words from Phrase, String List.
	 * 
	 * @param text
	 * @param location
	 * @return
	 */
	public List<String> remove(List<String> text, LOCATION location) {
		switch (location) {
		case CONTAINS:
			if (contains(text)) {
				return Lists.newArrayList();
			}
			return text;
		case ANY:
			return remove(text);
		case EDGE:
			return removeEdge(text);
		case LEADING:
			return removeLeading(text);
		case TRAILING:
			return removeTrailing(text);
		}

		return text;
	}

	/**
	 * Remove Stop Words from Phrase, String Array.
	 * 
	 * @param text
	 * @param location
	 * @return
	 */
	public String[] remove(String[] text, LOCATION location) {
		switch (location) {
		case CONTAINS:
			if (contains(text)) {
				return new String[] {};
			}
			return text;
		case ANY:
			return remove(text);
		case EDGE:
			return removeEdge(text);
		case LEADING:
			return removeLeading(text);
		case TRAILING:
			return removeTrailing(text);
		}

		return text;
	}

	/**
	 * Remove Leading Stop Words from text.
	 * 
	 * @return
	 */
	public String removeLeading(String text) {
		StringBuilder result = new StringBuilder(text.length());

		boolean leading = true;
		for (String word : text.split("\\s")) {
			if (!leading || !stopwords.contains(word)) {
				result.append(word).append(" ");
			} else {
				leading = false;
			}
		}

		return result.toString().trim();
	}

	/**
	 * Remove Leading Stop Words from text.
	 * 
	 * @return
	 */
	public String[] removeLeading(String... text) {
		List<String> result = new ArrayList<String>();

		boolean leading = true;
		for (String word : text) {
			if (!leading || !stopwords.contains(word)) {
				result.add(word);
			} else {
				leading = false;
			}
		}

		return result.toArray(new String[result.size()]);
	}

	/**
	 * Remove Leading Stop Words from text.
	 * 
	 * @return
	 */
	public List<String> removeLeading(Collection<String> text) {
		List<String> result = new ArrayList<String>();

		boolean leading = true;
		for (String word : text) {
			if (!leading || !stopwords.contains(word)) {
				result.add(word);
			} else {
				leading = false;
			}
		}

		return result;
	}

	/**
	 * Remove Trailing Stop Words from text.
	 * 
	 * @return
	 */
	public String removeTrailing(String text) {
		List<String> textList = Arrays.asList(text.split("\\s"));
		List<String> result = removeTrailing(textList);
		return Joiner.on(" ").join(result);
	}

	/**
	 * Remove Trailing Stop Words from text.
	 * 
	 * @return
	 */
	public String[] removeTrailing(String... text) {
		List<String> textList = Arrays.asList(text);
		List<String> result = removeTrailing(textList);
		return result.toArray(new String[result.size()]);
	}

	/**
	 * Remove Trailing Stop Words, List.
	 * 
	 * @return
	 */
	public List<String> removeTrailing(List<String> text) {
		Collections.reverse(text);
		List<String> result = removeLeading(text);
		Collections.reverse(result);
		return result;
	}

	/**
	 * Remove Edge Stop Words from text.
	 * 
	 * @return
	 */
	public String removeEdge(String text) {
		List<String> textList = Arrays.asList(text.split("\\s"));
		List<String> result = removeEdge(textList);
		return Joiner.on(" ").join(result);
	}

	/**
	 * Remove Edge Stop Words, String Array.
	 * 
	 * @return
	 */
	public String[] removeEdge(String... text) {
		List<String> textList = Arrays.asList(text);
		List<String> result = removeEdge(textList);
		return result.toArray(new String[result.size()]);
	}

	/**
	 * Remove Edge Stop Words, List.
	 * 
	 * @return
	 */
	public List<String> removeEdge(List<String> text) {
		List<String> resultLeading = removeLeading(text);
		List<String> result = removeTrailing(resultLeading);
		return result;
	}

}
