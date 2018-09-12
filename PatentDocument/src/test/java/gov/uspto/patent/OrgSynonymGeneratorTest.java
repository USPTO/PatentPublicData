package gov.uspto.patent;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
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
	private static Map<String, List<String>> COMPANIES = new LinkedHashMap<String, List<String>>();
	static {
		COMPANIES.put("CareFusion 2200, Inc.", Arrays.asList("CAREFUSION 2200"));
		COMPANIES.put("International Business Machines Corporation", Arrays.asList("INTERNATIONAL BUSINESS MACHINES", "INTERNATIONAL BUSINESS MACHINES CORP."));
		COMPANIES.put("Bayer Aktiengesellschaft", Arrays.asList("BAYER", "BAYER AG"));
		COMPANIES.put("Sumitomo Bakelite Company, Ltd.", Arrays.asList("SUMITOMO BAKELITE", "SUMITOMO BAKELITE COMPANY, LIMITED", "SUMITOMO BAKELITE CO., LTD."));
		COMPANIES.put("Yeda Research & Development Company Ltd.", Arrays.asList("YEDA RESEARCH & DEVELOPMENT", "YEDA RESEARCH AND DEVELOPMENT", "YEDA RESEARCH & DEVELOPMENT COMPANY LIMITED", "YEDA RESEARCH AND DEVELOPMENT COMPANY LIMITED"));
		COMPANIES.put("Samsung Electronics Co., Ltd.", Arrays.asList("SAMSUNG ELECTRONICS", "SAMSUNG ELECTRONICS CO., LIMITED", "SAMSUNG ELECTRONICS CO LIMITED", "SAMSUNG ELECTRONICS COMPANY, LIMITED", "SAMSUNG ELECTRONICS COMPANY LIMITED"));
		COMPANIES.put("Dongbu HiTek Co., ltd.", Arrays.asList("DONGBU HITEK", "DONGBU HITEK CO., LIMITED"));
		COMPANIES.put("NEC LCD Technologies, Ltd.", Arrays.asList("NEC LCD TECHNOLOGIES", "NEC LCD TECHNOLOGIES, LIMITED"));
		COMPANIES.put("Indus Biotech Pvt. Ltd.", Arrays.asList("INDUS BIOTECH", "INDUS BIOTECH PVT. LIMITED"));
		COMPANIES.put("Hitachi Plant Technologies, LTD.", Arrays.asList("HITACHI PLANT TECHNOLOGIES", "HITACHI PLANT TECHNOLOGIES, LIMITED"));
		COMPANIES.put("Orbotech Ltd", Arrays.asList("ORBOTECH"));
		COMPANIES.put("Silverbrook Research Pty Ltd", Arrays.asList("SILVERBROOK RESEARCH"));
		COMPANIES.put("Silverbrook Research Pty Limited", Arrays.asList("SILVERBROOK RESEARCH"));
		COMPANIES.put("Honeywell International Inc.", Arrays.asList("HONEYWELL INTERNATIONAL INCORPORATION", "HONEYWELL INTERNATIONAL"));
		COMPANIES.put("NEC Corporation", Arrays.asList("NEC", "NEC CORP.", "NEC CORP"));
		COMPANIES.put("ADC Telecommunications, Inc.", Arrays.asList("ADC TELECOMMUNICATIONS", "ADC TELECOMMUNICATIONS, INCORPORATION"));
		COMPANIES.put("Japan Aviation Electronics Industry, Limited", Arrays.asList("JAPAN AVIATION ELECTRONICS INDUSTRY", "JAPAN AVIATION ELECTRONICS INDUSTRY, LTD."));
		COMPANIES.put("Biopharm Gesellschaft zur Biotechnologischen Entwicklung von Pharmaka mbH", Arrays.asList("BIOPHARM GESELLSCHAFT ZUR BIOTECHNOLOGISCHEN ENTWICKLUNG VON PHARMAKA"));
		COMPANIES.put("Indo Internacional, S.A.", Arrays.asList("INDO INTERNACIONAL"));
		COMPANIES.put("Bliss Holdings, LLC", Arrays.asList("BLISS HOLDINGS"));
		COMPANIES.put("HeathCo LLC", Arrays.asList("HEATHCO"));
		COMPANIES.put("IP Holdings LLC.", Arrays.asList("IP HOLDINGS"));
		COMPANIES.put("Dupont Teijin Films U.S. Limited Partnership", Arrays.asList("DUPONT TEIJIN FILMS U.S."));
		COMPANIES.put("Hewlett-Packard Development Company, L.P.", Arrays.asList("HEWLETT-PACKARD DEVELOPMENT", "HEWLETT-PACKARD DEVELOPMENT COMPANY"));
		COMPANIES.put("Rochal Industries, LLP", Arrays.asList("ROCHAL INDUSTRIES"));
		COMPANIES.put("Rolls-Royce PLC", Arrays.asList("ROLLS-ROYCE", "ROLLS-ROYCE PLC"));
		COMPANIES.put("Rolls-Royce plc", Arrays.asList("ROLLS-ROYCE", "ROLLS-ROYCE PLC"));
		COMPANIES.put("Au Optronics Corporation", Arrays.asList("AU OPTRONICS CORP.", "AU OPTRONICS CORPORATION", "AU OPTRONICS"));
		COMPANIES.put("AU Optronics Corp.", Arrays.asList("AU OPTRONICS CORP.", "AU OPTRONICS CORPORATION", "AU OPTRONICS"));
		COMPANIES.put("S. C. Johnson & Son, Inc.", Arrays.asList("S. C. JOHNSON & SON, INCORPORATION", "S. C. JOHNSON & SON"));
		COMPANIES.put("DENSO CORPORATION", Arrays.asList("DENSO", "DENSO CORP.", "DENSO CORPORATION"));
		COMPANIES.put("Allen IP, Incorporated", Arrays.asList("ALLEN IP"));
		COMPANIES.put("Texas Instruments Incorporated", Arrays.asList("TEXAS INSTRUMENTS"));
		COMPANIES.put("Koronis Pharmaceuticals, Incorporated", Arrays.asList("KORONIS PHARMACEUTICALS"));
		COMPANIES.put("ITREC, B.V.", Arrays.asList("ITREC"));
		COMPANIES.put("Tresu Anlaeg A/S", Arrays.asList("TRESU ANLAEG")); // "Tresu Anlaeg Aktieselskab"
		COMPANIES.put("Coprecitec, S.L.", Arrays.asList("COPRECITEC"));
		COMPANIES.put("Zimmer Spine, S.A.S.", Arrays.asList("ZIMMER SPINE"));
		COMPANIES.put("F.LLI MARIS S.p.A.", Arrays.asList("F.LLI MARIS"));
		COMPANIES.put("Tyco Healthcare Group, LP", Arrays.asList("TYCO HEALTHCARE GROUP"));
		COMPANIES.put("Schott AG", Arrays.asList("SCHOTT"));
		COMPANIES.put("Minvasys, SA", Arrays.asList("MINVASYS"));
		COMPANIES.put("GlaxoSmithKline Biologicals S.A.", Arrays.asList("GLAXOSMITHKLINE BIOLOGICALS"));
		COMPANIES.put("Nerviano Medical Sciences S.r.l.", Arrays.asList("NERVIANO MEDICAL SCIENCES"));
		COMPANIES.put("Deere & Company", Arrays.asList("DEERE & CO."));
		COMPANIES.put("L & P Management Company", Arrays.asList("L&P MGMT", "L&P MANAGEMENT COMPANY", "L&P MANAGEMENT CO", "L & P MANAGEMENT COMPANY", "L & P MANAGEMENT CO"));
		COMPANIES.put("L&P Management Company", Arrays.asList("L&P MGMT", "L&P MANAGEMENT COMPANY", "L&P MANAGEMENT CO", "L & P MANAGEMENT COMPANY", "L & P MANAGEMENT CO"));
		COMPANIES.put("Compagnie Plastic Omnium", Arrays.asList("PLASTIC OMNIUM"));
		COMPANIES.put("Stamps.com Inc", Arrays.asList("STAMPS.COM"));
		COMPANIES.put("The Western Union Company", Arrays.asList("WESTERN UNION"));
		COMPANIES.put("Kulicke and Soffa Industries, Inc.", Arrays.asList("KULICKE & SOFFA IND"));
		COMPANIES.put("Landor & Hawa International Limited", Arrays.asList("LANDOR AND HAWA INTL"));
		COMPANIES.put("L'Oreal", Arrays.asList("LOREAL", "L'OREAL"));
		COMPANIES.put("Sportsman Corp.", Arrays.asList("SPORTSMAN", "SPORTSMAN CORPORATION"));
		COMPANIES.put("The Goodyear Tire & Rubber Company", Arrays.asList("GOODYEAR TIRE & RUBBER", "GOODYEAR TIRE & RUBBER COMPANY", "GOODYEAR TIRE & RUBBER CO"));
		COMPANIES.put("Black & Decker, Inc.", Arrays.asList("BLACK & DECKER", "BLACK AND DECKER"));
		COMPANIES.put("A.R.I. Flow Control Accessories Agricultural Cooperative Association Ltd.", Arrays.asList("ARI FLOW CONTROL ACCESSORIES AGRICULTURAL"));
		//COMPANIES.put("BP Exploration Operating Company Limited", Arrays.asList("BP EXPLORATION"));

		// Dutch
		COMPANIES.put("Firma G.B. Boucherie, naamloze vennootschap", Arrays.asList("G.B. BOUCHERIE", "GB BOUCHERIE"));
		COMPANIES.put("Koninklijke Philips Electronics N.V.", Arrays.asList("PHILIPS ELECTRONICS", "KONINKLIJKE PHILIPS ELECTRONICS", "PHILIPS ELECTRONICS N.V."));

		// Swedish (Sweden, Finland)
		COMPANIES.put("Alfa Laval Corporate AB", Arrays.asList("ALFA LAVAL", "ALFA LAVAL CORPORATE AKTIEBOLAG"));
		COMPANIES.put("Sandvik Intellectual Property AB", Arrays.asList("SANDVIK INTELLECTUAL PROPERTY", "SANDVIK INTELLECTUAL PROPERTY AKTIEBOLAG"));

		// Germany
		COMPANIES.put("Airbus Deutschland GmbH", Arrays.asList("AIRBUS DEUTSCHLAND"));
		COMPANIES.put("Phoenix Contact GmbH & Co. KG", Arrays.asList("PHOENIX CONTACT"));
		COMPANIES.put("DWS Group GmbH & Co KGaA", Arrays.asList("DWS GROUP"));

		// Japan
		COMPANIES.put("Kabushiki Kaisha Toshiba", Arrays.asList("TOSHIBA"));
		COMPANIES.put("Toshiba Tec Kabushiki Kaisha", Arrays.asList("TOSHIBA TEC"));
		COMPANIES.put("Canon Kabuhsiki Kaisha", Arrays.asList("CANON")); // misspelled in patent Kabuhsiki => Kabushiki.		
		COMPANIES.put("NSK-Warner K.K.", Arrays.asList("NSK-WARNER", "NSK-WARNER KABUSHIKI KAISHA"));

		// Found in Patents:
		// Artificial Lift Company Limited Lion Works
	}

	private static List<String> COMPANIES_FALSE_POS = new ArrayList<String>();
	static {
		//COMPANIES_FALSE_POS.add("Deere & Company");
		//COMPANIES_FALSE_POS.add("Milliken & Company");
		//COMPANIES_FALSE_POS.add("Becton, Dickinson and Company");
		COMPANIES_FALSE_POS.add("Rutgers, The State University");
		//COMPANIES_FALSE_POS.add("Hain, Uwe & Hain, Silke");
	}

	@Test
	public void CompanySynonyms() {
		OrgSynonymGenerator generator = new OrgSynonymGenerator();
		for (Entry<String, List<String>> entry : COMPANIES.entrySet()) {
			NameOrg name = new NameOrg(entry.getKey());
			generator.computeSynonyms(name); 
            assertTrue("Failed '" + entry.getKey() + "'\n\t Missing: " + missingSynonyms(name, entry.getValue()) + "\n\t expect to contain: " + entry.getValue().toString() + "\n\t actual: " + name.getSynonyms().toString(), name.getSynonyms().containsAll(entry.getValue()));
		}
	}

	private List<String> missingSynonyms(NameOrg name, List<String> expect) {
		List<String> missing = new ArrayList<String>();
		for(String expectItem: expect) {
			if (!name.getSynonyms().contains(expectItem)) {
				missing.add(expectItem);
				
			}
		}
		return missing;
	}
	
	@Test
	public void FalsePositives() {
		OrgSynonymGenerator generator = new OrgSynonymGenerator();
		for (String entry : COMPANIES_FALSE_POS) {
			NameOrg name = new NameOrg(entry);
			generator.computeSynonyms(name);
            assertTrue("Failed '" + entry, name.getSynonyms().size() == 2);
		}
	}
}
