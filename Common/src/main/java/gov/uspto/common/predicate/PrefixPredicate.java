package gov.uspto.common.predicate;

import java.util.function.Predicate;

public class PrefixPredicate implements Predicate<String> {

    private String prefix;

    public PrefixPredicate(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public boolean test(String valueStr) {
        return valueStr.startsWith(prefix);
    }

    public String getPrefix() {
        return prefix;
    }

    @Override
    public String toString() {
        return "PrefixPredicate [prefix=" + prefix + "]";
    }
}
