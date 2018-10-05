package gov.uspto.patent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.entity.NameOrg;

/**
 * Company Synonym Generator
 *
 * Generate synonyms by removing company prefix and suffixes.
 *
 * 1. Within Patent XML suffix variations exist, sometimes they are abbreviated or abbreviated differently:
 *   Corp. => Corporation
 *     Co. => Company
 *    Ltd. => Limited
 *    Inc. => Incorporated
 *    L.P. => Limited Partnership
 *
 * 2. Trailing comma is not the best indicator of suffix, comma is not always present and names can contain a comma.
 *
 * 3. Variation with Space around ampersand "&" sign. 'M&P' == 'M & P'
 *
 * @author Brian Feldman <brian.feldman@uspto.gov>
 *
 */
public class OrgSynonymGenerator {

	private static final Logger LOGGER = LoggerFactory.getLogger(OrgSynonymGenerator.class);

	// https://en.wikipedia.org/wiki/List_of_legal_entity_types_by_country
	private static final Pattern ORG_SUFFIX_PATTERN = Pattern.compile(
			"^(.+?),? (Co\\.?, [Ll]td|Co(?:\\.|mpany)?, LLC|(?:P\\.?)?(?:L\\.?)?L\\.?C|[Pp][Ll][Cc]|(?:P(?:ty|vt|te|TE)[,\\.]? )?(?:L(?:td|TD)|Limited)|LLP|LTD|Inc(?:orporated)?|(?:G|Ges)?mbH|Cooperative Association Ltd|Limited Partnership|Ltda|(?:GmbH|Ltd\\.?|Limited|AG) & Co\\.? (?:KG(?:aA)?|OHG)|A[BGS]|(?:[Nn]aamloze|[Bb]esloten) [Vv]ennootschap|Kabu(?:sh|hs)iki Kaisha|[BN]\\.?\\s?V\\.?|S\\.?A\\.?(:?S\\.?)?|S\\.?p\\.?A\\.?|[SGL]\\.?P\\.?|A/S|S\\.?L\\.?|S\\.[Rr]\\.[Ll]\\.|K\\.?K\\.?)\\.?$");

	private static final Pattern COMPANY_PATTERN = Pattern
			.compile("(.+) (Operating Company|(?<!(:?&|[Aa]nd) )Co(?:\\.|mpany)|Corp(?:oration|orate|\\.)|CORPORATION|Coop(\\\\.?|erative)?(?: Association)?|Association|Incorporation|PTE),?$");

	// Kabushiki Kaisha Toshiba == Toshiba Kabushiki Kaisha == Toshiba K.K.
	private static final Pattern ORG_PREFIX_PATTERN = Pattern.compile("^(The|Kabushiki Kaisha|Koninklijke|Kommandiittiyhtiö|Firma|Compagnie) (.+)$");

	private Matcher leadCompanyMatcher = ORG_PREFIX_PATTERN.matcher("");
	private Matcher suffixMatcher = ORG_SUFFIX_PATTERN.matcher("");
	private Matcher companyMatcher = COMPANY_PATTERN.matcher("");

	private String currentTxt = "";
	private Set<String> suffixes = new LinkedHashSet<String>();

	/**
	 * Expand Company Name to a possible list of synonym variants.
	 * 
	 * @param name
	 */
	public void computeSynonyms(NameOrg name) {
		name.addSynonymNorm(name.getName());

		/*
		 *  Variation 1 level.
		 *  	TOP->prefix
		 *  	TOP->suffix
		 *  	TOP->company//
		 */
		processAbbrev(name, lastWordsAbbrev(name, name.getName()));
		companyTerms(name, name.getName());
		String prefixLvl1 = prefix(name, name.getName());
		String suffixLvl1 = suffix(name, name.getName());

		/*
		 *  Variation 2 levels. 
		 *  	TOP->prefix->suffix
		 *  	TOP->prefix->company//
		 *  	TOP->suffix->company//
		 *  	TOP->prefix->lastWordsAbbrev//
		 *  	TOP->suffix->lastWordsAbbrev//
		 */
		String prefixSuffix2 = prefix(name, suffixLvl1);
		companyTerms(name, prefixLvl1);
		companyTerms(name, suffixLvl1);
		processAbbrev(name, lastWordsAbbrev(name, suffixLvl1));
		processAbbrev(name, lastWordsAbbrev(name, prefixLvl1));

		/*
		 *  Variation 3 levels;  
		 *  	TOP->prefix->suffix->company//	
		 */
		companyTerms(name, prefixSuffix2);

		// Process Synonyms
		ampersandVariantSynonyms(name);
		andWords(name);

		name.setSuffix(suffixes.toString());
	}

	/**
	 * Process Returned Abbreviated expanded synonym/variants.
	 * 
	 * @param abbrevs
	 */
	private void processAbbrev(NameOrg name, Set<String> abbrevs){
		//LOGGER.info("lastWordsAbbrev : {} -> {}", name.getName(),  abbrevSet1);
		for(String abbrevVar: abbrevs) {
			companyTerms(name, abbrevVar);

			String abbrevSuffix = suffix(name, abbrevVar);
			String abbrevPrefix =  prefix(name, abbrevVar);
			companyTerms(name, abbrevSuffix);
			companyTerms(name, abbrevPrefix);

			String abbrevSuffixPrefix = prefix(name, abbrevSuffix);
			companyTerms(name, abbrevSuffixPrefix);
		}
	}

	private String prefix(NameOrg name, String currentTxt) {
		leadCompanyMatcher.reset(currentTxt);
		String shortTitle = null;
		if (leadCompanyMatcher.find()) {
			String coPrefix = leadCompanyMatcher.group(1);
			shortTitle = leadCompanyMatcher.group(2);
			name.addSynonymNorm(shortTitle);
			name.setPrefix(coPrefix);
		}

		return shortTitle != null ? shortTitle : currentTxt;
	}

	private String suffix(NameOrg name, String currentTxt) {
		String regSuf = suffixMatchRegex(name, currentTxt);
		return suffixWord(name, regSuf);
	}

	private String suffixMatchRegex(NameOrg name, String currentTxt) {
		suffixMatcher.reset(currentTxt);
		String shortTitle = null;
		if (suffixMatcher.find()) {
			shortTitle = suffixMatcher.group(1);
			String suffix = suffixMatcher.group(2);
			suffixes.add(suffix);
			name.addSynonymNorm(shortTitle);
		}

		return shortTitle != null ? shortTitle : currentTxt;
	}

	private String companyTerms(NameOrg name, String currentTxt) {
		companyMatcher.reset(currentTxt);
		String shortTitle = null;
		if (companyMatcher.find()) {
			shortTitle = companyMatcher.group(1);
			String coSuffix = companyMatcher.group(2);
			suffixes.add(coSuffix);			
			name.addSynonymNorm(shortTitle);
		}

		return shortTitle != null ? shortTitle : currentTxt;
	}


	private static Map<String, List<CountryCode>> AND_WORDS = new HashMap<String, List<CountryCode>>();
	static {
		AND_WORDS.put("and", Arrays.asList(CountryCode.US));
		AND_WORDS.put("und", Arrays.asList(CountryCode.DE));
		//AND_WORDS.put("en", Arrays.asList(CountryCode.BE, CountryCode.NL));
		//AND_WORDS.put("ja", Arrays.asList(CountryCode.FI, CountryCode.SE));
		//AND_WORDS.put("et", Arrays.asList(CountryCode.FR));
	}

	/**
	 * And Words
	 *
	 * and, und ==> &
	 *
	 * @param name
	 */
	private void andWords(NameOrg name){
		String[] tokens = name.getName().split(" ");
		StringBuilder stringStb = new StringBuilder(tokens.length+1);
		boolean found = false;
		for (int i=0; i < tokens.length; i++) {
			// don't check the first and last words and maintain case to limit low risk of false positives.
			if (i > 0 && i < tokens.length-1 && AND_WORDS.containsKey(tokens[i])) {
				found = true;
				stringStb.append(" & ");
			}
			else {
				stringStb.append(" ").append(tokens[i]);
			}
		}

		if (found) {
			String newTitle = stringStb.toString().trim();

			for(String synonym: name.getSynonyms()) {
				String[] synTokens = synonym.split(" ");
				StringBuilder synStb = new StringBuilder(synTokens.length+1);
				for (int i=0; i < synTokens.length; i++) {
					if (i > 0 && i < synTokens.length-1 && AND_WORDS.containsKey(synTokens[i].toLowerCase())) {
						synStb.append(" & ");
					}
					else {
						synStb.append(" ").append(synTokens[i]);
					}
				}
				String newSyn = synStb.toString().trim();
				name.addSynonymNorm(newSyn);
			}
		
			name.addSynonymNorm(newTitle);
		}
	}

	/**
	 * Ampersand Sign "&"
	 *
	 * "L&P" => "L & P"
	 * "L & P" => "L&P"
	 * 
	 * @param name
	 */
	private void ampersandVariantSynonyms(NameOrg name) {
		if (name.getName().indexOf("&") == -1){
			return;
		}

		int matchedOriginal = -1;
		if (name.getName().matches("^[A-Z]+&[A-Z]+\\s.+")) {
			String[] parts = name.getName().split("&");
			if (parts.length == 2) {
				name.addSynonymNorm(parts[0] + " & " + parts[1]);
				name.addSynonymNorm(parts[0] + " and " + parts[1]);
			}
			matchedOriginal = 1;
		}
		else if (name.getName().matches("^[A-Z]{1,2} & [A-Z]{1,2}\\s.+")) {
			String[] parts = name.getName().split("\\s&\\s");
			if (parts.length == 2) {
				name.addSynonymNorm(parts[0] + "&" + parts[1]);
				name.addSynonymNorm(parts[0] + " and " + parts[1]);
			}
			matchedOriginal = 2;
		}
		else if (name.getName().indexOf(" & ") != -1) {
			String[] parts = name.getName().split("\\s&\\s");
			if (parts.length == 2) {
				name.addSynonymNorm(parts[0] + " and " + parts[1]);
			}
			matchedOriginal = 3;
		}

		if (matchedOriginal != -1) {
			for(String synonym: name.getSynonyms()) {
				if (matchedOriginal == 1) {
					String[] parts = synonym.split("&");
					if (parts.length == 2) {
						name.addSynonymNorm(parts[0] + " & " + parts[1]);
						name.addSynonymNorm(parts[0] + " and " + parts[1]);
					}
				}
				else if (matchedOriginal == 2) {
					String[] parts = synonym.split("\\s&\\s");
					if (parts.length == 2) {
						name.addSynonymNorm(parts[0] + "&" + parts[1]);
						name.addSynonymNorm(parts[0] + " and " + parts[1]);
					}
				}
				else {
					String[] parts = synonym.split("\\s&\\s");
					if (parts.length == 2) {
						name.addSynonymNorm(parts[0] + " and " + parts[1]);
					}
				}
			}
		}
	}

	// Last Word Suffix Words
	private static Set<String> SUFFIX_WORD = new HashSet<String>();
	static {
		SUFFIX_WORD.add("aktiengesellschaft"); // abbrev: AG
		SUFFIX_WORD.add("aktiebolag"); // abbrev: AB, Ab, A/B
		SUFFIX_WORD.add("aktieselskab"); // abbrev: A/S
		SUFFIX_WORD.add("anpartsselskab"); // abbrev: ApS
		SUFFIX_WORD.add("kommanditgesellschaft"); // abbrev: KG
		SUFFIX_WORD.add("limitada");
		SUFFIX_WORD.add("proprietary");
		SUFFIX_WORD.add("stiftung");
		SUFFIX_WORD.add("incorporation");
		SUFFIX_WORD.add("associazione");
		SUFFIX_WORD.add("association");
		SUFFIX_WORD.add("cyfyngedig");
		SUFFIX_WORD.add("anghyfyngedig");
	}
	private String suffixWord(NameOrg name, String currentTxt) {
		int lastWrdIdx = currentTxt.lastIndexOf(" ")+1;
		String lastWord = currentTxt.substring(lastWrdIdx).replace(".", ".").toLowerCase();
		String leftSide = null;
		if (SUFFIX_WORD.contains(lastWord)) {
			leftSide = currentTxt.substring(0, lastWrdIdx-1);
			if (leftSide.endsWith(",")) {
				leftSide = currentTxt.substring(0, lastWrdIdx-2);
			}
			name.addSynonymNorm(leftSide);
		}
		return leftSide != null ? leftSide : currentTxt;
	}

	// Last Word Abbreviations
	private static Map<String, String> ABBREVS = new HashMap<String, String>();
	static {
		ABBREVS.put("ltd", "Limited");
		ABBREVS.put("inc", "Incorporation");
		ABBREVS.put("co", "Company");
		ABBREVS.put("corp", "Corporation");
		ABBREVS.put("mfg", "Manufacturing");
		ABBREVS.put("mgmt", "Management");
		ABBREVS.put("aps", "Anpartsselskab"); // ApS in Denmark.
		ABBREVS.put("kg", "Kommanditgesellschaft");
		ABBREVS.put("ag", "Aktiengesellschaft");
		ABBREVS.put("ab", "Aktiebolag");
		ABBREVS.put("k.k", "Kabushiki Kaisha");
		ABBREVS.put("kk", "Kabushiki Kaisha");
		ABBREVS.put("lp", "Limited Partnership");
		ABBREVS.put("n.v", "naamloze vennootschap"); // dutch
		ABBREVS.put("oy", "Osakeyhtio"); // Finish Osakeyhtiö
		ABBREVS.put("oyj", "Julkinen Osakeyhtio");  // Finish
		
	}

	private static Map<String, String> WORD_TO_ABBREVS = new HashMap<String, String>();
	static {
		WORD_TO_ABBREVS.put("limited", "Ltd.");
		WORD_TO_ABBREVS.put("limitada", "Lda.");
		WORD_TO_ABBREVS.put("incorporation", "Inc.");
		WORD_TO_ABBREVS.put("company", "Co.");
		WORD_TO_ABBREVS.put("corporation", "Corp.");
		WORD_TO_ABBREVS.put("manufacturing", "Mfg.");
		WORD_TO_ABBREVS.put("management", "Mgmt.");
		WORD_TO_ABBREVS.put("handelsbolag", "HB");	
		WORD_TO_ABBREVS.put("kommanditgesellschaft", "KG");
		WORD_TO_ABBREVS.put("aktiengesellschaft", "AG");
		WORD_TO_ABBREVS.put("aktiengessellschaft", "AG"); // misspelled version.
		WORD_TO_ABBREVS.put("anpartsselskab", "ApS");
		WORD_TO_ABBREVS.put("aktieselskab", "A/S");
		WORD_TO_ABBREVS.put("aktiebolag", "AB");
		WORD_TO_ABBREVS.put("allmennaksjeselskap", "ASA");
		WORD_TO_ABBREVS.put("aksjeselskap", "AS");
		WORD_TO_ABBREVS.put("kommandiittiyhtiö", "Ky");
		WORD_TO_ABBREVS.put("kommandiittiyhtio", "Ky");
		WORD_TO_ABBREVS.put("kommandittselskap", "KS");
		WORD_TO_ABBREVS.put("kommanditbolag", "Kb");
		WORD_TO_ABBREVS.put("kommanditselskab", "K/S");
		WORD_TO_ABBREVS.put("kommanditaktieselskab", "P/S");
		WORD_TO_ABBREVS.put("kommanditaktiengesellschaft", "KomAG");		
		WORD_TO_ABBREVS.put("kollektivgesellschaft", "KolG");
		WORD_TO_ABBREVS.put("Interessentskab", "I/S");
		WORD_TO_ABBREVS.put("osuuskunta", "osk");
		WORD_TO_ABBREVS.put("osakeyhtio", "Oy");
		WORD_TO_ABBREVS.put("osakeyhtiö", "Oy");
		WORD_TO_ABBREVS.put("osakeyhtio", "Oy");
		WORD_TO_ABBREVS.put("partnerselskab", "P/S");
		WORD_TO_ABBREVS.put("laboratory", "Lab");
		WORD_TO_ABBREVS.put("industry", "Ind.");
		WORD_TO_ABBREVS.put("nanotechnology", "NanoTech.");
		WORD_TO_ABBREVS.put("institute", "Inst.");
		WORD_TO_ABBREVS.put("services", "Svcs.");
		WORD_TO_ABBREVS.put("service", "Svc.");
		WORD_TO_ABBREVS.put("partnership", "Partners");
		WORD_TO_ABBREVS.put("pharmaceuticals", "Pharma.");
		WORD_TO_ABBREVS.put("industries", "Ind.");
		WORD_TO_ABBREVS.put("technology", "Tech.");
		WORD_TO_ABBREVS.put("international", "Intl.");
		WORD_TO_ABBREVS.put("association", "Assoc.");
		WORD_TO_ABBREVS.put("engineering", "Eng.");
		WORD_TO_ABBREVS.put("cooperative", "Coop.");
		WORD_TO_ABBREVS.put("enterprise", "Ent.");
	}

	/**
	 * Last two word abbreviation
	 * 
	 * 1. Expand common abbreviated suffixes; ending in period.
	 * 2. Abbreviate common suffixes.
	 * 
	 * @param name
	 * @return
	 */
	private Set<String> lastWordsAbbrev(NameOrg name, String currentTxt) {
		String[] tokens = currentTxt.split("\\s");

		Set<String> retSet = new HashSet<String>();

		StringBuilder stringStb = new StringBuilder();
		for (int i=0; i<tokens.length-2; i++) {
			stringStb.append(" ").append(tokens[i]);
		}
		String leftSide = stringStb.toString().trim();

		if (leftSide.length() > 0) {
			leftSide = leftSide + " ";
		}

		if (tokens.length < 2) {
			return retSet;
		}

		String word = tokens[tokens.length-2];
		String lastWord = tokens[tokens.length-1];
		boolean hasComma = false;
		if (word.endsWith(",")) {
			hasComma = true;
			word = word.substring(0, word.length()-1);
		}
	
		// String newStr1 = leftSide + word + ", " + lookupWordAbbrevs(lastWord);
		StringBuilder stb1 = new StringBuilder();
		stb1.append(leftSide);
		stb1.append(word);
		if (hasComma) {
			stb1.append(",");
		}
		stb1.append(" ");
		stb1.append(lookupWordAbbrevs(lastWord));
		String newStr1 = stb1.toString();
		if (!name.getName().equals(newStr1)) {
			name.addSynonymNorm(newStr1);
			retSet.add(newStr1);
		}

		//String newStr2 = leftSide + lookupWordAbbrevs(word) + ", " + lookupWordAbbrevs(lastWord);
		if (tokens.length > 2) {
			StringBuilder stb2 = new StringBuilder();
			stb2.append(leftSide);
			stb2.append(lookupWordAbbrevs(word));
			if (hasComma) {
				stb2.append(",");
			}
			stb2.append(" ");
			stb2.append(lookupWordAbbrevs(lastWord));
			String newStr2 = stb2.toString();
			if (!name.getName().equals(newStr2)) {
				name.addSynonymNorm(newStr2);
				retSet.add(newStr2);
			}

			//String newStr3 = leftSide + lookupWordAbbrevs(word) + ", " + lastWord;
			StringBuilder stb3 = new StringBuilder();
			stb3.append(leftSide);
			stb3.append(lookupWordAbbrevs(word));
			if (hasComma) {
				stb3.append(",");
			}
			stb3.append(" ");
			stb3.append(lastWord);
			String newStr3 = stb3.toString();
			if (!name.getName().equals(newStr3)) {
				name.addSynonymNorm(newStr3);
				retSet.add(newStr3);
			}
		}

		return retSet;
	}

	private String lookupWordAbbrevs(final String word){
		String wordLc = word.replaceAll("\\.", "").toLowerCase();
		if (wordLc.length() < 5 && ABBREVS.containsKey(wordLc)) {
			return ABBREVS.get(wordLc);
		} else if (WORD_TO_ABBREVS.containsKey(wordLc)) {
			return WORD_TO_ABBREVS.get(wordLc);
		}
		return word;
	}

	/*
	 * TODO build a smarter rule set using a config objects such as below:
	 * 
	private class CompanyType {
		private final String longForm;
		private Set<String> shortForm;
		private String shortFormRegex;
		private Set<CountryCode> countryCodes;

		CompanyType(final String longForm, final String[] shortForm, final String shortFormRegex, final CountryCode[] countries){
			this.longForm = longForm;
			this.shortForm.addAll(Arrays.asList(shortForm));
			this.shortFormRegex = shortFormRegex;
			this.countryCodes.addAll(Arrays.asList(countries));
		}

		public boolean hasCountry(CountryCode country) {
			return countryCodes.contains(country);
		}

		public boolean matchLongForm(String longForm) {
			return this.longForm.equals(longForm);
		}

		public boolean matchShortForm(String shortForm) {
			return this.shortForm.contains(shortForm);
		}
	}
	*/
}
