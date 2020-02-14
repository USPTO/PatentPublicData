package gov.uspto.tm.doc.brs;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.dom4j.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import gov.uspto.parser.keyvalue.KeyValue;
import gov.uspto.parser.keyvalue.KeyValue2Dom4j;
import gov.uspto.parser.keyvalue.KvReaderFixedWidth;
import gov.uspto.patent.PatentReaderException;
import gov.uspto.patent.bulk.DumpFileAps;

/**
 * Trademark BRS
 * 
 *<p>Trademark record in BRS format, which has brackets around field names and closing tag when nested</p>
 *
 *
 **=========== Input ============= <pre>{@code
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
}</pre>
 *=========== Output ============= <pre>{@code
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
}</pre>
 * 
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 */
public class TmBrs extends KvReaderFixedWidth {

	private static final Logger LOGGER = LoggerFactory.getLogger(TmBrs.class);

	private final static Pattern KEY_TRANSFORM = Pattern.compile("^<([A-Z0-9]{2,})>");
	private final static Pattern CLOSING_TAGS = Pattern.compile("(:?</[A-Z][A-Z0-9]>\\s*)+$");

	private static List<String> MAINTAIN_SPACE_FIELDS = Arrays.asList(new String[] { "TL", "GS", "DE" });

	public TmBrs() {
		super(5);
		//super.setMaintainSpaceFields(MAINTAIN_SPACE_FIELDS);
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
	public String valueTransform(final String key, String value) {
		// remove trailing closing tags
		return CLOSING_TAGS.matcher(value).replaceAll("");
	}

	public List<KeyValue> parse(String rawRecord) throws PatentReaderException {
		Reader reader = new StringReader(rawRecord);
		List<KeyValue> keyValues = parse(reader);
		
		keyValues = keyValues.stream()
				.filter(e -> !e.getKey().equals("XX"))
				//.filter(e -> !e.getKey().equals("EN"))
				//.filter(e -> !e.getKey().equals("TP"))
				//.filter(e -> !e.getKey().equals("ST"))
				//.filter(e -> !e.getKey().equals("ON"))
				//.filter(e -> !e.getKey().equals("OW"))
				//.distinct()
				.collect(Collectors.toList());


		return keyValues;
	}

	public static void main(String[] args) throws PatentReaderException, IOException {

		File inputFile = new File(args[0]);

        DumpFileAps dumpReader = new DumpFileAps(inputFile);
		//DumpFileAps dumpReader = new DumpFileAps(inputFile, "<XX>");
		//DumpFileAps dumpReader = new DumpFileAps(inputFile, "*** BRS DOCUMENT BOUNDARY ***");

		//List<FieldGroup> fieldGroups = new ArrayList<FieldGroup>();
		//fieldGroups.add(new FieldGroup("GENERAL_SERVICES").addField("GS", true).addField("U1", "U2"));
		//fieldGroups.add(new FieldGroup("APPLICANT_ADDRESS").addField("OW", true).addField("ON", "PN", "DB", "EN", "CI", "AI", "CY", "AS", "SC"));

		long totalLimit = Long.MAX_VALUE;
		long totalCount = 0;

		try {
			dumpReader.open();

			for (; dumpReader.hasNext() && totalCount < totalLimit; totalCount++) {
				MDC.put("DOCID", dumpReader.getFile().getName() + ":" + dumpReader.getCurrentRecCount());
				
				String rawDocStr = dumpReader.next();
				if (rawDocStr == null) {
					break;
				}

				System.out.println(rawDocStr);

				TmBrs kvReader = new TmBrs();
				KeyValue2Dom4j kvWriter = new KeyValue2Dom4j();

				Reader reader = new StringReader(rawDocStr);

				List<KeyValue> keyValues = kvReader.parse(reader);
				keyValues = keyValues.stream()
						.filter(e -> !e.getKey().equals("XX"))
						//.filter(e -> !e.getKey().equals("EN"))
						//.filter(e -> !e.getKey().equals("TP"))
						//.filter(e -> !e.getKey().equals("ST"))
						//.filter(e -> !e.getKey().equals("ON"))
						//.filter(e -> !e.getKey().equals("OW"))
						//.distinct()
						.collect(Collectors.toList());

				//keyValues.stream().forEach(System.out::println);

				//Document xmlDoc = kvWriter.genXml(keyValues, fieldGroups);
				Document xmlDoc = kvWriter.genXml(keyValues);
				
				StringWriter outStr = new StringWriter();
				KeyValue2Dom4j.serializeDom(outStr, xmlDoc, StandardCharsets.UTF_8, true);
				System.out.println(outStr);

			}
		} finally {
			dumpReader.close();
		}
	}

}
