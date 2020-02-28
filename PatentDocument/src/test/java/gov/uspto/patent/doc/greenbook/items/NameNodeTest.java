package gov.uspto.patent.doc.greenbook.items;

import static org.junit.Assert.*;

import org.junit.Test;

import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.model.entity.Name;
import gov.uspto.patent.model.entity.NameOrg;
import gov.uspto.patent.model.entity.NamePerson;

public class NameNodeTest {

	@Test
	public void person_deceased() throws InvalidDataException {
		NameNode parser = new NameNode(null);
		Name name = parser.createName("Gee, Sr., deceased; Samuel");
		assertTrue("expect NamePerson", name instanceof NamePerson);
		assertEquals("Sr.", name.getSuffix());
	}

	@Test
	public void semicolon_andsign_person() throws InvalidDataException {
		NameNode parser = new NameNode(null);
		Name name = parser.createName("Fletcher; James C. Administrator of the National Aeronautics & Space Administration");
		assertTrue("expect NamePerson", name instanceof NamePerson);
	}

	//@Test @TODO
	public void semicolon_org() throws InvalidDataException {
		NameNode parser = new NameNode(null);
		Name name = parser.createName("Doe; John & Jane");
		assertTrue("expect NameOrg", name instanceof NameOrg);
		assertEquals("Doe; John & Jane", ((NameOrg)name).getName());
	}

	@Test
	public void suffixFix_org() throws InvalidDataException {
		NameNode parser = new NameNode(null);
		Name name = parser.createName("Flint Steel, LLC");
		assertTrue("expect NameOrg", name instanceof NameOrg);
	}


	@Test
	public void suffixFix_per() throws InvalidDataException {
		NameNode parser = new NameNode(null);
		Name name = parser.createName("Doe, Sr; John");
		assertTrue("expect NamePerson", name instanceof NamePerson);
		assertEquals("John", ((NamePerson)name).getFirstName());
		assertEquals("Doe", ((NamePerson)name).getLastName());
		assertEquals("Sr", name.getSuffix());
	}

	@Test
	public void suffixFix_per_org() throws InvalidDataException {
		NameNode parser = new NameNode(null);
		Name name = parser.createName("Flint, LLC; Steel");
		assertTrue("expect NameOrg", name instanceof NameOrg);
		assertEquals("LLC", name.getSuffix());
	}

	@Test
	public void suffixFix_nee() throws InvalidDataException {
		NameNode parser = new NameNode(null);
		Name name = parser.createName("Flintston, nee Dinotopia; Betty");
		assertTrue("expect NameOrg", name instanceof NamePerson);
		assertEquals("Betty", ((NamePerson)name).getFirstName());
		assertEquals("Flintston", ((NamePerson)name).getLastName());
		assertEquals("Dinotopia, B.", ((NamePerson)name).getShortestSynonym());
		assertEquals("Dinotopia, Betty", ((NamePerson)name).getLongestSynonym());
		assertEquals("nee Dinotopia", name.getSuffix());
	}	

	@Test
	public void suffixFix_changeOfName() throws InvalidDataException {
		NameNode parser = new NameNode(null);
		Name name = parser.createName("Doe, now by change of name Jane Doe Smith; Jane");
		assertTrue("expect NameOrg", name instanceof NamePerson);
		assertEquals("Jane", ((NamePerson)name).getFirstName());
		assertEquals("Doe", ((NamePerson)name).getLastName());
		assertEquals("Jane Doe Smith", ((NamePerson)name).getShortestSynonym());
	}
}
