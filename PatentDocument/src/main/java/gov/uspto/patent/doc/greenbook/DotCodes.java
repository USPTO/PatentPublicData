package gov.uspto.patent.doc.greenbook;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Greenbook Dot Codes for Special Characters mapped to their Unicode symbol.
 * 
 * https://r12a.github.io/apps/conversion/
 * http://sites.psu.edu/symbolcodes/accents/math/mathchart
 */
public class DotCodes {

	private static final Pattern ENTITY_PATTERN = Pattern.compile("\\.[A-z]+\\.");
	private static final Pattern FRACTION_PATTERN = Pattern.compile("\\b[1-9]/[1-9][0-9]*\\b");
	private static final Pattern SUBSUP_PATTERN = Pattern.compile("\\b\\.(su[bp]|s[bp]s[bp])\\.{1,2}([A-z0-9/+-÷×#]+)\\.?\\b");

	private static final HashMap<String, String> FRACTION_TO_UNICODE = new HashMap<String, String>();
	static {
		FRACTION_TO_UNICODE.put("1/4", "¼");  // &#xbc; 
		FRACTION_TO_UNICODE.put("1/2", "½");  // &#xbd; 
		FRACTION_TO_UNICODE.put("3/4", "¾");  // &#xbe; 
		FRACTION_TO_UNICODE.put("1/3", "⅓");  // &#x2153; 
		FRACTION_TO_UNICODE.put("2/3", "⅔");  // &#x2154; 
		FRACTION_TO_UNICODE.put("1/5", "⅕");  // &#x2155;
		FRACTION_TO_UNICODE.put("2/5", "⅖");  // &#x2156; 
		FRACTION_TO_UNICODE.put("3/5", "⅗");  // &#x2157; 
		FRACTION_TO_UNICODE.put("4/5", "⅘");  // &#x2158;
		FRACTION_TO_UNICODE.put("1/6", "⅙");  // &‌#x2159;
		FRACTION_TO_UNICODE.put("5/6", "⅚");  // &‌#x215A;
		FRACTION_TO_UNICODE.put("1/8", "⅛");  // &#x215b; 
		FRACTION_TO_UNICODE.put("3/8", "⅜");  // &#x215c; 
		FRACTION_TO_UNICODE.put("5/8", "⅝");  // &#x215d; 
		FRACTION_TO_UNICODE.put("7/8", "⅞");  // &#x215e; 
	}

	public static String fraction2number(String fraction){
		DecimalFormat df = new DecimalFormat("#.####");
		df.setRoundingMode(RoundingMode.CEILING);
		String[] parts = fraction.split("/");
		double fraction1 = (double) Integer.valueOf(parts[0]) / Integer.valueOf(parts[1]);
		//(double)Math.round(value * 100000d) / 100000d
		//return fraction1;
		return df.format(fraction1);
	}

	private static final HashMap<String, String> NAME_TO_UNICODE = new HashMap<String, String>();
	static {
		// Trademark and Copywrite
		NAME_TO_UNICODE.put(".TM.", "™"); // trademark symbol.  &#x2122; 
		NAME_TO_UNICODE.put(".SM.", "℠"); // service mark symbol  &#x2120;
		NAME_TO_UNICODE.put(".RTM.", "®"); // registered trademark symbol    &#xae;  
		NAME_TO_UNICODE.put(".COPYRG.", "©"); // copyright symbol   &#xa9; 
		
		// Arrows [ unicode block arrows: U+2190 to U+21FF ]
		NAME_TO_UNICODE.put(".uparw.", "↑"); // up arrow symbol &#x2191;
		NAME_TO_UNICODE.put(".fwdarw.", "→"); // forward arrow (arrow - right) symbol  &#x2192;
		NAME_TO_UNICODE.put(".dwnarw.", "↓"); // Down arrow symbol  &#x2193; 
		NAME_TO_UNICODE.put(".rarw.", "←"); // reverse arrow - left  &#x2190;
		
		// Greek Letters [ unicode block Greek and Coptic: U+0370 to U+03FF ]
		NAME_TO_UNICODE.put(".alpha.", "α"); // greek letter alpha - lowercase symbol  &#x3b1; 
		NAME_TO_UNICODE.put(".beta.", "β"); // greek letter beta  &#x3b2; 
		NAME_TO_UNICODE.put(".GAMMA.", "Γ"); // greek letter gamma upper case symbol  &#x393; 
		NAME_TO_UNICODE.put(".gamma.", "γ"); // greek letter gamma lower case symbol  &#x3b3; 
		NAME_TO_UNICODE.put(".DELTA.", "Δ"); // greek letter delta upper case symbol (increment)  &#x3b3; 
		NAME_TO_UNICODE.put(".delta.", "δ"); // greek letter delta lower case symbol  &#x3b4; 
		NAME_TO_UNICODE.put(".EPSILON.", "Ε"); // greek letter epsilon upper case symbol  &#x395; 
		NAME_TO_UNICODE.put(".epsilon.", "ε"); // greek letter epsilon lower case symbol  &#x3b5;  
		NAME_TO_UNICODE.put(".zeta.", "ζ"); // greek letter zeta symbol  &#x3b6;
		NAME_TO_UNICODE.put(".eta.", "η"); // greek letter eta  &#x3b7;
		NAME_TO_UNICODE.put(".THETA.", "Θ"); // greek letter theta upper case symbol  &#x398; 
		NAME_TO_UNICODE.put(".theta.", "θ"); // greek letter theta lower case symbol   &#x3b8; 
		NAME_TO_UNICODE.put(".iota.", "ι"); // greek letter iota symbol  &#x3b9;
		NAME_TO_UNICODE.put(".kappa.", "κ"); // greek letter kappa symbol  &#x3ba;
		NAME_TO_UNICODE.put(".LAMBDA.", "Λ"); // greek letter lamba upper case symbol  &#x3ba;
		NAME_TO_UNICODE.put(".lambda.", "λ"); // greek letter lamba lower case symbol  &#x3bb; 
		NAME_TO_UNICODE.put(".mu.", "μ"); // greek letter mu symbol, used in Statistics  &#x3bc;  
		NAME_TO_UNICODE.put(".nu.", "ν"); // greek nu symbol  &#x3bd;  
		NAME_TO_UNICODE.put(".XI.", "Ξ"); // greek xi upper case symbol  &#x3bd;  
		NAME_TO_UNICODE.put(".xi.", "ξ"); // greek xi lower case symbol  &#x3be;  
		NAME_TO_UNICODE.put(".omicron.", "ο"); // greek omicron symbol  &#x3bf;   
		NAME_TO_UNICODE.put(".pi.", "π"); // greek pi symbol  &#x3c0;    
		NAME_TO_UNICODE.put(".rho.", "ρ"); // greek rho symbol  &#x3c1;   
		NAME_TO_UNICODE.put(".sigma.", "σ"); // greek letter sigma lower case, used in Statistics &#x3c3; 
		NAME_TO_UNICODE.put(".SIGMA.", "Σ"); // greek letter sigma upper case, used in Statistics &#x3a3;   
		NAME_TO_UNICODE.put(".tau.", "τ"); // greek tau lower case symbol  &#x3c4; 
		NAME_TO_UNICODE.put(".UPSILON.", "Υ"); // greek upsilon upper case symbol  &#x3a5; 
		NAME_TO_UNICODE.put(".upsilon.", "υ"); // greek upsilon lower case symbol  &#x3c5;
		NAME_TO_UNICODE.put(".PHI.", "Φ"); // greek phi upper case symbol  &#x3a6;     
		NAME_TO_UNICODE.put(".phi.", "φ"); // greek phi lower case symbol  &#x3c6;
		NAME_TO_UNICODE.put(".chi.", "χ"); // greek letter chi lower case,used in Statistics  &#x3c7;   
		NAME_TO_UNICODE.put(".PSI.", "Ψ"); // greek psi upper case symbol  &#x3a8;     
		NAME_TO_UNICODE.put(".psi.", "ψ"); // greek psi lower case symbol  &#x3c8;    
		NAME_TO_UNICODE.put(".OMEGA.", "Ω"); // greek omega upper case symbol  &#x3a9;     
		NAME_TO_UNICODE.put(".omega.", "ω"); // greek omega lower case symbol  &#x3c9;

		// Mathematical Operators [ Unicode Block U+2200 to U+22FF ]
		NAME_TO_UNICODE.put(".differential.", "∂"); // differential; &#x2202;
		NAME_TO_UNICODE.put(".increment.", "∆"); // increment symbol ; &#x2206;
		NAME_TO_UNICODE.put(".gradient.", "∇"); // gradient NABLA symbol &#x2207; 
		NAME_TO_UNICODE.put(".-+.", "∓"); // minus-or-plus symbol &#x2213;
		NAME_TO_UNICODE.put(".+-.", "±"); // plus-or-minus symbol ; &#177;
		NAME_TO_UNICODE.put(".sqroot.", "√"); // SQUARE ROOT &#x221A;
		NAME_TO_UNICODE.put(".cuberoot.", "∛"); // cube root symbol &#8731;
		NAME_TO_UNICODE.put(".fourthroot.", "∜"); // fourth root symbol  &#x221C;
		NAME_TO_UNICODE.put(".4th root.", "∜"); // fourth root symbol  &#x221C;
		
		NAME_TO_UNICODE.put(".ltoreq.", "≤"); // &#x2264;
		NAME_TO_UNICODE.put(".gtoreq.", "≥"); // &#x2265;
		NAME_TO_UNICODE.put(".congruent.", "≅"); // congruent (approximately equal to) &#x2245;
		NAME_TO_UNICODE.put(".approxeq.", "≅"); // congruent (approximately equal to)  &#x2245;
		NAME_TO_UNICODE.put(".times.", "×"); // Multiplication sign ; &#215;
		NAME_TO_UNICODE.put(".div.", "÷"); // DIVISION SLASH ∕ symbol is &#x2215;  DIVIDES (U+2223) symbol 	∣ is &#x2223; ; DIVISION SIGN ÷ is &#xf7;
		NAME_TO_UNICODE.put(".notlessthan.", "≮"); // not less than symbol  &#x226e; 
		NAME_TO_UNICODE.put(".notgreaterthan.", "≯"); // not greater than symbol  &#x226f; 
		NAME_TO_UNICODE.put(".not <.", "≮"); // not less than.
		NAME_TO_UNICODE.put(".not >.", "≯"); // not greater than
		NAME_TO_UNICODE.put(".infin.", "∞"); // &#x221E;
		NAME_TO_UNICODE.put(".multidot.", "·"); // multiplication dot symbol.  &#xb7; 
		NAME_TO_UNICODE.put(".angle.", "∠"); // angle symbol  &#x2220; 
		NAME_TO_UNICODE.put(".parallel.", "∥"); // parallel to symbol  &#x2225;  
		NAME_TO_UNICODE.put(".vertline.", "|"); // vertical line symbol 
		NAME_TO_UNICODE.put(".perp.", "⊥"); // perpendicular (up tack) symbol  &#x22a5;
		NAME_TO_UNICODE.put(".intg.", "∫"); // integral symbol. &#x222B;
		NAME_TO_UNICODE.put(".andgate.", "∩"); // and gate (intersection) symbol  &#x2229; 
		NAME_TO_UNICODE.put(".orgate.", "∪"); // or gate (union)   &#x222a;  
		NAME_TO_UNICODE.put(".thrfore.", "∴"); // therefore symbol  &#x2234;
		NAME_TO_UNICODE.put(".because.", "∵"); // because symbol  &#x2235;
		NAME_TO_UNICODE.put(".perspectiveto.", "≃"); // perspective to (asymptotically equal to) symbol  &#x2243; 
		NAME_TO_UNICODE.put(".about.", "≅"); // APPROXIMATELY EQUAL TO: ≅  &#x2245; ,  difference (tilde) symbol ˜  &#x223c;  &sim;

		NAME_TO_UNICODE.put(".noteq.", "≠"); // NOT EQUALS symbol. &#x2260;
		NAME_TO_UNICODE.put(".ident.", "≡"); // &#x2261;
		NAME_TO_UNICODE.put(".notident.", "≢"); // &#x2262;
		NAME_TO_UNICODE.put(".lmtoreq.", "≤"); // less than or equal  	&#x2264;
		NAME_TO_UNICODE.put(".gtoreq.", "≥");  // greater or equal  &#x2265;

		NAME_TO_UNICODE.put(".apprxeq.", "≅");  // Approx. equal symbol  (also found ≈)
		NAME_TO_UNICODE.put(".varies.", "∝"); // varies as (proportional to)  &#x221d; 
		NAME_TO_UNICODE.put(".sym.", "⊕"); // positive earth (circled plus) symmetry symbol  &#x2295;  
		NAME_TO_UNICODE.put(".crclbar.", "⊖"); // negative earth (circled minus) symbol (circle bar)  &#x2296; 
		NAME_TO_UNICODE.put(".apprch.", "⟶"); // approaches the limit symbol  &#x2250; 

		NAME_TO_UNICODE.put(".lmorsim.", "≲"); // less than or approx
		NAME_TO_UNICODE.put(".gtorsim.", "≳"); // greater than or approx

		// Measurement
		NAME_TO_UNICODE.put(".ANG.", "Å"); // Angstrom upper case
		NAME_TO_UNICODE.put(".ang.", "Å"); // Angstrom lower case
		NAME_TO_UNICODE.put(".degree.", "°"); // &#xb0;
		NAME_TO_UNICODE.put(".cent.", "¢"); // &#162; U+00A2
		NAME_TO_UNICODE.put(".permill.", "‰"); // per mill symbol (salinity)  &#8240;

		// Sex
		NAME_TO_UNICODE.put(".female.", "♀"); // female symbol   &#x2640; 
		NAME_TO_UNICODE.put(".male.", "♂"); // male symbol  &#x2642;  

		// Music
		NAME_TO_UNICODE.put(".music-sharp.", "♯"); // music sharp symbol  &#x266F;
		NAME_TO_UNICODE.put(".music-flat.", "♭"); // music flat symbol  &#x266D;
		NAME_TO_UNICODE.put(".music-natrual.", "♮"); // music natural symbol  &#x266e;

		// Chemical
		NAME_TO_UNICODE.put(".dbd.", "═"); // double bond symbol &#x2550;
		NAME_TO_UNICODE.put(".guadbond.", "≃"); // guadbond symbol  &#x2243; 
		NAME_TO_UNICODE.put(".tbd.", "≡"); // identical (equivalent) – (triple bond) symbol &#x2261; 
		NAME_TO_UNICODE.put(".reveaction.", "⇌"); // reversible reaction symbol

		NAME_TO_UNICODE.put(".function.", "ƒ"); // function symbol  &#x192;
		NAME_TO_UNICODE.put(".hoarfrost.", "⊔"); // hoarfrost (square cup) symbol &#x2294;
		NAME_TO_UNICODE.put(".sctn.", "§"); // section symbol   &#xa7; 

		// Geometric Shapes [ unicode block U+25A0 to U+25FF ]
		NAME_TO_UNICODE.put(".cndot.", "•"); // solid dot - center symbol
		NAME_TO_UNICODE.put(".quadrature.", "□"); // quadrature (white square) symbol &#x25a1;
		NAME_TO_UNICODE.put(".smallcircle.", "○"); // small circle
		NAME_TO_UNICODE.put(".largecircle.", "◯"); // large circle
		NAME_TO_UNICODE.put(".circle.", "●"); // large circle   &#x25ef;
		NAME_TO_UNICODE.put(".dottedcircle.", "◌"); // dotted circle  &#x25cc;
		NAME_TO_UNICODE.put(".lhalfcircle.", "◐"); // left half of circle   	&#x25d0;
		NAME_TO_UNICODE.put(".rhalfcircle.", "◑"); // right half of circle  &#x25d1;
		NAME_TO_UNICODE.put(".solthalfcircle.", "◓"); // top half of circle &#x25d3;
		NAME_TO_UNICODE.put(".solbhalfcircle.", "◒"); // bottom half of circle  	&#x25d2;
		NAME_TO_UNICODE.put(".circleincircle.", "◎"); // circle in a large circle "BULLSEYE"  &#x25ce;
		NAME_TO_UNICODE.put(".THorizBrace.", "︷"); // Top horizonal brace
		NAME_TO_UNICODE.put(".BHorizBrace.", "︸"); // Bottom horizonal brace
		NAME_TO_UNICODE.put(".0.", "∅"); // Slash Zero
		NAME_TO_UNICODE.put(".prime.", "′"); // prime symbol
		NAME_TO_UNICODE.put(".dblprime.", "″"); // double prime symbol

		// NAME_TO_UNICODE.put(".dottlhalfcircle.", ""); // dotted left half circle
		// NAME_TO_UNICODE.put(".dottrhalfcircle.", ""); // dotted right half circle
		// NAME_TO_UNICODE.put(".dottbtalfcircle.", ""); // dotted top half circle
		// NAME_TO_UNICODE.put(".dottbhalfcircle.", ""); // dotted bottom half circle
		NAME_TO_UNICODE.put(".dblquote.", "\"");
		NAME_TO_UNICODE.put(".En.", "‥"); // leader	
		NAME_TO_UNICODE.put(".[.", "["); // open bold bracket (reissue)
		NAME_TO_UNICODE.put(".].", "]"); // close bold bracket (reissue)
	}

	private static final HashMap<String, String> SUBSUP_TO_UNICODE = new HashMap<String, String>();
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

		SUBSUP_TO_UNICODE.put(".sup.(", "⁽");
		SUBSUP_TO_UNICODE.put(".sub.(", "₍");
		SUBSUP_TO_UNICODE.put(".sup.)", "⁾");
		SUBSUP_TO_UNICODE.put(".sub.)", "₎");
		SUBSUP_TO_UNICODE.put(".sup.+", "⁺");
		SUBSUP_TO_UNICODE.put(".sub.+", "₊");
		SUBSUP_TO_UNICODE.put(".sup.-", "⁻");
		SUBSUP_TO_UNICODE.put(".sub.-", "₋");
		// SUBSUP_TO_UNICODE.put(".sup.+-", "");
		// SUBSUP_TO_UNICODE.put(".sub.+-", "");
		// SUBSUP_TO_UNICODE.put(".sup.-+", "");
		// SUBSUP_TO_UNICODE.put(".sub.-+", "");
		// SUBSUP_TO_UNICODE.put(".sup.*", "");
		// SUBSUP_TO_UNICODE.put(".sub.*", "");
		SUBSUP_TO_UNICODE.put(".sup.=", "⁼");
		SUBSUP_TO_UNICODE.put(".sub.=", "₌");

		SUBSUP_TO_UNICODE.put(".permill.", "‰"); // per mill symbol (salinity)  &#x2030; 

		SUBSUP_TO_UNICODE.put(".sup.0", "⁰");
		SUBSUP_TO_UNICODE.put(".sup.1", "¹");
		SUBSUP_TO_UNICODE.put(".sup.2", "²");
		SUBSUP_TO_UNICODE.put(".sup.3", "³");
		SUBSUP_TO_UNICODE.put(".sup.4", "⁴");
		SUBSUP_TO_UNICODE.put(".sup.5", "⁵");
		SUBSUP_TO_UNICODE.put(".sup.6", "⁶");
		SUBSUP_TO_UNICODE.put(".sup.7", "⁷");
		SUBSUP_TO_UNICODE.put(".sup.8", "⁸");
		SUBSUP_TO_UNICODE.put(".sup.9", "⁹");
		
		SUBSUP_TO_UNICODE.put(".sup.a", "ᵃ");
		SUBSUP_TO_UNICODE.put(".sup.b", "ᵇ");
		SUBSUP_TO_UNICODE.put(".sup.c", "ᶜ");
		SUBSUP_TO_UNICODE.put(".sup.d", "ᵈ");
		SUBSUP_TO_UNICODE.put(".sup.e", "ᵉ");
		SUBSUP_TO_UNICODE.put(".sup.f", "ᶠ");
		SUBSUP_TO_UNICODE.put(".sup.g", "ᵍ");
		SUBSUP_TO_UNICODE.put(".sup.h", "ʰ");
		SUBSUP_TO_UNICODE.put(".sup.i", "ⁱ");
		SUBSUP_TO_UNICODE.put(".sup.j", "ʲ");
		SUBSUP_TO_UNICODE.put(".sup.k", "ᵏ");
		SUBSUP_TO_UNICODE.put(".sup.l", "ˡ");
		SUBSUP_TO_UNICODE.put(".sup.m", "ᵐ");
		SUBSUP_TO_UNICODE.put(".sup.n", "ⁿ");
		SUBSUP_TO_UNICODE.put(".sup.o", "ᵒ");
		SUBSUP_TO_UNICODE.put(".sup.p", "ᵖ");
		//SUBSUP_TO_UNICODE.put(".sup.q", "");
		SUBSUP_TO_UNICODE.put(".sup.r", "ʳ");
		SUBSUP_TO_UNICODE.put(".sup.s", "ˢ");
		SUBSUP_TO_UNICODE.put(".sup.t", "ᵗ");
		SUBSUP_TO_UNICODE.put(".sup.u", "ᵘ");
		SUBSUP_TO_UNICODE.put(".sup.v", "ᵛ");
		SUBSUP_TO_UNICODE.put(".sup.w", "ʷ");
		SUBSUP_TO_UNICODE.put(".sup.x", "ˣ");
		SUBSUP_TO_UNICODE.put(".sup.y", "ʸ");
		SUBSUP_TO_UNICODE.put(".sup.z", "ᶻ");

		SUBSUP_TO_UNICODE.put(".sub.0", "₀");
		SUBSUP_TO_UNICODE.put(".sub.1", "₁");
		SUBSUP_TO_UNICODE.put(".sub.2", "₂");
		SUBSUP_TO_UNICODE.put(".sub.3", "₃");
		SUBSUP_TO_UNICODE.put(".sub.4", "₄");
		SUBSUP_TO_UNICODE.put(".sub.5", "₅");
		SUBSUP_TO_UNICODE.put(".sub.6", "₆");
		SUBSUP_TO_UNICODE.put(".sub.7", "₇");
		SUBSUP_TO_UNICODE.put(".sub.8", "₈");
		SUBSUP_TO_UNICODE.put(".sub.9", "₉");
		
		SUBSUP_TO_UNICODE.put(".sub.a", "ₐ");
		//SUBSUP_TO_UNICODE.put(".sub.b", "ₐ");
		//SUBSUP_TO_UNICODE.put(".sub.c", "ₐ");
		//SUBSUP_TO_UNICODE.put(".sub.d", "ₐ");
		SUBSUP_TO_UNICODE.put(".sub.e", "ₑ");
		SUBSUP_TO_UNICODE.put(".sub.i", "ᵢ");
		SUBSUP_TO_UNICODE.put(".sub.i", "ᵢ");
		SUBSUP_TO_UNICODE.put(".sub.o", "ₒ");
		SUBSUP_TO_UNICODE.put(".sub.r", "ᵣ");
		SUBSUP_TO_UNICODE.put(".sub.u", "ᵤ");
		SUBSUP_TO_UNICODE.put(".sub.v", "ᵥ");
		SUBSUP_TO_UNICODE.put(".sub.x", "ₓ");
		
		SUBSUP_TO_UNICODE.put(".sub.PHI.", "ᵩ");
		SUBSUP_TO_UNICODE.put(".sup.PHI.", "ᵠ");
		
		SUBSUP_TO_UNICODE.put(".sup..parallel.", "∥");
		SUBSUP_TO_UNICODE.put(".sub..parallel.", "∥");
		SUBSUP_TO_UNICODE.put(".sup..fwdarw.", "→");
		SUBSUP_TO_UNICODE.put(".sup..fwdarw.", "→");
		SUBSUP_TO_UNICODE.put(".sub..rarw.", "←");
		
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
			System.out.println("pattern match: " + match);
			if (NAME_TO_UNICODE.containsKey(match)){
				matcher.appendReplacement(stb, NAME_TO_UNICODE.get(match));
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
			System.out.println("subsup pattern match: " + fullMatch);

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
