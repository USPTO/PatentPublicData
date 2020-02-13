package gov.uspto.patent.doc.greenbook;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Greenbook Dot Codes
 * 
 * <p>
 * Dot codes are used for Special Characters, this class maps them to their
 * Unicode symbol.
 * </p>
 * 
 * <p>
 * Unicode was not around when Greenbook dot codes where introduced.
 * </p>
 * 
 * <p>
 * 
 * <pre>
 * Dotcode are terminated by:
 *   a space
 *   a comma (,)
 *   a period (.)
 *   a right parenthesis ()
 *   or another subscript .sub. 
 *   or superscript .sup.
 *</p>
 * </pre>
 *
 * @see https://www.w3.org/TR/REC-html40/sgml/entities.html
 * @see https://r12a.github.io/apps/conversion/
 * @see http://sites.psu.edu/symbolcodes/accents/math/mathchart
 *
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 * 
 */
public class DotCodes {

	private static final Pattern ENTITY_PATTERN = Pattern
			.compile("\\.(?:[0\\[\\]]|[^\\p{Z},;\\.)]{2,18})(?:\\.|(?=[\\p{Z},;)]))", Pattern.UNICODE_CHARACTER_CLASS);

	private static final Pattern SUBSUP_PATTERN = Pattern.compile(
			"\\.(su[bp]|s[bp]s[bp])\\.{1,2}([^\\p{Z},;\\.)]{1,10})(?:\\.|(?=[\\p{Z},;)]))",
			Pattern.UNICODE_CHARACTER_CLASS);

	// private static final Pattern FRACTION_PATTERN =
	// Pattern.compile("\\b[1-9]/[1-9][0-9]*\\b");

	public static String fraction2number(String fraction) {
		DecimalFormat df = new DecimalFormat("#.####");
		df.setRoundingMode(RoundingMode.CEILING);
		String[] parts = fraction.split("/");
		double fraction1 = (double) Integer.valueOf(parts[0]) / Integer.valueOf(parts[1]);
		// (double)Math.round(value * 100000d) / 100000d
		// return fraction1;
		return df.format(fraction1);
	}

	private static final HashMap<String, Character> NAME_TO_UNICODE = new HashMap<String, Character>();
	static {
		// Trademark and Copyright
		NAME_TO_UNICODE.put(".SM.", '\u2120'); // service mark
		NAME_TO_UNICODE.put(".RTM.", '\u00AE'); // registered trademark
		NAME_TO_UNICODE.put(".COPYRG.", '\u00A9'); // copyright

		// Greek Letters [ unicode block Greek and Coptic: U+0370 to U+03FF ]
		NAME_TO_UNICODE.put(".ALPHA.", '\u0391'); // greek letter alpha upper case
		NAME_TO_UNICODE.put(".BETA.", '\u0392'); // greek letter beta upper case
		NAME_TO_UNICODE.put(".EPSILON.", '\u0395'); // greek letter epsilon upper case
		NAME_TO_UNICODE.put(".ZETA.", '\u0396'); // greek letter zeta upper case
		NAME_TO_UNICODE.put(".ETA.", '\u0397'); // greek letter eta upper case
		NAME_TO_UNICODE.put(".IOTA.", '\u0399'); // greek letter iota upper case
		NAME_TO_UNICODE.put(".KAPPA.", '\u039A'); // greek letter kappa upper case
		NAME_TO_UNICODE.put(".MU.", '\u039C'); // greek letter mu upper case used in Statistics
		NAME_TO_UNICODE.put(".NU.", '\u039D'); // greek nu upper case
		NAME_TO_UNICODE.put(".OMICRON.", '\u039F'); // greek omicron upper case
		NAME_TO_UNICODE.put(".RHO.", '\u03A1'); // greek rho upper case
		NAME_TO_UNICODE.put(".TAU.", '\u03A4'); // greek tau upper case symbol
		NAME_TO_UNICODE.put(".CHI.", '\u03A7'); // greek letter chi upper case,used in Statistics
		NAME_TO_UNICODE.put(".iota.", '\u03B9'); // greek letter iota lower case
		NAME_TO_UNICODE.put(".lambda.", '\u03BB'); // greek letter lamba lower case

		// Mathematical Operators [ Unicode Block U+2200 to U+22FF ]
		NAME_TO_UNICODE.put(".increment.", '\u2206'); // increment
		NAME_TO_UNICODE.put(".sqroot.", '\u221A'); // SQUARE ROOT
		NAME_TO_UNICODE.put(".cuberoot.", '\u221B'); // cube root
		NAME_TO_UNICODE.put(".fourthroot.", '\u221C'); // fourth root
		NAME_TO_UNICODE.put(".4th root.", '\u221C'); // fourth root
		NAME_TO_UNICODE.put(".perspectiveto.", '\u2243'); // perspective to (asymptotically equal to)
		NAME_TO_UNICODE.put(".congruent.", '\u2245'); // congruent (approximately equal to)
		NAME_TO_UNICODE.put(".approxeq.", '\u2248'); // approximately equal to
		NAME_TO_UNICODE.put(".apprch.", '\u2250'); // approaches the limit
		NAME_TO_UNICODE.put(".notident.", '\u2262'); // not identical
		NAME_TO_UNICODE.put(".lmtoreq.", '\u2264'); // less-than or equal to
		NAME_TO_UNICODE.put(".perp.", '\u22a5'); // perpendicular (up tack)
		NAME_TO_UNICODE.put(".lmorsim.", '\u2A85'); // less-than or approximate
		NAME_TO_UNICODE.put(".vertline.", '\u007C'); // vertical line
		NAME_TO_UNICODE.put(".times.", '\u00D7'); // multiplication sign
		NAME_TO_UNICODE.put(".div.", '\u00F7'); // division sign
		NAME_TO_UNICODE.put(".not <.", '\u226E'); // not less-than
		NAME_TO_UNICODE.put(".not >.", '\u226F'); // not greater-than
		NAME_TO_UNICODE.put(".multidot.", '\u8901'); // multiplication dot operator

		// Measurement
		NAME_TO_UNICODE.put(".ang.", '\u00E5'); // Angstrom lower case
		NAME_TO_UNICODE.put(".degree.", '\u00B0'); // degree sign
		NAME_TO_UNICODE.put(".cent.", '\u00A2'); // cent sign
		NAME_TO_UNICODE.put(".permill.", '\u2030'); // per mille symbol (salinity)

		// Sex
		NAME_TO_UNICODE.put(".female.", '\u2640'); // female symbol
		NAME_TO_UNICODE.put(".male.", '\u2642'); // male symbol

		// Music
		NAME_TO_UNICODE.put(".music-flat.", '\u266D'); // music flat
		NAME_TO_UNICODE.put(".music-natrual.", '\u266e'); // music natural
		NAME_TO_UNICODE.put(".music-sharp.", '\u266F'); // music sharp

		// Chemical
		NAME_TO_UNICODE.put(".guadbond.", '\u2243'); // guad bond
		NAME_TO_UNICODE.put(".tbd.", '\u2261'); // identical (equivalent) (triple bond)
		NAME_TO_UNICODE.put(".reveaction.", '\u21cc'); // reversible reaction (equilibrium)
		//NAME_TO_UNICODE.put(".function.", '\u0192'); // function symbol (small letter f with hook)

		// Geometric Shapes [ unicode block U+25A0 to U+25FF ]
		NAME_TO_UNICODE.put(".circle.", '\u25ef'); // large circle
		NAME_TO_UNICODE.put(".dottedcircle.", '\u25cc'); // dotted circle
		NAME_TO_UNICODE.put(".lhalfcircle.", '\u25d0'); // left half of circle black
		NAME_TO_UNICODE.put(".rhalfcircle.", '\u25d1'); // right half of circle black
		NAME_TO_UNICODE.put(".solthalfcircle.", '\u25d3'); // top half of circle black
		NAME_TO_UNICODE.put(".solbhalfcircle.", '\u25d2'); // bottom half of circle black

		NAME_TO_UNICODE.put(".THorizBrace.", '\uFE37'); // Top horizonal brace - vertical left curly bracket
		NAME_TO_UNICODE.put(".BHorizBrace.", '\uFE38'); // Bottom horizonal brace - vertical right curly bracket
		NAME_TO_UNICODE.put(".prime.", '\u2032'); // prime
		NAME_TO_UNICODE.put(".dblprime.", '\u2033'); // double prime

		// NAME_TO_UNICODE.put(".dottlhalfcircle.", ""); // dotted left half circle
		// NAME_TO_UNICODE.put(".dottrhalfcircle.", ""); // dotted right half circle
		// NAME_TO_UNICODE.put(".dottbtalfcircle.", ""); // dotted top half circle
		// NAME_TO_UNICODE.put(".dottbhalfcircle.", ""); // dotted bottom half circle
		NAME_TO_UNICODE.put(".dblquote.", '"');
		NAME_TO_UNICODE.put(".En.", '\u2025'); // two dot leader


		/*
		 *  Taken directly from CSS Database Convert Characters Configuration
		 */
		NAME_TO_UNICODE.put(".dagger.", '\u2020');
		NAME_TO_UNICODE.put(".dagger-dbl.", '\u2021');
		NAME_TO_UNICODE.put(".cndot.", '\u2022'); // solid dot - bullet symbol
		NAME_TO_UNICODE.put(".Salinity.", '\u2030');
		NAME_TO_UNICODE.put(".asterisk-pseud.", '\u203b');
		NAME_TO_UNICODE.put(".function.", '\u2061');
		NAME_TO_UNICODE.put(".times.", '\u2062');
		NAME_TO_UNICODE.put(".TM.", '\u2122');
		NAME_TO_UNICODE.put(".ANG.", '\u212b');
		NAME_TO_UNICODE.put(".rarw.", '\u2190');
		NAME_TO_UNICODE.put(".uparw.", '\u2191');
		NAME_TO_UNICODE.put(".fwdarw.", '\u2192');
		NAME_TO_UNICODE.put(".dwnarw.", '\u2193');
		NAME_TO_UNICODE.put(".revreaction.", '\u21c4');
		//NAME_TO_UNICODE.put(".revreaction.", '\u21d4');
		NAME_TO_UNICODE.put(".A-inverted.", '\u2200');
		NAME_TO_UNICODE.put(".differential.", '\u2202');
		NAME_TO_UNICODE.put(".E-backward.", '\u2203');
		NAME_TO_UNICODE.put(".0.", '\u2205');
		NAME_TO_UNICODE.put(".gradient.", '\u2207');
		NAME_TO_UNICODE.put(".di-elect cons.", '\u2208');		
		NAME_TO_UNICODE.put(".-+.", '\u2213');
		NAME_TO_UNICODE.put(".smallcircle.", '\u2218');
		NAME_TO_UNICODE.put(".varies.", '\u221d');
		NAME_TO_UNICODE.put(".infin.", '\u221e');
		NAME_TO_UNICODE.put(".angle.", '\u2220');
		NAME_TO_UNICODE.put(".div.", '\u2223');
		NAME_TO_UNICODE.put(".parallel.", '\u2225');
		NAME_TO_UNICODE.put(".andgate.", '\u2229');
		NAME_TO_UNICODE.put(".orgate.", '\u222a');
		NAME_TO_UNICODE.put(".intg.", '\u222b');
		NAME_TO_UNICODE.put(".thrfore.", '\u2234');
		NAME_TO_UNICODE.put(".BECAUSE.", '\u2235');
		//NAME_TO_UNICODE.put(".because.", '\u2235'); check if only uppercase in data.
		NAME_TO_UNICODE.put(".about.", '\u223c');
		NAME_TO_UNICODE.put(".apprxeq.", '\u2245');
		//NAME_TO_UNICODE.put(".apprxeq.", '\u2248');
		NAME_TO_UNICODE.put(".ltoreq.", '\u2264');
		NAME_TO_UNICODE.put(".gtoreq.", '\u2265');
		NAME_TO_UNICODE.put(".ltoreg.", '\u2266');
		NAME_TO_UNICODE.put(".gtoreg.", '\u2267');
		NAME_TO_UNICODE.put(".notlessthan.", '\u226e');
		NAME_TO_UNICODE.put(".notgreaterthan.", '\u226f');
		NAME_TO_UNICODE.put(".gtorsim.", '\u2273');
		NAME_TO_UNICODE.put(".circleincircle.", '\u227a');
		NAME_TO_UNICODE.put(".OR right.", '\u2282');
		NAME_TO_UNICODE.put(".sym.", '\u2295');
		NAME_TO_UNICODE.put(".crclbar.", '\u2296');
		NAME_TO_UNICODE.put(".circle-w/dot.", '\u2299');
		//NAME_TO_UNICODE.put(".circleincircle.", '\u229a');
		NAME_TO_UNICODE.put(".perp.", '\u22a5');		
		NAME_TO_UNICODE.put(".diamond.", '\u22c4');
		NAME_TO_UNICODE.put(".hoarfrost.", '\u2423');
		NAME_TO_UNICODE.put(".left brkt-top.", '\u250c');
		NAME_TO_UNICODE.put(".right brkt-bot.", '\u2510');
		NAME_TO_UNICODE.put(".left brkt-bot.", '\u2514');
		NAME_TO_UNICODE.put(".right brkt-bot.", '\u2518');
		NAME_TO_UNICODE.put(".dbd.", '\u2550');
		NAME_TO_UNICODE.put(".quadrature.", '\u25a1');
		NAME_TO_UNICODE.put(".box-solid.", '\u25aa');
		NAME_TO_UNICODE.put(".tangle-solidup.", '\u25b4');
		NAME_TO_UNICODE.put(".circle-solid.", '\u25cf');
		NAME_TO_UNICODE.put(".largecircle.", '\u25ef');
		NAME_TO_UNICODE.put(".star-solid.", '\u2605');
		NAME_TO_UNICODE.put(".diamond-solid.", '\u2666');
		NAME_TO_UNICODE.put(".AMP.", '\u0026');
		NAME_TO_UNICODE.put(".GAMMA.", '\u0393');
		NAME_TO_UNICODE.put(".DELTA.", '\u0394');
		NAME_TO_UNICODE.put(".THETA.", '\u0398');
		NAME_TO_UNICODE.put(".LAMBDA.", '\u039b');
		NAME_TO_UNICODE.put(".XI.", '\u039e');
		NAME_TO_UNICODE.put(".PI.", '\u03a0');
		NAME_TO_UNICODE.put(".SIGMA.", '\u03a3');
		NAME_TO_UNICODE.put(".UPSILON.", '\u03a5');
		NAME_TO_UNICODE.put(".PHI.", '\u03a6');
		NAME_TO_UNICODE.put(".PSI.", '\u03a8');
		NAME_TO_UNICODE.put(".OMEGA.", '\u03a9');
		NAME_TO_UNICODE.put(".alpha.", '\u03b1');
		NAME_TO_UNICODE.put(".beta.", '\u03b2');
		NAME_TO_UNICODE.put(".gamma.", '\u03b3');
		NAME_TO_UNICODE.put(".delta.", '\u03b4');
		NAME_TO_UNICODE.put(".epsilon.", '\u03b5');
		NAME_TO_UNICODE.put(".zeta.", '\u03b6');
		NAME_TO_UNICODE.put(".eta.", '\u03b7');
		NAME_TO_UNICODE.put(".theta.", '\u03b8');
		NAME_TO_UNICODE.put(".kappa.", '\u03ba');
		NAME_TO_UNICODE.put(".lamda.", '\u03bb');
		NAME_TO_UNICODE.put(".mu.", '\u03bc');
		NAME_TO_UNICODE.put(".nu.", '\u03bd');
		NAME_TO_UNICODE.put(".xi.", '\u03be');
		NAME_TO_UNICODE.put(".omicron.", '\u03bf');
		NAME_TO_UNICODE.put(".pi.", '\u03c0');
		NAME_TO_UNICODE.put(".rho.", '\u03c1');
		NAME_TO_UNICODE.put(".sigma.", '\u03c3');
		NAME_TO_UNICODE.put(".tau.", '\u03c4');
		NAME_TO_UNICODE.put(".upsilon.", '\u03c5');
		NAME_TO_UNICODE.put(".phi.", '\u03c6');
		NAME_TO_UNICODE.put(".chi.", '\u03c7');
		NAME_TO_UNICODE.put(".psi.", '\u03c8');
		NAME_TO_UNICODE.put(".omega.", '\u03c9');
		NAME_TO_UNICODE.put(".LT.", '\u003c');
		//NAME_TO_UNICODE.put(".PHI.", '\u03d5');
		//NAME_TO_UNICODE.put(".PI.", '\u03d6');
		NAME_TO_UNICODE.put(".GT.", '\u003e');
		NAME_TO_UNICODE.put(".English Pound.", '\u00a3');
		NAME_TO_UNICODE.put(".sctn.", '\u00a7');		
		NAME_TO_UNICODE.put(".COPYRGT.", '\u00a9');
		NAME_TO_UNICODE.put(".RTM.", '\u00ae');
		NAME_TO_UNICODE.put(".degree.", '\u00b0');
		NAME_TO_UNICODE.put(".+-.", '\u00b1');
		NAME_TO_UNICODE.put(".times.", '\u00d7');
		NAME_TO_UNICODE.put(".paren open-st.", '\ue00c');
		NAME_TO_UNICODE.put(".brket open-st.", '\ue00e');
		NAME_TO_UNICODE.put(".brket close-st.", '\ue00f');		
		NAME_TO_UNICODE.put(".ae butted.", '\u00e6');
	}

	/*
	 * .En. found but is NOT a Dot Code in US4238984A: 38 at the timing T.sub.En. In
	 * this case, the output Q.sub.1, Q.sub.2 and The dots are NOT shared – we
	 * handled this type of exception with a simple change to our code.
	 * 
	 * .En. found again, but is NOT a Dot Code in US3992364A: F E.As. No Illness 250
	 * 245 M O.En. Cerebral Sclerosis The O.En. is an abbreviation used in biology,
	 * and this is not nearly as easy in programming logic.
	 */

	/*
	 * NAME_TO_UNICODE.put(".[.", '['); // open bold bracket (reissue)
	 * NAME_TO_UNICODE.put(".].", ']'); // close bold bracket (reissue)
	 * NAME_TO_UNICODE.put(".A.", '('); // Open bold bracket (reissue)
	 * NAME_TO_UNICODE.put(".1.", ')'); // Close bold bracket (reissue)
	 * 
	 * // Patent Reissue // oringinal data marked: .~. .]. // new data marked:
	 * .Iadd. .laddend. (Italic)
	 * 
	 * // Reissue of a Reissue Patent // .~. .(. .]. .J. // .Badd. .Baddend. (Bold)
	 */

	public static String replace(final String orig) {
		StringBuffer stb = new StringBuffer();
		Matcher matcher = ENTITY_PATTERN.matcher(orig);
		while (matcher.find()) {
			String match = matcher.group();
			// System.out.println("pattern match: " + match);
			if (NAME_TO_UNICODE.containsKey(match)) {
				matcher.appendReplacement(stb, NAME_TO_UNICODE.get(match).toString());
			}
		}
		matcher.appendTail(stb);
		return stb.toString();
	}

	/**
	 * Subscript and Superscript wrapped with HTML tags "sub" or "sup"
	 * 
	 * @param orig
	 * @return HTML
	 */
	public static String replaceSubSupHTML(final String orig) {
		StringBuilder stb = new StringBuilder(orig);
		Matcher matcher = SUBSUP_PATTERN.matcher(orig);
		int additionalChars = 0;
		while (matcher.find()) {
			String fullMatch = matcher.group(0);
			// System.out.println("subsup pattern match: " + fullMatch);

			String subSupMatch = matcher.group(1);

			String startTag = "";
			String endTag = "";
			switch (subSupMatch) {
			case "sub":
				startTag = "<sub>";
				endTag = "</sub>";
				break;
			case "sup":
				startTag = "<sup>";
				endTag = "</sup>";
				break;
			case "spsb":
				startTag = "<sup><sub>";
				endTag = "</sub></sup>";
				break;
			case "spsp":
				startTag = "<sup><sup>";
				endTag = "</sup></sup>";
				break;
			case "sbsb":
				startTag = "<sub><sub>";
				endTag = "</sub></sub>";
				break;
			case "sbsp":
				startTag = "<sub><sup>";
				endTag = "</sup></sub>";
				break;
			}

			String value = matcher.group(2);
			String newStr = startTag + value + endTag;

			stb.replace(matcher.start() + additionalChars, matcher.end() + additionalChars, newStr);
			additionalChars = additionalChars + (newStr.length() - fullMatch.length());
		}
		String htmlText = stb.toString();
		return htmlText;
	}
}
