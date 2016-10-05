package gov.uspto.patent.model;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.lang3.time.DateParser;
import org.apache.commons.lang3.time.FastDateFormat;

import gov.uspto.patent.DateTextType;
import gov.uspto.patent.InvalidDataException;

public class DocumentDate {
    /*
     * FastDateFormat is Thread-Safe version of SimpleDateFormat
     */
    private static final FastDateFormat DATE_ISO_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private static final DateParser DATE_YEAR_FORMAT = FastDateFormat.getInstance("yyyy");
    private static final DateParser DATE_PATENT_FORMAT = FastDateFormat.getInstance("yyyyMMdd");

    private Date date;
    private String rawDate;

    public DocumentDate(String date) throws InvalidDataException {
        this.rawDate = date;
        setDate(date);
    }

    public DocumentDate(Date date) throws InvalidDataException {
        this.date = date;
    }

    public int getYear() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR);
    }

    public void setDate(String date) throws InvalidDataException {
        if (date != null && date.trim().length() == 8) {
            try {
                this.date = DATE_PATENT_FORMAT.parse(date);
            } catch (ParseException e) {
                throw new InvalidDataException("Invalid Date: " + date, e);
            }
        } else if (date != null && date.trim().length() == 4) {
            try {
                this.date = DATE_YEAR_FORMAT.parse(date);
            } catch (ParseException e) {
                throw new InvalidDataException("Invalid Date: " + date, e);
            }
        } else {
            throw new InvalidDataException("Invalid Date: " + date);
        }
    }

    public Date getDate() {
        return date;
    }

    public String getDateText(DateTextType dateType) {
        switch (dateType) {
        case RAW:
            return rawDate;
        case ISO:
            return getISOString();
        default:
            return rawDate;
        }
    }
    
    private String getISOString() {
        if (date == null) {
            return null;
        }
        return DATE_ISO_FORMAT.format(date);
    }

    @Override
    public String toString() {
        return "DocumentDate [date=" + date + "]";
    }
}
