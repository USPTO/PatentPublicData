package gov.uspto.patent.doc.greenbook.items;

import static org.junit.Assert.*;

import org.junit.Test;

public class NameNodeTest {

	@Test
	public void suffixFix() {
		NameNode name = new NameNode(null);
		String[] expect  = new String[] {"Haines", "SR"};
		String[] actual = name.suffixFix("Haines, Sr");
		assertArrayEquals(expect, actual);

		String[] expect2  = new String[] {"Swann", "III"};
		String[] actual2 = name.suffixFix("Swann, III");
		assertArrayEquals(expect2, actual2);
	}

}
