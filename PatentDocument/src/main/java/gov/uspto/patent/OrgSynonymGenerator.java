package gov.uspto.patent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.entity.Entity;
import gov.uspto.patent.model.entity.NameOrg;
import gov.uspto.patent.model.entity.NamePerson;

/**
 * Company Synonym Expansion Generator
 *
 * Generate synonyms by both removing and expanding company prefix and suffixes.<br/>
 * 
 *<p>1. Within Patent XML suffix variations exist, sometimes they are abbreviated or abbreviated differently: </br>
 *   Corp. => Corporation,
 *     Co. => Company,
 *    Ltd. => Limited,
 *    Inc. => Incorporated,
 *    L.P. => Limited Partnership
 *</p>
 *
 *<p>2. Trailing comma is not the best indicator of suffix, comma is not always present and names can contain a comma. </p>
 *
 *<p>3. Variation with Space around ampersand "&" sign. 'M&P' == 'M & P'</p>
 * 
 *<p>4. Abbreviated trailing words
 *    Ind. => Industries,
 *    Mgmt. => Management,
 *    Mfg. => Manufacturing
 *    LABS => Laboratories
 *</p>
 *
 * @author Brian Feldman <brian.feldman@uspto.gov>
 *
 */
public class OrgSynonymGenerator {

	// https://en.wikipedia.org/wiki/List_of_legal_entity_types_by_country
	private static final Pattern ORG_SUFFIX_PATTERN = Pattern.compile(
			"^(.+?),? (Co\\.?, [Ll]td|Co(?:\\.|mpany)?, LLC|(?:P\\.?)?(?:L\\.?)?L\\.?[CP]|[Pp][Ll][Cc]|(?:P(?:ty|TY|vt|VT|te|TE)[,\\.]? )?(?:L(?:td|TD)|Limited)|LLP|LTD|Inc(?:orporated)?|(?:G|Ges)?mbH|Cooperative Association Ltd|Limited Partnership|Ltda|(?:GmbH|Ltd\\.?|Limited|AG) & Co\\.? (?:[KO]G(?:aA)?|[Oo]HG)|A[BGS]|(?:[Nn]aamloze|[Bb]esloten) [Vv]ennootschap|Kabu(?:sh|hs)iki Kaisha|[BN]\\.?\\s?V\\.?|S\\.?A\\.?(:?S\\.?)?|S\\.?p\\.?A\\.?|[SGL]\\.?P\\.?|A/S|S\\.?L\\.?|S\\.([Aa]\\.?)?[Rr]\\.[Ll]\\.?|K\\.?K\\.?|S\\.A\\./N\\.V\\.)\\.?$");

	private static final Pattern COMPANY_PATTERN = Pattern
			.compile("(.+) ((?:Operating|Holding|Public Limited) Company|(?<!(:?&|[Aa]nd) )Co(?:\\.|mpany)|Corp(?:oration|orate|\\.)|CORPORATION|Coop(\\\\.?|erative)?(?: Association)?|Association|Incorporation|PTE|P\\.?[AC]\\.?),?$");

	// "Toshiba K.K." == ["Kabushiki Kaisha Toshiba", "Toshiba Kabushiki Kaisha", "Toshiba KK"]
	private static final Pattern ORG_PREFIX_PATTERN = Pattern.compile("^(The|Kabushiki Kaisha|Koninklijke|Kommandiittiyhtiö|Firma|Compagnie) (.+)$", Pattern.CASE_INSENSITIVE);

	private Matcher leadCompanyMatcher = ORG_PREFIX_PATTERN.matcher("");
	private Matcher suffixMatcher = ORG_SUFFIX_PATTERN.matcher("");
	private Matcher companyMatcher = COMPANY_PATTERN.matcher("");

	private Set<String> suffixes = new LinkedHashSet<String>();

	/**
	 * Expand Company Name to a possible list of synonym variants.
	 * 
	 * @param name
	 */
	public void computeSynonyms(Entity entity) {
		NameOrg name = (NameOrg) entity.getName();
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

		// Process All Synonyms
		andWords(name);
		andSymbolVariants(name);
		multiWordAbbrev(name);

		if (entity.getAddress() != null && CountryCode.CN.equals(entity.getAddress().getCountry())) {
			chineseCompanyNames(name);
		}

		name.setSuffix(suffixes.toString());
	}

	/**
	 * Process Returned Abbreviated expanded synonym/variants.
	 * 
	 * @param abbrevs
	 */
	protected void processAbbrev(NameOrg name, Set<String> abbrevs){
		for(String abbrevVar: abbrevs) {
			String abbrevSuffix = suffix(name, abbrevVar);
			String abbrevPrefix =  prefix(name, abbrevVar);
			companyTerms(name, abbrevSuffix);
			companyTerms(name, abbrevPrefix);

			String abbrevSuffixPrefix = prefix(name, abbrevSuffix);
			companyTerms(name, abbrevSuffixPrefix);
		}
	}

	protected String prefix(NameOrg name, String currentTxt) {
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

	protected String suffix(NameOrg name, String currentTxt) {
		String regSuf = suffixMatchRegex(name, currentTxt);
		return suffixWord(name, regSuf);
	}

	protected String suffixMatchRegex(NameOrg name, String currentTxt) {
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

	protected String companyTerms(NameOrg name, String currentTxt) {
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
		AND_WORDS.put("+", Arrays.asList(CountryCode.US, CountryCode.UK));
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
	protected void andWords(NameOrg name){
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
	 * And Symbol Variants [+&]
	 *
	 * "L&P" => ("L & P", "L + P")
	 * "L & P" => ("L&P", "L+P")
	 * 
	 * @param name
	 */
	protected void andSymbolVariants(NameOrg name) {
		for(String synonym: name.getSynonyms()) {
			String[] parts = synonym.split("\\s*[&\\+]\\s*");
			if (parts.length == 2) {
				//name.addSynonymNorm(parts[0] + "&" + parts[1]);
				//name.addSynonymNorm(parts[0] + "+" + parts[1]);
				name.addSynonymNorm(parts[0] + " & " + parts[1]);
				name.addSynonymNorm(parts[0] + " + " + parts[1]);
				name.addSynonymNorm(parts[0] + " and " + parts[1]);
			}
		}
	}

	// Last Word Suffix Words
	private static Set<String> SUFFIX_WORD = new HashSet<String>();
	static {
		SUFFIX_WORD.add("aktiengesellschaft"); // abbrev: AG
		SUFFIX_WORD.add("aktiebolag"); // abbrev: AB, Ab, A/B
		SUFFIX_WORD.add("aktieselskab"); // abbrev: A/S
		SUFFIX_WORD.add("anpartsselskab"); // Danish abbrev: ApS
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
	protected String suffixWord(NameOrg name, String currentTxt) {
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

	private static Map<String, String[]> MULTI_WORD_ABBREVS = new LinkedHashMap<String, String[]>();
	static {
		MULTI_WORD_ABBREVS.put(" ip holding company", new String[]{" IPHC", " IP HC."});
		MULTI_WORD_ABBREVS.put(" ip holding", new String[]{" IPHC", " IP HC."});
		MULTI_WORD_ABBREVS.put(" ip hc", new String[]{" IPHC", " IP HC."});
		MULTI_WORD_ABBREVS.put(" innovative properties", new String[]{" IP"});
		MULTI_WORD_ABBREVS.put(" intellectual property", new String[]{" IP"}); // IP Law Firm
		MULTI_WORD_ABBREVS.put(" intellectual property holding company", new String[]{" IPHC ", " IP HC."});
		MULTI_WORD_ABBREVS.put(" intellectual property", new String[]{" IP"}); // IP Law Firm
		MULTI_WORD_ABBREVS.put(" intelectual property", new String[]{" IP"}); // Miss-spelled IP Law Firm
		MULTI_WORD_ABBREVS.put(" operating company", new String[]{" OC.", " OpCo "}); // OpCo, O.C.
		MULTI_WORD_ABBREVS.put(" holding n.v.", new String[]{" HC."});
		MULTI_WORD_ABBREVS.put(" holdings limited", new String[]{" HC."});
		MULTI_WORD_ABBREVS.put(" holding limited", new String[]{" HC."});
		MULTI_WORD_ABBREVS.put(" holdings company", new String[]{" HC."});
		MULTI_WORD_ABBREVS.put(" holding company", new String[]{" HC."});
		MULTI_WORD_ABBREVS.put(" holdings, inc", new String[]{" HC."});
		MULTI_WORD_ABBREVS.put(" holdings inc", new String[]{" HC."});
		MULTI_WORD_ABBREVS.put(" holdings, llc", new String[]{" HC."});
		MULTI_WORD_ABBREVS.put(" holdings llc", new String[]{" HC."});
		MULTI_WORD_ABBREVS.put(" public limited company", new String[]{" P.L.C", ""});
		MULTI_WORD_ABBREVS.put(" patent law firm", new String[]{"", " Patent Law", " Pat. Law Firm", " Pat. Law Group", " Pat. Law Grp.",
												" Patent Law Group", " Patent Law Firm", " Patent Law Grp."});
		MULTI_WORD_ABBREVS.put(" ip law firm", new String[]{"", " IP Law", " IP Law Group", " IP Law Grp."});
		MULTI_WORD_ABBREVS.put(" legal group", new String[]{"", " Law Group", " Law Grp.", " Law Firm"});
		MULTI_WORD_ABBREVS.put(" law group", new String[]{"", " Law Firm", " Law Grp."});
		MULTI_WORD_ABBREVS.put(" law firm", new String[]{"", " Law Group", " Law Grp."});
		MULTI_WORD_ABBREVS.put(" et al.", new String[]{""});
		MULTI_WORD_ABBREVS.put(" kabushiki kaisha", new String[]{"", " KK", " K.K.", " K.K"});
		MULTI_WORD_ABBREVS.put(" research and development", new String[]{" R&D", " RND", "RAND", " Research and Dev"});
		MULTI_WORD_ABBREVS.put(" science and technology", new String[]{" S&T", " SNT"});
		MULTI_WORD_ABBREVS.put(" bio tech", new String[]{" Biotechnology", " bio-tech", " biotech"});
		MULTI_WORD_ABBREVS.put(" pharm tech", new String[]{" pharma tech", " pharmtech", " pharm-tech"});
		MULTI_WORD_ABBREVS.put(" med tech", new String[]{" medtech"});
		MULTI_WORD_ABBREVS.put(" agricultural science", new String[]{" AgriScience", " AgriSci.", " Ag Sciences"});
		MULTI_WORD_ABBREVS.put(" physical therapy", new String[]{" PT."});
		MULTI_WORD_ABBREVS.put(" health care", new String[]{"HHC."});
		MULTI_WORD_ABBREVS.put(" dental care", new String[]{"DC."});
	}

	protected void multiWordAbbrev(NameOrg name) {
		for(String synName: name.getSynonyms()) {
			String currentTxtLower = synName.toLowerCase();
			for(Entry<String, String[]> entry : MULTI_WORD_ABBREVS.entrySet()) {
				int idxStart = currentTxtLower.indexOf(entry.getKey());
				if (idxStart != -1) {
					int idxEnd = idxStart + entry.getKey().length();
					for(String synTxt:  entry.getValue()) {
						String updatedName = synName.substring(0,idxStart) 
								+ synTxt 
								+ synName.substring(idxEnd);
						name.addSynonymNorm(updatedName);
					}
					break;
				}
			}
		}
	}

	private static List<String> ChinaRegisteredLocs = Arrays.asList("cn", "hk", "beijing", "changzhou", "chengdu", "chongqing", "dongguan",
			"guangzhou", "hangzhou", "hong kong", "hui zhou", "huizhou", "jiashan", "jiangsu", "jingdezhen", "kunshan", "mianyang", "pudong",
			"shanghai", "shenyang",	"shenzhen", "suzhou", "tianjin", "qinhuangdao", "wuhan", "wuxi", "xi'an", "xiamen", "zhengzhou");
	/**
	 * Remove the Registered location from Chinese Company Names.
	 * 
	 * Registered location + Chosen name + What the company does + Company type
	 * Chosen name + (Registered location) + What the company does + Company type
	 * 
	 * @param name
	 * @return 
	 */
	protected void chineseCompanyNames(NameOrg name){
		for(String nameStr: name.getSynonyms()) {
			int beginIdx = nameStr.indexOf('(');
			if (beginIdx != -1) {
				int endIdx = nameStr.indexOf(')');
				if (endIdx != -1) {
					String bracketWord = nameStr.substring(beginIdx+1, endIdx);
					if (ChinaRegisteredLocs.contains(bracketWord.toLowerCase())) {
						String updatedName = nameStr.substring(0, beginIdx-1) + nameStr.substring(endIdx+1);
						//System.out.println(" China Name fix: " + name.getName() + " --->  " + nameStr);
						name.addSynonymNorm(updatedName);
					} else {
						//System.out.println("Possible China City Not Defined: " + bracketWord);
					}
				}
			} else {
				int spaceIdx = nameStr.indexOf(' ');
				if (spaceIdx != -1) {
					String firstWord = nameStr.substring(0, spaceIdx);
					if (ChinaRegisteredLocs.contains(firstWord.toLowerCase())) {
						String updatedName = nameStr.substring(spaceIdx+1);
						name.addSynonymNorm(updatedName);
					}
				}
			}
		}
	}

	// Last Word Abbreviations
	private static Map<String, String[]> ABBREVS = new HashMap<String, String[]>();
	static {
		ABBREVS.put("co", new String[]{"Company"});
		ABBREVS.put("corp", new String[]{"Corporation", ""});
		ABBREVS.put("inc", new String[]{"Incorporation", "Incorporated", ""});
		ABBREVS.put("pc", new String[]{"Professional Corporation", ""});
		ABBREVS.put("p.c", new String[]{"Professional Corporation", ""});
		ABBREVS.put("ip", new String[]{"Intellectual Property"});
		ABBREVS.put("ltd", new String[]{"Limited", ""});
		ABBREVS.put("ltda", new String[]{"Sociedad de Responsabilidad Limitada", ""});
		ABBREVS.put("gmbh", new String[]{"Gesellschaft mit beschränkter Haftung", ""});
		ABBREVS.put("kgaa", new String[]{"Kommanditgesellschaft Auf Aktien", ""});
		ABBREVS.put("mbh", new String[]{"Mit Beschrnkter Haftung", ""});
		ABBREVS.put("mbb", new String[]{"Partnerschaftsgesellschaft", ""}); // Germany "mbB" professional partnership 
		ABBREVS.put("aps", new String[]{"Anpartsselskab", ""}); // ApS in Denmark.
		ABBREVS.put("a.p.s", new String[]{"Anpartsselskab", ""}); // ApS in Denmark.
		ABBREVS.put("kg", new String[]{"Kommanditgesellschaft", ""}); // German, Belgium, Austria
		ABBREVS.put("ag", new String[]{"Aktiengesellschaft", ""});  // German, Austria, Switzerland
		ABBREVS.put("ab", new String[]{"Aktiebolag", ""}); // Sweden, Finland
		ABBREVS.put("k.k", new String[]{"Kabushiki Kaisha", ""}); // Japan
		ABBREVS.put("kk", new String[]{"Kabushiki Kaisha", ""}); // Japan
		ABBREVS.put("lp", new String[]{"Limited Partnership", ""});
		ABBREVS.put("l.p", new String[]{"Limited Partnership", ""});
		ABBREVS.put("pl", new String[]{"Public Limited Company", "P.L.C", "PLC", ""}); // UK
		ABBREVS.put("p.l.c", new String[]{"Public Limited Company", "PLC", ""});
		ABBREVS.put("plc", new String[]{"Public Limited Company", "P.L.C", ""});
		ABBREVS.put("l.l.c", new String[]{"Limited Liability Company", "LLC", ""});
		ABBREVS.put("llc", new String[]{"Limited Liability Partnership", "L.L.C", ""});
		ABBREVS.put("l.l.p", new String[]{"Limited Liability Partnership", "LLP", ""});
		ABBREVS.put("llp", new String[]{"Limited Liability Company", "L.L.P", ""});
		ABBREVS.put("s.l", new String[]{"Sociedad Limitada", ""}); // Spanish
		ABBREVS.put("s.l.u", new String[]{"Sociedad Limitada Unipersonal", ""}); // Spanish
		ABBREVS.put("n.v", new String[] {"naamloze vennootschap", "NV", ""}); // dutch
		ABBREVS.put("b.v", new String[] {"Besloten vennootschap", "BV", ""}); // dutch
		ABBREVS.put("oy", new String[]{"Osakeyhtio", "Osakeyhtiö", ""}); // Finish Osakeyhtiö
		ABBREVS.put("oyj", new String[]{"Julkinen Osakeyhtio", "Julkinen Osakeyhtiö", ""});  // Finish
		ABBREVS.put("ulc", new String[]{"unlimited liability corporation", ""});  // Canada
		ABBREVS.put("s.a.s", new String[]{"société par actions simplifiée", "SAS", ""}); // French
		ABBREVS.put("s.a.r.l", new String[]{"Société Anonyme à Responsabilité Limitée", "SARL", ""}); // French
		ABBREVS.put("sarl", new String[]{"Société Anonyme à Responsabilité Limitée", ""});
		ABBREVS.put("s.p.a", new String[]{"Societa per Azioni", "Società per Azioni", "Sociedad por Acciones", "SpA", ""}); // S.p.A. valid in USA (Connecticut), mostly many other countries. Società Per Azioni (italian), Sociedad por Acciones
		ABBREVS.put("s.a", new String[]{""}); // France, Canada,... S.A => Société anonyme (French) Sociedad Anónima (Spanish) Sociedade Anônima (Brazil) ... 
		ABBREVS.put("sa", new String[]{""}); // France, Canada,...
		ABBREVS.put("s.a/n.v", new String[]{"", "naamloze vennootschap", "NV"});
		ABBREVS.put("a/s", new String[]{"aktieselskab", ""}); // Danish
		ABBREVS.put("i/s", new String[]{"interessentskab", ""}); // Danish
		ABBREVS.put("k/s", new String[]{"kommanditselskab", ""}); // Danish
		ABBREVS.put("p/s", new String[]{"kommanditaktieselskab", "partnerselskab", ""}); // Danish
		ABBREVS.put("mfg", new String[]{"Manufacturing"});
		ABBREVS.put("mgmt", new String[]{"Management"});
		ABBREVS.put("labs", new String[]{"Labratories"});
		ABBREVS.put("r&d", new String[]{"research and development", "RND", "RAND"});
		ABBREVS.put("d/b/a", new String[]{" doing buisness as"});
		ABBREVS.put("dba", new String[]{" doing buisness as"});
	}

	private static Map<String, String[]> WORD_TO_ABBREVS = new HashMap<String, String[]>();
	static {
		WORD_TO_ABBREVS.put("college", new String[]{"Coll."});
		WORD_TO_ABBREVS.put("company", new String[]{"Co."});
		WORD_TO_ABBREVS.put("corporation", new String[]{"Corp."});
		WORD_TO_ABBREVS.put("corporate", new String[]{"Corp."});
		WORD_TO_ABBREVS.put("incorporated", new String[]{"Inc.", ""});
		WORD_TO_ABBREVS.put("incorporation", new String[]{"Inc.", ""});
		WORD_TO_ABBREVS.put("hospital", new String[]{"Hosp."});
		WORD_TO_ABBREVS.put("limited", new String[]{"Ltd.", ""});
		WORD_TO_ABBREVS.put("limitada", new String[] {"Ltda.", "Lda.", ""}); // Spanish and Portuguese  Ltda. or Lda
		WORD_TO_ABBREVS.put("handelsbolag", new String[]{"HB", ""});
		WORD_TO_ABBREVS.put("kommanditgesellschaft", new String[]{"KG", ""}); // German, Belgium, Austria
		WORD_TO_ABBREVS.put("aktiengesellschaft", new String[]{"AG", ""}); // German, Austria, Switzerland
		WORD_TO_ABBREVS.put("aktiengessellschaft", new String[]{"AG", "Aktiengesellschaft", ""}); // misspelled version.
		WORD_TO_ABBREVS.put("anpartsselskab", new String[]{"ApS", "A.p.S", ""});
		WORD_TO_ABBREVS.put("aktieselskab", new String[]{"A/S", ""}); // Danish
		WORD_TO_ABBREVS.put("aktiebolag", new String[]{"AB", ""}); // Swedish
		WORD_TO_ABBREVS.put("aktsiaselts", new String[]{"AS", "A.S.", ""}); // Estonian
		WORD_TO_ABBREVS.put("aksjeselskap", new String[]{"AS", "A/S", ""}); // Norwegian (NO)
		WORD_TO_ABBREVS.put("allmennaksjeselskap", new String[]{"ASA", ""}); // Norwegian (NO)
		WORD_TO_ABBREVS.put("hlutafélag", new String[]{"Hf", "hlutafelag", ""}); // Icelandic
		WORD_TO_ABBREVS.put("kommandiittiyhtiö", new String[]{"Ky", "Kommandiittiyhtio", ""});
		WORD_TO_ABBREVS.put("kommandiittiyhtio", new String[]{"Ky", "Kommandiittiyhtiö", ""});
		WORD_TO_ABBREVS.put("kommandittselskap", new String[]{"KS", ""});
		WORD_TO_ABBREVS.put("kommanditbolag", new String[]{"Kb", ""});
		WORD_TO_ABBREVS.put("kommanditselskab", new String[]{"K/S", ""}); // Danish
		WORD_TO_ABBREVS.put("kommanditaktieselskab", new String[]{"P/S", "Partnerselskab", ""}); // Danish
		WORD_TO_ABBREVS.put("partnerselskab", new String[]{"P/S", "Kommanditaktieselskab", ""}); // Danish
		WORD_TO_ABBREVS.put("kommanditaktiengesellschaft", new String[]{"KomAG"});
		WORD_TO_ABBREVS.put("kollektivgesellschaft", new String[]{"KolG", ""});
		WORD_TO_ABBREVS.put("interessentskab", new String[]{"I/S", ""}); // Danish
		WORD_TO_ABBREVS.put("osuuskunta", new String[]{"osk", ""});
		WORD_TO_ABBREVS.put("osakeyhtio", new String[]{"Oy", "Osakeyhtiö", ""}); // Finnish
		WORD_TO_ABBREVS.put("osakeyhtiö", new String[]{"Oy", "Osakeyhtio", ""}); // Finnish
		WORD_TO_ABBREVS.put("partnerschaftsgesellschaft", new String[]{"mbB", ""}); // Germany
		WORD_TO_ABBREVS.put("fabrication", new String[]{"Fab.", "Fabn."});
		WORD_TO_ABBREVS.put("manufacturing", new String[]{"Mfg."});
		WORD_TO_ABBREVS.put("management", new String[]{"Mgmt."});
		WORD_TO_ABBREVS.put("appliances", new String[]{"Appl."});
		WORD_TO_ABBREVS.put("appliance", new String[]{"Appl."});
		WORD_TO_ABBREVS.put("laboratory", new String[]{"Lab"});
		WORD_TO_ABBREVS.put("industry", new String[]{"Ind."});
		WORD_TO_ABBREVS.put("institute", new String[]{"Inst."});
		WORD_TO_ABBREVS.put("services", new String[]{"Svcs."});
		WORD_TO_ABBREVS.put("service", new String[]{"Svc."});
		WORD_TO_ABBREVS.put("design", new String[]{"DESG.", "Dsgn."});
		WORD_TO_ABBREVS.put("products", new String[]{"Prod."});
		WORD_TO_ABBREVS.put("product", new String[]{"Prod."});
		WORD_TO_ABBREVS.put("systems", new String[]{"Sys."});
		WORD_TO_ABBREVS.put("system", new String[]{"Sys."});
		WORD_TO_ABBREVS.put("information", new String[]{"Info."});
		WORD_TO_ABBREVS.put("innovation", new String[]{"Innov."});
		WORD_TO_ABBREVS.put("partnership", new String[]{"Partners"});
		WORD_TO_ABBREVS.put("pharmaceutica", new String[]{"Pharma.", "Pharm"});
		WORD_TO_ABBREVS.put("pharmaceutical", new String[]{"Pharma.", "Pharm"});
		WORD_TO_ABBREVS.put("pharmaceuticals", new String[]{"Pharma.", "Pharm"});
		WORD_TO_ABBREVS.put("biopharmaceutical", new String[]{"BioPharma.", "BioPharm"});
		WORD_TO_ABBREVS.put("biopharmaceuticals", new String[]{"BioPharma.", "BioPharm"});
		WORD_TO_ABBREVS.put("industries", new String[]{"Ind."});
		WORD_TO_ABBREVS.put("international", new String[]{"Intl."});
		WORD_TO_ABBREVS.put("internacional", new String[]{"Intl.", "International"}); // spanish spelling with "c"
		WORD_TO_ABBREVS.put("organization", new String[]{"Org."});
		WORD_TO_ABBREVS.put("association", new String[]{"Assoc."});
		WORD_TO_ABBREVS.put("asociación", new String[]{"Assoc."});
		WORD_TO_ABBREVS.put("associates", new String[]{"Assocs."});
		WORD_TO_ABBREVS.put("advanced", new String[]{"Adv."});
		WORD_TO_ABBREVS.put("engineering", new String[]{"Eng."});
		WORD_TO_ABBREVS.put("cooperative", new String[]{"Coop."});
		WORD_TO_ABBREVS.put("enterprises", new String[]{"Ent."});
		WORD_TO_ABBREVS.put("enterprise", new String[]{"Ent."});
		WORD_TO_ABBREVS.put("agency", new String[]{"Agcy."});
		WORD_TO_ABBREVS.put("consultancy", new String[]{"consultant", "consult"});
		WORD_TO_ABBREVS.put("operations", new String[]{"Ops."});
		WORD_TO_ABBREVS.put("department", new String[]{"Dept."});
		WORD_TO_ABBREVS.put("development", new String[]{"Dev."});
		WORD_TO_ABBREVS.put("developments", new String[]{"Dev."});
		WORD_TO_ABBREVS.put("agricultural", new String[]{"Ag.", "Agri."});
		WORD_TO_ABBREVS.put("communication", new String[]{"Com.", "COMM."});
		WORD_TO_ABBREVS.put("communications", new String[]{"Com.", "Comm."});
		WORD_TO_ABBREVS.put("telecommunication", new String[]{"Tele.", "Telecom."});
		WORD_TO_ABBREVS.put("telecommunications", new String[]{"Tele.", "Telecom."});
		WORD_TO_ABBREVS.put("telefonaktiebolaget", new String[]{"Tele.", "Telecom"});
		WORD_TO_ABBREVS.put("electronics", new String[]{"Elec.", "Electr."});
		WORD_TO_ABBREVS.put("entertainment", new String[]{"ENTMT.", "ENT."});
		WORD_TO_ABBREVS.put("graphics", new String[]{"GFX."});
		WORD_TO_ABBREVS.put("mobility", new String[]{"mobile"});
		WORD_TO_ABBREVS.put("university", new String[]{"Uni.", "Univ"});
		WORD_TO_ABBREVS.put("foundation", new String[]{"fdn.", "fndn."});
		WORD_TO_ABBREVS.put("institute", new String[] {"Inst."});
		WORD_TO_ABBREVS.put("research", new String[]{"Res."});
		WORD_TO_ABBREVS.put("performance", new String[]{"Perform."});
		WORD_TO_ABBREVS.put("high-technologies", new String[]{"High-Tech."});
		WORD_TO_ABBREVS.put("technology", new String[]{"Tech.", "Technol."});
		WORD_TO_ABBREVS.put("technologies", new String[]{"Tech.", "Technol."});
		WORD_TO_ABBREVS.put("nanotechnology", new String[]{"NanoTech."});
		WORD_TO_ABBREVS.put("biotechnology", new String[]{"Biotech.", "Bio-tech"});
		WORD_TO_ABBREVS.put("agriscience", new String[]{"Agricultural Science"});
		WORD_TO_ABBREVS.put("dentistry", new String[]{"Dent."});
		WORD_TO_ABBREVS.put("dentist", new String[]{"Dent."});
		WORD_TO_ABBREVS.put("cardiology", new String[]{"Cardio."});
		WORD_TO_ABBREVS.put("cardiovascular", new String[]{"Cardio.", "CV."});
		WORD_TO_ABBREVS.put("chemical", new String[]{"Chem."});
		WORD_TO_ABBREVS.put("biosciences", new String[]{"Biosci."});
		WORD_TO_ABBREVS.put("bioscience", new String[]{"Biosci."});
		WORD_TO_ABBREVS.put("biochemical", new String[]{"BioChem."});
		WORD_TO_ABBREVS.put("biochemicals", new String[]{"BioChem."});
		WORD_TO_ABBREVS.put("biochemistry", new String[]{"BioChem."});
		WORD_TO_ABBREVS.put("biologics", new String[]{"Bio.", "Biologic."});
		WORD_TO_ABBREVS.put("biological", new String[]{"Bio.", "Biologic."});
		WORD_TO_ABBREVS.put("biologicals", new String[]{"Bio.", "Biologic."});
		WORD_TO_ABBREVS.put("biopharmaceuticals", new String[]{"BioPharm.", "BioPharma."});
		WORD_TO_ABBREVS.put("biopharma", new String[]{"biopharmaceuticals", "BioPharm."});
		WORD_TO_ABBREVS.put("property", new String[]{"Prop.", "Propty."});
		WORD_TO_ABBREVS.put("security", new String[]{"Sec.", "Secur."});
		WORD_TO_ABBREVS.put("national", new String[]{"NAT'L", "NATL"});
		WORD_TO_ABBREVS.put("healthcare", new String[]{"HHC."});
		WORD_TO_ABBREVS.put("dentalcare", new String[]{"DC."});
		WORD_TO_ABBREVS.put("group", new String[]{"Grp."});
		WORD_TO_ABBREVS.put("section", new String[]{"Sec."});
		WORD_TO_ABBREVS.put("division", new String[]{"Div.", "Divn."});
		WORD_TO_ABBREVS.put("physics", new String[]{"Phys."});
		WORD_TO_ABBREVS.put("medical", new String[]{"Med."});
		WORD_TO_ABBREVS.put("medicinal", new String[]{"Med."});
		WORD_TO_ABBREVS.put("medicine", new String[]{"Med."});
		WORD_TO_ABBREVS.put("sciences", new String[]{"Sci."});
		WORD_TO_ABBREVS.put("science", new String[]{"Sci."});
		WORD_TO_ABBREVS.put("surgical", new String[]{"Surg."});
		WORD_TO_ABBREVS.put("scientifique", new String[]{"Sci."});
		WORD_TO_ABBREVS.put("technique", new String[]{"Tech."});
		WORD_TO_ABBREVS.put("industrial", new String[]{"Ind.", "Indust."});
		WORD_TO_ABBREVS.put("construction", new String[]{"Const.", "Constr."});
		WORD_TO_ABBREVS.put("equipment", new String[]{"Equip.", "Eqpt."});
		WORD_TO_ABBREVS.put("Semiconductor", new String[]{"Semicon."});
		WORD_TO_ABBREVS.put("machinery", new String[]{"Mach."});
		WORD_TO_ABBREVS.put("nutrition", new String[]{"Nutr."});
		WORD_TO_ABBREVS.put("trust", new String[]{"TRU."});
		WORD_TO_ABBREVS.put("laboratories", new String[]{"Labs.", "Lab."});
		WORD_TO_ABBREVS.put("laboratory", new String[]{"Lab."});
		WORD_TO_ABBREVS.put("microelectronics", new String[]{"MT."});
		WORD_TO_ABBREVS.put("neuroscience", new String[]{"Neurosci."});
		WORD_TO_ABBREVS.put("microbiology", new String[]{"Microbio.", "Microbiol."});
		WORD_TO_ABBREVS.put("geochemistry", new String[]{"Geochem."});
		WORD_TO_ABBREVS.put("geometries", new String[]{"Geom."});
		WORD_TO_ABBREVS.put("geometry", new String[]{"Geom."});
		WORD_TO_ABBREVS.put("environmental", new String[]{"Environ."});
		WORD_TO_ABBREVS.put("optics", new String[]{"Opt."});
		WORD_TO_ABBREVS.put("optic", new String[]{"Opt."});
		WORD_TO_ABBREVS.put("orthopedic", new String[]{"Ortho."});
		WORD_TO_ABBREVS.put("orthopedics", new String[]{"Ortho."});
		WORD_TO_ABBREVS.put("orthopaedics", new String[]{"Ortho."});
		WORD_TO_ABBREVS.put("orthopaedic", new String[]{"Ortho."});
		WORD_TO_ABBREVS.put("rehabilitation", new String[]{"Rehab."});
		WORD_TO_ABBREVS.put("pediatrics", new String[]{"Peds."});
		WORD_TO_ABBREVS.put("exploration", new String[]{"Explor.", "Expl."});
		WORD_TO_ABBREVS.put("applied", new String[]{"Appl."});
		WORD_TO_ABBREVS.put("toxicology", new String[]{"Toxicol."});
		WORD_TO_ABBREVS.put("biophysics", new String[]{"Biophys."});
		WORD_TO_ABBREVS.put("virology", new String[]{"Virol."});
		WORD_TO_ABBREVS.put("physiology", new String[]{"Physiol."});
		WORD_TO_ABBREVS.put("phytopathology", new String[]{"Phytopathol."});
		WORD_TO_ABBREVS.put("solutions", new String[]{"Soln."});
		WORD_TO_ABBREVS.put("storage", new String[]{"Stg."});
		WORD_TO_ABBREVS.put("diabetes", new String[]{"Diab.", "Dia."});
		//WORD_TO_ABBREVS.put("network", new String[]{"Netwrk.", "Ntwrk"});
		WORD_TO_ABBREVS.put("licensing", new String[]{"license", "licence", "licencing"});
		WORD_TO_ABBREVS.put("lecensing", new String[]{"license", "licence", "licencing"});
		WORD_TO_ABBREVS.put("licencing", new String[]{"license", "licence", "licensing", "lecensing"});
		WORD_TO_ABBREVS.put("law", new String[]{"legal", ""});
		WORD_TO_ABBREVS.put("legal", new String[]{"law", ""});
		WORD_TO_ABBREVS.put("patents", new String[]{"Pat."});
		WORD_TO_ABBREVS.put("patent", new String[]{"Pat."});
		WORD_TO_ABBREVS.put("trademark", new String[]{"TM."});
		WORD_TO_ABBREVS.put("telephone", new String[]{"phone"});
		WORD_TO_ABBREVS.put("distribution", new String[]{"Dist."});
		WORD_TO_ABBREVS.put("alliance", new String[]{"AAC"});
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
	protected Set<String> lastWordsAbbrev(NameOrg name, String currentTxt) {
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
		String[] lastWordAbbrev = lookupWordAbbrevs(lastWord);
		for(String lwordA: lastWordAbbrev) {
			StringBuilder stb1 = new StringBuilder();
			stb1.append(leftSide);
			stb1.append(word);
			if (hasComma) {
				stb1.append(",");
			}
			stb1.append(" ");
			stb1.append(lwordA);
			String newStr1 = stb1.toString();
			if (!name.getName().equals(newStr1)) {
				name.addSynonymNorm(newStr1);
				retSet.add(newStr1);
			}
		}

		
		//String[] wordAbbrev = lookupWordAbbrevs(word);
		//String newStr2 = leftSide + lookupWordAbbrevs(word) + ", " + lookupWordAbbrevs(lastWord);
		if (tokens.length > 2) {
			String[] wordAbbrev = lookupWordAbbrevs(word);
			
			for(String wordA: wordAbbrev) {
				for(String lwordA: lastWordAbbrev) {
					StringBuilder stb2 = new StringBuilder();
					stb2.append(leftSide);
					stb2.append(wordA);
					if (hasComma) {
						stb2.append(",");
					}
					stb2.append(" ");
					stb2.append(lwordA);
					String newStr2 = stb2.toString();
					if (!name.getName().equals(newStr2)) {
						name.addSynonymNorm(newStr2);
						retSet.add(newStr2);
					}
				}
			}

			//String newStr3 = leftSide + lookupWordAbbrevs(word) + ", " + lastWord;
			for(String wordA: wordAbbrev) {
				StringBuilder stb3 = new StringBuilder();
				stb3.append(leftSide);
				stb3.append(wordA);
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
		}

		return retSet;
	}

	protected String[] lookupWordAbbrevs(final String word){
		String wordLc = word.replaceFirst("\\.$", "").replaceAll("[\\(\\)]", "").toLowerCase();
		if (wordLc.length() < 8 && ABBREVS.containsKey(wordLc)) {
			return ABBREVS.get(wordLc);
		} else if (WORD_TO_ABBREVS.containsKey(wordLc)) {
			return WORD_TO_ABBREVS.get(wordLc);
		}
		return new String[]{word};
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
