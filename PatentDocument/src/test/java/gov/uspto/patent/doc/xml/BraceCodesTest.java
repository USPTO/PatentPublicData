package gov.uspto.patent.doc.xml;

import static org.junit.Assert.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

public class BraceCodesTest {

	private static BraceCodes braceCode = new  BraceCodes();

	private static Map<String, String> accentOver = new LinkedHashMap<String, String>();
	static {
		accentOver.put("Andr{acute over (e)}", "Andr\u00E9"); // Andrê
		accentOver.put("jalape{tilde over(n)}o", "jalape\u00F1o"); // jalapeño
		accentOver.put("puree pur{acute over (e)}e p{umlaut(u)}ree", "puree pur\u00E9e p\u00FCree");
				// puree purée püree
	}

	private static Map<String, String> accentGreek = new LinkedHashMap<String, String>();
	static {
		accentGreek.put("{circumflex (\u03B8)}", "\u03B8"); // theta_circumflex
	}

	private static Map<String, String> accentMathSymbols = new LinkedHashMap<String, String>();
	static {
		accentMathSymbols.put("{hacek over (\u2207)}", "\u030C\u2207"); // hacek nabla  Grant US8139142B2
	}

	private static Map<String, String> nested = new LinkedHashMap<String, String>();
	static {
		nested.put("{dot over ({tilde over (\u03C6)}}", "\u0307\u0303\u03C6"); // Grant US6192305B1
		nested.put(" {umlaut over ({circumflex over (\u03C6)}})}", "\u0308\u0302\u03C6"); // Grant US6192305B1
		nested.put("{tilde over ({circumflex over (K)})}", "\u0303\u0302K"); // Grant US6341257B1
	}

	private static Map<String, String> unicodeComposit = new LinkedHashMap<String, String>();
	static {
		unicodeComposit.put("{hacek over (\u0125)}", "\u030C\u0125");
		unicodeComposit.put("{grave over (p)}", "\u0300p");
		unicodeComposit.put("{overscore (S)}", "\u0305S"); // US6314147B1
		unicodeComposit.put("{circumflex over (T)}", "\u0302T");
		unicodeComposit.put("where {circumflex over (p)}(n) is the measurement of", "where \u0302p(n) is the measurement of");
		unicodeComposit.put("1{overscore (1)}00 crystalline", "1\u0305100 crystalline");
	}

	@Test
	public void testOver() {
		for (Entry<String, String> check : accentOver.entrySet()) {
			String actual = braceCode.brace2Unicode(check.getKey());
			assertEquals(check.getValue(), actual);
		}
	}

	@Test
	public void testUnicodeComposite() {
		for (Entry<String, String> check : unicodeComposit.entrySet()) {
			String actual = braceCode.brace2Unicode(check.getKey());
			assertEquals(check.getValue(), actual);
		}
	}

	@Test
	public void testGreek() {
		for (Entry<String, String> check : accentGreek.entrySet()) {
			String actual = braceCode.brace2Unicode(check.getKey());
			assertEquals(check.getValue(), actual);
		}
	}

	@Test
	public void testSpace() {
		String empty = "{circumflex over ( )}"; // Grant US6185654B1
		String actual = braceCode.brace2Unicode(empty);
		String expect = "\u0302 ";
		assertEquals(expect, actual);
	}

	@Test
	public void testNoMatch() {
		String empty = "Some {badmatch over (a)} was here.";
		String actual = braceCode.brace2Unicode(empty);
		String expect = "Some {badmatch over (a)} was here.";
		assertEquals(expect, actual);
	}

	// @TODO support nested.
	//@Test
	public void testNested() {
		for (Entry<String, String> check : nested.entrySet()) {
			String actual = braceCode.brace2Unicode(check.getKey());
			assertEquals(check.getValue(), actual);
		}
	}

	@Test
	public void testAccentMathSymbol() {
		for (Entry<String, String> check : accentMathSymbols.entrySet()) {
			String actual = braceCode.brace2Unicode(check.getKey());
			assertEquals(check.getValue(), actual);
		}
	}

}
