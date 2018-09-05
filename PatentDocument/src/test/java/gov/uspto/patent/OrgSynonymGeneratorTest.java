package gov.uspto.patent;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import gov.uspto.patent.model.entity.NameOrg;

public class OrgSynonymGeneratorTest {

	/*
	 * Real Company Examples taken from Patent XML
	 * synonyms are generate by removing company prefix and suffixes.
	 */
	private static Map<String, String> COMPANIES = new LinkedHashMap<String, String>();
	static {
		COMPANIES.put("International Business Machines Corporation", "International Business Machines");
		COMPANIES.put("Kabushiki Kaisha Toshiba", "Toshiba");
		COMPANIES.put("Toshiba Tec Kabushiki Kaisha", "Toshiba Tec");
		COMPANIES.put("Canon Kabuhsiki Kaisha", "Canon"); // misspelled in patent Kabuhsiki => Kabushiki.
		COMPANIES.put("Bayer Aktiengesellschaft", "Bayer");
		COMPANIES.put("Sumitomo Bakelite Company, Ltd.", "Sumitomo Bakelite");
		COMPANIES.put("Yeda Research & Development Company Ltd.", "Yeda Research & Development");
		COMPANIES.put("Samsung Electronics Co., Ltd.", "Samsung Electronics");
		COMPANIES.put("Dongbu HiTek Co., ltd.", "Dongbu HiTek");
		COMPANIES.put("NEC LCD Technologies, Ltd.", "NEC LCD Technologies");
		COMPANIES.put("Indus Biotech Pvt. Ltd.", "Indus Biotech");
		COMPANIES.put("Hitachi Plant Technologies, LTD.", "Hitachi Plant Technologies");
		COMPANIES.put("Orbotech Ltd", "Orbotech");
		COMPANIES.put("Honeywell International Inc.", "Honeywell International");
		COMPANIES.put("NEC Corporation", "NEC");
		COMPANIES.put("ADC Telecommunications, Inc.", "ADC Telecommunications");
		COMPANIES.put("Japan Aviation Electronics Industry, Limited", "Japan Aviation Electronics Industry");
		COMPANIES.put("Biopharm Gesellschaft zur Biotechnologischen Entwicklung von Pharmaka mbH", "Biopharm Gesellschaft zur Biotechnologischen Entwicklung von Pharmaka");
		COMPANIES.put("Airbus Deutschland GmbH", "Airbus Deutschland");
		COMPANIES.put("Phoenix Contact GmbH & Co. KG", "Phoenix Contact");
		COMPANIES.put("Indo Internacional, S.A.", "Indo Internacional");
		COMPANIES.put("Sandvik Intellectual Property AB", "Sandvik Intellectual Property");
		COMPANIES.put("Bliss Holdings, LLC", "Bliss Holdings");
		COMPANIES.put("HeathCo LLC", "HeathCo");
		COMPANIES.put("IP Holdings LLC.", "IP Holdings");
		COMPANIES.put("Dupont Teijin Films U.S. Limited Partnership", "Dupont Teijin Films U.S.");
		COMPANIES.put("Hewlett-Packard Development Company, L.P.", "Hewlett-Packard Development Company");
		COMPANIES.put("Hewlett-Packard Development Company, L.P.", "Hewlett-Packard Development");
		COMPANIES.put("Rochal Industries, LLP", "Rochal Industries");
		COMPANIES.put("Rolls-Royce PLC", "Rolls-Royce");
		COMPANIES.put("Rolls-Royce plc", "Rolls-Royce");
		COMPANIES.put("Au Optronics Corporation", "Au Optronics");
		COMPANIES.put("AU Optronics Corp.", "AU Optronics");
		COMPANIES.put("S. C. Johnson & Son, Inc.", "S. C. Johnson & Son");
		COMPANIES.put("DENSO CORPORATION", "DENSO");
		COMPANIES.put("Allen IP, Incorporated", "Allen IP");
		COMPANIES.put("Texas Instruments Incorporated", "Texas Instruments");
		COMPANIES.put("Koronis Pharmaceuticals, Incorporated", "Koronis Pharmaceuticals");
		COMPANIES.put("Koninklijke Philips Electronics N.V.", "Philips Electronics");
		COMPANIES.put("Koninklijke Philips Electronics N.V.", "Koninklijke Philips Electronics");
		COMPANIES.put("ITREC, B.V.", "ITREC");
		COMPANIES.put("Tresu Anlaeg A/S", "Tresu Anlaeg");
		COMPANIES.put("Coprecitec, S.L.", "Coprecitec");
		COMPANIES.put("Zimmer Spine, S.A.S.", "Zimmer Spine");
		COMPANIES.put("F.LLI MARIS S.p.A.", "F.LLI MARIS");
		COMPANIES.put("Tyco Healthcare Group, LP", "Tyco Healthcare Group");
		COMPANIES.put("Schott AG", "Schott");
		COMPANIES.put("Minvasys, SA", "Minvasys");
		COMPANIES.put("GlaxoSmithKline Biologicals S.A.", "GlaxoSmithKline Biologicals");
		COMPANIES.put("Nerviano Medical Sciences S.r.l.", "Nerviano Medical Sciences");
	}

	private static List<String> COMPANIES_FALSE_POS = new ArrayList<String>();
	static {
		COMPANIES_FALSE_POS.add("Deere & Company");
		COMPANIES_FALSE_POS.add("Milliken & Company");
		COMPANIES_FALSE_POS.add("Becton, Dickinson and Company");
		COMPANIES_FALSE_POS.add("Rutgers, The State University");
		// The Hershey Company
	}

	@Test
	public void CompanySynonyms() {
		for (Entry<String, String> entry : COMPANIES.entrySet()) {
			NameOrg name = new NameOrg(entry.getKey());
            OrgSynonymGenerator.computeSynonyms(name);
            assertTrue("Missing Synonym " + entry.getValue(), name.getSynonyms().contains(entry.getValue()));
		}
	}

	@Test
	public void FalsePositives() {
		for (String entry : COMPANIES_FALSE_POS) {
			NameOrg name = new NameOrg(entry);
            OrgSynonymGenerator.computeSynonyms(name);
            assertTrue(name.getSynonyms().size() == 0);
		}
	}
}
