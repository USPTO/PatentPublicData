package gov.uspto.patent.doc.xml;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.icu.text.Normalizer2;

/**
 * Brace Codes
 *
 *<p>Brace Codes are used to escape diacritic/accent characters in about 5% of Patent SGML, PAP and XML documents.
 *Brace Codes are used similarly to Dot Codes used within the Patent Greenbook format.<p>
 *
 *<p>Patent XML is in UTF-8, some characters can only be encoded in UTF-16 (surrogate pairs), 
 *or no equivalent exists. Either way, most accents even when a UTF-8 equivalent exist are written as a brace code.</p>
 *
 *<p>Within patents diacritic/accents are used within proper names, such as inventor names, inline
 *mathematical equations, as well as titles of foreign Non-Patent Literature (NPL). They may also
 *appear as descriptive words in the text body.</p>
 *
 *<p> Possible solutions: <br/>
 * -- Within proper names, drop the accents. (often dropped already when entered by law firms)<br/>
 * -- Covert only to UTF-8, don't create UTF-16 surrogate pairs; some brace codes, mostly within in-line math will still exist.<br/>
 *</p>
 *
 * <p>
 * Not Supported Yet: <br/>
 * 1) Sequence of characters: {square root over (n+1)} ; unicode does not support spanning multiple characters<br/>
 * 2) Nested accents: {acute over ({hacek over (a)}s)} <br/>
 * </p>
 *
 * @see gov.uspto.patent.doc.greenbook.DotCodes
 * @see https://emw3.com/unicode-accents.html
 * @see https://www.unicode.org/charts/PDF/U0300.pdf
 * @see https://unicode.org/reports/tr25/
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class BraceCode {

	private static final Logger LOGGER = LoggerFactory.getLogger(BraceCode.class);

	private static final Pattern ACCENT_PATTERN = Pattern.compile(
			"\\{([A-z]+[A-z ]*)\\((?:\\s|\\s?([\\p{L}\\p{N}\\p{Sm}\\s]+)?)\\s?\\)\\}", Pattern.UNICODE_CHARACTER_CLASS);
	// \\p{InCombiningDiacriticalMarks}\\p{IsLm}\\p{IsSk}

	/*
	 * Unicode UTF-16 Combining Diacritical Marks Table
	 * 
	 * - Combing marks are only used if a unicode does not exist. - Note for Search
	 * Indexes as well as RDMS, they are mapped and converted to ASCII see Solr mapping-FoldToASCII.txt
	 * 
	 * Unicode blocks: Combining Diacritical Marks and Combining Diacritical Marks
	 * for Symbols
	 */
	private static final Map<String, Character> UTF16_COMBINING = new HashMap<String, Character>();
	static {
		UTF16_COMBINING.put("grave", '\u0300');
		UTF16_COMBINING.put("acute", '\u0301');
		UTF16_COMBINING.put("circumflex", '\u0302');
		UTF16_COMBINING.put("tilde", '\u0303');
		UTF16_COMBINING.put("macron", '\u0304');
		UTF16_COMBINING.put("overline", '\u0305');
		UTF16_COMBINING.put("overscore", '\u0305');
		UTF16_COMBINING.put("breve", '\u0306');
		UTF16_COMBINING.put("dot", '\u0307');
		UTF16_COMBINING.put("diaeresis", '\u0308');
		UTF16_COMBINING.put("umlaut", '\u0308');
		UTF16_COMBINING.put("hook", '\u0309');
		UTF16_COMBINING.put("ring", '\u030A');
		UTF16_COMBINING.put("double_acute", '\u030B');
		UTF16_COMBINING.put("caron", '\u030C');
		UTF16_COMBINING.put("hacek", '\u030C');
		UTF16_COMBINING.put("cedilla", '\u0327');
		UTF16_COMBINING.put("ogonek", '\u0328');
		UTF16_COMBINING.put("grave_under", '\u0316');
		UTF16_COMBINING.put("acute_under", '\u0317');
		UTF16_COMBINING.put("circumflex_under", '\u032D');
		UTF16_COMBINING.put("tilde_under", '\u0330');
		UTF16_COMBINING.put("macron_under", '\u0331');
		UTF16_COMBINING.put("dot_under", '\u0323');
		UTF16_COMBINING.put("diaeresis_under", '\u0324');
		UTF16_COMBINING.put("umlaut_under", '\u0324');
		UTF16_COMBINING.put("ring_under", '\u0325');
		UTF16_COMBINING.put("comma_under", '\u0326');
		UTF16_COMBINING.put("caron_under", '\u032C');
		UTF16_COMBINING.put("hacek_under", '\u032C');
		UTF16_COMBINING.put("circumflex_under", '\u032D');
		UTF16_COMBINING.put("breve_under", '\u032E');
		UTF16_COMBINING.put("asterisk_under", '\u0359');
		UTF16_COMBINING.put("circle", '\u20DD');
		UTF16_COMBINING.put("underscore", '\u0332');
		UTF16_COMBINING.put("right_arrow", '\u20D7');
		UTF16_COMBINING.put("right_arrow_under", '\u20EF');
		UTF16_COMBINING.put("left_arrow", '\u20D6');
		UTF16_COMBINING.put("left_arrow_under", '\u20EE');
		UTF16_COMBINING.put("double_overline", '\u0333');
		UTF16_COMBINING.put("square_root", '\u221A');
		UTF16_COMBINING.put("cube_root", '\u221B');
		UTF16_COMBINING.put("fourth_root", '\u221C');
	}

	/*
	 * Unicode UTF-8 Accent Lookup Table
	 */
	private static final Map<String, Character> UTF8_ACCENTS = new HashMap<String, Character>();
	static {
		// \u00A0
		UTF8_ACCENTS.put(" _umlaut", '\u00A8');
		UTF8_ACCENTS.put(" _diaeresis", '\u00A8');
		UTF8_ACCENTS.put(" _tilde", '\u223C'); // ~
		UTF8_ACCENTS.put(" _grave", '\u0060');
		// Spacing Modifier Letters
		UTF8_ACCENTS.put(" _circumflex", '\u02C6'); // \u005E
		UTF8_ACCENTS.put(" _caron", '\u02C7');
		UTF8_ACCENTS.put(" _hacek", '\u02C7');
		UTF8_ACCENTS.put(" _acute", '\u02CA'); // \u00B4
		UTF8_ACCENTS.put(" _grave", '\u02CB');
		UTF8_ACCENTS.put(" _macron", '\u02C9');
		UTF8_ACCENTS.put(" _breve", '\u02D8');
		UTF8_ACCENTS.put(" _dot", '\u02D9');
		UTF8_ACCENTS.put(" _ring", '\u02DA');
		UTF8_ACCENTS.put(" _ogonek", '\u02DB');
		UTF8_ACCENTS.put(" _grave", '\u02CB');

		UTF8_ACCENTS.put("a_grave", '\u00E0');
		UTF8_ACCENTS.put("a_acute", '\u00E1');
		UTF8_ACCENTS.put("a_circumflex", '\u00E2');
		UTF8_ACCENTS.put("a_tilde", '\u00E3');
		UTF8_ACCENTS.put("a_umlaut", '\u00E4');
		UTF8_ACCENTS.put("a_diaeresis", '\u00E4');
		UTF8_ACCENTS.put("a_ring", '\u00E5');
		UTF8_ACCENTS.put("a_caron", '\u01CE');
		UTF8_ACCENTS.put("a_hacek", '\u01CE');
		UTF8_ACCENTS.put("a_macron", '\u0101');
		UTF8_ACCENTS.put("a_overline", '\u0101');
		UTF8_ACCENTS.put("a_breve", '\u0103');
		UTF8_ACCENTS.put("a_ogonek", '\u0105');
		UTF8_ACCENTS.put("a_double_grave", '\u0201');
		UTF8_ACCENTS.put("a_circle", '\u24D0');

		UTF8_ACCENTS.put("b_circle", '\u24D1');

		UTF8_ACCENTS.put("c_acute", '\u0107');
		UTF8_ACCENTS.put("c_circumflex", '\u0109');
		UTF8_ACCENTS.put("c_dot", '\u010B');
		UTF8_ACCENTS.put("c_caron", '\u010D');
		UTF8_ACCENTS.put("c_hacek", '\u010D');
		UTF8_ACCENTS.put("c_cedilla", '\u00E7');
		UTF8_ACCENTS.put("c_hook", '\u0188');
		UTF8_ACCENTS.put("c_stroke", '\u023C');
		UTF8_ACCENTS.put("c_curl", '\u0255');
		UTF8_ACCENTS.put("c_cedilla_acute", '\u1E09');
		UTF8_ACCENTS.put("c_circle", '\u24D2');
		
		UTF8_ACCENTS.put("d_cedilla", '\u1E11');
		UTF8_ACCENTS.put("d_caron", '\u010F');
		UTF8_ACCENTS.put("d_hacek", '\u010F');
		UTF8_ACCENTS.put("d_stroke", '\u0111');
		UTF8_ACCENTS.put("d_bar", '\u0111');
		UTF8_ACCENTS.put("d_topbar", '\u018B');
		UTF8_ACCENTS.put("d_tail", '\u0256');
		UTF8_ACCENTS.put("d_hook", '\u0257');
		UTF8_ACCENTS.put("d_circle", '\u24D3');

		
		UTF8_ACCENTS.put("e_grave", '\u00E8');
		UTF8_ACCENTS.put("e_acute", '\u00E9');
		UTF8_ACCENTS.put("e_circumflex", '\u00EA');
		UTF8_ACCENTS.put("e_umlaut", '\u00EB');
		UTF8_ACCENTS.put("e_diaeresis", '\u00EB');
		UTF8_ACCENTS.put("e_tilde", '\u1EBD');
		UTF8_ACCENTS.put("e_caron", '\u011B');
		UTF8_ACCENTS.put("e_hacek", '\u011B');
		UTF8_ACCENTS.put("e_macron", '\u0113');
		UTF8_ACCENTS.put("e_overline", '\u0113');
		UTF8_ACCENTS.put("e_breve", '\u0115');
		UTF8_ACCENTS.put("e_dot", '\u0117');
		UTF8_ACCENTS.put("e_ogonek", '\u0119');
		UTF8_ACCENTS.put("e_cedilla", '\u0229');
		UTF8_ACCENTS.put("e_cedilla_grave", '\u1E1D');
		UTF8_ACCENTS.put("e_double_grave", '\u0205');
		UTF8_ACCENTS.put("e_inverted_breve", '\u0207');
		UTF8_ACCENTS.put("e_stroke", '\u0247');
		UTF8_ACCENTS.put("d_circle", '\u24D4');
		
		UTF8_ACCENTS.put("f_hook", '\u0192');
		UTF8_ACCENTS.put("f_circle", '\u24D5');
		
		UTF8_ACCENTS.put("g_acute", '\u01F5');
		UTF8_ACCENTS.put("g_circumflex", '\u011D');
		UTF8_ACCENTS.put("g_breve", '\u011F');
		UTF8_ACCENTS.put("g_dot", '\u0121');
		UTF8_ACCENTS.put("g_caron", '\u01E7');
		UTF8_ACCENTS.put("g_hacek", '\u01E7');
		UTF8_ACCENTS.put("g_cedilla", '\u0123');
		UTF8_ACCENTS.put("g_macron", '\u1E21');
		UTF8_ACCENTS.put("g_overline", '\u1E21');
		UTF8_ACCENTS.put("g_hook", '\u0260');
		UTF8_ACCENTS.put("g_circle", '\u24D6');

		UTF8_ACCENTS.put("h_circumflex", '\u0125');
		UTF8_ACCENTS.put("h_cedilla", '\u1E29');
		UTF8_ACCENTS.put("h_stroke", '\u0127');
		UTF8_ACCENTS.put("h_bar", '\u0127');
		UTF8_ACCENTS.put("h_caron", '\u021F');
		UTF8_ACCENTS.put("h_hacek", '\u021F');
		UTF8_ACCENTS.put("h_hook", '\u0266');
		UTF8_ACCENTS.put("h_circle", '\u24D7');
		
		UTF8_ACCENTS.put("i_grave", '\u00EC');
		UTF8_ACCENTS.put("i_acute", '\u00ED');
		UTF8_ACCENTS.put("i_circumflex", '\u00EE');
		UTF8_ACCENTS.put("i_umlaut", '\u00EF');
		UTF8_ACCENTS.put("i_diaeresis", '\u00EF');
		UTF8_ACCENTS.put("i_tilde", '\u0129');
		UTF8_ACCENTS.put("i_caron", '\u01D0');
		UTF8_ACCENTS.put("i_hacek", '\u01D0');
		UTF8_ACCENTS.put("i_macron", '\u012B');
		UTF8_ACCENTS.put("i_overline", '\u012B');
		UTF8_ACCENTS.put("i_breve", '\u012D'); 
		UTF8_ACCENTS.put("i_ogonek", '\u012F');
		UTF8_ACCENTS.put("i_double_grave", '\u0209');
		UTF8_ACCENTS.put("i_inverted_breve", '\u020B');
		UTF8_ACCENTS.put("i_stroke", '\u0268');
		UTF8_ACCENTS.put("i_circle", '\u24D8');
		
		UTF8_ACCENTS.put("j_circumflex", '\u0135'); 
		UTF8_ACCENTS.put("j_caron", '\u01F0');
		UTF8_ACCENTS.put("j_hacek", '\u01F0'); 
		UTF8_ACCENTS.put("j_stroke", '\u0249');
		UTF8_ACCENTS.put("j_crossed-tail", '\u029D');
		UTF8_ACCENTS.put("j_circle", '\u24D9');

		UTF8_ACCENTS.put("k_acute", '\u1E31'); 
		UTF8_ACCENTS.put("k_caron", '\u01E9');
		UTF8_ACCENTS.put("k_hacek", '\u01E9'); 
		UTF8_ACCENTS.put("k_cedilla", '\u0137');
		UTF8_ACCENTS.put("k_hook", '\u0199');
		UTF8_ACCENTS.put("k_circle", '\u24DA');

		UTF8_ACCENTS.put("l_acute", '\u013A'); 
		UTF8_ACCENTS.put("l_cedilla", '\u013C');
		UTF8_ACCENTS.put("l_caron", '\u013E'); 
		UTF8_ACCENTS.put("l_hacek", '\u013E');
		UTF8_ACCENTS.put("l_stroke", '\u0141');
		UTF8_ACCENTS.put("l_slash", '\u0142');
		UTF8_ACCENTS.put("l_circle", '\u24DB');
		
		UTF8_ACCENTS.put("m_acute", '\u1E3F'); 
		UTF8_ACCENTS.put("m_dot", '\u1E41');
		UTF8_ACCENTS.put("m_dot_below", '\u1E42'); 
		UTF8_ACCENTS.put("m_hook", '\u0271');
		UTF8_ACCENTS.put("m_circle", '\u24DC');

		UTF8_ACCENTS.put("n_grave", '\u01F9'); 
		UTF8_ACCENTS.put("n_acute", '\u0144');
		UTF8_ACCENTS.put("n_tilde", '\u00F1'); 
		UTF8_ACCENTS.put("n_cedilla", '\u0146');
		UTF8_ACCENTS.put("n_caron", '\u0148'); 
		UTF8_ACCENTS.put("n_hacek", '\u0148');
		UTF8_ACCENTS.put("n_circle", '\u24DD');
		
		UTF8_ACCENTS.put("o_grave", '\u00F2'); 
		UTF8_ACCENTS.put("o_acute", '\u00F3');
		UTF8_ACCENTS.put("o_circumflex", '\u00F4'); 
		UTF8_ACCENTS.put("o_umlaut", '\u00F6');
		UTF8_ACCENTS.put("o_diaeresis", '\u00F6'); 
		UTF8_ACCENTS.put("o_tilde", '\u00F5');
		UTF8_ACCENTS.put("o_caron", '\u01D2'); 
		UTF8_ACCENTS.put("o_hacek", '\u01D2');
		UTF8_ACCENTS.put("o_macron", '\u014D'); 
		UTF8_ACCENTS.put("o_overline", '\u014D');
		UTF8_ACCENTS.put("o_breve", '\u014F'); 
		UTF8_ACCENTS.put("o_ogonek", '\u01EB');
		UTF8_ACCENTS.put("o_double_accute", '\u0151');
		UTF8_ACCENTS.put("o_stroke", '\u00F8');
		UTF8_ACCENTS.put("o_circle", '\u24DE');

		UTF8_ACCENTS.put("p_acute", '\u1E55');
		UTF8_ACCENTS.put("p_hook", '\u01A5');
		UTF8_ACCENTS.put("p_circle", '\u24DF');
		
		UTF8_ACCENTS.put("q_circle", '\u24E0');
		 
		UTF8_ACCENTS.put("r_acute", '\u0155'); 
		UTF8_ACCENTS.put("r_cedilla", '\u0157');
		UTF8_ACCENTS.put("r_caron", '\u0159'); 
		UTF8_ACCENTS.put("r_hacek", '\u0159');
		UTF8_ACCENTS.put("r_stroke", '\u024D');
		UTF8_ACCENTS.put("r_circle", '\u24E1');
		 
		UTF8_ACCENTS.put("s_acute", '\u015B'); 
		UTF8_ACCENTS.put("s_circumflex", '\u015D');
		UTF8_ACCENTS.put("s_cedilla", '\u015F'); 
		UTF8_ACCENTS.put("s_caron", '\u0161');
		UTF8_ACCENTS.put("s_hacek", '\u0161');
		UTF8_ACCENTS.put("s_circle", '\u24E2');

		UTF8_ACCENTS.put("t_cedilla", '\u0163'); 
		UTF8_ACCENTS.put("t_caron", '\u0165');
		UTF8_ACCENTS.put("t_hacek", '\u0165'); 
		UTF8_ACCENTS.put("t_umlaut", '\u1E97');
		UTF8_ACCENTS.put("t_dieresis", '\u1E97'); 
		UTF8_ACCENTS.put("t_hook", '\u01AD');
		UTF8_ACCENTS.put("t_stroke", '\u0167');
		UTF8_ACCENTS.put("t_bar", '\u0167');
		UTF8_ACCENTS.put("t_circle", '\u24E3');

		UTF8_ACCENTS.put("u_acute", '\u00FA'); 
		UTF8_ACCENTS.put("u_circumflex", '\u00FB');
		UTF8_ACCENTS.put("u_umlaut", '\u00FC'); 
		UTF8_ACCENTS.put("u_diaeresis", '\u00FC');
		UTF8_ACCENTS.put("u_grave", '\u00F9'); 
		UTF8_ACCENTS.put("u_ogonek", '\u0173');
		UTF8_ACCENTS.put("u_horn", '\u01B0'); 
		UTF8_ACCENTS.put("u_caron", '\u01D4');
		UTF8_ACCENTS.put("u_hacek", '\u01D4'); 
		UTF8_ACCENTS.put("u_tilde", '\u0169');
		UTF8_ACCENTS.put("u_macron", '\u016B');
		UTF8_ACCENTS.put("u_overline", '\u016B');
		UTF8_ACCENTS.put("u_breve", '\u016D');
		UTF8_ACCENTS.put("u_ring", '\u016F');
		UTF8_ACCENTS.put("u_double_accute", '\u0171');
		UTF8_ACCENTS.put("u_diaeresis_macron", '\u01D6');
		UTF8_ACCENTS.put("u_diaeresis_acute", '\u01D8');
		UTF8_ACCENTS.put("u_diaeresis_caron", '\u01DA');
		UTF8_ACCENTS.put("u_diaeresis_hacek", '\u01DA');
		UTF8_ACCENTS.put("u_diaeresis_grave", '\u01DC');
		UTF8_ACCENTS.put("u_double_grave", '\u0215');
		UTF8_ACCENTS.put("u_inverted_breve", '\u0217');
		UTF8_ACCENTS.put("u_circle", '\u24E4');

		UTF8_ACCENTS.put("v_hook", '\u028B');
		UTF8_ACCENTS.put("v_tilde", '\u1E7D');
		UTF8_ACCENTS.put("v_dot_below", '\u1E7F');
		UTF8_ACCENTS.put("v_circle", '\u24E5');
		 
		UTF8_ACCENTS.put("w_circumflex", '\u0175');
		UTF8_ACCENTS.put("w_grave", '\u1E81');
		UTF8_ACCENTS.put("w_acute", '\u1E83');
		UTF8_ACCENTS.put("w_umlaut", '\u1E85');
		UTF8_ACCENTS.put("w_diaeresis", '\u1E85');
		UTF8_ACCENTS.put("w_dot", '\u1E87');
		UTF8_ACCENTS.put("w_dot_below", '\u1E89');
		UTF8_ACCENTS.put("w_ring", '\u1E98');
		UTF8_ACCENTS.put("w_circle", '\u24E6');

		UTF8_ACCENTS.put("x_partial_hook", '\u1D8D');
		UTF8_ACCENTS.put("x_dot", '\u1E8B');
		UTF8_ACCENTS.put("x_umlaut", '\u1E8D');
		UTF8_ACCENTS.put("x_diaeresis", '\u1E8D');
		UTF8_ACCENTS.put("x_circle", '\u24E7'); // \u2BBE

		UTF8_ACCENTS.put("y_acute", '\u00FD'); 
		UTF8_ACCENTS.put("y_umlaut", '\u00FF');
		UTF8_ACCENTS.put("y_dieresis", '\u00FF'); 
		UTF8_ACCENTS.put("y_circumflex", '\u0177');
		UTF8_ACCENTS.put("y_hook", '\u01B4');
		UTF8_ACCENTS.put("y_grave", '\u1EF3');
		UTF8_ACCENTS.put("y_tilde", '\u1EF9');
		UTF8_ACCENTS.put("y_macron", '\u0233');
		UTF8_ACCENTS.put("y_overline", '\u0233');
		UTF8_ACCENTS.put("y_dot", '\u1EF7');
		UTF8_ACCENTS.put("y_dot_below", '\u1EF5');
		UTF8_ACCENTS.put("y_stroke", '\u024F');
		UTF8_ACCENTS.put("y_loop", '\u1EFF');
		UTF8_ACCENTS.put("y_double_accute", '\u04F3');
		UTF8_ACCENTS.put("y_circle", '\u24E8');
		
		UTF8_ACCENTS.put("z_acute", '\u017A');
		UTF8_ACCENTS.put("z_dot", '\u017C');
		UTF8_ACCENTS.put("z_dot_below", '\u1E93');
		UTF8_ACCENTS.put("z_circumflex", '\u1E91');
		UTF8_ACCENTS.put("z_caron", '\u017E');
		UTF8_ACCENTS.put("z_hacek", '\u017E');
		UTF8_ACCENTS.put("z_hook", '\u0225');
		UTF8_ACCENTS.put("z_stroke", '\u01B6');
		UTF8_ACCENTS.put("z_bar", '\u01B6');
		UTF8_ACCENTS.put("z_curl", '\u0291');
		UTF8_ACCENTS.put("z_line_below", '\u1E95');
		UTF8_ACCENTS.put("z_middle_tilde", '\u1D76');
		UTF8_ACCENTS.put("z_circle", '\u24E9');

		UTF8_ACCENTS.put("1_circle", '\u2460');
		UTF8_ACCENTS.put("2_circle", '\u2461');
		UTF8_ACCENTS.put("3_circle", '\u2462');
		UTF8_ACCENTS.put("4_circle", '\u2463');
		UTF8_ACCENTS.put("5_circle", '\u2464');
		UTF8_ACCENTS.put("6_circle", '\u2465');
		UTF8_ACCENTS.put("7_circle", '\u2466');
		UTF8_ACCENTS.put("8_circle", '\u2467');
		UTF8_ACCENTS.put("9_circle", '\u2468');
		//UTF8_ACCENTS.put("10_circle", '\u2469');

		// ******* MATH
		UTF8_ACCENTS.put("x_circle", '\u24E7'); // \u2BBE
		UTF8_ACCENTS.put("\u00D7_circle", '\u2297'); // circled times.
		UTF8_ACCENTS.put("<_underscore_under", '\u2264'); // less-than or equal.
		UTF8_ACCENTS.put(">_underscore_under", '\u2265'); // greater-than or equal.
		UTF8_ACCENTS.put("+_overscore", '\u2213');
		UTF8_ACCENTS.put("+_dot", '\u2214');
		UTF8_ACCENTS.put("+_circle", '\u2295');
		UTF8_ACCENTS.put("-_circle", '\u2295');
		UTF8_ACCENTS.put("=_circle", '\u229C'); // aprox equal.
		UTF8_ACCENTS.put("-_tilde", '\u2243');
		UTF8_ACCENTS.put("=_tilde", '\u2245');
		UTF8_ACCENTS.put("~_tilde", '\u2248');
		UTF8_ACCENTS.put("=_dot", '\u2250'); // approch limit.
		UTF8_ACCENTS.put("<_tilde", '\u2272'); // less-than or equivalent to.
		UTF8_ACCENTS.put(">_tilde", '\u2273'); // greater-than or equivalent to.

		// ******* GREEK
		UTF8_ACCENTS.put("epsilon_tilde", '\u1F72');
		UTF8_ACCENTS.put("gamma_tilde", '\u03B3'); // US6549801B1
		UTF8_ACCENTS.put("sigma_circumflex", '\u03C3');
		UTF8_ACCENTS.put("theta_circumflex", '\u03B8'); // US7221152B2
		UTF8_ACCENTS.put("theta_tilde", '\u03B8'); // US7221152B2
		UTF8_ACCENTS.put("lambda_tilde", '\u03BB'); // US6888809B1 
		UTF8_ACCENTS.put("mu_tilde", '\u03BC');
		UTF8_ACCENTS.put("psi_circumflex", '\u03C8'); // US7221152B2
		UTF8_ACCENTS.put("phi_tilde", '\u03C6'); // US6192305B1
		UTF8_ACCENTS.put("omega_circumflex", '\u03C9'); // US7221152B2
		UTF8_ACCENTS.put("omega_tilde", '\u03C9'); // US7825543B2
		UTF8_ACCENTS.put("tau_circumflex", '\u03C4');
		UTF8_ACCENTS.put("alpha_psili", '\u1F00');
		UTF8_ACCENTS.put("alpha_tonos", '\u03AC');
		UTF8_ACCENTS.put("epsilon_varia", '\u1F72');
		UTF8_ACCENTS.put("omega_persipomeni", '\u1FF6');
		UTF8_ACCENTS.put("omega_ypogegrammeni", '\u1FF3');
		UTF8_ACCENTS.put("omega_dasia", '\u1F61');
		UTF8_ACCENTS.put("omega_varia", '\u1F7C');
		UTF8_ACCENTS.put("omega_oxia", '\u1F7D');
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

	private final boolean wantUTF16;

	public BraceCode(boolean uft16) {
		this.wantUTF16 = uft16;
	}

	public Character getUnicode(String name) throws ParseException {
		if (UTF16_COMBINING.containsKey(name)) {
			return UTF16_COMBINING.get(name);
		} else {
			throw new ParseException("Unknown Accent: " + name, 0);
		}
	}

	public String lookup(String letter, String... accents) throws ParseException {
		if (letter.length() == 1) {
			return lookup(letter.charAt(0), accents);
		} else {
			return lookupSeq(letter, accents);
		}
	}

	public String lookupSeq(String letters, String... accents) throws ParseException {
		String unicode = combineUtf16(letters, accents);
		if (unicode != null) {
			unicode = Normalizer.normalize(unicode, Form.NFC);
		}
		return unicode;
	}

	public String mapUtf8(Character letter, String... accents) {
		Arrays.sort(accents);
		String accentsStr = String.join("_", accents);

		String key = Character.toLowerCase(letter) + "_" + accentsStr.toLowerCase();
		if (GREEK_LETTERS.containsKey(letter)) {
			key = GREEK_LETTERS.get(letter) + "_" + accentsStr.toLowerCase();
		}

		//LOGGER.info("map utf-8 Key: '{}'", key);

		Character unicode = null;
		if (UTF8_ACCENTS.containsKey(key)) {
			unicode = UTF8_ACCENTS.get(key);
			if (Character.isUpperCase(letter)) {
				unicode = Character.toUpperCase(unicode);
			}
		}

		String norm = null;
		if (unicode != null) {
			norm = Normalizer.normalize(unicode.toString(), Form.NFC);
		}

		return norm;
	}

	public String combineUtf16(Character letter, String... accents) throws ParseException {
		return combineUtf16(letter.toString(), accents);
	}

	
	public String combineUtf16(CharSequence letters, String... accents) throws ParseException {
		List<Character> before = new ArrayList<Character>();
		List<Character> after = new ArrayList<Character>();

		for (String ac : accents) {
			Character unicode = getUnicode(ac);
			if (ac.endsWith("_under")) {
				before.add(unicode);
			} else {
				before.add(unicode);
			}
		}

		StringBuilder accentUnicodeStb = new StringBuilder();

		for (Character unicode : before) {
			accentUnicodeStb.append(unicode);
		}

		accentUnicodeStb.append(letters);
		
		for (Character unicode : after) {
			accentUnicodeStb.append(unicode);
		}
		//accentUnicodeStb.append('\uFEFF'); // BOM
		//accentUnicodeStb.append('\u2060'); // Word-Joiner
		
		String sequence = accentUnicodeStb.toString();
		//sequence = Normalizer.normalize(sequence, Form.NFC);

		return (accentUnicodeStb.length() > letters.length()) ? sequence : null;
	}

	public String lookup(Character letter, String... accents) throws ParseException {

		String unicode = mapUtf8(letter, accents);
		if (unicode == null && wantUTF16) {
			unicode = combineUtf16(letter, accents);
		}

		//LOGGER.info("letter: '{}' accents: {} unicode: '{}' - {}", letter, accents, unicode);

		return unicode;
	}

	/**
	 * 
	 * @param orig
	 * @param charset Charset.UTF-8 or 
	 * @return
	 */
	public String covert2Unicode(final String orig) {
		StringBuffer stb = new StringBuffer();
		Matcher matcher = ACCENT_PATTERN.matcher(orig);
		while (matcher.find()) {
			String accent = matcher.group(1);
			String letter = matcher.group(2);

			accent = accent.trim();
			accent = accent.replaceAll("\\s", "_");
			accent = accent.replaceAll("_(over|around)$", ""); 	// Over and Around seem to be optionally used, so treat them as defaults.

			//LOGGER.trace("'{}' ---> {} with accent {}", orig, letter, accent);

			if (letter == null) {
				//LOGGER.info("LETTER NULL {}", orig);
				// null is returned for captured empty space.
				letter = "\u00A0";
			} else {
				letter = letter.trim();
			}

			String unicode;
			try {
				unicode = lookup(letter, accent);
				if (unicode != null) {
					matcher.appendReplacement(stb, unicode.toString());
				}
			} catch (ParseException e) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.warn("Unable to convert: {}", matcher.group(0), e);
				} else {
					LOGGER.warn("Unable to convert: '{}' {}", matcher.group(0), e.getMessage());
				}
			}
		}
		matcher.appendTail(stb);
		return stb.toString();
	}

	/**
	 * Strip Accents and Brace Codes, useful for normalizing names of people.
	 * 
	 * @param orig
	 * @return
	 */
	public String stripAccents(final String orig) {
		StringBuffer stb = new StringBuffer();
		Matcher matcher = ACCENT_PATTERN.matcher(orig);
		while (matcher.find()) {
			String accent = matcher.group(1);
			String letter = matcher.group(2);

			accent = accent.trim();
			if (accent.contains(" root")) { // (square|cube|fourth) root over 
				continue;
			}

			matcher.appendReplacement(stb, letter);
		}
		matcher.appendTail(stb);

	    return StringUtils.stripAccents(stb.toString());

	    //String norm = Normalizer.normalize(stb.toString(), Normalizer.Form.NFD);
	    //norm = norm.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
		//return norm;
	}
}
