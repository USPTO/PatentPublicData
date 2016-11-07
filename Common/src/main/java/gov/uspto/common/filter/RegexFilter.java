package gov.uspto.common.filter;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class RegexFilter implements FileFilter, StringFilter {
    private List<Pattern> regexes = new ArrayList<Pattern>();

    public RegexFilter(String... regexes) {
        for (String regex : regexes) {
            this.regexes.add(Pattern.compile(regex));
        }
    }

    @Override
    public boolean accept(String valueStr) {
        for (Pattern regex : regexes) {
            if (regex.matcher(valueStr).matches()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean accept(File file) {
        return accept(file.getName());
    }

    @Override
    public String toString() {
        return "RegexFilter [regexs=" + Arrays.toString(regexes.toArray()) + "]";
    }
}