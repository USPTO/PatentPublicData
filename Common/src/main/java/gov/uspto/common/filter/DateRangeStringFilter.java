package gov.uspto.common.filter;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.uspto.common.DateRange;

public class DateRangeStringFilter implements FileFilter, StringFilter {
    private DateRange dateRange;
    private List<Pattern> nameDateRegexs = new ArrayList<Pattern>();

    public DateRangeStringFilter(DateRange dateRange, String... regexes) {
        this.dateRange = dateRange;
        for (String regex : regexes) {
            this.nameDateRegexs.add(Pattern.compile(regex));
        }
    }

    @Override
    public boolean accept(String filename) {
        for (Pattern regex : nameDateRegexs) {
            Matcher matcher = regex.matcher(filename);
            if (matcher.matches()) {
                String fileDate = matcher.group(1);
                if (dateRange.between(fileDate)) {
                    return true;
                }
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
        return "NameDateRangeFileFilter [dateRange=" + dateRange + ", nameDateRegexs=" + nameDateRegexs + "]";
    }
}