package gov.uspto.common.predicate;

import java.util.function.Predicate;

public class SuffixPredicate implements Predicate<String> {

    private String suffix;

    public SuffixPredicate(String suffix) {
        this.suffix = suffix;
    }

    @Override
    public boolean test(String valueStr) {
        return valueStr.endsWith(suffix);
    }

    public String getPrefix() {
        return suffix;
    }

    @Override
    public String toString() {
        return "SuffixPredicate [suffix=" + suffix + "]";
    }
}
