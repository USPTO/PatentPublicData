package gov.uspto.patent.doc.xml;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

public class BraceCode2UnicodeTest {

	private static BraceCode braceCodeUTF8 = new BraceCode(false);
	private static BraceCode braceCodeUTF16 = new BraceCode(true);

	private static Map<String, String> accentOver = new LinkedHashMap<String, String>();
	static {
		accentOver.put("{hacek over (C)}", "\u010C");
		accentOver.put("Andr{acute (e)}", "Andr\u00E9");
		accentOver.put("jalape{tilde over(n)}o", "jalape\u00F1o");
		accentOver.put("puree pur{acute over (e)}e p{umlaut(u)}ree", "puree pur\u00E9e p\u00FCree");
	}

	private static Map<String, String> accentUnder = new LinkedHashMap<String, String>();
	static {
		accentUnder.put("{tilde under (x)}", "\u0330x");
	}

	private static Map<String, String> accentGreek = new LinkedHashMap<String, String>();
	static {
		accentGreek.put("{circumflex (\u03B8)}", "\u03B8"); // theta_circumflex
	}

	private static Map<String, String> accentMathSymbols = new LinkedHashMap<String, String>();
	static {
		//accentMathSymbols.put("{hacek over (\u2207)}", "\u030C\u2207"); // hacek nabla Grant US8139142B2
		accentMathSymbols.put("{tilde under (>)}", "\u0330>");
		accentMathSymbols.put("{underscore under (>)}", "\u2265");
		accentMathSymbols.put("{circle around (x)}", "\u24E7");
		accentMathSymbols.put("{circle around ( 3 )}", "\u2462");
		accentMathSymbols.put("{dot over (q)}", "\u0307q"); //qdot heat transfer per unit mass.
		accentMathSymbols.put("{right arrow over (m)}", "\u20D7m");
		//accentMathSymbols.put("{circle around (10)}1", "\u20DD10");
		//accentMathSymbols.put("{circle around (1+L )}", "\u20DD3");
	}

	private static Map<String, String> nested = new LinkedHashMap<String, String>();
	static {
		nested.put("{dot over ({tilde over (\u03C6)}}", "\u0307\u0303\u03C6"); // Grant US6192305B1
		nested.put(" {umlaut over ({circumflex over (\u03C6)}})}", "\u0308\u0302\u03C6"); // Grant US6192305B1
		nested.put("{tilde over ({circumflex over (K)})}", "\u0303\u0302K"); // Grant US6341257B1
		nested.put("{tilde under ({circumflex over (m)}", ""); // Grant US7119816B2
	}

	private static Map<String, String> unicodeComposit = new LinkedHashMap<String, String>();
	static {
		unicodeComposit.put("A{hacek over (\u0125)}B", "A\u030C\u0125B");
		unicodeComposit.put("A{grave over (p)}B", "A\u0300pB");
		unicodeComposit.put("A{overscore (S)}B", "A\u0305SB"); // US6314147B1
		unicodeComposit.put("A{circumflex over (T)}B", "A\u0302TB");
		unicodeComposit.put("where {circumflex over (p)}(n) is the measurement of",
				"where \u0302p(n) is the measurement of");
		unicodeComposit.put("1{overscore (1)}00 crystalline", "1\u0305100 crystalline");
	}

	@Test
	public void combineUtf16() throws ParseException {	
		String letter = "A";
		String[] accents = new String[]{"acute"};
		String expect = "\u0301A";
		String actual = braceCodeUTF8.combineUtf16(letter, accents);
		assertEquals(expect, actual);
	}

	@Test
	public void combineMultipleUtf16() throws ParseException {	
		String letter = "A";
		String[] accents = new String[]{"acute", "hacek"};
		String expect = "\u0301\u030CA";
		String actual = braceCodeUTF16.combineUtf16(letter, accents);
		assertEquals(expect, actual);
	}

	@Test
	public void over() {
		for (Entry<String, String> check : accentOver.entrySet()) {
			String actual = braceCodeUTF8.covert2Unicode(check.getKey());
			assertEquals(check.getValue(), actual);
		}
	}

	@Test
	public void under() {
		for (Entry<String, String> check : accentUnder.entrySet()) {
			String actual = braceCodeUTF16.covert2Unicode(check.getKey());
			assertEquals(check.getValue(), actual);
		}
	}

	@Test
	public void unicodeCompositeUtf16() {
		for (Entry<String, String> check : unicodeComposit.entrySet()) {
			String actual = braceCodeUTF16.covert2Unicode(check.getKey());
			assertEquals(check.getValue(), actual);
		}
	}

	@Test
	public void greek() {
		for (Entry<String, String> check : accentGreek.entrySet()) {
			String actual = braceCodeUTF8.covert2Unicode(check.getKey());
			assertEquals(check.getValue(), actual);
		}
	}

	@Test
	public void noMatch() {
		String expectInput = "Some {badmatch over (a)} was here.";
		String actual = braceCodeUTF8.covert2Unicode(expectInput);
		assertEquals(expectInput, actual);
	}

	//@Test FIXME
	public void space() {
		String empty = "{circumflex over ( )}"; // Bracket with space - Grant US6185654B1
		String actual = braceCodeUTF8.covert2Unicode(empty);
		String expect = "\u02C6";
		assertEquals("Failed Accent 'circumflex' Over Space", expect, actual);
	}

	//@Test FIXME
	public void empty() {
		String empty = "{acute over ()}"; // Bracket with nothing - Grant US8083177B2, US7237273B2, US7516635B2
		String actual = braceCodeUTF8.covert2Unicode(empty);
		String expect = "\u02CA";
		assertEquals("Failed Accent 'acute' Over Empty", expect, actual);
	}
	
	@Test
	public void accentMathSymbol() {
		for (Entry<String, String> check : accentMathSymbols.entrySet()) {
			String actual = braceCodeUTF16.covert2Unicode(check.getKey());
			assertEquals(check.getValue(), actual);
		}
	}

	@Test
	public void squareRoot() {
		String text = "{square root over (X)}"; // Grant US6349319B1
		String actual = braceCodeUTF16.covert2Unicode(text);
		String expect = "\u221AX";
		assertEquals(expect, actual);
	}

	/*
	@Test
	public void overscoreSequence() {
		String text = "{overscore (ABC)}";
		String actual = braceCodeUTF16.covert2Unicode(text);
		String expect = "\u0305A\u200DB\u200DC";
		assertEquals(expect, actual);
	}
	
	@Test
	public void squareRootSequence() {
		String text = "{square root over (I 2 +Q 2 )}"; // Grant US6658445B1
		String actual = braceCodeUTF16.covert2Unicode(text);
		String expect = "sqrt(I 2 +Q 2 )";
		assertEquals(expect, actual);
	}
	*/

	// TODO support nested.
	// @Test
	public void nested() {
		for (Entry<String, String> check : nested.entrySet()) {
			String actual = braceCodeUTF16.covert2Unicode(check.getKey());
			assertEquals(check.getValue(), actual);
		}
	}

	@Test
	public void stripAccentsKeepSquareRoot() {
		String text = "{square root over (X)}";
		String actual = braceCodeUTF8.stripAccents(text);
		assertEquals(text, actual);
	}

	@Test
	public void stripAccents() {
		String text = "br\u00FBl\u00E9e br{circumflex over (u)}l{acute (e)}e";
		String expect = "brulee brulee";
		String actual = braceCodeUTF8.stripAccents(text);
		assertEquals(expect, actual);
	}

}
