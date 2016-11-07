package gov.uspto.common.predicate;

import java.io.File;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.function.Predicate;

import gov.uspto.common.DateRange;

public class FileDateRangePredicate implements Predicate<File> {

    private DateRange dateRange;

    public FileDateRangePredicate(DateRange dateRange) {
        this.dateRange = dateRange;
    }

    @Override
    public boolean test(File file) {
        LocalDate date = Instant.ofEpochMilli(file.lastModified()).atZone(ZoneId.systemDefault()).toLocalDate();
        return dateRange.between(date);
    }
}
