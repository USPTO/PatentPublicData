package gov.uspto.tm.doc.brs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import gov.uspto.common.io.TeeWriter;
import gov.uspto.common.text.DateUtil;
import gov.uspto.parser.keyvalue.KeyValue;
import gov.uspto.parser.keyvalue.Kv2KvXml;
import gov.uspto.parser.keyvalue.Kv2SolrXml;
import gov.uspto.parser.keyvalue.KvReaderFixedWidth;
import gov.uspto.parser.keyvalue.KvWriter;
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
			Arrays.asList("PD", "PO", "FD", "PF", "RE", "RD", "AD", "CD", "U1", "U2"));

	private static final Map<String, String> RENAME_FIELDS = new HashMap<String, String>() {
		{
			put("SN", "id"); // 77367856
			put("SO", "serial_other_id_ss"); // 77-367856
			put("IR", "intl_id_ss"); // 0272587
			put("RN", "reg_id_s");
			put("PR", "prior_reg_ids_td"); // "3282741;3891127;3891128"
			put("WM", "wmark_s");
			put("PM", "wmark_pseudo_t");
			put("PD", "priority_dt");
			put("FD", "filed_dt");
			put("RD", "register_dt");
			put("AD", "abandon_dt");
			put("CD", "cancel_dt");
			put("RE", "renewal_dt");
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
			put("ST", "st_char_claimed_b");
			put("LD", "alive_b");
			put("TM", "mark_type_s");
			put("RG", "register_s");
			put("DD", "design_desc_txt");
			put("TD", "design_count_i"); // 2
			put("DC", "design_code_txt"); // 020102 020107
			put("CL", "intl_class_txt"); // 009
			put("CP", "coordinated_class_txt"); // 042 001 002 003
			put("US", "us_class_txt"); // 100 101
			put("OW", "ignored_OW");

			put("U1", "u1_dts");
			put("U2", "u2_dts");
			put("AN", "an_txt"); // The mark was first used anywhere in a different form other than that sought
									// to be registered at least as early as 12/01/2018
			put("TD", "td_i"); // 0000
			put("TP", "tp_i"); // 0000
		}
	};

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
			return DateUtil.toDateTimeISO(value.trim());
		}

		if ("ST".equalsIgnoreCase(key)) {
			return String.valueOf("STANDARD CHARACTERS CLAIMED".equals(value.trim()));
		}

		if ("LD".equalsIgnoreCase(key)) {
			return String.valueOf("LIVE".equals(value.trim()));
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

		keyValues = keyValues.stream().filter(e -> !e.getKey().equals("XX"))
				// .filter(e -> !e.getKey().equals("EN"))
				// .filter(e -> !e.getKey().equals("TP"))
				// .filter(e -> !e.getKey().equals("ST"))
				// .filter(e -> !e.getKey().equals("ON"))
				// .filter(e -> !e.getKey().equals("OW"))
				// .distinct()
				.collect(Collectors.toList());

		return keyValues;
	}

	@Override
	public List<KeyValue> postProcess(List<KeyValue> keyValues) {
		List<KeyValue> newList = new ArrayList<KeyValue>();
		Iterator<KeyValue> it = keyValues.iterator();
		//for (int i = 0; it.hasNext(); i++) {
		while(it.hasNext()) {
			KeyValue kv = it.next();
			if (TOKENIZE_FIELDS.contains(kv.getKeyOriginal())) {
				String[] tokens = PATTERN_TOKENIZER.split(kv.getValue());
				for (String tok : tokens) {
					tok = POST_TOKEN_PATTERN_FILTER.matcher(tok).replaceAll("");
					KeyValue kv2 = new KeyValue(kv.getKey().replaceFirst("_txt$", "_ss"), tok);
					kv2.setKeyOriginal(kv.getKeyOriginal());
					newList.add(kv2);
				}
			}
		}

		keyValues.addAll(newList);
		return keyValues;
	}

	public static void main(String[] args) throws PatentReaderException, IOException {

		File inputFile = new File(args[0]);

		boolean wantSolrXml = true;

		Path outputPath = Paths.get("./output");
		if (!outputPath.toFile().isDirectory()) {
			outputPath.toFile().mkdirs();
		}

		DumpFileAps dumpReader = new DumpFileAps(inputFile, "<XX>");
		// DumpFileAps dumpReader = new DumpFileAps(inputFile, "*** BRS DOCUMENT BOUNDARY ***");

		Set<String> fields = new LinkedHashSet<String>();

		Path outputFileTmp = outputPath.resolve(dumpReader.getFile().getName() + "-solr.tmp");
		Path outputFile = outputPath.resolve(dumpReader.getFile().getName() + "-solr.xml");

		Writer writer;
		if (LOGGER.isDebugEnabled()) {
			Writer fileW = new BufferedWriter(new FileWriter(outputFileTmp.toFile()));
			Writer stdout = new BufferedWriter(new OutputStreamWriter(System.out));
			writer = new TeeWriter(fileW, stdout);
		} else {
			writer = new BufferedWriter(new FileWriter(outputFileTmp.toFile()));
		}
		// Writer writer = new OutputStreamWriter(System.out);

		writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n");
		writer.write("<add>\n");

		long totalLimit = Long.MAX_VALUE;

		try {
			dumpReader.open();

			while (dumpReader.hasNext() && dumpReader.getCurrentRecCount() < totalLimit) {

				String docLoc = dumpReader.getFile().getName() + "-" + dumpReader.getCurrentRecCount();
				MDC.put("DOCID", docLoc);

				// Writer writer = new FileWriter(outputPath.resolve(docLoc + ".xml").toFile());

				String rawDocStr = dumpReader.next();
				if (rawDocStr == null) {
					break;
				}

				LOGGER.trace("Raw Document: {}", rawDocStr);

				TmBrs kvReader = new TmBrs(true, true);

				KvWriter kvWriter;

				if (wantSolrXml) {
					kvWriter = new Kv2SolrXml(writer);
				} else {
					kvWriter = new Kv2KvXml(writer);
				}

				Reader reader = new StringReader(rawDocStr);

				List<KeyValue> keyValues = kvReader.parse(reader);
				keyValues = keyValues.stream().filter(e -> !e.getKey().equals("XX")).collect(Collectors.toList());

				Set<String> iFields = keyValues.stream().map(e -> e.getKey()).collect(Collectors.toSet());
				fields.addAll(iFields);

				LOGGER.trace("Key Values: {}", keyValues);

				writer.write("  ");
				kvWriter.write(keyValues);
				writer.write('\n');
				writer.flush();
			}

			writer.write("</add>\n");
			writer.close();

			LOGGER.info("****** File Processed '{}', record count: {}", dumpReader.getFile().getName(), dumpReader.getCurrentRecCount());

			try {
				Files.move(outputFileTmp, outputFile, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				LOGGER.error("!! Failed to rename file '{}' to '{}'", outputFileTmp, outputFile, e);
			}

		} catch (IOException e) {
			LOGGER.error("!! Failed when processing file: '{}'", inputFile, e);
		} finally {
			writer.close();
			dumpReader.close();
		}
	}

}
