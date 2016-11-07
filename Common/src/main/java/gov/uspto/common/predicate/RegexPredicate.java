package gov.uspto.common.predicate;

import java.util.function.Predicate;
import java.util.regex.Pattern;

public class RegexPredicate implements Predicate<String> {
    private Pattern regexPattern;

    public RegexPredicate(String regex) {
        this.regexPattern = Pattern.compile(regex);
    }

    @Override
    public boolean test(String valueStr) {
        return regexPattern.matcher(valueStr).matches();
    }

    public Pattern getRegexPattern() {
        return regexPattern;
    }

    @Override
    public String toString() {
        return "RegexPredicate [regexPattern=" + regexPattern + "]";
    }
}
