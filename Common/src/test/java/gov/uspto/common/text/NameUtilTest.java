package gov.uspto.common.text;

import static org.junit.Assert.*;

import org.junit.Test;

public class NameUtilTest {

	/*
	 * Last Names common mixed case and just lower case:
	 * 
	 * DuPlantis
	 * LeBlanc
	 * 
	 */

	@Test
	public void normalizeCase_Hyphen() {
		String name = "JO-ANN BRICE";
		String actual = NameUtil.normalizeCase(name);
		assertEquals("Jo-Ann Brice", actual);
	}

	@Test
	public void normalizeCase_deLa() {
		String name = "DE LA BORBOLLA, Ian Rubin";
		String actual = NameUtil.normalizeCase(name);
		assertEquals("De la Borbolla, Ian Rubin", actual);

		String name2 = "REYES DE LA BARRERA";
		String actual2 = NameUtil.normalizeCase(name2);
		assertEquals("Reyes de la Barrera", actual2);
	}

	@Test
	public void normalizeCase_van() {
		String name = "VAN DEN HAAK";
		String actual = NameUtil.normalizeCase(name);
		assertEquals("Van den Haak", actual);

		String name2 = "PAUL VAN ITTERZON";
		String actual2 = NameUtil.normalizeCase(name2);
		assertEquals("Paul van Itterzon", actual2);

		String name3 = "VAN DER BEEK";
		String actual3 = NameUtil.normalizeCase(name3);
		assertEquals("Van der Beek", actual3);
	}

	@Test
	public void normalizeCase_McMac() {
		String name = "MCDONALD, Ronald";
		String actual = NameUtil.normalizeCase(name);
		assertEquals("McDonald, Ronald", actual);

		String name2 = "MACDONALD, Ronald";
		String actual2 = NameUtil.normalizeCase(name2);
		assertEquals("MacDonald, Ronald", actual2);
	}

	@Test
	public void normalizeCase_letterApostrophe() {
		String name = "O\u2019NEAL, shaquille";
		String actual = NameUtil.normalizeCase(name);
		assertEquals("O\u2019Neal, Shaquille", actual);

		String name2 = "DINESH D'SOUZA";
		String actual2 = NameUtil.normalizeCase(name2);
		assertEquals("Dinesh D'Souza", actual2);

		String name3 = "REMEI DE CA L'ELVIRA";
		String actual3 = NameUtil.normalizeCase(name3);
		assertEquals("Remei de Ca L'Elvira", actual3);
		
		String name4 = "L'OREAL";
		String actual4 = NameUtil.normalizeCase(name4);
		assertEquals("L'Oreal", actual4);
	}

	@Test
	public void normalizeCase_suffix() {
		String name = "SWAN, III, Ronald";
		String actual = NameUtil.normalizeCase(name);
		assertEquals("Swan, III, Ronald", actual);
	}

	@Test
	public void isPersonSuffix() {
		assertTrue(NameUtil.isPersonSuffix("III"));
		assertTrue(NameUtil.isPersonSuffix("VI,"));
		assertTrue(NameUtil.isPersonSuffix("Esq."));
	}

	@Test
	public void lastnameSuffix() {
		String name = "John Doe, Esq.";
		String[] actual = NameUtil.lastnameSuffix(name);
		assertEquals("John Doe", actual[0]);
		assertEquals("ESQ", actual[1]);

		String name2 = "John Doe, VI";
		String[] actual2 = NameUtil.lastnameSuffix(name2);
		assertEquals("John Doe", actual2[0]);
		assertEquals("VI", actual2[1]);
	}

	@Test
	public void orgName() {
		String companyName = "B&B TECHNOLOGIES L.P.";
		String actual = NameUtil.normalizeOrgNameCase(companyName);
		assertEquals("B&B Technologies L.P.", actual);

		String companyName2 = "BB TECHNOLOGIES L.P.";
		String actual2 = NameUtil.normalizeOrgNameCase(companyName2);
		assertEquals("BB Technologies L.P.", actual2);

		String companyName3 = "THE UNIVERSITY OF CALIFORNIA";
		String actual3 = NameUtil.normalizeOrgNameCase(companyName3);
		assertEquals("The University of California", actual3);

		String companyName4 = "L'OREAL";
		String actual4 = NameUtil.normalizeOrgNameCase(companyName4);
		assertEquals("L'Oreal", actual4);

		String companyName5 = "MCDONALD'S";
		String actual5 = NameUtil.normalizeOrgNameCase(companyName5);
		assertEquals("McDonald's", actual5);

		String companyName6 = "VIA TECHNOLOGIES, INC.";
		String actual6 = NameUtil.normalizeOrgNameCase(companyName6);
		assertEquals("Via Technologies, Inc.", actual6);

		String companyName7 = "VIA OF THE LEHIGH VALLEY";
		String actual7 = NameUtil.normalizeOrgNameCase(companyName7);
		assertEquals("Via of the Lehigh Valley", actual7);

		String companyName8 = "3SHAPE A/S";
		String actual8 = NameUtil.normalizeOrgNameCase(companyName8);
		assertEquals("3Shape A/S", actual8);

		String companyName9 = "COLGATE-PALMOLIVE COMPANY";
		String actual9 = NameUtil.normalizeOrgNameCase(companyName9);
		assertEquals("Colgate-Palmolive Company", actual9);

		// Maintain case of mixed case words.
		String companyName10 = "SharkNinja Operating LLC";
		String actual10 = NameUtil.normalizeOrgNameCase(companyName10);
		assertEquals("SharkNinja Operating LLC", actual10);

		// Maintain case of mixed case suffix
		String companyName11 = "Anton Paar GmbH";
		String actual11 = NameUtil.normalizeOrgNameCase(companyName11);
		assertEquals("Anton Paar GmbH", actual11);
	}
}
