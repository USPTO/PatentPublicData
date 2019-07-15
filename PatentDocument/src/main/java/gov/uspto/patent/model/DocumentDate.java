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
	private String rawDate;

	public DocumentDate(String date) throws InvalidDataException {
		this.rawDate = date;
		setDate(date);
	}

	public DocumentDate(LocalDate date) throws InvalidDataException {
		this.date = date;
	}

	public DocumentDate(Date date) throws InvalidDataException {
		this.date = date.toInstant().atZone(ZoneId.of("GMT")).toLocalDate();
	}

	public void setDate(final String date) throws InvalidDataException {
		if (date == null || date.trim().isEmpty() || !date.trim().matches("\\d+")) {
			try {
				this.date = LocalDate.of(0, 1, 1);
				return;
			} catch (DateTimeParseException e) {
				// empty.
			}
		}

		String dateStr = date.trim();

		// Greenbook citations have yyyy0000 "19560000" and yyyy0000 "19560000"
		dateStr = dateStr.replaceFirst("(?:0{4}|0{2})$", "");

		if (dateStr.length() == 8) { // Full Date: Year Month Day
			try {
				this.date = LocalDate.parse(date.trim(), DateTimeFormatter.BASIC_ISO_DATE);
			} catch (DateTimeParseException e) {
				throw new InvalidDataException("Invalid Date: '" + date + "'", e);
			}
		} else if (dateStr.length() == 6) { // Year Month
			try {
				YearMonth ymDate = YearMonth.parse(dateStr, YEAR_MONTH);
				this.date = ymDate.atDay(1);
			} catch (DateTimeException e) {
				LOGGER.warn("Invalid Date: {} ; trying with only year", dateStr);
				setDate(dateStr.substring(0, 4));
			}
		} else if (dateStr.length() == 4) { // Year Only
			this.date = LocalDate.of(Integer.parseInt(dateStr), 1, 1);
		} else {
			throw new InvalidDataException("Invalid Date: '" + date + "'");
		}
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
