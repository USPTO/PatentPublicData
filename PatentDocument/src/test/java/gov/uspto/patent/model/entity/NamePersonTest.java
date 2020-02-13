package gov.uspto.patent.model.entity;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import gov.uspto.patent.InvalidDataException;

public class NamePersonTest {

	@Test(expected = InvalidDataException.class)
	public void invalidNameNullLastName() throws InvalidDataException {
		NamePerson name = new NamePerson("John", null);
		name.validate();
	}

	@Test(expected = InvalidDataException.class)
	public void invalidNameEmptyLastName() throws InvalidDataException {
		NamePerson name = new NamePerson("John", "");
		name.validate();
	}

	@Test
	public void abbreviatedName() throws InvalidDataException {
		NamePerson name = new NamePerson("John", "DOE");
		String abbrev = name.getAbbreviatedName();
		String expect = "Doe, J.";
		assertEquals(expect, abbrev);
	}

	@Test
	public void abbreviatedName2() throws InvalidDataException {
		NamePerson name = new NamePerson("John", "McMillian");
		String abbrev = name.getAbbreviatedName();
		String expect = "McMillian, J.";
		assertEquals(expect, abbrev);
	}

	@Test
	public void fullNameNormCase() throws InvalidDataException {
		NamePerson name = new NamePerson("JOHN", "VAN DER DOE");
		String normCase = name.getNameNormalizeCase();
		String expect = "Van der Doe, John";
		assertEquals(expect, normCase);
	}

	@Test
	public void initializeName() throws InvalidDataException {
		NamePerson name = new NamePerson("John", "Smith", "DOE");
		String abbrev = name.getInitials();
		String expect = "JSD";
		assertEquals(expect, abbrev);
	}

	@Test
	public void initializeName2() throws InvalidDataException {
		NamePerson name = new NamePerson("John Smith", "DOE");
		String abbrev = name.getInitials();
		String expect = "JSD";
		assertEquals(expect, abbrev);
	}

	@Test
	public void initializeMultiwordSurname() throws InvalidDataException {
		NamePerson name = new NamePerson("Bernardus", "van den Bosch");
		String abbrev = name.getInitials();
		String expect = "BvdB";
		assertEquals(expect, abbrev);
	}

	@Test
	public void initializeHyphenSurname() throws InvalidDataException {
		NamePerson name = new NamePerson("Karla", "BRAVO-ALTAMIRANO");
		String abbrev = name.getInitials();
		String expect = "KB";
		assertEquals(expect, abbrev);
	}
}
