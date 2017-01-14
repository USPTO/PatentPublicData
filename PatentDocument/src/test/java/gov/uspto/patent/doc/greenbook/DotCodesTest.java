package gov.uspto.patent.doc.greenbook;

import static org.junit.Assert.*;

import org.junit.Test;

public class DotCodesTest {

	@Test
	public void replace(){
		String input = ".alpha..beta. .-+. .0.";

		String expect = "\u03B1\u03B2 \u2213 \u00F8";

		String actual = DotCodes.replace(input);

		assertEquals(expect, actual);		
	}
	
	@Test
	public void replaceSubSupHTML() {
		String input = "h.sub.2O";

		String expect = "h<sub>2O</sub>";

		String actual = DotCodes.replaceSubSupHTML(input);

		assertEquals(expect, actual);
	}

	@Test
	public void replaceSubSupHTML2() {
		String input = "h.sub.2.O";
		
		String expect = "h<sub>2</sub>O";

		String actual = DotCodes.replaceSubSupHTML(input);
		
		assertEquals(expect, actual);
	}
	
}
