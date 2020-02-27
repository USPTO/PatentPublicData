package gov.uspto.tm.doc.brs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import gov.uspto.common.io.PartitionFileWriter;
import gov.uspto.common.io.TeeWriter;
import gov.uspto.common.text.DateUtil;
import gov.uspto.common.text.WordUtil;
import gov.uspto.parser.keyvalue.KeyValue;
import gov.uspto.parser.keyvalue.Kv2KvXml;
import gov.uspto.parser.keyvalue.Kv2SolrXml;
import gov.uspto.parser.keyvalue.KvDocBuilder;
import gov.uspto.parser.keyvalue.KvReaderFixedWidth;
import gov.uspto.patent.PatentReaderException;
import gov.uspto.patent.bulk.DumpFileAps;

/**
 * Trademark BRS
 * 
 * <p>
 * Trademark record in BRS format, which has brackets around field names and
 * closing tag when nested
 * </p>
 *
 *
 ** =========== Input =============
 * 
 * <pre>
 * {@code
<WM> BOOGIE'S                                
</WM>                                        
</BI>                                        
<CL> IC  041.                                
<US>   US 100 101 107.                       
<CP> 009 015 016 028 035 036 037 038 039 040 
     041 042 043 044 045 200.                
<GS> SPORTS EVENTS AND ENTERTAINMENT EVENTS T
     ICKETING AGENT SERVICES                 
<U1> 20090201                                
<U2> 20090201                                
</GS>                          
<SN> 76720770                             
<ON> (APPLICANT)                             
<PN> BOOGIE'S TICKETS, INC.                  
<EN> LIMITED LIABILITY COMPANY               
<OW> (APPLICANT)                             
<PN> BOOGIE'S TICKETS, INC.                  
<EN> LIMITED LIABILITY COMPANY               
<CI> NEW JERSEY
}
 * </pre>
 * 
 * =========== Output =============
 * 
 * <pre>
 * {@code
<DOCUMENT>
  <WM>BOOGIE'S</WM>
  <CL>IC 041.</CL>
  <US>US 100 101 107.</US>
  <CP>009 015 016 028 035 036 037 038 039 040041 042 043 044 045 200.</CP>
  <GS>SPORTS EVENTS AND ENTERTAINMENT EVENTS TICKETING AGENT SERVICES</GS>
  <U1>20090201</U1>
  <U2>20090201</U2>
  <ON>(APPLICANT)</ON>
  <PN>BOOGIE'S TICKETS, INC.</PN>
  <EN>LIMITED LIABILITY COMPANY</EN>
  <OW>(APPLICANT)</OW>
  <PN>BOOGIE'S TICKETS, INC.</PN>
  <EN>LIMITED LIABILITY COMPANY</EN>
  <CI>NEW JERSEY</CI>
}
 * </pre>
 * 
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 */
public class TmBrs extends KvReaderFixedWidth {

	private static final Logger LOGGER = LoggerFactory.getLogger(TmBrs.class);

	private final static Pattern KEY_TRANSFORM = Pattern.compile("^<([A-Z0-9]{2,})>");
	private final static Pattern CLOSING_TAGS = Pattern.compile("(:?</[A-Z][A-Z0-9]>\\s*)+$");

	// private static List<String> MAINTAIN_SPACE_FIELDS = Arrays.asList(new
	// String[] { "TL", "GS", "DE" });

	// Solr can only use a single tokenizer. Individual Values can be tokenized
	// before sending to Solr.
	private static final Pattern PATTERN_TOKENIZER = Pattern.compile("[,; ]");
	private static final Pattern POST_TOKEN_PATTERN_FILTER = Pattern.compile("[^A-z0-9-\\\\/]");

	private static final Set<String> TOKENIZE_FIELDS = new HashSet<>(Arrays.asList("DC", "US", "CP", "CL"));

	private static final Set<String> DATE_FIELDS = new HashSet<>(
			Arrays.asList("PD", "PO", "FD", "PF", "RD", "AD", "CD", "U1", "U2", "SR"));

	private static final Map<String, String> BOOLEAN_FIELDS = new HashMap<String, String>() {
		{
			put("ST", "STANDARD CHARACTERS CLAIMED");
			put("CR", "CHANGE IN REGISTRATION HAS OCCURRED");
			put("AR", "ASSIGNMENT RECORDED");
			put("LD", "LIVE");
		}
	};

	private static final Map<String, String> RENAME_FIELDS = new HashMap<String, String>() {
		{
			put("SN", "id"); // 77367856
			put("SO", "serial_other_id_ss"); // 77-367856
			put("IR", "intl_id_ss"); // 0272587
			put("RN", "reg_id_s");
			put("PR", "prior_reg_ids_td"); // "3282741;3891127;3891128" "0541197;0839755;AND OTHERS"
			put("WM", "wmark_s");
			put("PM", "wmark_pseudo_t");
			put("PD", "priority_dt");
			put("FD", "filed_dt");
			put("RD", "register_dt");
			put("AD", "abandon_dt");
			put("CD", "cancel_dt");
			put("RE", "renewal_t"); // 2ND RENEWAL 20160123
			put("PF", "phys_filed_dt"); // Physical Filing Date
			put("PO", "pub_op_dt"); // Published for Opposition Date
			put("SR", "supp_reg_dt"); // Supplemental Register Date
			put("TL", "translate_txt");
			put("GS", "goods_services_txt");
			put("DE", "mark_desc_txt");
			put("D1", "mark_desc_txt");
			put("TF", "distinct_limit_txt");
			put("OD", "other_data_txt");
			put("OB", "original_basis_s");
			put("CB", "current_basis_s");
			put("PN", "owner_name_txt");
			put("CO", "owner_other_name_txt");
			put("DB", "owner_dba_txt");
			put("ON", "owner_type_txt");
			put("CI", "owner_citizen_txt");
			put("EN", "owner_entity_txt");
			put("AI", "owner_street_1_addr_txt");
			put("AS", "owner_street_2_addr_txt");
			put("CY", "owner_city_addr_txt");
			put("SC", "owner_state_cntry_addr_txt");
			put("AT", "attorney_t");
			put("MD", "drawing_code_i");
			put("AR", "assignment_recorded_b"); // ASSIGNMENT RECORDED
			put("CR", "reg_changed_b"); // CHANGE IN REGISTRATION HAS OCCURRED
			put("ST", "st_char_claimed_b");
			put("LD", "alive_b");
			put("TM", "mark_type_s");
			put("RG", "register_s");
			put("DD", "design_desc_txt");
			put("TD", "design_count_i"); // 2
			put("DC", "design_code_txt"); // 020102 020107
			put("CL", "intl_class_txt"); // 009
			put("CP", "coordinated_class_txt"); // 042 001 002 003
			put("PC", "psudo_class_txt"); // IC: 039; PSEUDO CLASS(ES): 035
			put("US", "us_class_txt"); // 100 101
			put("OW", "ignored_OW");
			put("AF", "affidavit_t"); // SECT 12C. SECT 15. SECTION 8(10-YR) 20050214.
			put("U1", "u1_dts");
			put("U2", "u2_dts");
			put("AN", "an_txt"); // The mark was first used anywhere in a different form other than that sought
									// to be registered at least as early as 12/01/2018
			put("TD", "td_i"); // 0000
			put("TP", "tp_i"); // 0000

			put("D0", "d0_txt");
			put("NC", "nc_txt");
		}
	};

	Map<String, String> AF_SECTION = new HashMap<String, String>() {
		{
			put("SECT 12C", "12C");
			put("SECT 15", "15");
			put("SECT 8 (6-YR)", "8_6yr");
			put("SECTION 8(10-YR)", "8_10yr");
			put("PARTIAL SECTION 8(10-YR)", "8_partial_10yr");
			put("SECTION 71", "71");
		}
	};

	private static final String SOLR_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n<add>\n";
	private static final String SOLR_FOOTER = "</add>\n";

	private final boolean convertValues;
	private final boolean renameFields;

	public TmBrs(boolean renameFields, boolean convertValues) {
		super(5);
		this.renameFields = renameFields;
		this.convertValues = convertValues;
		// super.setMaintainSpaceFields(MAINTAIN_SPACE_FIELDS);
	}

	@Override
	public String keyTransform(String key) {
		// transform <AB> to AB
		return KEY_TRANSFORM.matcher(key.trim()).replaceFirst("$1");
	}

	@Override
	public boolean isKeyValid(final String key) {
		if (key.trim().length() == 4) {
			if (key.startsWith("<") && key.endsWith(">")) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String keyRename(String key) {
		if (renameFields) {
			return RENAME_FIELDS.getOrDefault(key.toUpperCase(), key);
		} else {
			return key;
		}
	}

	@Override
	public String valueTransform(String key, String keyRenamed, String value) {
		value = CLOSING_TAGS.matcher(value).replaceAll("");

		if (convertValues) {
			return convertValue(key, value);
		} else {
			return value;
		}
	}

	public String convertValue(final String key, String value) {
		if (DATE_FIELDS.contains(key.toUpperCase())) {
			try {
				return DateUtil.toDateTimeISO(value.trim());
			} catch (DateTimeParseException e) {
				LOGGER.warn("!! Invalid Date: '{}' from field: '{}'", value.trim(), key, e);
			}
		}

		if (BOOLEAN_FIELDS.containsKey(key.toUpperCase())) {
			return String.valueOf(BOOLEAN_FIELDS.get(key.toUpperCase()).equals(value.trim()));
		}

		/*
		 * if ("PR".equalsIgnoreCase(key)) { return Strings.join(value.split(";"), " ");
		 * }
		 */

		if ("CL".equalsIgnoreCase(key) || "US".equalsIgnoreCase(key)) {
			value = value.replaceAll("^\\s*(IC|US)\\s+", "").replaceAll("\\.\\s*$", "");
		}

		// if ("CL".equalsIgnoreCase(key) || ("DC".equalsIgnoreCase(key))) {
		// return value.replaceFirst("\\.$", "");
		// }

		return value;
	}

	public List<KeyValue> parse(String rawRecord) throws PatentReaderException {
		Reader reader = new StringReader(rawRecord);
		List<KeyValue> keyValues = parse(reader);

		keyValues = keyValues.stream().filter(e -> !e.getKey().equals("XX")).collect(Collectors.toList());

		return keyValues;
	}

	@Override
	public List<KeyValue> postProcess(List<KeyValue> keyValues) {
		List<KeyValue> newList = new ArrayList<KeyValue>();
		Iterator<KeyValue> it = keyValues.iterator();
		// for (int i = 0; it.hasNext(); i++) {
		while (it.hasNext()) {
			KeyValue kv = it.next();

			if ("WM".equalsIgnoreCase(kv.getKeyOriginal())) {
				String value = WordUtil.toDecimal(kv.getValue().trim());
				KeyValue kv2 = new KeyValue("wmark_decimal_ws", value);
				kv2.setKeyOriginal("WM:DECIMAL");
				newList.add(kv2);
			}

			if ("RE".equalsIgnoreCase(kv.getKeyOriginal())) {
				Pattern RENEWAL = Pattern.compile("(\\d+)(?:ST|ND|RD|TH) RENEWAL \\s+(\\d+)$");
				Matcher renMatcher = RENEWAL.matcher(kv.getValue().trim());
				if (renMatcher.matches()) {
					String renCount = renMatcher.group(1);
					String dateStr = renMatcher.group(2);

					try {
						String date = DateUtil.toDateTimeISO(dateStr);
						KeyValue kv2 = new KeyValue(kv.getKey().replaceFirst("_t$", "_dt"), date);
						kv2.setKeyOriginal(kv.getKeyOriginal());
						newList.add(kv2);
					} catch (DateTimeParseException e) {
						LOGGER.warn("!! Invalid Date: '{}' from field: '{}'", dateStr, kv.getKeyOriginal());
					}

					KeyValue kv2 = new KeyValue(kv.getKey().replaceFirst("_t$", "_count_i"), renCount);
					kv2.setKeyOriginal(kv.getKeyOriginal());
					newList.add(kv2);
				}
			}

			if (TOKENIZE_FIELDS.contains(kv.getKeyOriginal())) {
				String[] tokens = PATTERN_TOKENIZER.split(kv.getValue());
				for (String tok : tokens) {
					tok = POST_TOKEN_PATTERN_FILTER.matcher(tok).replaceAll("");
					KeyValue kv2 = new KeyValue(kv.getKey().replaceFirst("_txt$", "_ss"), tok);
					kv2.setKeyOriginal(kv.getKeyOriginal());
					newList.add(kv2);
				}
			}

			if ("AF".equalsIgnoreCase(kv.getKeyOriginal())) {
				String[] tokens = kv.getValue().trim().split("\\.\\s?");
				for (String tok : tokens) {
					tok = tok.trim().replaceFirst("\\s+\\d{8}$", ""); // remove trailing date.
					String secNum = AF_SECTION.get(tok);
					if (secNum != null) {
						KeyValue kv2 = new KeyValue("af_section_" + secNum + "_bs", "true");
						kv2.setKeyOriginal(kv.getKeyOriginal());
						newList.add(kv2);
					}
				}
			}
		}

		List<Map<String, String>> entities = parseEntities(keyValues);
		for (Map<String, String> entity : entities) {
			String nameStr = entity.get("PN");
			String dbStr = entity.get("DB");
			String street1 = entity.get("AI");
			String street2 = entity.get("AS");
			String city = entity.get("CY");
			String stateCountry = entity.get("SC");

			StringBuilder stb = new StringBuilder();
			stb.append(nameStr.trim());
			if (dbStr != null && !dbStr.trim().isEmpty()) {
				stb.append(", ");
				stb.append(dbStr.trim());
			}
			if (street1 != null && !street1.trim().isEmpty()) {
				stb.append(", ");
				stb.append(street1.trim());
			}
			stb.append(",");
			if (street2 != null && !street2.trim().isEmpty()) {
				stb.append(", ");
				stb.append(street2.trim());
			}
			if (city != null && !city.trim().isEmpty()) {
				stb.append(", ");
				stb.append(city.trim());
			}
			if (stateCountry != null && !stateCountry.trim().isEmpty()) {
				stb.append(", ");
				stb.append(stateCountry.trim());
			}

			KeyValue kv2 = new KeyValue("owner_addr_txt", stb.toString());
			kv2.setKeyOriginal("PN:AI:AS:CY:SC");
			newList.add(kv2);
		}

		keyValues.addAll(newList);
		return keyValues;
	}

	public List<Map<String, String>> parseEntities(List<KeyValue> keyValues) {
		List<Map<String, String>> entities = new ArrayList<Map<String, String>>();
		List<String> ENTITY_FIELDS = Arrays.asList("PN", "EN", "CI", "CO", "DB", "AI", "AS", "CY", "SC", "NC");
		Iterator<KeyValue> it = keyValues.iterator();
		boolean entityBlock = false;
		List<KeyValue> entityFields = new ArrayList<KeyValue>();
		while (it.hasNext()) {
			KeyValue kv = it.next();

			if (entityBlock == true) {
				if (ENTITY_FIELDS.contains(kv.getKeyOriginal())) {
					entityFields.add(kv);
				} else {
					entityBlock = false;
					Map<String, String> entity = entityFields.stream()
							.collect(Collectors.toMap(KeyValue::getKeyOriginal, KeyValue::getValue, (u, v) -> {
								throw new IllegalStateException(String.format("Duplicate key %s", u));
							}, LinkedHashMap::new));
					// LOGGER.info(" {} PE {}", kv.getKeyOriginal(), entity);
					entities.add(entity);
				}
			}

			if ("OW".equalsIgnoreCase(kv.getKeyOriginal())) {
				entityFields = new ArrayList<KeyValue>();
				entityFields.add(kv);
				entityBlock = true;
			}
		}
		return entities;
	}

	public void run(File inputFile, KvDocBuilder kvDocBuilder, Writer writer) throws IOException {

		//DumpFileAps dumpReader = new DumpFileAps(inputFile, "<XX>");
		DumpFileAps dumpReader = new DumpFileAps(inputFile, "*** BRS DOCUMENT BOUNDARY ***");

		long totalLimit = Long.MAX_VALUE;

		try {
			dumpReader.open();

			while (dumpReader.hasNext() && dumpReader.getCurrentRecCount() < totalLimit) {

				String docLoc = dumpReader.getFile().getName() + "-" + dumpReader.getCurrentRecCount();
				MDC.put("DOCID", docLoc);

				if (dumpReader.getCurrentRecCount() % 1000 == 0) {
					LOGGER.info("... mark {}", dumpReader.getCurrentRecCount());
				}

				String rawDocStr = dumpReader.next();
				if (rawDocStr == null) {
					break;
				}
				LOGGER.trace("Raw Document: {}", rawDocStr);

				// Write out individual raw input files for debugging
//				Path rawOutPath = Paths.get("./output/raw");
//				rawOutPath.toFile().mkdirs();
//				try (Writer rawWriter = new BufferedWriter(new OutputStreamWriter(
//						new FileOutputStream(Paths.get("./output/raw").resolve(docLoc + ".txt").toFile()),
//						StandardCharsets.UTF_8))) {
//					rawWriter.write(rawDocStr);
//					rawWriter.flush();
//				}

				List<KeyValue> keyValues;
				try {
					keyValues = parse(new StringReader(rawDocStr));
					keyValues = keyValues.stream().filter(e -> !e.getKey().equals("XX")).collect(Collectors.toList());

					LOGGER.trace("Key Values: {}", keyValues);

					// Adding record source and raw document text for debugging.
					keyValues.add(new KeyValue("record_source_s", docLoc));
					keyValues.add(new KeyValue("brs_doc_txt", rawDocStr));

					StringWriter outRecord = new StringWriter();
					kvDocBuilder.write(keyValues, outRecord);
					writer.write(outRecord.toString());
				} catch (PatentReaderException e) {
					LOGGER.error("!! Failed processing record : {}", dumpReader.getCurrentRecCount(), e);
				}
			}

			LOGGER.info("****** File Processed '{}', record count: {}", dumpReader.getFile().getName(),
					dumpReader.getCurrentRecCount());

		} catch (IOException e) {
			LOGGER.error("!! Failed when processing file: '{}'", inputFile, e);
		} finally {
			writer.close();
			dumpReader.close();
		}
	}

	public static void main(String[] args) throws IOException {
		File inputFile = new File(args[0]);
		boolean wantSolrXml = true;
		int partitionRecordLimit = 5000;
		int partitionSizeMBLimit = 25;

		Path outputPath = Paths.get("./output");

		Writer writer;
		if (LOGGER.isDebugEnabled() || LOGGER.isTraceEnabled()) {
			PartitionFileWriter partWrite = new PartitionFileWriter(outputPath, inputFile.getName(), "-solr.xml",
					partitionRecordLimit, partitionSizeMBLimit, StandardCharsets.UTF_8);
			Writer stdout = new BufferedWriter(new OutputStreamWriter(System.out));
			writer = new TeeWriter(partWrite, stdout);
		} else {
			PartitionFileWriter partWrite = new PartitionFileWriter(outputPath, inputFile.getName(), "-solr.xml",
					partitionRecordLimit, partitionSizeMBLimit, StandardCharsets.UTF_8);
			partWrite.setHeader(SOLR_HEADER);
			partWrite.setFooter(SOLR_FOOTER);
			writer = partWrite;
		}
		// Test without writing output
		// writer = new DummyWriter();

		KvDocBuilder kvDocBuilder;
		if (wantSolrXml) {
			kvDocBuilder = new Kv2SolrXml();
		} else {
			kvDocBuilder = new Kv2KvXml();
		}

		TmBrs kvReader = new TmBrs(true, true);
		kvReader.run(inputFile, kvDocBuilder, writer);
	}

}
