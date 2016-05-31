package gov.uspto.patent.model;

import java.text.ParseException;
import java.util.Date;

import org.apache.commons.lang3.time.DateParser;
import org.apache.commons.lang3.time.FastDateFormat;

import gov.uspto.patent.InvalidDataException;

public class DocumentDate {
	/*
	 * FastDateFormat is Thread-Safe version of SimpleDateFormat
	 */
	private static final DateParser DATE_PATENT_FORMAT = FastDateFormat.getInstance("yyyyMMdd");
	private static final FastDateFormat DATE_ISO_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss'Z'");

	private Date date;
	private String rawDate;

	public DocumentDate(String date) throws InvalidDataException {
		this.rawDate = date;
		setDate(date);
	}

	public DocumentDate(Date date) throws InvalidDataException {
		this.date = date;
	}

	public void setDate(String date) throws InvalidDataException {
		if (date != null && date.trim().length() == 8) {
			try {
				this.date = DATE_PATENT_FORMAT.parse(date);
			} catch (ParseException e) {
				throw new InvalidDataException("Invalid Date: " + date, e);
			}
		} else {
			throw new InvalidDataException("Invalid Date: " + date);
		}
	}

	public String getISOString() {
		if (date == null) {
			return null;
		}
		return DATE_ISO_FORMAT.format(date);
	}

	public Date getDate() {
		return date;
	}

	/**
	 * Raw originally passed in date string.
	 */
	public String getRawDate() {
		return rawDate;
	}

	@Override
	public String toString() {
		return "DocumentDate [date=" + date + "]";
	}
}
