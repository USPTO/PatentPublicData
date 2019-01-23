package gov.uspto.common.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 *
 * Stop Words, many different ways.
 *
 * <p>
 * Match: <br/>
 * 1) Identical Match (isStopWord) <br/>
 * 2) Phrase Contains (contains) <br/>
 * 3) Phrase Starts With (hasLeading) <br/>
 * 4) Phrase Ends With (hasTrailing) <br/>
 * 5) Phrase Edge, ends with or start with (hasEdge) <br/>
 * </p>
 *
 * <p>
 * Options: <br/>
 * Ignore Sentence Punctuation (default) [period, question-mark, comma, colon,
 * semicolon] <br/>
 * Case Sensitive <br/>
 * </p>
 *
 * <p>
 * Remove: <br/>
 * 1) Removal All (remove) <br/>
 * 2) Remove Starts With (removeLeading) <br/>
 * 3) Remove Ends With (removeTrailing) <br/>
 * 4) Remove Edge, ends with or start with (removeEdge) <br/>
 * </p>
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

	private static Pattern punctuationRegex = Pattern.compile("(?<![\\d])([,;:\\.\\?])(?![\\d])");

	private Matcher punctuationMatcher = punctuationRegex.matcher("");

	private final Path file;
	private final Charset charset;
	private boolean caseSensitive = false;
	private boolean ignorePunctuation = true;
	private Set<String> stopwords = new HashSet<String>();

	public StopWord(Path file) {
		this(file, Charset.defaultCharset());
	}

	public StopWord(Path file, Charset charset) {
		Preconditions.checkArgument(Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS),
				"StopWord file does not exist: " + file);
		this.file = file;
		this.charset = charset;
	}

	/**
	 * Case Insensitive Match
	 * 
	 * <p>
	 * After calling this function, load or reload the stopword file by calling
	 * load().
	 * </p>
	 * 
	 * @param bool
	 */
	public void caseSensitive(final boolean bool) {
		this.caseSensitive = bool;
	}

	/**
	 * Ignore Sentence Punctuation [period, question-mark, comma, semi-colon]
	 * 
	 * <p>
	 * After calling this function, load or reload the stopword file by calling
	 * load().
	 * </p>
	 *
	 * @param bool
	 */
	public void ignorePunctuation(final boolean bool) {
		this.ignorePunctuation = bool;
	}

	/**
	 * Reads stopword file
	 *
	 * <p>
	 * Skipping comment lines, leading #, and Strips Trailing Comments, after #.
	 * </p>
	 *
	 * @throws IOException
	 */
	public void load() throws IOException {
		stopwords = new HashSet<String>();

		BufferedReader reader = null;
		try {
			reader = Files.newBufferedReader(file, charset);

			String line;
			while ((line = reader.readLine()) != null) {
				// Skip Comment Lines
				if (line.startsWith("#")) {
					continue;
				}

				line = line.trim();

				// Strip Trailing Comments
				String[] stripTrailingComments = line.split("#+", 2);
				if (stripTrailingComments.length > 0) {
					line = stripTrailingComments[0].trim();
				}

				if (line.length() < 1) {
					continue;
				}

				line = normalize(line);

				stopwords.add(line);
			}

		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	protected String normalize(final String token) {
		String ret = token;
		if (!caseSensitive) {
			ret = token.toLowerCase();
		}

		if (ignorePunctuation) {
			// Remove sentence punctuation, support Unicode and maintain punctuation within
			// numbers.
			// Matcher m = punctuationRegex.matcher(ret);
			punctuationMatcher.reset(ret);
			ret = punctuationMatcher.replaceAll("");
		}

		return ret;
	}

	/**
	 * Is Stop Word, String
	 * 
	 * @param stop_word
	 * @return
	 */
	public boolean isStopWord(final String token) {
		String check = normalize(token);
		return stopwords.contains(check);
	}

	/**
	 * Is Stop Word, String Array
	 * 
	 * @param stop_word
	 * @return
	 */
	public boolean isStopWord(final String... token) {
		String check = normalize(Arrays.toString(token));
		return stopwords.contains(check);
	}

	/**
	 * Is Stop Word, String
	 * 
	 * @param stop_word
	 * @return
	 */
	public boolean isStopWord(final List<String> token) {
		String tokenStr = Joiner.on(" ").join(token);
		tokenStr = normalize(tokenStr);
		return stopwords.contains(tokenStr);
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
	public boolean hasLeading(final String text) {
		for (String word : text.split("\\s")) {
			word = normalize(word);
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
	public boolean hasLeading(final String... text) {
		for (String word : text) {
			word = normalize(word);
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
	public boolean hasLeading(final Collection<String> text) {
		for (String word : text) {
			word = normalize(word);
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
	public boolean hasTrailing(final String text) {
		List<String> textList = Arrays.asList(text.split("\\s"));
		Collections.reverse(textList);

		for (String word : textList) {
			word = normalize(word);
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
	public boolean hasTrailing(final String... text) {
		for (int i = text.length - 1; i > 0; i--) {
			String word = normalize(text[i]);
			if (stopwords.contains(word)) {
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
	public boolean hasTrailing(final List<String> text) {
		for (int i = text.size() - 1; i == 0; i--) {
			String word = normalize(text.get(i));
			if (stopwords.contains(word)) {
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
	public boolean hasEdge(final String text) {
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
	public boolean contains(final String text) {
		for (String word : text.split("\\s")) {
			word = normalize(word);
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
	public boolean contains(final String... text) {
		for (String word : text) {
			word = normalize(word);
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
	public boolean contains(final Collection<String> text) {
		for (String word : text) {
			word = normalize(word);
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
	public String remove(final String text) {
		StringBuilder result = new StringBuilder(text.length());

		for (String word : text.split("\\s")) {
			String checkWord = normalize(word);
			if (!stopwords.contains(checkWord)) {
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
	public String[] remove(final String... text) {
		List<String> list = remove(Arrays.asList(text));
		return list.toArray(new String[list.size()]);
	}

	/**
	 * Remove Stop Words from Iterable<String> List or Set.
	 * 
	 * @return
	 */
	public List<String> remove(final Iterable<String> text) {
		List<String> result = new ArrayList<String>();
		for (String word : text) {
			String checkWord = normalize(word);
			if (!stopwords.contains(checkWord)) {
				result.add(word);
			}
		}

		return result;
	}

	/**
	 * Remove Stop Words from Collection: List or Set.
	 * 
	 * @return
	 */
	/*
	 * public List<String> remove(final Collection<String> text) { List<String>
	 * result = new ArrayList<String>(); for (String word : text) { String checkWord
	 * = normalize(word); if (!stopwords.contains(checkWord)) { result.add(word); }
	 * }
	 * 
	 * return result; }
	 */

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
		default:
			return text;
		}
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
		default:
			return text;
		}
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
		default:
			return text;
		}
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
			String checkWord = normalize(word);
			if (!leading || !stopwords.contains(checkWord)) {
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
			String checkWord = normalize(word);
			if (!leading || !stopwords.contains(checkWord)) {
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
			String checkWord = normalize(word);
			if (!leading || !stopwords.contains(checkWord)) {
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
