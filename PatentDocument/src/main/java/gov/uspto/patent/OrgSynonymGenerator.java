package gov.uspto.patent;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * @author Brian Feldman <brian.feldman@uspto.gov>
 *
 */
public class OrgSynonymGenerator {

	private static final Logger LOGGER = LoggerFactory.getLogger(OrgSynonymGenerator.class);
	
	// https://en.wikipedia.org/wiki/List_of_legal_entity_types_by_country
	private static final Pattern ORG_SUFFIX_PATTERN = Pattern.compile(
			"^(.+?),? (Co\\.?, [Ll]td|Pvt\\. Ltd|Company, LLC|LLP|P?L\\.?L\\.?C|Plc|PLC|plc|(P(ty|vt|te|TE),? )?Ltd|LTD|Inc(:?orporated)?|G?mbH|(:?Pty )?Limited|Limited Partnership|Aktiengesellschaft|Aktiebolag|Aktieselskab|A[BGS]|GmbH & Co\\.? KG|Kommanditgesellschaft|naamloze vennootschap|Kabu(:?sh|hs)iki Kaisha|[BN]\\.?V\\.?|S\\.?A\\.?(:?S\\.?)?|S\\.?p\\.?A\\.?|[SGL]\\.?P\\.?|A/S|S\\.?L\\.?|S\\.[Rr]\\.[Ll]\\.)\\.?$");
	
	private static final Pattern COMPANY_PATTERN = Pattern
			.compile("(.+) ((?<!(:?&|[Aa]nd) )Co.|(?<!(:?&|[Aa]nd) )Company|Corporation|CORPORATION|Cooperative|Corp.|PTE),?$");

	// Kabushiki Kaisha Toshiba == Toshiba Kabushiki Kaisha
	private static final Pattern LEAD_COMPANY_PATTERN = Pattern.compile("^(Kabu(:?sh|hs)iki Kaisha|Koninklijke) (.+)$");

	public static void computeSynonyms(NameOrg name) {
		Set<String> synonyms = name.getSynonyms();

		Matcher suffixMatcher = ORG_SUFFIX_PATTERN.matcher(name.getName());
		if (suffixMatcher.find()) {
			String title = suffixMatcher.group(1);
			String suffix = suffixMatcher.group(2);
			LOGGER.debug("Org Name Suffix '{}' ; '{}' => '{}'", suffix, name.getName(), title);
			name.setSuffix(suffix);
			synonyms.add(title);

			Matcher leadCompanyMatcher = LEAD_COMPANY_PATTERN.matcher(title);
			if (leadCompanyMatcher.find()) {
				String coSuffix = leadCompanyMatcher.group(1);
				String shortTitle = leadCompanyMatcher.group(2);
				synonyms.add(shortTitle);
				//name.setSuffix(coSuffix); // TODO add PREFIX property to name class.
			}

			Matcher companyMatcher = COMPANY_PATTERN.matcher(title);
			if (companyMatcher.find()) {
				String shortTitle = companyMatcher.group(1);
				String coSuffix = companyMatcher.group(2);
				name.setSuffix(coSuffix + " " + suffix);
				synonyms.add(shortTitle);
			}
		}

		Matcher companyMatcher = COMPANY_PATTERN.matcher(name.getName());
		if (companyMatcher.find()) {
			String shortTitle = companyMatcher.group(1);
			String coSuffix = companyMatcher.group(2);
			synonyms.add(shortTitle);
			name.setSuffix(coSuffix);
		}

		Matcher leadCompanyMatcher = LEAD_COMPANY_PATTERN.matcher(name.getName());
		if (leadCompanyMatcher.find()) {
			String coSuffix = leadCompanyMatcher.group(1);
			String shortTitle = leadCompanyMatcher.group(2);
			synonyms.add(shortTitle);
			name.setSuffix(coSuffix);
		}
	}

}
