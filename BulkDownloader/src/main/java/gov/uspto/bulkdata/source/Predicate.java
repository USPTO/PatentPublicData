package gov.uspto.bulkdata.source;

import javax.xml.bind.annotation.XmlElement;

import gov.uspto.common.predicate.EqualPredicate;
import gov.uspto.common.predicate.PrefixPredicate;
import gov.uspto.common.predicate.RegexPredicate;
import gov.uspto.common.predicate.SuffixPredicate;

public class Predicate implements java.util.function.Predicate<String> {

    private java.util.function.Predicate<String> predicate;

    public void addPredicate(java.util.function.Predicate<String> predicate) {
        if (this.predicate == null) {
            this.predicate = predicate;
        } else {
            this.predicate = predicate.and(predicate);
        }
    }

    @XmlElement(name = "equal")
    public void setEqual(String equalValue) {
        if (!equalValue.isEmpty()) {
            addPredicate(new EqualPredicate(equalValue));
        }
    }

    @XmlElement(name = "suffix")
    public void setSuffix(String suffix) {
        if (!suffix.isEmpty()) {
            addPredicate(new SuffixPredicate(suffix));
        }
    }

    @XmlElement(name = "prefix")
    public void setPrefix(String prefix) {
        if (!prefix.isEmpty()) {
            addPredicate(new PrefixPredicate(prefix));
        }
    }

    @XmlElement(name = "pattern")
    public void setPattern(String pattern) {
        if (!pattern.isEmpty()) {
            addPredicate(new RegexPredicate(pattern));
        }
    }

    /*
    @XmlElement(name = "datePattern")
    public void setDatePattern(String datePattern, String dateRange) {
        new DateRange();
        fileFilerList.add(new RegexFileDateFilter(pattern));
    }
    */

    @Override
    public String toString() {
        return "Predicate [" + predicate + "]";
    }

    @Override
    public boolean test(String valueStr) {
        return predicate.test(valueStr);
    }
}
