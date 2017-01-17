package gov.uspto.patent.mathml;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MathML
 * 
 * serialize MathML as non-xml within XML, HTML, or its DOM 
 * by replacing "<" amd ">"; while not interfering with other
 * tags when deserializing.
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 * @deprecated use java.util.Base64
 *
 */
public class MathmlEscaper {

    private static final Pattern MATHML_TAGS = Pattern.compile("<(/?m[a-z0-9]{1,7})>");
    private static final Pattern MATHML_DETAGS = Pattern.compile("\\s*\\[\\[(/?m[a-z0-9]{1,7})\\]\\]\\s*");

    private MathmlEscaper(){
        // singleton util class.
    }

    public static String escape(String mathml) {
        Matcher matcher = MATHML_TAGS.matcher(mathml);
        StringBuilder stb = new StringBuilder(mathml);
        int additionalChars = 0;
        while (matcher.find()) {
            String fullMatch = matcher.group(0);
            String replace = "[[" + matcher.group(1) + "]]";
            stb.replace(matcher.start() + additionalChars, matcher.end() + additionalChars, replace);
            additionalChars = additionalChars + (replace.length() - fullMatch.length());
        }
        return stb.toString();
    }

    public static String unescape(String mathml) {
        Matcher matcher = MATHML_DETAGS.matcher(mathml);
        StringBuilder stb = new StringBuilder(mathml);
        int additionalChars = 0;
        while (matcher.find()) {
            String fullMatch = matcher.group(0);
            String replace = "<" + matcher.group(1) + ">";
            stb.replace(matcher.start() + additionalChars, matcher.end() + additionalChars, replace);
            additionalChars = additionalChars + (replace.length() - fullMatch.length());
        }
        mathml = stb.toString();

        return mathml;
    }
}
