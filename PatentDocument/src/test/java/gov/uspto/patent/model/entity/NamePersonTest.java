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
    public void testAbbreviatedName() throws InvalidDataException {
        NamePerson name = new NamePerson("John", "DOE");
        String abbrev = name.getAbbreviatedName();
        String expect = "Doe, J.";
        assertEquals(expect, abbrev);
    }

    @Test
    public void testAbbreviatedName2() throws InvalidDataException {
        NamePerson name = new NamePerson("John", "McMillian");
        String abbrev = name.getAbbreviatedName();
        String expect = "McMillian, J.";
        assertEquals(expect, abbrev);
    }

    @Test
    public void testFullNameTitleCase() throws InvalidDataException {
        NamePerson name = new NamePerson("JOHN", "DOE");
        String titleCase = name.getNameTitleCase();
        String expect = "Doe, John";
        assertEquals(expect, titleCase);
    }
}
