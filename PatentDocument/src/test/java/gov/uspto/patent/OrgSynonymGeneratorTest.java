package gov.uspto.patent;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.entity.Address;
import gov.uspto.patent.model.entity.Assignee;
import gov.uspto.patent.model.entity.NameOrg;

public class OrgSynonymGeneratorTest {

	/*
	 * Real Company Examples taken from Patent XML
	 * synonyms are generate by removing company prefix and suffixes.
	 */
	private static Map<Assignee, List<String>> COMPANIES = new LinkedHashMap<Assignee, List<String>>();
	static {
		COMPANIES.put(entity("CareFusion 2200, Inc.", CountryCode.US), Arrays.asList("CAREFUSION 2200"));
		COMPANIES.put(entity("International Business Machines Corporation", CountryCode.US), Arrays.asList("INTERNATIONAL BUSINESS MACHINES", "INTERNATIONAL BUSINESS MACHINES CORP."));
		COMPANIES.put(entity("Sumitomo Bakelite Company, Ltd.", CountryCode.JP), Arrays.asList("SUMITOMO BAKELITE", "SUMITOMO BAKELITE COMPANY, LIMITED", "SUMITOMO BAKELITE CO., LTD."));
		COMPANIES.put(entity("Samsung Electronics Co., Ltd.", CountryCode.KR), Arrays.asList("SAMSUNG ELECTRONICS", "SAMSUNG ELECTRONICS CO., LIMITED", "SAMSUNG ELECTRONICS CO LIMITED", "SAMSUNG ELECTRONICS COMPANY, LIMITED", "SAMSUNG ELECTRONICS COMPANY LIMITED"));
		COMPANIES.put(entity("Dongbu HiTek Co., ltd.", CountryCode.KR), Arrays.asList("DONGBU HITEK", "DONGBU HITEK CO., LIMITED"));
		COMPANIES.put(entity("NEC LCD Technologies, Ltd.", CountryCode.JP), Arrays.asList("NEC LCD TECHNOLOGIES", "NEC LCD TECHNOLOGIES, LIMITED"));
		COMPANIES.put(entity("Indus Biotech Pvt. Ltd.", CountryCode.IN), Arrays.asList("INDUS BIOTECH", "INDUS BIOTECH PVT. LIMITED"));
		COMPANIES.put(entity("Hitachi Plant Technologies, LTD.", CountryCode.JP), Arrays.asList("HITACHI PLANT TECHNOLOGIES", "HITACHI PLANT TECHNOLOGIES, LIMITED"));
		COMPANIES.put(entity("NEC Corporation", CountryCode.JP), Arrays.asList("NEC", "NEC CORP.", "NEC CORP"));
		COMPANIES.put(entity("Orbotech Ltd", CountryCode.IL), Arrays.asList("ORBOTECH"));
		COMPANIES.put(entity("Silverbrook Research Pty Ltd", CountryCode.AU), Arrays.asList("SILVERBROOK RESEARCH"));
		COMPANIES.put(entity("Silverbrook Research Pty Limited", CountryCode.AU), Arrays.asList("SILVERBROOK RESEARCH"));
		COMPANIES.put(entity("Honeywell International Inc.", CountryCode.US), Arrays.asList("HONEYWELL INTL", "HONEYWELL INTERNATIONAL INCORPORATION", "HONEYWELL INTERNATIONAL"));
		COMPANIES.put(entity("ADC Telecommunications, Inc.", CountryCode.US), Arrays.asList("ADC TELECOMMUNICATIONS", "ADC TELECOMMUNICATIONS, INCORPORATION"));
		COMPANIES.put(entity("Japan Aviation Electronics Industry, Limited", CountryCode.JP), Arrays.asList("JAPAN AVIATION ELECTRONICS INDUSTRY", "JAPAN AVIATION ELECTRONICS INDUSTRY, LTD."));
		COMPANIES.put(entity("HeathCo LLC", CountryCode.US), Arrays.asList("HEATHCO"));
		COMPANIES.put(entity("Dupont Teijin Films U.S. Limited Partnership", CountryCode.US), Arrays.asList("DUPONT TEIJIN FILMS U.S."));
		COMPANIES.put(entity("Hewlett-Packard Development Company, L.P.", CountryCode.US), Arrays.asList("HEWLETT-PACKARD DEVELOPMENT", "HEWLETT PACKARD DEVELOPMENT", "HEWLETT-PACKARD DEVELOPMENT COMPANY"));
		COMPANIES.put(entity("Rochal Industries, LLP", CountryCode.US), Arrays.asList("ROCHAL INDUSTRIES"));
		COMPANIES.put(entity("Rolls-Royce PLC", CountryCode.UK), Arrays.asList("ROLLS-ROYCE", "ROLLS-ROYCE PLC"));
		COMPANIES.put(entity("Rolls-Royce plc", CountryCode.UK), Arrays.asList("ROLLS-ROYCE", "ROLLS-ROYCE PLC"));
		COMPANIES.put(entity("Au Optronics Corporation", CountryCode.TW), Arrays.asList("AU OPTRONICS CORP.", "AU OPTRONICS CORPORATION", "AU OPTRONICS"));
		COMPANIES.put(entity("AU Optronics Corp.", CountryCode.TW), Arrays.asList("AU OPTRONICS CORP.", "AU OPTRONICS CORPORATION", "AU OPTRONICS"));
		COMPANIES.put(entity("DENSO CORPORATION", CountryCode.JP), Arrays.asList("DENSO", "DENSO CORP.", "DENSO CORPORATION"));
		COMPANIES.put(entity("Texas Instruments Incorporated", CountryCode.US), Arrays.asList("TEXAS INSTRUMENTS"));
		COMPANIES.put(entity("Koronis Pharmaceuticals, Incorporated", CountryCode.US), Arrays.asList("KORONIS PHARMACEUTICALS"));
		COMPANIES.put(entity("Tyco Healthcare Group, LP", CountryCode.US), Arrays.asList("TYCO HEALTHCARE GROUP"));
		COMPANIES.put(entity("The Western Union Company", CountryCode.US), Arrays.asList("WESTERN UNION"));
		COMPANIES.put(entity("Sportsman Corp.", CountryCode.UNDEFINED), Arrays.asList("SPORTSMAN", "SPORTSMAN CORPORATION"));
		COMPANIES.put(entity("A.R.I. Flow Control Accessories Agricultural Cooperative Association Ltd.", CountryCode.UNDEFINED), Arrays.asList("ARI FLOW CONTROL ACCESSORIES AGRICULTURAL"));
		COMPANIES.put(entity("BP Exploration Operating Company Limited", CountryCode.US), Arrays.asList("BP EXPLORATION OC"));
		COMPANIES.put(entity("Sling Media PVT LTD", CountryCode.US), Arrays.asList("SLING MEDIA"));
		COMPANIES.put(entity("e-ImageData Corp.", CountryCode.US), Arrays.asList("E-IMAGEDATA", "E-IMAGEDATA CORP.", "E-IMAGEDATA CORPORATION"));
		COMPANIES.put(entity("GM GLOBAL TECHNOLOGY OPERATIONS LLC", CountryCode.US), Arrays.asList("GM GLOBAL TECH OPS"));
		COMPANIES.put(entity("Lawrence Livermore National Security, LLC", CountryCode.US), Arrays.asList("LAWRENCE LIVERMORE NATL SEC"));
		COMPANIES.put(entity("ELECTRONICS AND TELECOMMUNICATIONS RESEARCH INSTITUTE", CountryCode.US), Arrays.asList("ELECTRONICS AND TELECOMMUNICATIONS RES INST"));
		COMPANIES.put(entity("Kulicke and Soffa Industries, Inc.", CountryCode.US), Arrays.asList("KULICKE & SOFFA IND", "KULICKE AND SOFFA IND"));
		COMPANIES.put(entity("The George Washington University", CountryCode.US), Arrays.asList("GEORGE WASHINGTON UNI"));		
		COMPANIES.put(entity("Gazdzinski & Associates, PC", CountryCode.US), Arrays.asList("GAZDZINSKI & ASSOCIATES"));
		COMPANIES.put(entity("INMOBI PTE. LTD.", CountryCode.SG), Arrays.asList("INMOBI"));

		COMPANIES.put(entity("BRITISH TELECOMMUNICATIONS PUBLIC LIMITED COMPANY", CountryCode.UK), Arrays.asList("BRITISH TELECOMMUNICATIONS")); // TODO : BRITISH TELECOMM	
		COMPANIES.put(entity("Tata Consultancy Services Limited", CountryCode.IN), Arrays.asList("TATA CONSULT SVCS", "TATA CONSULTANT SVCS"));
		COMPANIES.put(entity("SCHERRER PATENT + TRADEMARK LAW", CountryCode.US), Arrays.asList("SCHERRER PATENT + TM", "SCHERRER PATENT AND TRADEMARK LAW"));
		COMPANIES.put(entity("Vista IP Law Group, LLP", CountryCode.US), Arrays.asList("VISTA IP", "VISTA IP GRP"));
		COMPANIES.put(entity("Open Text SA ULC", CountryCode.CA), Arrays.asList("OPEN TEXT"));
		COMPANIES.put(entity("MASTERCARD INTERNATIONAL INCORPORATED", CountryCode.US), Arrays.asList("MASTERCARD INTL"));
		COMPANIES.put(entity("Fleit Gibbons Gutman Bongini Bianco PL", CountryCode.UK), Arrays.asList("FLEIT GIBBONS GUTMAN BONGINI BIANCO"));

		// Company Name is internet domain name with ".com"
		COMPANIES.put(entity("Stamps.com Inc", CountryCode.US), Arrays.asList("STAMPS.COM"));

		// IP, IP Law Firms and Holding Companies
		COMPANIES.put(entity("Anglehart et al.", CountryCode.US), Arrays.asList("ANGLEHART"));
		COMPANIES.put(entity("Oblon, McClelland, Maier & Neustadt, L.L.P.", CountryCode.US), Arrays.asList("OBLON MCCLELLAND MAIER & NEUSTADT"));
		COMPANIES.put(entity("Panasonic Intellectual Property Management Co., Ltd.", CountryCode.US), Arrays.asList("PANASONIC IP MGMT", "PANASONIC IP MANAGEMENT", "PANASONIC INTELLECTUAL PROPERTY MANAGEMENT"));
		COMPANIES.put(entity("Patents on Demand P.A.", CountryCode.US), Arrays.asList("PATENTS ON DEMAND"));
		COMPANIES.put(entity("Fish & Richardson P.C.", CountryCode.US), Arrays.asList("FISH & RICHARDSON PC", "FISH & RICHARDSON", "FISH AND RICHARDSON", "FISH AND RICHARDSON P.C.", "FISH AND RICHARDSON PC"));
		COMPANIES.put(entity("Andrus Intellectual Property Law, LLP", CountryCode.UNDEFINED), Arrays.asList("ANDRUS IP LAW"));
		COMPANIES.put(entity("Allen IP, Incorporated", CountryCode.UNDEFINED), Arrays.asList("ALLEN IP"));
		COMPANIES.put(entity("AT&T INTELLECTUAL PROPERTY I, L.P.", CountryCode.US), Arrays.asList("AT&T IP I"));
		COMPANIES.put(entity("EMC IP Holding Company LLC", CountryCode.US), Arrays.asList("EMC IPHC", "EMC IP HC", "EMC IP HOLDING", "EMC IP HOLDING CO"));
		COMPANIES.put(entity("Yahoo Holdings, Inc.", CountryCode.US), Arrays.asList("YAHOO HC", "YAHOO HOLDINGS", "YAHOO HOLDINGS, INCORPORATION"));
		COMPANIES.put(entity("Bliss Holdings, LLC", CountryCode.US), Arrays.asList("BLISS HC", "BLISS HOLDINGS"));
		COMPANIES.put(entity("IP Holdings LLC.", CountryCode.US), Arrays.asList("IP HC", "IP HOLDINGS"));
		COMPANIES.put(entity("Vista IP Law Group, LLP", CountryCode.US), Arrays.asList("VISTA IP LAW GRP"));

		// Variations with (AND|&|+) company
		COMPANIES.put(entity("Deere & Company", CountryCode.US), Arrays.asList("DEERE & CO.", "DEERE & COMPANY", "DEERE + COMPANY", "DEERE AND COMPANY"));
		COMPANIES.put(entity("Milliken & Company", CountryCode.US), Arrays.asList("MILLIKEN & COMPANY", "MILLIKEN AND COMPANY", "MILLIKEN + COMPANY", "MILLIKEN AND CO", "MILLIKEN & CO", "MILLIKEN + CO"));

		// Variations with "AND" "+" or "&"
		COMPANIES.put(entity("S. C. Johnson & Son, Inc.", CountryCode.US), Arrays.asList("S C JOHNSON & SON", "S. C. JOHNSON & SON, INCORPORATION", "S. C. JOHNSON & SON"));	
		COMPANIES.put(entity("S.C. Johnson & Son, Inc.", CountryCode.US), Arrays.asList("S C JOHNSON & SON"));
		COMPANIES.put(entity("KED & Associates, LLP", CountryCode.UNDEFINED), Arrays.asList("KED & ASSOCS"));
		COMPANIES.put(entity("Harness, Dickey & Pierce, P.L.C.", CountryCode.US), Arrays.asList("HARNESS DICKEY & PIERCE", "HARNESS, DICKEY & PIERCE", "HARNESS DICKEY AND PIERCE"));
		COMPANIES.put(entity("Patterson + Sheridan, LLP", CountryCode.US), Arrays.asList("PATTERSON & SHERIDAN", "PATTERSON + SHERIDAN", "PATTERSON AND SHERIDAN"));
		COMPANIES.put(entity("Yeda Research and Development Co. Ltd.", CountryCode.UNDEFINED), Arrays.asList("YEDA RESEARCH & DEV", "YEDA RESEARCH AND DEV", "YEDA RESEARCH AND DEVELOPMENT"));
		COMPANIES.put(entity("Yeda Research & Development Company Ltd.", CountryCode.UNDEFINED), Arrays.asList("YEDA RESEARCH & DEVELOPMENT", "YEDA RESEARCH AND DEVELOPMENT", "YEDA RESEARCH & DEVELOPMENT COMPANY LIMITED", "YEDA RESEARCH AND DEVELOPMENT COMPANY LIMITED"));
		COMPANIES.put(entity("L & P Management Company", CountryCode.US), Arrays.asList("L & P MGMT", "L & P MANAGEMENT COMPANY", "L & P MANAGEMENT CO"));
		COMPANIES.put(entity("L&P Management Company", CountryCode.US), Arrays.asList("L & P MGMT", "L & P MANAGEMENT COMPANY", "L & P MANAGEMENT CO"));
		COMPANIES.put(entity("Landor & Hawa International Limited", CountryCode.UNDEFINED), Arrays.asList("LANDOR AND HAWA INTL"));
		COMPANIES.put(entity("The Goodyear Tire & Rubber Company", CountryCode.US), Arrays.asList("GOODYEAR TIRE & RUBBER", "GOODYEAR TIRE AND RUBBER", "GOODYEAR TIRE & RUBBER COMPANY", "GOODYEAR TIRE & RUBBER CO"));
		COMPANIES.put(entity("Black & Decker, Inc.", CountryCode.US), Arrays.asList("BLACK & DECKER", "BLACK AND DECKER"));
		
		//COMPANIES.put(entity("FRANZ SCHNEIDER BRAKEL GMBH + CO KG", CountryCode.UNDEFINED), Arrays.asList("FRANZ SCHNEIDER BRAKEL"));
		// Canon USA, Inc. I.P. Division
		// Bereskin & Parr LLP/S.E.N.C.R.L., s.r.l.
		// COMPANIES.put(entity("Nokia Technologies Oy", CountryCode.UNDEFINED), Arrays.asList("NOKIA TECH OY", "NOKIA TECH"));

		// Spanish (ES)
		COMPANIES.put(entity("Coprecitec, S.L.", CountryCode.ES), Arrays.asList("COPRECITEC"));
		COMPANIES.put(entity("TELEFONICA DIGITAL ESPAÑA, S.L.U.", CountryCode.ES), Arrays.asList("TELEFONICA DIGITAL ESPAÑA"));
		COMPANIES.put(entity("Indo Internacional, S.A.", CountryCode.ES), Arrays.asList("INDO INTL.", "INDO INTERNACIONAL"));

		// French Speaking Countries (FR, BE, LU)
		COMPANIES.put(entity("L'Oreal", CountryCode.FR), Arrays.asList("LOREAL", "L'OREAL"));
		COMPANIES.put(entity("Compagnie Plastic Omnium", CountryCode.FR), Arrays.asList("PLASTIC OMNIUM"));
		COMPANIES.put(entity("Zimmer Spine, S.A.S.", CountryCode.FR), Arrays.asList("ZIMMER SPINE"));
		COMPANIES.put(entity("Conversant Wireless Lecensing S.a.r.l", CountryCode.LU), Arrays.asList("CONVERSANT WIRELESS LECENSING", "CONVERSANT WIRELESS LECENSING SOCIÉTÉ ANONYME À RESPONSABILITÉ LIMITÉE"));
		COMPANIES.put(entity("GlaxoSmithKline Biologicals S.A.", CountryCode.BE), Arrays.asList("GLAXOSMITHKLINE BIO", "GLAXOSMITHKLINE BIOLOGICALS"));
		COMPANIES.put(entity("Minvasys, SA", CountryCode.UNDEFINED), Arrays.asList("MINVASYS"));
		COMPANIES.put(entity("Airbus Operations (SAS)", CountryCode.FR), Arrays.asList("AIRBUS OPS SAS", "AIRBUS OPERATIONS SAS"));

		// Italian (IT)
		COMPANIES.put(entity("F.LLI MARIS S.p.A.", CountryCode.IT), Arrays.asList("F.LLI MARIS", "F LLI MARIS", "F.LLI MARIS SOCIETÀ PER AZIONI"));
		COMPANIES.put(entity("Nerviano Medical Sciences S.r.l.", CountryCode.IT), Arrays.asList("NERVIANO MEDICAL SCIENCES")); // SRL can also be spanish..

		// Dutch (NL)
		COMPANIES.put(entity("Firma G.B. Boucherie, naamloze vennootschap", CountryCode.NL), Arrays.asList("G.B. BOUCHERIE", "GB BOUCHERIE"));
		COMPANIES.put(entity("Koninklijke Philips Electronics N.V.", CountryCode.NL), Arrays.asList("PHILIPS ELECTRONICS", "KONINKLIJKE PHILIPS ELECTRONICS", "PHILIPS ELECTRONICS N.V."));
		COMPANIES.put(entity("HERE Global B.V.", CountryCode.NL), Arrays.asList("HERE GLOBAL"));
		COMPANIES.put(entity("ITREC, B.V.", CountryCode.NL), Arrays.asList("ITREC"));

		// Swedish (SE)
		COMPANIES.put(entity("Alfa Laval Corporate AB", CountryCode.SE), Arrays.asList("ALFA LAVAL", "ALFA LAVAL CORPORATE AKTIEBOLAG"));
		COMPANIES.put(entity("Sandvik Intellectual Property AB", CountryCode.SE), Arrays.asList("SANDVIK IP", "SANDVIK INTELLECTUAL PROPERTY", "SANDVIK INTELLECTUAL PROPERTY AKTIEBOLAG"));
		COMPANIES.put(entity("Wonderland Switzerland AG", CountryCode.UNDEFINED), Arrays.asList("WONDERLAND SWITZERLAND", "WONDERLAND SWITZERLAND AKTIENGESELLSCHAFT"));

		// Finish (FI)
		COMPANIES.put(entity("Siemens Osakeyhtiö", CountryCode.FI), Arrays.asList("SIEMENS", "SIEMENS OY", "SIEMENS OSAKEYHTIÖ"));
		COMPANIES.put(entity("Siemens OY", CountryCode.FI), Arrays.asList("SIEMENS", "SIEMENS OY", "SIEMENS OSAKEYHTIÖ"));

		// Danish (DK)
		COMPANIES.put(entity("Tresu Anlaeg A/S", CountryCode.DK), Arrays.asList("TRESU ANLAEG")); // "Tresu Anlaeg Aktieselskab"
		COMPANIES.put(entity("Widex A/S", CountryCode.DK), Arrays.asList("WIDEX"));

		// German (DE)
		COMPANIES.put(entity("Viering, Jentschura & Partner mbB", CountryCode.DE), Arrays.asList("VIERING, JENTSCHURA & PARTNER"));
		COMPANIES.put(entity("Airbus Operations GmbH", CountryCode.DE), Arrays.asList("AIRBUS OPS", "AIRBUS OPS GMBH"));
		COMPANIES.put(entity("Schott AG", CountryCode.DE), Arrays.asList("SCHOTT"));
		COMPANIES.put(entity("Bayer Aktiengesellschaft", CountryCode.DE), Arrays.asList("BAYER", "BAYER AG"));
		COMPANIES.put(entity("Airbus Deutschland GmbH", CountryCode.DE), Arrays.asList("AIRBUS DEUTSCHLAND"));
		COMPANIES.put(entity("Phoenix Contact GmbH & Co. KG", CountryCode.DE), Arrays.asList("PHOENIX CONTACT"));
		COMPANIES.put(entity("DWS Group GmbH & Co KGaA", CountryCode.DE), Arrays.asList("DWS GRP", "DWS GROUP"));
		COMPANIES.put(entity("Biopharm Gesellschaft zur Biotechnologischen Entwicklung von Pharmaka mbH", CountryCode.DE), Arrays.asList("BIOPHARM GESELLSCHAFT ZUR BIOTECHNOLOGISCHEN ENTWICKLUNG VON PHARMAKA"));

		// Japan (JP)
		COMPANIES.put(entity("Kabushiki Kaisha Toshiba", CountryCode.JP), Arrays.asList("TOSHIBA"));
		COMPANIES.put(entity("Toshiba Tec Kabushiki Kaisha", CountryCode.JP), Arrays.asList("TOSHIBA TEC"));
		COMPANIES.put(entity("Canon Kabuhsiki Kaisha", CountryCode.JP), Arrays.asList("CANON")); // misspelled in patent Kabuhsiki => Kabushiki.		
		COMPANIES.put(entity("NSK-Warner K.K.", CountryCode.JP), Arrays.asList("NSK-WARNER", "NSK-WARNER KABUSHIKI KAISHA"));

		// China (CN)
		COMPANIES.put(entity("Lenovo (Beijing) Co., Ltd.", CountryCode.CN), Arrays.asList("LENOVO"));
		COMPANIES.put(entity("Shanghai Jianguo Electronics Co., Ltd.", CountryCode.CN), Arrays.asList("JIANGUO ELECTRONICS"));

		// Found in Patents:
		// Artificial Lift Company Limited Lion Works
	}

	public static Assignee entity(String companyName, CountryCode countryCode) {
		Address address = new Address(null, null, countryCode);
		NameOrg name = new NameOrg(companyName);
		return new Assignee(name, address);
	}

	@Test
	public void CompanySynonyms() {
		OrgSynonymGenerator generator = new OrgSynonymGenerator();
		for (Entry<Assignee, List<String>> entry : COMPANIES.entrySet()) {			
			NameOrg name = (NameOrg) entry.getKey().getName();
			generator.computeSynonyms(entry.getKey());
            assertTrue("Failed '" + entry.getKey() + "'\n\t Missing: " + missingSynonyms(name, entry.getValue()) + "\n\t expect to contain: " + entry.getValue().toString() + "\n\t actual: " + name.getSynonyms().toString(), name.getSynonyms().containsAll(entry.getValue()));
		}
	}

	private static Map<String, String> CHINESE_COMPANIES = new LinkedHashMap<String, String>();
	static {
		CHINESE_COMPANIES.put("Lenovo (Beijing) Co., Ltd.", "LENOVO CO., LTD.");
		CHINESE_COMPANIES.put("Huawei Device (Dongguan) Co., Ltd.", "HUAWEI DEVICE CO., LTD.");
		CHINESE_COMPANIES.put("HUAWEI DEVICE (DONGGUAN) CO., LTD.", "HUAWEI DEVICE CO., LTD.");
		CHINESE_COMPANIES.put("BAILIJIA CANDY TOYS AND GIFT PRODUCTION (DONGGUAN) LIMITED", "BAILIJIA CANDY TOYS AND GIFT PRODUCTION LIMITED");
		CHINESE_COMPANIES.put("Delta Electronics (Jiangsu) Ltd.", "DELTA ELECTRONICS LTD.");
		CHINESE_COMPANIES.put("HONGFUJIN PRECISION ELECTRONICS (ZHENGZHOU) CO., LTD.", "HONGFUJIN PRECISION ELECTRONICS CO., LTD.");
		CHINESE_COMPANIES.put("HongQiSheng Precision Electronics (QinHuangDao) Co., Ltd.", "HONGQISHENG PRECISION ELECTRONICS CO., LTD.");
		CHINESE_COMPANIES.put("TPK Touch Solutions (Xiamen) Inc.", "TPK TOUCH SOLUTIONS INC.");
		CHINESE_COMPANIES.put("HONG FU JIN PRECISION INDUSTRY (WuHan) CO., LTD.", "HONG FU JIN PRECISION INDUSTRY CO., LTD.");
		CHINESE_COMPANIES.put("HONGFUJIN PRECISION ELECTRONICS (TIANJIN) CO., LTD.", "HONGFUJIN PRECISION ELECTRONICS CO., LTD.");
		CHINESE_COMPANIES.put("EYEBRIGHT MEDICAL TECHNOLOGY (BEIJING) CO., LTD.", "EYEBRIGHT MEDICAL TECHNOLOGY CO., LTD.");
		CHINESE_COMPANIES.put("Qisda Optronics (Suzhou) Co., Ltd.", "QISDA OPTRONICS CO., LTD.");
		CHINESE_COMPANIES.put("STERILANCE MEDICAL (SUZHOU) INC.", "STERILANCE MEDICAL INC.");
		CHINESE_COMPANIES.put("Spruson & Ferguson (HK)", "SPRUSON & FERGUSON");
		CHINESE_COMPANIES.put("Bio-Medical Engineering (HK) Limited", "BIO-MEDICAL ENGINEERING LIMITED");
		CHINESE_COMPANIES.put("LITE-ON ELECTRONICS (GUANGZHOU) LIMITED", "LITE-ON ELECTRONICS LIMITED");
		CHINESE_COMPANIES.put("TENCENT TECHNOLOGY (SHENZHEN) COMPANY LIMITED", "TENCENT TECHNOLOGY COMPANY LIMITED");
		CHINESE_COMPANIES.put("Jenkins Asia Tech (Shanghai) Limited", "JENKINS ASIA TECH LIMITED");
		CHINESE_COMPANIES.put("Shanghai Jianguo Electronics Co., Ltd.", "JIANGUO ELECTRONICS CO., LTD.");
	}
	@Test 
	public void ChineseCompanyNames() {				
		OrgSynonymGenerator generator = new OrgSynonymGenerator();

		for (Entry<String, String> entry : CHINESE_COMPANIES.entrySet()) {
			NameOrg name = new NameOrg(entry.getKey());
			name.addSynonymNorm(entry.getKey());
			generator.chineseCompanyNames(name);
			
            assertTrue("Failed '" + entry.getKey() + "'\n\t Missing: " + entry.getValue() + "\n\t expect to contain: " + entry.getValue().toString() + "\n\t actual: " + name.getSynonyms().toString(), name.getSynonyms().contains(entry.getValue()));
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

}
