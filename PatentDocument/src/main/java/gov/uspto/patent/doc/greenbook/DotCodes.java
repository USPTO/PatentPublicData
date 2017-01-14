package gov.uspto.patent.doc.greenbook;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Greenbook Dot Codes for Special Characters mapped to their Unicode symbol.
 * 
 * https://www.w3.org/TR/REC-html40/sgml/entities.html
 * 
 * https://r12a.github.io/apps/conversion/
 * http://sites.psu.edu/symbolcodes/accents/math/mathchart
 */
public class DotCodes {

	//private static final Pattern ENTITY_PATTERN = Pattern.compile("\\.su[bp]\\.[A-z0-9]|\\.[A-z]+\\.");
	private static final Pattern ENTITY_PATTERN = Pattern.compile("\\.(?:[0\\[\\]]|[A-z\\-\\+]{2,18})\\.");
	//private static final Pattern FRACTION_PATTERN = Pattern.compile("\\b[1-9]/[1-9][0-9]*\\b");
	private static final Pattern SUBSUP_PATTERN = Pattern.compile("\\b\\.(su[bp]|s[bp]s[bp])\\.{1,2}([^\\.]+)\\.?\\b");

	public static String fraction2number(String fraction){
		DecimalFormat df = new DecimalFormat("#.####");
		df.setRoundingMode(RoundingMode.CEILING);
		String[] parts = fraction.split("/");
		double fraction1 = (double) Integer.valueOf(parts[0]) / Integer.valueOf(parts[1]);
		//(double)Math.round(value * 100000d) / 100000d
		//return fraction1;
		return df.format(fraction1);
	}

	private static final HashMap<String, Character> NAME_TO_UNICODE = new HashMap<String, Character>();
	static {
		// Trademark and Copywrite
		NAME_TO_UNICODE.put(".TM.", '\u8482'); // trademark symbol
		NAME_TO_UNICODE.put(".SM.", '\u2120'); // service mark symbol
		NAME_TO_UNICODE.put(".RTM.", '\u00AE'); // registered trademark symbol
		NAME_TO_UNICODE.put(".COPYRG.", '\u00A9'); // copyright symbol
		
		// Arrows [ unicode block arrows: U+2190 to U+21FF ]
		NAME_TO_UNICODE.put(".rarw.", '\u2190'); // reverse arrow - left  &#x2190;
		NAME_TO_UNICODE.put(".uparw.", '\u2191'); // up arrow symbol
		NAME_TO_UNICODE.put(".fwdarw.", '\u2192'); // forward arrow (arrow - right) symbol  &#x2192;
		NAME_TO_UNICODE.put(".dwnarw.", '\u2193'); // Down arrow symbol 
		
		// Greek Letters [ unicode block Greek and Coptic: U+0370 to U+03FF ]
		NAME_TO_UNICODE.put(".ALPHA.", '\u0391'); // greek letter alpha - uppercase symbol
		NAME_TO_UNICODE.put(".BETA.", '\u0392'); // greek letter beta - uppercase symbol 
		NAME_TO_UNICODE.put(".GAMMA.", '\u0393'); // greek letter gamma upper case symbol 
		NAME_TO_UNICODE.put(".DELTA.", '\u0394'); // greek letter delta upper case symbol (increment)
		NAME_TO_UNICODE.put(".EPSILON.", '\u0395'); // greek letter epsilon upper case symbol 
		NAME_TO_UNICODE.put(".ZETA.", '\u0396'); // greek letter zeta upper case symbol
		NAME_TO_UNICODE.put(".ETA.", '\u0397'); // greek letter eta upper case symbol
		NAME_TO_UNICODE.put(".THETA.", '\u0398'); // greek letter theta upper case symbol 
		NAME_TO_UNICODE.put(".IOTA.", '\u0399'); // greek letter iota upper case symbol
		NAME_TO_UNICODE.put(".KAPPA.", '\u039A'); // greek letter kappa upper case symbol
		NAME_TO_UNICODE.put(".LAMBDA.", '\u039B'); // greek letter lamba upper case symbol
		NAME_TO_UNICODE.put(".MU.", '\u039C'); // greek letter mu upper case symbol, used in Statistics  
		NAME_TO_UNICODE.put(".NU.", '\u039D'); // greek nu upper case symbol
		NAME_TO_UNICODE.put(".XI.", '\u039E'); // greek xi upper case symbol  
		NAME_TO_UNICODE.put(".OMICRON.", '\u039F'); // greek omicron upper case symbol
		NAME_TO_UNICODE.put(".PI.", '\u03A0'); // greek pi upper case symbol 
		NAME_TO_UNICODE.put(".RHO.", '\u03A1'); // greek rho upper case symbol
		NAME_TO_UNICODE.put(".SIGMA.", '\u03A3'); // greek letter sigma upper case, used in Statistics
		NAME_TO_UNICODE.put(".TAU.", '\u03A4'); // greek tau upper case symbol
		NAME_TO_UNICODE.put(".UPSILON.", '\u03A5'); // greek upsilon upper case symbol
		NAME_TO_UNICODE.put(".PHI.", '\u03A6'); // greek phi upper case symbol
		NAME_TO_UNICODE.put(".CHI.", '\u03A7'); // greek letter chi upper case,used in Statistics
		NAME_TO_UNICODE.put(".PSI.", '\u03A8'); // greek psi upper case symbol  
		NAME_TO_UNICODE.put(".OMEGA.", '\u03A9'); // greek omega upper case symbol  

		NAME_TO_UNICODE.put(".alpha.", '\u03B1'); // greek letter alpha lowercase symbol 
		NAME_TO_UNICODE.put(".beta.", '\u03B2'); // greek letter beta lowercase symbol
		NAME_TO_UNICODE.put(".gamma.", '\u03B3'); // greek letter gamma lower case symbol
		NAME_TO_UNICODE.put(".delta.", '\u03B4'); // greek letter delta lower case symbol
		NAME_TO_UNICODE.put(".epsilon.", '\u03B5'); // greek letter epsilon lower case symbol
		NAME_TO_UNICODE.put(".zeta.", '\u03B6'); // greek letter zeta lower case symbol
		NAME_TO_UNICODE.put(".eta.", '\u03B7'); // greek letter eta lower case symbol
		NAME_TO_UNICODE.put(".theta.", '\u03B8'); // greek letter theta lower case symbol
		NAME_TO_UNICODE.put(".iota.", '\u03B9'); // greek letter iota lower case symbol
		NAME_TO_UNICODE.put(".kappa.", '\u03BA'); // greek letter kappa lower case symbol
		NAME_TO_UNICODE.put(".lambda.", '\u03BB'); // greek letter lamba lower case symbol
		NAME_TO_UNICODE.put(".mu.", '\u03BC'); // greek letter mu lower case symbol, used in Statistics
		NAME_TO_UNICODE.put(".nu.", '\u03BD'); // greek nu lower case symbol 
		NAME_TO_UNICODE.put(".xi.", '\u03BE'); // greek xi lower case symbol  
		NAME_TO_UNICODE.put(".omicron.", '\u03BF'); // greek omicron lower case symbol   
		NAME_TO_UNICODE.put(".pi.", '\u03C0'); // greek pi lower case symbol    
		NAME_TO_UNICODE.put(".rho.", '\u03C1'); // greek rho lower case symbol   
		NAME_TO_UNICODE.put(".sigma.", '\u03C2'); // greek letter sigma lower case, used in Statistics
		NAME_TO_UNICODE.put(".tau.", '\u03C4'); // greek tau lower case symbol
		NAME_TO_UNICODE.put(".upsilon.", '\u03C5'); // greek upsilon lower case symbol
		NAME_TO_UNICODE.put(".phi.", '\u03C6'); // greek phi lower case symbol
		NAME_TO_UNICODE.put(".chi.", '\u03C7'); // greek letter chi lower case,used in Statistics 
		NAME_TO_UNICODE.put(".psi.", '\u03C8'); // greek psi lower case symbol 
		NAME_TO_UNICODE.put(".omega.", '\u03C9'); // greek omega lower case symbol

		// Mathematical Operators [ Unicode Block U+2200 to U+22FF ]
		NAME_TO_UNICODE.put(".differential.", '\u2202'); // partial differential
		
		NAME_TO_UNICODE.put(".increment.", '\u2206'); // increment symbol
		NAME_TO_UNICODE.put(".gradient.", '\u2207'); // gradient NABLA = backward difference symbol

		NAME_TO_UNICODE.put(".-+.", '\u2213'); // minus-or-plus symbol &#x2213;
		NAME_TO_UNICODE.put(".+-.", '\u00B1'); // plus-minus sign = plus-or-minus sign symbol
		NAME_TO_UNICODE.put(".sqroot.", '\u221A'); // SQUARE ROOT
		NAME_TO_UNICODE.put(".cuberoot.", '\u221B'); // cube root symbol
		NAME_TO_UNICODE.put(".fourthroot.", '\u221C'); // fourth root symbol
		NAME_TO_UNICODE.put(".4th root.", '\u221C'); // fourth root symbol
		
		NAME_TO_UNICODE.put(".ltoreq.", '\u2264'); // less-than or equal to symbol
		NAME_TO_UNICODE.put(".gtoreq.", '\u2265'); // greater-than or equal to symbol
		NAME_TO_UNICODE.put(".congruent.", '\u2245'); // congruent (approximately equal to)
		NAME_TO_UNICODE.put(".approxeq.", '\u2248'); // approximately equal to

		NAME_TO_UNICODE.put(".times.", '\u00D7'); // multiplication sign
		NAME_TO_UNICODE.put(".div.", '\u00F7'); // division sign
		NAME_TO_UNICODE.put(".notlessthan.", '\u226e'); // not less than symbol 
		NAME_TO_UNICODE.put(".notgreaterthan.", '\u226f'); // not greater than symbol 
		NAME_TO_UNICODE.put(".not <.", '\u226E'); // not less-than
		NAME_TO_UNICODE.put(".not >.", '\u226F'); // not greater-than
		NAME_TO_UNICODE.put(".infin.", '\u221E'); // infinity
		NAME_TO_UNICODE.put(".multidot.", '\u8901'); // multiplication dot operator symbol 
		NAME_TO_UNICODE.put(".angle.", '\u2220'); // angle symbol
		NAME_TO_UNICODE.put(".parallel.", '\u2225'); // parallel to symbol  
		NAME_TO_UNICODE.put(".vertline.", '\u007C'); // vertical line symbol 
		NAME_TO_UNICODE.put(".perp.", '\u22a5'); // up tack = orthogonal to = perpendicular symbol 
		NAME_TO_UNICODE.put(".intg.", '\u222B'); // integral symbol
		NAME_TO_UNICODE.put(".andgate.", '\u2229'); // and gate (intersection) symbol 
		NAME_TO_UNICODE.put(".orgate.", '\u222a'); // or gate (union)  
		NAME_TO_UNICODE.put(".thrfore.", '\u2234'); // therefore symbol
		NAME_TO_UNICODE.put(".because.", '\u2235'); // because symbol 
		NAME_TO_UNICODE.put(".perspectiveto.", '\u2243'); // perspective to (asymptotically equal to) symbol 
		NAME_TO_UNICODE.put(".about.", '\u2245'); // APPROXIMATELY EQUAL TO: ≅  &#x2245; ,  difference (tilde) symbol ˜  &#x223c;  &sim;
		
		NAME_TO_UNICODE.put(".apprxeq.", '\u2245');  // approximately equal to symbol
		NAME_TO_UNICODE.put(".noteq.", '\u2260'); // not equal to symbol
		NAME_TO_UNICODE.put(".ident.", '\u2261'); // identical to symbol
		NAME_TO_UNICODE.put(".notident.", '\u2262'); // not identical
		NAME_TO_UNICODE.put(".lmtoreq.", '\u2264'); // less-than or equal to symbol
		NAME_TO_UNICODE.put(".gtoreq.", '\u2265');  // greater-than or equal to symbol

		NAME_TO_UNICODE.put(".varies.", '\u221D'); // varies as (proportional to) 
		NAME_TO_UNICODE.put(".sym.", '\u2295'); // positive earth (circled plus) symmetry symbol  
		NAME_TO_UNICODE.put(".crclbar.", '\u2296'); // negative earth (circled minus) symbol (circle bar) 
		NAME_TO_UNICODE.put(".apprch.", '\u2250'); // approaches the limit symbol 

		NAME_TO_UNICODE.put(".lmorsim.", '\u2A85'); // less-than or approximate
		NAME_TO_UNICODE.put(".gtorsim.", '\u2A86'); // greater-than or approxoximate

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
		NAME_TO_UNICODE.put(".music-sharp.", '\u266F'); // music sharp symbol
		NAME_TO_UNICODE.put(".music-flat.", '\u266D'); // music flat symbol
		NAME_TO_UNICODE.put(".music-natrual.", '\u266e'); // music natural symbol

		// Chemical
		NAME_TO_UNICODE.put(".dbd.", '\u2550'); // double bond symbol
		NAME_TO_UNICODE.put(".guadbond.", '\u2243'); // guadbond symbol 
		NAME_TO_UNICODE.put(".tbd.", '\u2261'); // identical (equivalent)   (triple bond) symbol 
		NAME_TO_UNICODE.put(".reveaction.", '\u21cc'); // reversible reaction symbol (equilibrium symbol)

		//NAME_TO_UNICODE.put(".function.", ''); // function symbol
		NAME_TO_UNICODE.put(".hoarfrost.", '\u2294'); // hoarfrost (square cup) symbol
		NAME_TO_UNICODE.put(".sctn.", '\u00A7'); // section symbol 

		// Geometric Shapes [ unicode block U+25A0 to U+25FF ]
		NAME_TO_UNICODE.put(".cndot.", '\u2022'); // solid dot - bullet symbol
		NAME_TO_UNICODE.put(".quadrature.", '\u25a1'); // quadrature (white square) symbol
		NAME_TO_UNICODE.put(".smallcircle.", '\u25CB'); // small circle
		NAME_TO_UNICODE.put(".largecircle.", '\u25ef'); // large circle
		NAME_TO_UNICODE.put(".circle.", '\u25ef'); // large circle
		NAME_TO_UNICODE.put(".dottedcircle.", '\u25cc'); // dotted circle
		NAME_TO_UNICODE.put(".lhalfcircle.", '\u25d0'); // left half of circle
		NAME_TO_UNICODE.put(".rhalfcircle.", '\u25d1'); // right half of circle
		NAME_TO_UNICODE.put(".solthalfcircle.", '\u25d3'); // top half of circle
		NAME_TO_UNICODE.put(".solbhalfcircle.", '\u25d2'); // bottom half of circle
		NAME_TO_UNICODE.put(".circleincircle.", '\u25ce'); // circle in a large circle "BULLSEYE"
		NAME_TO_UNICODE.put(".THorizBrace.", '\uFE37'); // Top horizonal brace - vertical left curly bracket
		NAME_TO_UNICODE.put(".BHorizBrace.", '\uFE38'); // Bottom horizonal brace - vertical right curly bracket
		NAME_TO_UNICODE.put(".0.", '\u00F8'); // Slash Zero
		NAME_TO_UNICODE.put(".prime.", '\u2032'); // prime symbol
		NAME_TO_UNICODE.put(".dblprime.", '\u2033'); // double prime symbol

		// NAME_TO_UNICODE.put(".dottlhalfcircle.", ""); // dotted left half circle
		// NAME_TO_UNICODE.put(".dottrhalfcircle.", ""); // dotted right half circle
		// NAME_TO_UNICODE.put(".dottbtalfcircle.", ""); // dotted top half circle
		// NAME_TO_UNICODE.put(".dottbhalfcircle.", ""); // dotted bottom half circle
		NAME_TO_UNICODE.put(".dblquote.", '"');
		NAME_TO_UNICODE.put(".En.", '\u2025'); // two dot leader	
		NAME_TO_UNICODE.put(".[.", '['); // open bold bracket (reissue)
		NAME_TO_UNICODE.put(".].", ']'); // close bold bracket (reissue)
	}

	private static final HashMap<String, Character> SUBSUP_TO_UNICODE = new HashMap<String, Character>();
	static {
		
		//NAME_TO_UNICODE.put(".sup..parallel.", ""); // superscript parallel symbol 
		//NAME_TO_UNICODE.put(".sub..parallel.", ""); // subscript parallel symbol 
		// NAME_TO_UNICODE.put(".sup..crclbar.", "");
		// NAME_TO_UNICODE.put(".sub..crclbar.", "");
		// NAME_TO_UNICODE.put(".sup..sym.", "");
		// NAME_TO_UNICODE.put(".sub..sym.", "");
		//SUBSUP_TO_UNICODE.put(".sup..fwdarw.", ""); // superscript forward arrow  
		//SUBSUP_TO_UNICODE.put(".sub..fwdarw.", ""); // subscript forward arrow  
		//SUBSUP_TO_UNICODE.put(".sup..rarw.", ""); // superscript reverse arrow  
		//SUBSUP_TO_UNICODE.put(".sub..rarw.", ""); // subscript reverse arrow  
		// SUBSUP_TO_UNICODE.put(".sup.[", "");
		// SUBSUP_TO_UNICODE.put(".sup.]", "");
		// SUBSUP_TO_UNICODE.put(".sub.[", "");
		// SUBSUP_TO_UNICODE.put(".sub.]", "");
		
		//SUBSUP_TO_UNICODE.put(".sup.'", "");
		//SUBSUP_TO_UNICODE.put(".sub.'", "");
		//SUBSUP_TO_UNICODE.put(".sup.\"", "");
		//SUBSUP_TO_UNICODE.put(".sub.\"", "");
		//SUBSUP_TO_UNICODE.put(".sup.div", "");
		//SUBSUP_TO_UNICODE.put(".sub.div", "");

		SUBSUP_TO_UNICODE.put(".sup.(", '\u207D');
		SUBSUP_TO_UNICODE.put(".sub.(", '\u208D');
		SUBSUP_TO_UNICODE.put(".sup.)", '\u207E');
		SUBSUP_TO_UNICODE.put(".sub.)", '\u208E');
		SUBSUP_TO_UNICODE.put(".sup.+", '\u207A');
		SUBSUP_TO_UNICODE.put(".sub.+", '\u208A');
		SUBSUP_TO_UNICODE.put(".sup.-", '\u207B');
		SUBSUP_TO_UNICODE.put(".sub.-", '\u208B');
		// SUBSUP_TO_UNICODE.put(".sup.+-", "");
		// SUBSUP_TO_UNICODE.put(".sub.+-", "");
		// SUBSUP_TO_UNICODE.put(".sup.-+", "");
		// SUBSUP_TO_UNICODE.put(".sub.-+", "");
		// SUBSUP_TO_UNICODE.put(".sup.*", "");
		// SUBSUP_TO_UNICODE.put(".sub.*", "");
		SUBSUP_TO_UNICODE.put(".sup.=", '\u207C');
		SUBSUP_TO_UNICODE.put(".sub.=", '\u208C');
		SUBSUP_TO_UNICODE.put(".sup.0", '\u2070');
		SUBSUP_TO_UNICODE.put(".sup.1", '\u00B9');
		SUBSUP_TO_UNICODE.put(".sup.2", '\u00B2');
		SUBSUP_TO_UNICODE.put(".sup.3", '\u00B3');
		SUBSUP_TO_UNICODE.put(".sup.4", '\u2074');
		SUBSUP_TO_UNICODE.put(".sup.5", '\u2075');
		SUBSUP_TO_UNICODE.put(".sup.6", '\u2076');
		SUBSUP_TO_UNICODE.put(".sup.7", '\u2077');
		SUBSUP_TO_UNICODE.put(".sup.8", '\u2078');
		SUBSUP_TO_UNICODE.put(".sup.9", '\u2079');
		
		SUBSUP_TO_UNICODE.put(".sup.a", '\u1d43');
		SUBSUP_TO_UNICODE.put(".sup.b", '\u1d47');
		SUBSUP_TO_UNICODE.put(".sup.c", '\u1d9c');
		SUBSUP_TO_UNICODE.put(".sup.d", '\u1d48');
		SUBSUP_TO_UNICODE.put(".sup.e", '\u1d49');
		SUBSUP_TO_UNICODE.put(".sup.f", '\u1da0');
		SUBSUP_TO_UNICODE.put(".sup.g", '\u1d4d');
		//SUBSUP_TO_UNICODE.put(".sup.h", '');
		SUBSUP_TO_UNICODE.put(".sup.i", '\u2071');
		//SUBSUP_TO_UNICODE.put(".sup.j", '');
		SUBSUP_TO_UNICODE.put(".sup.k", '\u1d4d');
		//SUBSUP_TO_UNICODE.put(".sup.l", '');
		//SUBSUP_TO_UNICODE.put(".sup.m", '');
		//SUBSUP_TO_UNICODE.put(".sup.n", '');
		//SUBSUP_TO_UNICODE.put(".sup.o", '');
		//SUBSUP_TO_UNICODE.put(".sup.p", '');
		//SUBSUP_TO_UNICODE.put(".sup.q", '');
		//SUBSUP_TO_UNICODE.put(".sup.r", '');
		//SUBSUP_TO_UNICODE.put(".sup.s", '');
		//SUBSUP_TO_UNICODE.put(".sup.t", '');
		//SUBSUP_TO_UNICODE.put(".sup.u", '');
		//SUBSUP_TO_UNICODE.put(".sup.v", '');
		//SUBSUP_TO_UNICODE.put(".sup.w", '');
		//SUBSUP_TO_UNICODE.put(".sup.x", '');
		//SUBSUP_TO_UNICODE.put(".sup.y", '');
		//SUBSUP_TO_UNICODE.put(".sup.z", '');

		SUBSUP_TO_UNICODE.put(".sub.0", '\u2080');
		SUBSUP_TO_UNICODE.put(".sub.1", '\u2081');
		SUBSUP_TO_UNICODE.put(".sub.2", '\u2082');
		SUBSUP_TO_UNICODE.put(".sub.3", '\u2083');
		SUBSUP_TO_UNICODE.put(".sub.4", '\u2084');
		SUBSUP_TO_UNICODE.put(".sub.5", '\u2085');
		SUBSUP_TO_UNICODE.put(".sub.6", '\u2086');
		SUBSUP_TO_UNICODE.put(".sub.7", '\u2087');
		SUBSUP_TO_UNICODE.put(".sub.8", '\u2088');
		SUBSUP_TO_UNICODE.put(".sub.9", '\u2089');
		
		SUBSUP_TO_UNICODE.put(".sub.a", '\u2090');
		//SUBSUP_TO_UNICODE.put(".sub.b", '');
		//SUBSUP_TO_UNICODE.put(".sub.c", '');
		//SUBSUP_TO_UNICODE.put(".sub.d", '');
		//SUBSUP_TO_UNICODE.put(".sub.i", '');
		SUBSUP_TO_UNICODE.put(".sub.e", '\u2091');
		SUBSUP_TO_UNICODE.put(".sub.o", '\u2092');
		SUBSUP_TO_UNICODE.put(".sub.x", '\u2093');
		SUBSUP_TO_UNICODE.put(".sub.h", '\u2095');
		SUBSUP_TO_UNICODE.put(".sub.k", '\u2096');
		SUBSUP_TO_UNICODE.put(".sub.l", '\u2097');
		
		/*
		SUBSUP_TO_UNICODE.put(".sub.PHI.", '');
		SUBSUP_TO_UNICODE.put(".sup.PHI.", '');
		
		SUBSUP_TO_UNICODE.put(".sup..parallel.", '');
		SUBSUP_TO_UNICODE.put(".sub..parallel.", '');
		SUBSUP_TO_UNICODE.put(".sup..fwdarw.", '');
		SUBSUP_TO_UNICODE.put(".sup..fwdarw.", '');
		SUBSUP_TO_UNICODE.put(".sub..rarw.", '');
		*/
		
		// NAME_TO_UNICODE.put(".sup.1/8", "");
		// NAME_TO_UNICODE.put(".sub.1/8", "");
		// NAME_TO_UNICODE.put(".sup.3/8", "");
		// NAME_TO_UNICODE.put(".sub.3/8", "");
		// NAME_TO_UNICODE.put(".sup.5/8", "");
		// NAME_TO_UNICODE.put(".sub.5/8", "");
		// NAME_TO_UNICODE.put(".sup.7/8", "");
		// NAME_TO_UNICODE.put(".sub.7/8", "");
		// NAME_TO_UNICODE.put(".sup.1/3", "");
		// NAME_TO_UNICODE.put(".sub.1/3", "");
		// NAME_TO_UNICODE.put(".sup.2/3", "");
		// NAME_TO_UNICODE.put(".sub.2/3", "");
		//NAME_TO_UNICODE.put(".sup.1/4", "¼");
		// NAME_TO_UNICODE.put(".sub.1/4", "");
		// NAME_TO_UNICODE.put(".sup.1/2", "");
		// NAME_TO_UNICODE.put(".sub.1/2", "");
		// NAME_TO_UNICODE.put(".sup.3/4", "");
		// NAME_TO_UNICODE.put(".sub.3/4", "");
		
		// NAME_TO_UNICODE.put(".sup..lmtoreg.", "");
		// NAME_TO_UNICODE.put(".sub..lmtoreg.", "");
		
		// NAME_TO_UNICODE.put(".sup..lmtorsim.", "");
		// NAME_TO_UNICODE.put(".sub..lmtorsim.", "");
	}

	public static String replace(final String orig){
		StringBuffer stb = new StringBuffer();
		Matcher matcher = ENTITY_PATTERN.matcher(orig);
		while(matcher.find()){
			String match = matcher.group();
			//System.out.println("pattern match: " + match);
			if (NAME_TO_UNICODE.containsKey(match)){
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
	public static String replaceSubSupHTML(final String orig){
		StringBuilder stb = new StringBuilder(orig);
		Matcher matcher = SUBSUP_PATTERN.matcher(orig);
		int additionalChars = 0;
		while (matcher.find()) {
			String fullMatch = matcher.group(0);
			//System.out.println("subsup pattern match: " + fullMatch);

			String subSupMatch = matcher.group(1);

			String startTag = "";
			String endTag = "";
			switch(subSupMatch){
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
