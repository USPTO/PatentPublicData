package gov.uspto.common.text;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Methods to process words or sequence of words.
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class WordUtil {

    private static List<String> LOWERCASE_WORDS = Arrays.asList(new String[] { "a", "an", "and", "as", "at", "but",
            "by", "for", "in", "nor", "of", "off", "on", "or", "per", "so", "the", "to", "up", "via", "with", "yet" });

    /**
     * Check for ONLY letters and numbers in Word or Sequence of Words
     * 
     * @param str
     * @return
     */
    public static boolean isAlphaNumberic(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isAlphabetic(str.charAt(i)) && !Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check for ONLY Uppercase letters and numbers in Word or Sequence of Words
     * 
     * @param str
     * @return
     */
    public static boolean isUpperAlphaNumberic(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isUpperCase(str.charAt(i)) && !Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check for ONLY letters in Word or Sequence of Words
     * 
     * @param str
     * @return
     */
    public static boolean isAlphabetic(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isAlphabetic(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check for defined Characters in Word or Sequence of words
     * 
     * @param str
     * @param matchChars
     * @return
     */
    public static boolean hasCharacter(String str, String matchChars) {
        for (int i = 0; i < str.length(); i++) {
            final char ch = str.charAt(i);
            final int index = matchChars.indexOf(ch);
            if (index != -1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check for defined Characters at start of Word or Sequence of words
     * 
     * @param str
     * @param matchChars
     * @return
     */
    public static boolean startsWithCharacter(String str, String matchChars) {
        for (int i = 0; i < str.length(); i++) {
            final char ch = str.charAt(i);
            final int index = matchChars.indexOf(ch);
            if (index != -1) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    /**
     * Check for Number in Word or Sequence of Words
     * 
     * @param str
     * @return
     */
    public static boolean hasNumber(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (Character.isAlphabetic(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check for Letters in Word or Sequence of Words
     * 
     * @param str
     * @return
     */
    public static boolean hasLetter(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (Character.isLetter(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check for Capital Letters in Word or Sequence of Words
     * 
     * @param str
     * @return
     */
    public static boolean hasCapital(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (Character.isUpperCase(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get Capital Letters in Word or Sequence of Words
     * 
     * @param seq
     * @return
     */
    public static String getCapital(String seq) {
        StringBuilder stb = new StringBuilder();
        for (int i = 0; i < seq.length(); i++) {
            char ch = seq.charAt(i);
            if (Character.isUpperCase(ch)) {
                stb.append(ch);
            }
        }
        return stb.toString();
    }

    /**
     * Capitalize first letter of each word.
     * 
     * @param str
     * @param wordDelimiters
     * @return
     */
    public static String capitalize(String str, char[] wordDelimiters) {
        int strLen = str.length();
        StringBuffer buffer = new StringBuffer(strLen);
        boolean capitalizeNext = true;
        for (int i = 0; i < strLen; i++) {
            char ch = str.charAt(i);

            boolean isDelimiter = false;
            for (int j = 0, isize = wordDelimiters.length; j < isize; j++) {
                if (ch == wordDelimiters[j]) {
                    isDelimiter = true;
                }
            }

            if (isDelimiter) {
                buffer.append(ch);
                capitalizeNext = true;
            } else if (capitalizeNext) {
                buffer.append(Character.toTitleCase(ch));
                capitalizeNext = false;
            } else {
                buffer.append(ch);
            }
        }
        return buffer.toString();
    }

    public static String capitalizeTitle(String str) {
        return capitalizeTitle(str, new char[] { ' ' });
    }

    /**
     * Capitalize first letter of each word, using rules for titles
     * 
     * lowercase of select insignificant words "the, an, by, for, in, ..."
     * 
     * @param str
     * @param wordDelimiters
     * @return
     */
    public static String capitalizeTitle(String str, char[] wordDelimiters) {
        int strLen = str.length();
        StringBuilder stb = new StringBuilder(strLen);
        StringBuilder word = new StringBuilder();
        boolean capitalizeNext = true;
        for (int i = 0; i < strLen; i++) {
            char ch = str.charAt(i);

            boolean isDelimiter = false;
            for (int j = 0, isize = wordDelimiters.length; j < isize; j++) {
                if (ch == wordDelimiters[j]) {
                    isDelimiter = true;
                }
            }

            if (isDelimiter) {
                stb.append(ch);

                if (LOWERCASE_WORDS.contains(word.toString().toLowerCase())) {
                    stb = stb.replace((stb.length() - word.length()) - 1, stb.length() - word.length(),
                            Character.toString(Character.toLowerCase(word.charAt(0))));
                }

                capitalizeNext = true;
            } else if (capitalizeNext) {
                stb.append(Character.toTitleCase(ch));
                capitalizeNext = false;
                word = new StringBuilder();
                word.append(ch);
            } else {
                stb.append(Character.toLowerCase(ch));
                word.append(ch);
            }
        }
        return stb.toString();
    }

    /**
     * Check if Word Sequence contains a word.
     * 
     * @param segment
     * @param stopwords
     * @return
     */
    public static boolean hasWord(String segment, Collection<String> stopwords) {
        return hasWord(segment, " \t\n\r\f-", stopwords);
    }

    /**
     * Check if Word Sequence contains a word.
     * 
     * @param segment
     * @param wordDelimiters
     * @param stopwords
     * @return
     */
    public static boolean hasWord(String segment, String wordDelimiters, Collection<String> stopwords) {
        StringTokenizer tokenizer = new StringTokenizer(segment, wordDelimiters);
        return hasWord(tokenizer, stopwords);
    }

    /**
     * Check if Word Sequence contains a word.
     * 
     * @param tokenizer
     * @param stopwords
     * @return
     */
    public static boolean hasWord(StringTokenizer tokenizer, Collection<String> stopwords) {
        while (tokenizer.hasMoreElements()) {
            String token = tokenizer.nextToken();
            if (stopwords.contains(token.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if Word Sequence starts with a word.
     * 
     * @param segment
     * @param stopwords
     * @return
     */
    public static boolean hasLeadWord(String segment, Collection<String> stopwords, boolean caseSensative) {
        return hasLeadWord(segment, " \t\n\r\f-", stopwords, caseSensative);
    }

    /**
     * Check if Word Sequence starts with a word.
     * 
     * @param segment
     * @param wordDelimiters
     * @param stopwords
     * @return
     */
    public static boolean hasLeadWord(String segment, String wordDelimiters, Collection<String> stopwords,
            boolean caseSensative) {
        StringTokenizer tokenizer = new StringTokenizer(segment, wordDelimiters);
        return hasLeadWord(tokenizer, stopwords, caseSensative);
    }

    /**
     * Check if Word Sequence starts with a word.
     * 
     * @param tokenizer
     * @param stopwords
     * @return
     */
    public static boolean hasLeadWord(StringTokenizer tokenizer, Collection<String> stopwords, boolean caseSensative) {
        if (tokenizer.hasMoreElements()) {
            String token = tokenizer.nextToken();
            if (caseSensative) {
                if (stopwords.contains(token.toLowerCase())) {
                    return true;
                }
            } else {
                if (stopwords.contains(token)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Count Words
     * 
     * @param segment
     * @param wordDelimiters
     * @return
     */
    public static int countWords(String segment, char[] wordDelimiters) {
        int wordCount = 0;
        boolean lastWasGap = true;

        for (int i = 0; i < segment.length(); i++) {
            char ch = segment.charAt(i);

            boolean isDelimiter = false;
            for (int j = 0, isize = wordDelimiters.length; j < isize; j++) {
                if (ch == wordDelimiters[j]) {
                    isDelimiter = true;
                    lastWasGap = true;
                }
            }

            if (!isDelimiter && lastWasGap) {
                lastWasGap = false;
                wordCount++;
            }
        }
        return wordCount;
    }

    /**
     * Initials, letter which starts each word.
     * 
     * @param segment
     * @return
     */
    public static String initials(String segment) {
        return initials(segment, new char[] { ' ' });
    }

    /**
     * Initials, letter which starts each word.
     * 
     * @param segment
     * @param wordDelimiters
     * @return
     */
    public static String initials(String segment, char[] wordDelimiters) {
        boolean lastWasGap = true;
        StringBuilder wordStb = new StringBuilder();
        StringBuilder initialStb = new StringBuilder();
        for (int i = 0; i < segment.length(); i++) {
            char ch = segment.charAt(i);

            boolean isDelimiter = false;
            for (int j = 0, isize = wordDelimiters.length; j < isize; j++) {
                if (ch == wordDelimiters[j]) {
                    isDelimiter = true;
                    lastWasGap = true;
                }
            }

            if (!isDelimiter) {
                if (lastWasGap) {
                    wordStb = new StringBuilder();
                    wordStb.append(ch);
                    lastWasGap = false;
                    initialStb.append(ch);
                } else {
                    wordStb.append(ch);
                }
            } else {
                if (LOWERCASE_WORDS.contains(wordStb.toString().toLowerCase())) {
                    initialStb = initialStb.deleteCharAt(initialStb.length() - 1);
                }
            }
        }
        return initialStb.toString();
    }

    /**
     * Longest Common Character sequence between words, or sequence of words.
     * 
     * @param S1
     * @param S2
     * @return
     */
    public static String longestCommonSubstring(String word1, String word2) {
        int start = 0;
        int max = 0;
        for (int i = 0; i < word1.length(); i++) {
            for (int j = 0; j < word2.length(); j++) {
                int x = 0;
                while (word1.charAt(i + x) == word2.charAt(j + x)) {
                    x++;
                    if (((i + x) >= word1.length()) || ((j + x) >= word2.length())) {
                        break;
                    }
                }
                if (x > max) {
                    max = x;
                    start = i;
                }
            }
        }
        return word1.substring(start, (start + max));
    }
}
