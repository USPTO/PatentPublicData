package gov.uspto.common.filter;

import java.io.File;
import java.io.FileFilter;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.uspto.common.DateRange;

public class DateRangeFileFilter implements FileFilter {
    private final DateRange[] dateRanges;

    public DateRangeFileFilter(DateRange... dateRanges) {
        this.dateRanges = dateRanges;
    }
    
    @Override
    public boolean accept(File file) {
        LocalDate date = Instant.ofEpochMilli(file.lastModified()).atZone(ZoneId.systemDefault()).toLocalDate();
        for (DateRange dateRange : dateRanges) {
            if (dateRange.between(date)){
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "DateRangeFileFilter [dateRanges=" + Arrays.toString(dateRanges) + "]";
    }
}