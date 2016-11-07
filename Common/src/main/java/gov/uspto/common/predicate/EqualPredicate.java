package gov.uspto.common.predicate;

import java.util.function.Predicate;

public class EqualPredicate implements Predicate<String> {

    private String equalTo;

    public EqualPredicate(String equalTo) {
        this.equalTo = equalTo;
    }

    @Override
    public boolean test(String valueStr) {
        return equalTo.equals(valueStr);
    }

    public String getEqualTo() {
        return equalTo;
    }

    @Override
    public String toString() {
        return "EqualPredicate [equalTo=" + equalTo + "]";
    }
}
