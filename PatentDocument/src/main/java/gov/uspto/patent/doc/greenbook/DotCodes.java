package gov.uspto.patent.doc.greenbook;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Greenbook Dot Codes for Special Characters mapped to their Unicode symbol.
 * 
 * Unicode was not around when Greenbook dot codes where introduced.
 * 
 *<p><pre>
 * Dotcode are terminated by a space, a comma (,), a period (.), a right
 * parenthesis ()) or another subscript .sub. or superscript .sup.
 *</p></pre>
 *
 * https://www.w3.org/TR/REC-html40/sgml/entities.html
 * https://r12a.github.io/apps/conversion/
 * http://sites.psu.edu/symbolcodes/accents/math/mathchart
 *
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 * 
 */
public class DotCodes {

	private static final Pattern ENTITY_PATTERN = Pattern
			.compile("\\.(?:[0\\[\\]]|[^\\p{Z},;\\.)]{2,18})(?:\\.|(?=[\\p{Z},;)]))", Pattern.UNICODE_CHARACTER_CLASS);

	private static final Pattern SUBSUP_PATTERN = Pattern
			.compile("\\.(su[bp]|s[bp]s[bp])\\.{1,2}([^\\p{Z},;\\.)]{1,10})(?:\\.|(?=[\\p{Z},;)]))", Pattern.UNICODE_CHARACTER_CLASS);

	// private static final Pattern FRACTION_PATTERN = Pattern.compile("\\b[1-9]/[1-9][0-9]*\\b");

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
		NAME_TO_UNICODE.put(".TM.", '\u2122'); // trademark
		NAME_TO_UNICODE.put(".SM.", '\u2120'); // service mark
		NAME_TO_UNICODE.put(".RTM.", '\u00AE'); // registered trademark
		NAME_TO_UNICODE.put(".COPYRG.", '\u00A9'); // copyright

		// Arrows [ unicode block arrows: U+2190 to U+21FF ]
		NAME_TO_UNICODE.put(".rarw.", '\u2190'); // arrow left (reverse)
		NAME_TO_UNICODE.put(".uparw.", '\u2191'); // arrow up
		NAME_TO_UNICODE.put(".fwdarw.", '\u2192'); // arrow right (forward)
		NAME_TO_UNICODE.put(".dwnarw.", '\u2193'); // arrow down

		// Greek Letters [ unicode block Greek and Coptic: U+0370 to U+03FF ]
		NAME_TO_UNICODE.put(".ALPHA.", '\u0391'); // greek letter alpha upper case
		NAME_TO_UNICODE.put(".BETA.", '\u0392'); // greek letter beta upper case
		NAME_TO_UNICODE.put(".GAMMA.", '\u0393'); // greek letter gamma upper case
		NAME_TO_UNICODE.put(".DELTA.", '\u0394'); // greek letter delta upper case (increment)
		NAME_TO_UNICODE.put(".EPSILON.", '\u0395'); // greek letter epsilon upper case
		NAME_TO_UNICODE.put(".ZETA.", '\u0396'); // greek letter zeta upper case
		NAME_TO_UNICODE.put(".ETA.", '\u0397'); // greek letter eta upper case
		NAME_TO_UNICODE.put(".THETA.", '\u0398'); // greek letter theta upper case
		NAME_TO_UNICODE.put(".IOTA.", '\u0399'); // greek letter iota upper case
		NAME_TO_UNICODE.put(".KAPPA.", '\u039A'); // greek letter kappa upper case
		NAME_TO_UNICODE.put(".LAMBDA.", '\u039B'); // greek letter lamba upper case
		NAME_TO_UNICODE.put(".MU.", '\u039C'); // greek letter mu upper case used in Statistics
		NAME_TO_UNICODE.put(".NU.", '\u039D'); // greek nu upper case
		NAME_TO_UNICODE.put(".XI.", '\u039E'); // greek xi upper case
		NAME_TO_UNICODE.put(".OMICRON.", '\u039F'); // greek omicron upper case
		NAME_TO_UNICODE.put(".PI.", '\u03A0'); // greek pi upper case
		NAME_TO_UNICODE.put(".RHO.", '\u03A1'); // greek rho upper case
		NAME_TO_UNICODE.put(".SIGMA.", '\u03A3'); // greek letter sigma upper case, used in Statistics
		NAME_TO_UNICODE.put(".TAU.", '\u03A4'); // greek tau upper case symbol
		NAME_TO_UNICODE.put(".UPSILON.", '\u03A5'); // greek upsilon upper case
		NAME_TO_UNICODE.put(".PHI.", '\u03A6'); // greek phi upper case
		NAME_TO_UNICODE.put(".CHI.", '\u03A7'); // greek letter chi upper case,used in Statistics
		NAME_TO_UNICODE.put(".PSI.", '\u03A8'); // greek psi upper case
		NAME_TO_UNICODE.put(".OMEGA.", '\u03A9'); // greek omega upper case

		NAME_TO_UNICODE.put(".alpha.", '\u03B1'); // greek letter alpha lowercase
		NAME_TO_UNICODE.put(".beta.", '\u03B2'); // greek letter beta lowercase
		NAME_TO_UNICODE.put(".gamma.", '\u03B3'); // greek letter gamma lowercase
		NAME_TO_UNICODE.put(".delta.", '\u03B4'); // greek letter delta lowercase
		NAME_TO_UNICODE.put(".epsilon.", '\u03B5'); // greek letter epsilon lower case
		NAME_TO_UNICODE.put(".zeta.", '\u03B6'); // greek letter zeta lower case
		NAME_TO_UNICODE.put(".eta.", '\u03B7'); // greek letter eta lower case
		NAME_TO_UNICODE.put(".theta.", '\u03B8'); // greek letter theta lower case
		NAME_TO_UNICODE.put(".iota.", '\u03B9'); // greek letter iota lower case
		NAME_TO_UNICODE.put(".kappa.", '\u03BA'); // greek letter kappa lower case
		NAME_TO_UNICODE.put(".lambda.", '\u03BB'); // greek letter lamba lower case
		NAME_TO_UNICODE.put(".mu.", '\u03BC'); // greek letter mu lower case, used in Statistics
		NAME_TO_UNICODE.put(".nu.", '\u03BD'); // greek nu lower case
		NAME_TO_UNICODE.put(".xi.", '\u03BE'); // greek xi lower case
		NAME_TO_UNICODE.put(".omicron.", '\u03BF'); // greek omicron lower case
		NAME_TO_UNICODE.put(".pi.", '\u03C0'); // greek pi lower case
		NAME_TO_UNICODE.put(".rho.", '\u03C1'); // greek rho lower case
		NAME_TO_UNICODE.put(".sigma.", '\u03C2'); // greek letter sigma lower case, used in Statistics
		NAME_TO_UNICODE.put(".tau.", '\u03C4'); // greek tau lower case
		NAME_TO_UNICODE.put(".upsilon.", '\u03C5'); // greek upsilon lower case
		NAME_TO_UNICODE.put(".phi.", '\u03C6'); // greek phi lower case
		NAME_TO_UNICODE.put(".chi.", '\u03C7'); // greek letter chi lower case,used in Statistics
		NAME_TO_UNICODE.put(".psi.", '\u03C8'); // greek psi lower case
		NAME_TO_UNICODE.put(".omega.", '\u03C9'); // greek omega lower case

		// Mathematical Operators [ Unicode Block U+2200 to U+22FF ]
		NAME_TO_UNICODE.put(".differential.", '\u2202'); // partial differential
		NAME_TO_UNICODE.put(".0.", '\u2205'); // Slash Zero, empty/null set
		NAME_TO_UNICODE.put(".increment.", '\u2206'); // increment
		NAME_TO_UNICODE.put(".gradient.", '\u2207'); // gradient NABLA = backward difference
		NAME_TO_UNICODE.put(".parallel.", '\u2225'); // parallel to

		NAME_TO_UNICODE.put(".-+.", '\u2213'); // minus-or-plus
		NAME_TO_UNICODE.put(".+-.", '\u00B1'); // plus-minus = plus-or-minus
		NAME_TO_UNICODE.put(".sqroot.", '\u221A'); // SQUARE ROOT
		NAME_TO_UNICODE.put(".cuberoot.", '\u221B'); // cube root
		NAME_TO_UNICODE.put(".fourthroot.", '\u221C'); // fourth root
		NAME_TO_UNICODE.put(".4th root.", '\u221C'); // fourth root

		NAME_TO_UNICODE.put(".angle.", '\u2220'); // angle symbol
		NAME_TO_UNICODE.put(".andgate.", '\u2229'); // and gate (intersection)
		NAME_TO_UNICODE.put(".orgate.", '\u222A'); // or gate (union)
		NAME_TO_UNICODE.put(".intg.", '\u222B'); // integral symbol
		NAME_TO_UNICODE.put(".thrfore.", '\u2234'); // therefore symbol
		NAME_TO_UNICODE.put(".because.", '\u2235'); // because symbol
		NAME_TO_UNICODE.put(".perspectiveto.", '\u2243'); // perspective to (asymptotically equal to)
		NAME_TO_UNICODE.put(".about.", '\u2245'); // approximately equal to
		NAME_TO_UNICODE.put(".apprxeq.", '\u2245'); // approximately equal to
		NAME_TO_UNICODE.put(".congruent.", '\u2245'); // congruent (approximately equal to)
		NAME_TO_UNICODE.put(".approxeq.", '\u2248'); // approximately equal to
		NAME_TO_UNICODE.put(".apprch.", '\u2250'); // approaches the limit
		NAME_TO_UNICODE.put(".noteq.", '\u2260'); // not equal to
		NAME_TO_UNICODE.put(".ident.", '\u2261'); // identical to
		NAME_TO_UNICODE.put(".notident.", '\u2262'); // not identical
		NAME_TO_UNICODE.put(".lmtoreq.", '\u2264'); // less-than or equal to
		NAME_TO_UNICODE.put(".ltoreq.", '\u2264'); // less-than or equal to
		NAME_TO_UNICODE.put(".gtoreq.", '\u2265'); // greater-than or equal to
		NAME_TO_UNICODE.put(".varies.", '\u221D'); // varies as (proportional to)
		NAME_TO_UNICODE.put(".sym.", '\u2295'); // positive earth (circled plus) symmetry
		NAME_TO_UNICODE.put(".crclbar.", '\u2296'); // negative earth (circled minus) symbol (circle bar)

		NAME_TO_UNICODE.put(".perp.", '\u22a5'); // perpendicular (up tack)
		NAME_TO_UNICODE.put(".lmorsim.", '\u2A85'); // less-than or approximate
		NAME_TO_UNICODE.put(".gtorsim.", '\u2A86'); // greater-than or approximate
		NAME_TO_UNICODE.put(".vertline.", '\u007C'); // vertical line
		NAME_TO_UNICODE.put(".times.", '\u00D7'); // multiplication sign
		NAME_TO_UNICODE.put(".div.", '\u00F7'); // division sign
		NAME_TO_UNICODE.put(".notlessthan.", '\u226e'); // not less than
		NAME_TO_UNICODE.put(".notgreaterthan.", '\u226f'); // not greater than
		NAME_TO_UNICODE.put(".not <.", '\u226E'); // not less-than
		NAME_TO_UNICODE.put(".not >.", '\u226F'); // not greater-than
		NAME_TO_UNICODE.put(".infin.", '\u221E'); // infinity
		NAME_TO_UNICODE.put(".multidot.", '\u8901'); // multiplication dot operator
		
		// Measurement
		NAME_TO_UNICODE.put(".ANG.", '\u212B'); // Angstrom upper case
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
		NAME_TO_UNICODE.put(".dbd.", '\u2550'); // double bond
		NAME_TO_UNICODE.put(".guadbond.", '\u2243'); // guad bond
		NAME_TO_UNICODE.put(".tbd.", '\u2261'); // identical (equivalent) (triple bond)
		NAME_TO_UNICODE.put(".reveaction.", '\u21cc'); // reversible reaction (equilibrium)

		NAME_TO_UNICODE.put(".function.", '\u0192'); // function symbol (small letter f with hook)
		NAME_TO_UNICODE.put(".hoarfrost.", '\u2294'); // hoarfrost (square cup)
		NAME_TO_UNICODE.put(".sctn.", '\u00A7'); // section

		// Geometric Shapes [ unicode block U+25A0 to U+25FF ]
		NAME_TO_UNICODE.put(".cndot.", '\u2022'); // solid dot - bullet symbol
		NAME_TO_UNICODE.put(".quadrature.", '\u25a1'); // quadrature (white square)
		NAME_TO_UNICODE.put(".smallcircle.", '\u25CB'); // small circle
		NAME_TO_UNICODE.put(".largecircle.", '\u25ef'); // large circle
		NAME_TO_UNICODE.put(".circle.", '\u25ef'); // large circle
		NAME_TO_UNICODE.put(".dottedcircle.", '\u25cc'); // dotted circle
		NAME_TO_UNICODE.put(".lhalfcircle.", '\u25d0'); // left half of circle black
		NAME_TO_UNICODE.put(".rhalfcircle.", '\u25d1'); // right half of circle black
		NAME_TO_UNICODE.put(".solthalfcircle.", '\u25d3'); // top half of circle black
		NAME_TO_UNICODE.put(".solbhalfcircle.", '\u25d2'); // bottom half of circle black
		NAME_TO_UNICODE.put(".circleincircle.", '\u25ce'); // circle in a large circle "BULLSEYE"

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
	}

	/* 
	.En. found but is NOT a Dot Code in US4238984A:
 		38 at the timing T.sub.En. In this case, the output Q.sub.1, Q.sub.2 and
		The dots are NOT shared – we handled this type of exception with a simple change to our code.
	
	.En. found again, but is NOT a Dot Code in US3992364A:
 		F E.As. No Illness 250 245
 		M O.En. Cerebral Sclerosis
		The O.En. is an abbreviation used in biology, and this is not nearly as easy in programming logic.
	 */

	/*
	NAME_TO_UNICODE.put(".[.", '['); // open bold bracket (reissue)
	NAME_TO_UNICODE.put(".].", ']'); // close bold bracket (reissue)
	NAME_TO_UNICODE.put(".A.", '('); // Open bold bracket (reissue)
	NAME_TO_UNICODE.put(".1.", ')'); // Close bold bracket (reissue)
 
	// Patent Reissue
	//   oringinal data marked:  .~.   .].
	//   new data marked:   .Iadd.  .laddend.  (Italic)
	
	// Reissue of a Reissue Patent
	//   .~. .(.     .]. .J.
	//  .Badd.      .Baddend.  (Bold)
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
			//System.out.println("subsup pattern match: " + fullMatch);

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
