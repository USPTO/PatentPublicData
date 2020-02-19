package gov.uspto.common.text;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class DateUtil {

	/**
	 * Convert ISO Date
	 *
	 * <p>
	 * When Month is missing or 00 then 01 is used and when day is missing or 00
	 * then 01 is used.
	 * <p>
	 * 
	 * <p><pre>Common formats:
	 *    DateTimeFormatter.ISO_DATE_TIME '2011-12-03T00:00:00'
	 *    DateTimeFormatter.ISO_INSTANT   '2011-12-03T00:00:00Z'
	 * </pre></p>
	 * @param dateStr
	 * @return
	 */
	public static String toDateTime(String dateStr, DateTimeFormatter dateFormater) {
		if (dateStr.endsWith("0000")) {
			dateStr = dateStr.substring(0, 4) + "0101";
		} else if (dateStr.endsWith("00")) {
			dateStr = dateStr.substring(0, 6) + "01";
		} else if (dateStr.length() == 4) {
			dateStr += "0101";
		} else if (dateStr.length() == 6) {
			dateStr += "01";
		}

		LocalDateTime dt = LocalDate.parse(dateStr, DateTimeFormatter.BASIC_ISO_DATE).atStartOfDay().withSecond(0);
		return dateFormater.withZone(ZoneOffset.UTC).format(dt.atZone(ZoneOffset.UTC));
	}

	/**
	 * Convert ISO Date into ISO DateTime
	 * 
	 * <p>
	 * Covert '20111203' to '2011-12-03T00:00:00Z'
	 * <p>
	 *
	 * <p>
	 * When Month is missing or 00 then 12 is used and when day is missing or 00
	 * then 01 is used. When year is 0000 or less than 4 then empty value is returned.
	 * <p>
	 * 
	 * @param dateStr
	 * @return
	 */
	public static String toDateTimeISO(String dateStr) {
		return toDateTime(dateStr, DateTimeFormatter.ISO_INSTANT);
	}

}
