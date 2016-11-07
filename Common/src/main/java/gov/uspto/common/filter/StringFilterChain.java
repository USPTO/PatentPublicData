package gov.uspto.common.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Criteria for Matching files; matches if all provided rules return true.
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class StringFilterChain implements StringFilter {

    public List<StringFilter> filters = new ArrayList<StringFilter>();

    public void addRule(StringFilter... rules) {
        for (StringFilter rule : rules) {
            filters.add(rule);
        }
    }

    @Override
    public boolean accept(String filename) {
        for (StringFilter rule : filters) {
            if (!rule.accept(filename)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "StringFilter [filters=" + Arrays.toString(filters.toArray()) + "]";
    }
}
