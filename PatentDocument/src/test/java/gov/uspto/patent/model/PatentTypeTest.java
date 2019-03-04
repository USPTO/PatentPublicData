package gov.uspto.patent.model;

import static org.junit.Assert.*;

import org.junit.Test;

import gov.uspto.patent.InvalidDataException;

public class PatentTypeTest {

	@Test
	public void parseInt() throws InvalidDataException {
		PatentType expected = PatentType.UTILITY;
		PatentType actual = PatentType.parse(1);
		assertEquals(expected, actual);
	}

	@Test
	public void parseStr() throws InvalidDataException {
		PatentType expected = PatentType.UTILITY;
		PatentType actual = PatentType.parse("utility");
		assertEquals(expected, actual);
	}

}
