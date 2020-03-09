package gov.uspto.patent.model;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.patent.DateTextType;
import gov.uspto.patent.InvalidDataException;

public class DocumentDate {
	private static final Logger LOGGER = LoggerFactory.getLogger(DocumentDate.class);

	private static final DateTimeFormatter YEAR_MONTH = DateTimeFormatter.ofPattern("yyyyMM");

	private LocalDate date;
	private final String rawDate;

	public DocumentDate(String rawDate) throws InvalidDataException {
		this.rawDate = rawDate;
		this.date = toDate(rawDate);
	}

	public DocumentDate(String rawDate, LocalDate date) throws InvalidDataException {
		this.rawDate = rawDate;
		this.date = date;
	}

	public DocumentDate(String rawDate, Date date) throws InvalidDataException {
		this.rawDate = rawDate;
		this.date = date.toInstant().atZone(ZoneId.of("GMT")).toLocalDate();
	}

	private LocalDate toDate(final String date) throws InvalidDataException {
		if (date == null || date.trim().isEmpty() || !date.trim().matches("\\d+")) {
			try {
				return LocalDate.of(0, 1, 1);
			} catch (DateTimeParseException e) {
				// empty.
			}
		}

		String dateStr = date.trim();

		// Greenbook citations have yyyy0000 "19560000" and yyyy0000 "19560000"
		dateStr = dateStr.replaceFirst("(?:0{4}|0{2})$", "");

		if (dateStr.length() == 8) { // Full Date: Year Month Day
			try {
				return LocalDate.parse(date.trim(), DateTimeFormatter.BASIC_ISO_DATE);
			} catch (DateTimeParseException e) {
				throw new InvalidDataException("Invalid Date: '" + date + "'", e);
			}
		} else if (dateStr.length() == 6) { // Year Month
			try {
				YearMonth ymDate = YearMonth.parse(dateStr, YEAR_MONTH);
				this.date = ymDate.atDay(1);
			} catch (DateTimeException e) {
				LOGGER.warn("Invalid Date: {} ; trying with only year", dateStr);
				return toDate(dateStr.substring(0, 4));
			}
		} else if (dateStr.length() == 4) { // Year Only
			return LocalDate.of(Integer.parseInt(dateStr), 1, 1);
		} else {
			throw new InvalidDataException("Invalid Date: '" + date + "'");
		}

		return null;
	}

	public LocalDate getDate() {
		return date;
	}

	public String getDateText(DateTextType dateType) {
		switch (dateType) {
		case RAW:
			return rawDate;
		case ISO_DATE:
			return getISOString();
		case ISO_DATE_TIME:
			return getISODateString();
		default:
			return rawDate;
		}
	}

	public int getYear() {
		return date.getYear();
	}

	private String getISODateString() {
		return date.format(DateTimeFormatter.ISO_DATE);
	}

	private String getISOString() {
		return DateTimeFormatter.ISO_INSTANT.format(date.atStartOfDay());
	}

	@Override
	public final int hashCode() {
		if (date != null) {
			return date.hashCode();
		} else if (rawDate != null) {
			return rawDate.hashCode();
		} else {
			return 0;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof DocumentDate)) {
			return false;
		}
		DocumentDate other = (DocumentDate) o;
		return other.date.equals(this.date);
	}

	@Override
	public String toString() {
		return "DocumentDate [date=" + getISODateString() + "]";
	}

	public static DocumentDate getEmpty() {
		try {
			return new DocumentDate("");
		} catch (InvalidDataException e) {
			return null;
		}
	}

}
