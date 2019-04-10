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
		String name = "O’NEAL, shaquille";
		String actual = NameUtil.normalizeCase(name);
		assertEquals("O’Neal, Shaquille", actual);

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
	
}
