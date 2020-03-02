package gov.uspto.patent.doc.greenbook.items;

import static org.junit.Assert.*;

import org.junit.Test;

import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.model.entity.Name;
import gov.uspto.patent.model.entity.NameOrg;
import gov.uspto.patent.model.entity.NamePerson;

public class NameNodeTest {

	@Test
	public void person_aka() throws InvalidDataException {
		NameNode parser = new NameNode(null);
		Name name = parser.createName("Cory, a/k/a Cynthia S. Timmerman, executrix; Cynthia S.");
		assertTrue("expect NamePerson", name instanceof NamePerson);
		assertEquals("Cynthia S.", ((NamePerson) name).getFirstName());
		assertEquals("Cory", ((NamePerson) name).getLastName());
		assertEquals("a/k/a Cynthia S. Timmerman, executrix", name.getSuffix());
		assertEquals("Timmerman, Cynthia S.", ((NamePerson) name).getLongestSynonym().trim());
	}

	@Test
	public void person_bySaid() throws InvalidDataException {
		NameNode parser = new NameNode(null);
		Name name = parser.createName("Schutz, by said Murdoch N. McIntosh; Juris E.");
		assertTrue("expect NamePerson", name instanceof NamePerson);
		assertEquals("Juris E.", ((NamePerson) name).getFirstName());
		assertEquals("Schutz", ((NamePerson) name).getLastName());
		assertEquals("by said Murdoch N. McIntosh", name.getSuffix());
	}

	@Test
	public void person_multicomma() throws InvalidDataException {
		NameNode parser = new NameNode(null);
		Name name = parser.createName("Crisafulli, Jr., executor; by Joseph");
		assertTrue("expect NamePerson", name instanceof NamePerson);
		assertEquals("Jr., executor", name.getSuffix());
	}

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
		Name name = parser
				.createName("Fletcher; James C. Administrator of the National Aeronautics & Space Administration");
		assertTrue("expect NamePerson", name instanceof NamePerson);
	}

	// @Test @TODO
	public void semicolon_org() throws InvalidDataException {
		NameNode parser = new NameNode(null);
		Name name = parser.createName("Doe; John & Jane");
		assertTrue("expect NameOrg", name instanceof NameOrg);
		assertEquals("Doe; John & Jane", ((NameOrg) name).getName());
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
		assertEquals("John", ((NamePerson) name).getFirstName());
		assertEquals("Doe", ((NamePerson) name).getLastName());
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
		Name name = parser.createName("Flintstone, nee Dinotopia; Betty");
		assertTrue("expect NamePerson", name instanceof NamePerson);
		assertEquals("Betty", ((NamePerson) name).getFirstName());
		assertEquals("Flintstone", ((NamePerson) name).getLastName());
		assertEquals("Flintstone, Betty, nee Dinotopia", ((NamePerson) name).getName());
		assertEquals("Flintstone, B.", ((NamePerson) name).getAbbreviatedName());
		assertEquals("Dinotopia, B.", ((NamePerson) name).getShortestSynonym());
		assertEquals("Dinotopia, Betty", ((NamePerson) name).getLongestSynonym());
		assertEquals("nee Dinotopia", name.getSuffix());
	}

	@Test
	public void suffixFix_nee_2() throws InvalidDataException {
		NameNode parser = new NameNode(null);
		Name name = parser.createName("Flintstone nee Dinotopia, legal guardian; Betty");
		assertTrue("expect NamePerson", name instanceof NamePerson);
		assertEquals("Betty", ((NamePerson) name).getFirstName());
		assertEquals("Flintstone", ((NamePerson) name).getLastName());
		// assertEquals("Dinotopia, B.", ((NamePerson) name).getShortestSynonym());
		// assertEquals("Dinotopia, Betty", ((NamePerson) name).getLongestSynonym());
		assertEquals("nee Dinotopia, legal guardian", name.getSuffix());
	}

	@Test
	public void suffixFix_changeOfName() throws InvalidDataException {
		NameNode parser = new NameNode(null);
		Name name = parser.createName("Doe, now by change of name Jane Doe Smith; Jane");
		assertTrue("expect NamePerson", name instanceof NamePerson);
		assertEquals("Jane", ((NamePerson) name).getFirstName());
		assertEquals("Doe", ((NamePerson) name).getLastName());
		assertEquals("Doe Smith, Jane", ((NamePerson) name).getLongestSynonym().trim());
		assertEquals("Doe Smith, J.", ((NamePerson) name).getShortestSynonym().trim());
		assertEquals("Doe, J.", ((NamePerson) name).getAbbreviatedName());
		assertEquals("JD", ((NamePerson) name).getInitials());
	}

	@Test
	public void company_semicolon_final_comma() throws InvalidDataException {
		NameNode parser = new NameNode(null);
		Name name = parser.createName("Schecter; Manny W. Felsman, Bradley, Vaden, Gunter & Dillon, LLP");
		assertTrue("expect NameOrg", name instanceof NameOrg);
		assertEquals("Schecter; Manny W. Felsman, Bradley, Vaden, Gunter & Dillon, LLP", ((NameOrg) name).getName());
	}
	
	@Test
	public void company_semicolon() throws InvalidDataException {
		NameNode parser = new NameNode(null);
		Name name = parser.createName("AT&T Information Systems Inc; AT&T Bell Laboratories");
		assertFalse(name == null);
		assertTrue("expect NameOrg", name instanceof NameOrg);
		assertEquals("AT&T Information Systems Inc; AT&T Bell Laboratories", ((NameOrg) name).getName());
	}

	@Test
	public void company_semicolon_final_word() throws InvalidDataException {
		NameNode parser = new NameNode(null);
		Name name = parser.createName("Bongini; Stephen Fleit, Kahn, Gibbons, Gutman & Bongini P.L.");
		assertTrue("expect NameOrg", name instanceof NameOrg);
		assertEquals("Bongini; Stephen Fleit, Kahn, Gibbons, Gutman & Bongini P.L.", ((NameOrg) name).getName());
	}

	@Test
	public void company_semicolon_final_words() throws InvalidDataException {
		NameNode parser = new NameNode(null);
		Name name = parser.createName("Christine; Christine, Roberts and Cushman, Intellectual Property Practice Group");
		assertTrue("expect NameOrg", name instanceof NameOrg);
		assertEquals("Christine; Christine, Roberts and Cushman, Intellectual Property Practice Group",
				((NameOrg) name).getName());
	}

	@Test
	public void long_suffix() throws InvalidDataException {
		NameNode parser = new NameNode(null);
		Name name = parser.createName("Hayashi, President, University of Tokyo; Kentaro");
		assertFalse(name == null);
		assertTrue("expect NamePerson", name instanceof NamePerson);
		assertEquals("Kentaro",((NamePerson) name).getFirstName());
		assertEquals("Hayashi",((NamePerson) name).getLastName());
		assertEquals("Hayashi, Kentaro, President, University of Tokyo",((NamePerson) name).getName());
	}

	@Test
	public void name_space_company() throws InvalidDataException {
		NameNode parser = new NameNode(null);
		Name name = parser.createName("Eugene M. The Law Offices of Eugene M. Lee P.L.L.C.");
		assertFalse(name == null);
		assertTrue("expect NameOrg", name instanceof NameOrg);
		assertEquals("Eugene M. The Law Offices of Eugene M. Lee P.L.L.C.",((NameOrg) name).getName());
	}

}
