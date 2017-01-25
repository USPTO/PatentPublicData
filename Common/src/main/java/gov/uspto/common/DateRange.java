package gov.uspto.common;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;

/**
 * Date Range includes the Start, the End Date, and all dates between.
 */
public class DateRange {

    private final LocalDate startDate;
    private final LocalDate endDate;

    public DateRange(Date startDate, Date endDate) {
        this.startDate = Instant.ofEpochMilli(startDate.getTime()).atZone(ZoneId.systemDefault()).toLocalDate().minusDays(1);
        this.endDate = Instant.ofEpochMilli(endDate.getTime()).atZone(ZoneId.systemDefault()).toLocalDate().plusDays(1);
    }

    public DateRange(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate.minusDays(1);
        this.endDate = endDate.plusDays(1);
    }

    public boolean between(Date date) {
        LocalDate checkDate = null;
        if (date != null){
            checkDate = Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate().minusDays(1);
        }
        return between(checkDate);
    }
 
    public boolean between(LocalDate checkDate) {
        // return a.compareTo(d) * d.compareTo(b) > 0;
        return checkDate != null && startDate.isBefore(checkDate) && endDate.isAfter(checkDate);
    }

    public boolean between(CharSequence checkDateStr, DateTimeFormatter... dateFormaters)
            throws DateTimeParseException {
        LocalDate checkDate = DateRange.parseDate(checkDateStr, dateFormaters);
        return between(checkDate);
    }

    public boolean within(CharSequence checkDateStr) throws DateTimeParseException {
        //String[] dateFormats = new String[]{"yyMMdd", "yyyyMMdd", "MM-dd-yyyy", "MM/dd/yyyy", "M-d-yyyy", "M/d/yyyy", "yyyy-MM-dd", "yyyy/MM/dd", "dd-MM-yy"};

        /*
        DateTimeFormatter dateFormats = new DateTimeFormatterBuilder()
                .appendOptional(DateTimeFormatter.BASIC_ISO_DATE)                                                                 
                .appendOptional(DateTimeFormatter.ISO_LOCAL_DATE)
                .appendOptional(DateTimeFormatter.ofPattern("yyMMdd"))      
                .appendOptional(DateTimeFormatter.ofPattern("yyyy/MM/dd"))    
                .appendOptional(DateTimeFormatter.ofPattern("MM/dd/yyyy"))      
                .toFormatter();
                */

        DateTimeFormatter[] dateFormats = new DateTimeFormatter[] { DateTimeFormatter.BASIC_ISO_DATE, // '20111203'
                DateTimeFormatter.ISO_LOCAL_DATE, // '2011-12-03'
                DateTimeFormatter.ofPattern("yyMMdd"), // '111203'
                DateTimeFormatter.ofPattern("yyyy/MM/dd"), // '2011/12/03'
                DateTimeFormatter.ofPattern("MM/dd/yyyy") // '12/03/2011'
        };

        return between(checkDateStr, dateFormats);
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public List<Integer> getYearsBetween() {
        return ContiguousSet.create(Range.closed(startDate.getYear(), endDate.getYear()), DiscreteDomain.integers())
                .asList();
    }

    /**
     * Number of Days, Months, Years, ... Between
     * 
     * @param unit ChronoUnit.DAY, ChronoUnit.MONTH, ChronoUnit.YEAR
     * @return
     */
    public long getNumberBetween(ChronoUnit unit) {
        return unit.between(startDate, endDate);
    }

    public static DateRange parse(CharSequence startDateStr, CharSequence endDateStr,
            DateTimeFormatter... dateFormatters) throws DateTimeParseException {
        LocalDate startDate = DateRange.parseDate(startDateStr, dateFormatters);
        LocalDate endDate = DateRange.parseDate(endDateStr, dateFormatters);
        return new DateRange(startDate, endDate);
    }

    public static LocalDate parseDate(CharSequence text, DateTimeFormatter... dateFormatters)
            throws DateTimeParseException {
        for (DateTimeFormatter dateFormat : dateFormatters) {
            try {
                return LocalDate.parse(text, dateFormat);
            } catch (DateTimeParseException e) {
                // ignore.
            }
        }
        throw new DateTimeParseException("Failed to parse date: " + text, text, 0);
    }

    @Override
    public String toString() {
        return "DateRange [startDate=" + startDate + ", endDate=" + endDate + "]";
    }
}
