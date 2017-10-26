package gov.uspto.patent.doc.xml;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Brace Codes
 *
 *<p>
 * Brace Codes are used within Patent documents in XML. They are used for accents
 * or diacritics in proper names, mathematical equations, titles of foreign NPL and
 * occasionally descriptive words in the text body.
 * The question remains, why do brace codes appear in xml when a unicode equivalent exists?
 * Since it occurs high enough this class was created to normalize Brace Codes into unicode.
 *</p>
 *
 *<p>
 * Supported:
 *  Single character accents : {circumflex over (e)} <br/>
 *</p>
 *
 *<p>
 * Not Supported Yet: <br/>
 * 1) Multi-word accent name: {square root over (n)} <br/>
 * 2) Nested accents: {acute over ({hacek over (a)}s)} <br/>
 *</p>
 *
 * https://emw3.com/unicode-accents.html
 * https://www.unicode.org/charts/PDF/U0300.pdf
 * 
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class BraceCodes {

	private static final Pattern ACCENT_PATTERN = Pattern
			.compile("\\{([A-z]+)(?:\\sover|\\sabove)?\\s?\\((?:\\s|\\s?([\\p{L}\\p{N}\\p{Sm}]))\\s?\\)\\}", Pattern.UNICODE_CHARACTER_CLASS);

	/*
	 * Unicode Combining Accent Lookup Table
	 * Unicode combing marks are to only be use if the unicode does not exist.
	 * Note for Search Indexes as well as RDMS see Solr mapping-FoldToASCII.txt
	 */
	private static final Map<String, Character> ACCENT_COMBINING = new HashMap<String, Character>();
	static {
		ACCENT_COMBINING.put("grave", '\u0300');
		ACCENT_COMBINING.put("acute", '\u0301');
		ACCENT_COMBINING.put("circumflex", '\u0302');
		ACCENT_COMBINING.put("tilde", '\u0303');
		ACCENT_COMBINING.put("macron", '\u0304');
		ACCENT_COMBINING.put("overline", '\u0305');
		ACCENT_COMBINING.put("overscore", '\u0305');
		ACCENT_COMBINING.put("breve", '\u0306');
		ACCENT_COMBINING.put("dot", '\u0307');
		ACCENT_COMBINING.put("diaeresis", '\u0308');
		ACCENT_COMBINING.put("umlaut", '\u0308');
		ACCENT_COMBINING.put("hook", '\u0309');
		ACCENT_COMBINING.put("ring", '\u030A');
		ACCENT_COMBINING.put("caron", '\u030C');
		ACCENT_COMBINING.put("hacek", '\u030C');
		ACCENT_COMBINING.put("cedilla", '\u0327');
		ACCENT_COMBINING.put("ogonek", '\u0328');
	}

	/*
	 * Unicode Accent Lookup Table
	 */
	private static final Map<String, String> ACCENTS = new HashMap<String, String>();
	static {
		ACCENTS.put("blank_umlaut", "");
		
		ACCENTS.put("a_grave", "\u00E0");
		ACCENTS.put("a_acute", "\u00E1");
		ACCENTS.put("a_circumflex", "\u00E2");
		ACCENTS.put("a_tilde", "\u00E3");
		ACCENTS.put("a_umlaut", "\u00E4");
		ACCENTS.put("a_diaeresis", "\u00E4");
		ACCENTS.put("a_ring", "\u00E5");
		ACCENTS.put("a_caron", "\u01CE");
		ACCENTS.put("a_hacek", "\u01CE");
		ACCENTS.put("a_macron", "\u0101");
		ACCENTS.put("a_overline", "\u0101");
		ACCENTS.put("a_breve", "\u0103");
		ACCENTS.put("a_ogonek", "\u0105");
		ACCENTS.put("a_double_grave", "\u0201");

		ACCENTS.put("c_acute", "\u0107");
		ACCENTS.put("c_circumflex", "\u0109");
		ACCENTS.put("c_dot", "\u010B");
		ACCENTS.put("c_caron", "\u010D");
		ACCENTS.put("c_hacek", "\u010D");
		ACCENTS.put("c_cedilla", "\u00E7");
		ACCENTS.put("c_hook", "\u0188");
		ACCENTS.put("c_stroke", "\u023C");
		ACCENTS.put("c_curl", "\u0255");
		ACCENTS.put("c_cedilla_acute", "\u1E09");

		ACCENTS.put("d_cedilla", "\u1E11");
		ACCENTS.put("d_caron", "\u010F");
		ACCENTS.put("d_hacek", "\u010F");
		ACCENTS.put("d_stroke", "\u0111");
		ACCENTS.put("d_bar", "\u0111");
		ACCENTS.put("d_topbar", "\u018B");
		ACCENTS.put("d_tail", "\u0256");
		ACCENTS.put("d_hook", "\u0257");

		ACCENTS.put("e_grave", "\u00E8");
		ACCENTS.put("e_acute", "\u00E9");
		ACCENTS.put("e_circumflex", "\u00EA");
		ACCENTS.put("e_umlaut", "\u00EB");
		ACCENTS.put("e_diaeresis", "\u00EB");
		ACCENTS.put("e_tilde", "\u1EBD");
		ACCENTS.put("e_caron", "\u011B");
		ACCENTS.put("e_hacek", "\u011B");
		ACCENTS.put("e_macron", "\u0113");
		ACCENTS.put("e_overline", "\u0113");
		ACCENTS.put("e_breve", "\u0115");
		ACCENTS.put("e_dot", "\u0117");
		ACCENTS.put("e_ogonek", "\u0119");
		ACCENTS.put("e_cedilla", "\u0229");
		ACCENTS.put("e_cedilla_grave", "\u1E1D");
		ACCENTS.put("e_double_grave", "\u0205");
		ACCENTS.put("e_inverted_breve", "\u0207");
		ACCENTS.put("e_stroke", "\u0247");

		ACCENTS.put("f_hook", "\u0192");
		
		ACCENTS.put("g_acute", "\u01F5");
		ACCENTS.put("g_circumflex", "\u011D");
		ACCENTS.put("g_breve", "\u011F");
		ACCENTS.put("g_dot", "\u0121");
		ACCENTS.put("g_caron", "\u01E7");
		ACCENTS.put("g_hacek", "\u01E7");
		ACCENTS.put("g_cedilla", "\u0123");
		ACCENTS.put("g_macron", "\u1E21");
		ACCENTS.put("g_overline", "\u1E21");
		ACCENTS.put("g_hook", "\u0260");

		ACCENTS.put("h_circumflex", "\u0125");
		ACCENTS.put("h_cedilla", "\u1E29");
		ACCENTS.put("h_stroke", "\u0127");
		ACCENTS.put("h_bar", "\u0127");
		ACCENTS.put("h_caron", "\u021F");
		ACCENTS.put("h_hacek", "\u021F");
		ACCENTS.put("h_hook", "\u0266");
		
		ACCENTS.put("i_grave", "\u00EC");
		ACCENTS.put("i_acute", "\u00ED");
		ACCENTS.put("i_circumflex", "\u00EE");
		ACCENTS.put("i_umlaut", "\u00EF");
		ACCENTS.put("i_diaeresis", "\u00EF");
		ACCENTS.put("i_tilde", "\u0129");
		ACCENTS.put("i_caron", "\u01D0");
		ACCENTS.put("i_hacek", "\u01D0");
		ACCENTS.put("i_macron", "\u012B");
		ACCENTS.put("i_overline", "\u012B");
		ACCENTS.put("i_breve", "\u012D");
		ACCENTS.put("i_ogonek", "\u012F");
		ACCENTS.put("i_double_grave", "\u0209");
		ACCENTS.put("i_inverted_breve", "\u020B");
		ACCENTS.put("i_stroke", "\u0268");

		ACCENTS.put("j_circumflex", "\u0135");
		ACCENTS.put("j_caron", "\u01F0");
		ACCENTS.put("j_hacek", "\u01F0");
		ACCENTS.put("j_stroke", "\u0249");
		ACCENTS.put("j_crossed-tail", "\u029D");
		
		ACCENTS.put("k_acute", "\u1E31");
		ACCENTS.put("k_caron", "\u01E9");
		ACCENTS.put("k_hacek", "\u01E9");
		ACCENTS.put("k_cedilla", "\u0137");
		ACCENTS.put("k_hook", "\u0199");
		
		ACCENTS.put("l_acute", "\u013A");
		ACCENTS.put("l_cedilla", "\u013C");
		ACCENTS.put("l_caron", "\u013E");
		ACCENTS.put("l_hacek", "\u013E");
		ACCENTS.put("l_stroke", "\u0141");
		ACCENTS.put("l_slash", "\u0142");

		ACCENTS.put("m_acute", "\u1E3F");
		ACCENTS.put("m_dot", "\u1E41");
		ACCENTS.put("m_dot_below", "\u1E42");
		ACCENTS.put("m_hook", "\u0271");

		ACCENTS.put("n_grave", "\u01F9");
		ACCENTS.put("n_acute", "\u0144");
		ACCENTS.put("n_tilde", "\u00F1");
		ACCENTS.put("n_cedilla", "\u0146");
		ACCENTS.put("n_caron", "\u0148");
		ACCENTS.put("n_hacek", "\u0148");

		ACCENTS.put("o_grave", "\u00F2");
		ACCENTS.put("o_acute", "\u00F3");
		ACCENTS.put("o_circumflex", "\u00F4");
		ACCENTS.put("o_umlaut", "\u00F6");
		ACCENTS.put("o_diaeresis", "\u00F6");
		ACCENTS.put("o_stroke", "\u00F8");
		ACCENTS.put("o_tilde", "\u00F5");
		ACCENTS.put("o_caron", "\u01D2");
		ACCENTS.put("o_hacek", "\u01D2");
		ACCENTS.put("o_macron", "\u014D");
		ACCENTS.put("o_overline", "\u014D");
		ACCENTS.put("o_breve", "\u014F");
		ACCENTS.put("o_ogonek", "\u01EB");
		ACCENTS.put("o_double_accute", "\u0151");

		ACCENTS.put("p_acute", "\u1E55");
		ACCENTS.put("p_hook", "\u01A5");
		
		ACCENTS.put("r_acute", "\u0155");
		ACCENTS.put("r_cedilla", "\u0157");
		ACCENTS.put("r_caron", "\u0159");
		ACCENTS.put("r_hacek", "\u0159");
		ACCENTS.put("r_stroke", "\u024D");
		
		ACCENTS.put("s_acute", "\u015B");
		ACCENTS.put("s_circumflex", "\u015D");
		ACCENTS.put("s_cedilla", "\u015F");
		ACCENTS.put("s_caron", "\u0161");
		ACCENTS.put("s_hacek", "\u0161");

		ACCENTS.put("t_cedilla", "\u0163");
		ACCENTS.put("t_caron", "\u0165");
		ACCENTS.put("t_hacek", "\u0165");
		ACCENTS.put("t_umlaut", "\u1E97");
		ACCENTS.put("t_dieresis", "\u1E97");
		ACCENTS.put("t_stroke", "\u0167");
		ACCENTS.put("t_bar", "\u0167");
		ACCENTS.put("t_hook", "\u01AD");

		ACCENTS.put("u_acute", "\u00FA");
		ACCENTS.put("u_circumflex", "\u00FB");
		ACCENTS.put("u_umlaut", "\u00FC");
		ACCENTS.put("u_diaeresis", "\u00FC");
		ACCENTS.put("u_grave", "\u00F9");
		ACCENTS.put("u_ogonek", "\u0173");
		ACCENTS.put("u_horn", "\u01B0");
		ACCENTS.put("u_caron", "\u01D4");
		ACCENTS.put("u_hacek", "\u01D4");
		ACCENTS.put("u_tilde", "\u0169");
		ACCENTS.put("u_macron", "\u016B");
		ACCENTS.put("u_overline", "\u016B");
		ACCENTS.put("u_breve", "\u016D");
		ACCENTS.put("u_ring", "\u016F");
		ACCENTS.put("u_double_accute", "\u0171");
		ACCENTS.put("u_diaeresis_macron", "\u01D6");
		ACCENTS.put("u_diaeresis_acute", "\u01D8");
		ACCENTS.put("u_diaeresis_caron", "\u01DA");
		ACCENTS.put("u_diaeresis_hacek", "\u01DA");
		ACCENTS.put("u_diaeresis_grave", "\u01DC");
		ACCENTS.put("u_double_grave", "\u0215");
		ACCENTS.put("u_inverted_breve", "\u0217");
		
		ACCENTS.put("v_hook", "\u028B");
		ACCENTS.put("v_tilde", "\u1E7D");
		ACCENTS.put("v_dot_below", "\u1E7F");

		ACCENTS.put("w_circumflex", "\u0175");
		ACCENTS.put("w_grave", "\u1E81");
		ACCENTS.put("w_acute", "\u1E83");
		ACCENTS.put("w_umlaut", "\u1E85");
		ACCENTS.put("w_diaeresis", "\u1E85");
		ACCENTS.put("w_dot", "\u1E87");
		ACCENTS.put("w_dot_below", "\u1E89");
		ACCENTS.put("w_ring", "\u1E98");
		
		ACCENTS.put("x_partial_hook", "\u1D8D");
		ACCENTS.put("x_dot", "\u1E8B");
		ACCENTS.put("x_umlaut", "\u1E8D");
		ACCENTS.put("x_diaeresis", "\u1E8D");

		ACCENTS.put("y_acute", "\u00FD");
		ACCENTS.put("y_umlaut", "\u00FF");
		ACCENTS.put("y_dieresis", "\u00FF");
		ACCENTS.put("y_circumflex", "\u0177");
		ACCENTS.put("y_hook", "\u01B4");
		ACCENTS.put("y_stroke", "\u024F");
		ACCENTS.put("y_grave", "\u1EF3");
		ACCENTS.put("y_tilde", "\u1EF9");
		ACCENTS.put("y_macron", "\u0233");
		ACCENTS.put("y_overline", "\u0233");
		ACCENTS.put("y_dot", "\u1EF7");
		ACCENTS.put("y_dot_below", "\u1EF5");
		ACCENTS.put("y_loop", "\u1EFF");
		ACCENTS.put("y_double_accute", "\u04F3");

		ACCENTS.put("z_acute", "\u017A");
		ACCENTS.put("z_dot", "\u017C");
		ACCENTS.put("z_dot_below", "\u1E93");
		ACCENTS.put("z_circumflex", "\u1E91");
		ACCENTS.put("z_caron", "\u017E");
		ACCENTS.put("z_hacek", "\u017E");
		ACCENTS.put("z_stroke", "\u01B6");
		ACCENTS.put("z_bar", "\u01B6");
		ACCENTS.put("z_hook", "\u0225");
		ACCENTS.put("z_curl", "\u0291");
		ACCENTS.put("z_line_below", "\u1E95");
		ACCENTS.put("z_middle_tilde", "\u1D76");

		ACCENTS.put("alpha_psili", "\u1F00");
		ACCENTS.put("alpha_tonos", "\u03AC");
		ACCENTS.put("epsilon_varia", "\u1F72");
		ACCENTS.put("epsilon_tilde", "\u1F72");
		ACCENTS.put("gamma_tilde", "\u03B3"); // US6549801B1
		ACCENTS.put("sigma_circumflex", "\u03C3");
		ACCENTS.put("theta_circumflex", "\u03B8"); // US7221152B2
		ACCENTS.put("theta_tilde", "\u03B8"); // US7221152B2
		ACCENTS.put("lambda_tilde", "\u03BB"); // US6888809B1
		ACCENTS.put("mu_tilde", "\u03BC");
		ACCENTS.put("psi_circumflex", "\u03C8"); // US7221152B2	
		ACCENTS.put("phi_tilde", "\u03C6"); // US6192305B1
		ACCENTS.put("omega_circumflex", "\u03C9"); // US7221152B2
		ACCENTS.put("omega_tilde", "\u03C9"); // US7825543B2
		ACCENTS.put("omega_persipomeni", "\u1FF6");
		ACCENTS.put("omega_ypogegrammeni", "\u1FF3");
		ACCENTS.put("omega_dasia", "\u1F61");
		ACCENTS.put("omega_varia", "\u1F7C");
		ACCENTS.put("omega_oxia", "\u1F7D");
		ACCENTS.put("tau_circumflex", "\u03C4");
	}

	/*
	 * Map Greek Letters
	 */
	private static final HashMap<Character, String> GREEK_LETTERS = new HashMap<Character, String>();
	static {
		GREEK_LETTERS.put('\u03B1', "alpha");
		GREEK_LETTERS.put('\u03B2', "beta");
		GREEK_LETTERS.put('\u03B3', "gamma");
		GREEK_LETTERS.put('\u03B4', "delta");
		GREEK_LETTERS.put('\u03B5', "epsilon");
		GREEK_LETTERS.put('\u03B6', "zeta");
		GREEK_LETTERS.put('\u03B7', "eta");
		GREEK_LETTERS.put('\u03B8', "theta");
		GREEK_LETTERS.put('\u03B9', "iota");
		GREEK_LETTERS.put('\u03BA', "kappa");
		GREEK_LETTERS.put('\u03BB', "lambda");
		GREEK_LETTERS.put('\u03BC', "mu");
		GREEK_LETTERS.put('\u03BD', "nu");
		GREEK_LETTERS.put('\u03BE', "xi");
		GREEK_LETTERS.put('\u03BF', "omicron");
		GREEK_LETTERS.put('\u03C0', "pi");
		GREEK_LETTERS.put('\u03C1', "rho");
		GREEK_LETTERS.put('\u03C2', "sigmaf");
		GREEK_LETTERS.put('\u03C3', "sigma");
		GREEK_LETTERS.put('\u03C4', "tau");
		GREEK_LETTERS.put('\u03C5', "upsilon");
		GREEK_LETTERS.put('\u03C6', "phi");
		GREEK_LETTERS.put('\u03C7', "chi");
		GREEK_LETTERS.put('\u03C8', "psi");
		GREEK_LETTERS.put('\u03C9', "omega");
	}

	public String lookup(String letter, String... accent) {
		return lookup(letter.charAt(0), accent);
	}

	public String lookup(Character letter, String... accent) {
		Arrays.sort(accent);
		String accentsStr = String.join("_", accent);
		String key = Character.toLowerCase(letter) + "_" + accentsStr.toLowerCase();

		if ( GREEK_LETTERS.containsKey(letter)) {
			key =  GREEK_LETTERS.get(letter) + "_" + accentsStr.toLowerCase();
		}
		
		//System.out.println("Lookup Key: " + key);
		
		String unicode = null;
		if (ACCENTS.containsKey(key)) {
			unicode = ACCENTS.get(key);
			if (Character.isUpperCase(letter)) {
				unicode = unicode.toUpperCase();
			}
		} else {
			if ( ACCENT_COMBINING.containsKey(accentsStr) ) {
				unicode = ACCENT_COMBINING.get(accentsStr).toString() + letter;
			}
		}

		return unicode;
	}

	public String brace2Unicode(final String orig) {
		StringBuffer stb = new StringBuffer();
		Matcher matcher = ACCENT_PATTERN.matcher(orig);
		while (matcher.find()) {
			String accent = matcher.group(1);
			String letter = matcher.group(2);

			if (letter == null) {
				// null is returned for captured empty space.
				letter = " ";
			}

			String unicode = lookup(letter, accent);
			if (unicode != null) {
				matcher.appendReplacement(stb, unicode.toString());
			}
		}
		matcher.appendTail(stb);
		return stb.toString();
	}

}
