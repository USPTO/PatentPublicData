package gov.uspto.common.text;

import static org.junit.Assert.*;

import org.junit.Test;

public class DateUtilTest {

	@Test
	public void toDateTimeISO_AllZeros() {
		assertEquals("0000-01-01T00:00:00Z", DateUtil.toDateTimeISO("00000000"));
	}
	
	@Test
	public void toDateTimeISO() {
		assertEquals("1980-11-20T00:00:00Z", DateUtil.toDateTimeISO("19801120"));
	}

	@Test
	public void toDateTimeISO_ZeroDay() {
		assertEquals("1980-11-01T00:00:00Z", DateUtil.toDateTimeISO("19801100"));
	}

	@Test
	public void toDateTimeISO_ZeroMonthAndDay() {
		assertEquals("1981-01-01T00:00:00Z", DateUtil.toDateTimeISO("19810000"));
	}

	@Test
	public void toDateTimeISO_OnlyYear() {
		assertEquals("1981-01-01T00:00:00Z", DateUtil.toDateTimeISO("1981"));
	}

	@Test
	public void toDateTimeISO_OnlyYearMonth() {
		assertEquals("1980-12-01T00:00:00Z", DateUtil.toDateTimeISO("198012"));
	}
}
