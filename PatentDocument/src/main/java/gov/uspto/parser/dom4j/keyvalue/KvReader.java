package gov.uspto.parser.dom4j.keyvalue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.google.common.base.Strings;

import gov.uspto.parser.dom4j.keyvalue.config.FieldGroup;
import gov.uspto.parser.dom4j.keyvalue.config.FieldIndex;
import gov.uspto.parser.dom4j.keyvalue.config.IndexEntry;
import gov.uspto.patent.PatentReaderException;

/**
 * Transform plaintext Field-Space-Value formated file into an XML Document
 * 
 * Key space value, long values wrap with indentation on next line.
 * 
 * Example of flat fields with no sections:
 * 
 * <pre>
 * WKU  039305848
 * APN  4584481
 * APT  1
 * ART  316
 * TTL Method for performing chip level electromagnetic interference reduction,
 *     and associated apparatus
 * URPN 2003/0169838
 * </pre>
 * 
 * Example same as above with the addition of section "PATN" and "INVT" each
 * having subfields directly following underneath.
 * 
 * <pre>
 * PATN
 * WKU  039305848
 * APN  4584481
 * INVT
 * NAM  Doe; John
 * STR  1 Main St
 * CTY  Springfield
 * </pre>
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */

public class KvReader {

    /*
    private Collection<String> sections;
    private Map<String, Collection<String>> sectionMapping;
    
    public void setSectionSequence(Collection<String> sections) {
        this.sections = sections;
    }
    */

	private List<String> maintainSpaceFields = new ArrayList<String>();
	private List<String> paragraphFields = new ArrayList<String>();
	private List<String> headerFields = new ArrayList<String>();
	private List<String> tableFields = new ArrayList<String>();

	private String currentFieldName;

	/**
	 * Paragraph Fields, used to add num and id.
	 */
	public void setFieldsForId(Collection<String> paragraphFields, Collection<String> headerFields, Collection<String> tableFields){
		this.paragraphFields.addAll(paragraphFields);
		this.headerFields.addAll(headerFields);
		this.tableFields.addAll(tableFields);
	}

	/**
	 * Fields which space and new lines should be maintained, like HTML pre tag.
	 */
	public void setMaintainSpaceFields(Collection<String> capitalizedFieldNames){
		this.maintainSpaceFields.addAll(capitalizedFieldNames);
	}

    /**
     * Generate XML which is either flat, or sections of matching key which are
     * one level deep.
     * 
     * Sections are auto detected by being a field without a value.
     * 
     *<pre>
     * INVT
     * NAM  Doe; John
     * STR  1 Main St
     * CTY  Springfield
     *</pre>
     * 
     * @param keyValues
     * @return
     */
    public Document genXml(List<KeyValue> keyValues) {    	
        Document document = DocumentHelper.createDocument();
        Element rootNode = document.addElement("DOCUMENT");
        Element currentSection = rootNode;

        int pCount = 1;
        int hCount = 1;
        int tCount = 1;
 
        for (KeyValue kv : keyValues) {
        	if (kv.getKey().trim().isEmpty()){
        		continue;
        	}
            if (kv.getValue().trim().isEmpty()) { // auto detect section.
                if (currentSection != rootNode) {
                    rootNode.add(currentSection);
                }
                currentSection = DocumentHelper.createElement(kv.getKey());                               
            } else {
                Element field = DocumentHelper.createElement(kv.getKey());
 
                /*
                 * Add field ids
                 */
                if (paragraphFields.contains(kv.getKey().toUpperCase())){
                	String idValue = "p-" + Strings.padStart(String.valueOf(pCount), 4, '0');
                	field.addAttribute("id", idValue);
                	pCount++;
                } else if (headerFields.contains(kv.getKey().toUpperCase())){
                	String idValue = "h-" + Strings.padStart(String.valueOf(hCount), 4, '0');
                	field.addAttribute("id", idValue);
                	hCount++;
                } else if (tableFields.contains(kv.getKey().toUpperCase())){
                	String idValue = "t-" + Strings.padStart(String.valueOf(tCount), 4, '0');
                	field.addAttribute("id", idValue);
                	tCount++;
                }

               	field.setText(kv.getValue());
                currentSection.add(field);
            }
        }

        if (currentSection != rootNode) {
            rootNode.add(currentSection);
        }

        return document;
    }

    /**
     * Generate XML which is either flat, or sections of matching key which are
     * one level deep.
     * 
     * @param keyValues
     * @param sections
     * @return
     */
    public Document genXml(List<KeyValue> keyValues, Collection<String> sections) {

        Document document = DocumentHelper.createDocument();
        Element rootNode = document.addElement("DOCUMENT");
        Element currentSection = rootNode;

        for (KeyValue kv : keyValues) {

            if (kv.getValue().isEmpty() && sections.contains(kv.getKey())) {
                rootNode.add(currentSection);
                currentSection = DocumentHelper.createElement(kv.getKey());
            } else {
                Element field = DocumentHelper.createElement(kv.getKey());
                field.setText(kv.getValue());
                currentSection.add(field);
            }
        }

        return document;
    }

    /**
     * Generate XML from fields which are flat, but sequencial fields can be mapped to individual entities.
     * 
     * Example use case: 
     *  -- Data from database is flat. 
     *  -- to capture an Inventor there is an inventor name and inventor address field
     *  -- when multiple inventors are present, the fields repeats for each inventor in sequence.
     *  
     * <pre>
     * INNM Doe; John
     * INSA 1 Main St
     * INCI Springfield
     * INNM Smith; Kevin 
     * INSA 1 Main St
     * INCI Springfield 
     * </pre>
     *  
     * new FieldGroup("INVENTOR").setAncorField("INNM").addField("INSA", "INCI", "INST"); 
     *  
     * Notes: 
     *  -- All fields not defined within a field group will be added to root xml node. 
     *  -- If a field belongs to an entity it needs to be added to that field group else it might split the group apart.
     *  
     * @param keyValues
     * @param fieldGroup
     * @return
     */
    public Document genXml(List<KeyValue> keyValues, List<FieldGroup> fieldGroup) {
        FieldIndex index = new FieldIndex(fieldGroup);

        Document document = DocumentHelper.createDocument();
        Element rootNode = document.addElement("DOCUMENT");
        Element currentSection = rootNode;
        FieldGroup currentFieldGroup = null;

        for (KeyValue kv : keyValues) {
            IndexEntry entry = index.getEntry(kv.getKey());


            if (entry == null){
                continue;
            }
            /*
             * Single-instance FieldGroup
             */
            else if (!entry.getFieldGroup().isMultivalued()){
                if (currentFieldGroup != entry.getFieldGroup()){
                	
                    if (currentSection != rootNode) {
                        rootNode.add(currentSection);
                        currentFieldGroup = null;
                    }

                    currentFieldGroup = entry.getFieldGroup();
                    currentSection = DocumentHelper.createElement(currentFieldGroup.getName());

                    Element field = DocumentHelper.createElement(kv.getKey());
                    field.setText(kv.getValue());
                    currentSection.add(field);
                } else {                    
                    Element field = DocumentHelper.createElement(kv.getKey());
                    field.setText(kv.getValue());
                    currentSection.add(field);
                }
            }
            /*
             * Multi-instance FieldGroup.
             */
            else if (entry.getField().isAchor()) {
                
                // if (sections.contains(kv.getKey())) {
                if (currentSection != rootNode) {
                    rootNode.add(currentSection);
                }
                currentFieldGroup = entry.getFieldGroup();
                currentSection = DocumentHelper.createElement(currentFieldGroup.getName());

                Element field = DocumentHelper.createElement(kv.getKey());
                field.setText(kv.getValue());
                currentSection.add(field);

            } else if (currentFieldGroup == entry.getFieldGroup() && currentSection != rootNode) {
                Element field = DocumentHelper.createElement(kv.getKey());
                field.setText(kv.getValue());
                currentSection.add(field);
            } else {
                if (currentSection != rootNode) {
                    rootNode.add(currentSection);
                    currentFieldGroup = null;
                }
                currentSection = rootNode;

                Element field = DocumentHelper.createElement(kv.getKey());
                field.setText(kv.getValue());
                currentSection.add(field);
            }
        }

        return document;

    }

    /**
     * Read Plaintext Document and parse field values into List of Key Values.
     * 
     * @param reader
     *            - Reader for plaintext document
     * @return - List of Key Value Pairs
     * @throws PatentReaderException
     * @throws IOException
     */
    public List<KeyValue> parse(Reader reader) throws PatentReaderException {
        List<KeyValue> keyValues = new ArrayList<KeyValue>();
        currentFieldName = "";

        try (BufferedReader breader = new BufferedReader(reader)) {

            String currentLine;
            while ((currentLine = breader.readLine()) != null) {
                // String[] parts = processLineRegex(currentLine);
                String[] parts = processLineLeadingWhiteSpace(currentLine);
                if (parts.length == 2) {
                    keyValues.add(new KeyValue(parts[0], parts[1]));
                    // } else if (sections != null && parts.length == 1 &&
                    // sections.contains(parts[0])) {
                    // keyValues.add(new KeyValue(parts[0], ""));
                } else if (parts.length == 1 && isValidKey(parts[0])) { // auto detect section.
                    keyValues.add(new KeyValue(parts[0], ""));
                } else {
                    if (keyValues.isEmpty()) {
                        continue;
                    }
                    int lastLoc = keyValues.size() - 1;
                    KeyValue lastKv = keyValues.get(lastLoc);
                    lastKv.appendValue(parts[0]);
                    currentFieldName = lastKv.getKey().toUpperCase();
                }
            }

        } catch (IOException e) {
            throw new PatentReaderException(e);
        }

        return keyValues;
    }

    /**
     * Process Line, split line by key value, else return line.
     * 
     * @param line
     * @return
     */
    /*
     * private final static Pattern KEY_VALUE_PATTERN = Pattern.compile("^([A-Z][A-Z0-9]{2,3}):?\\s(.+?)$"); 
     * private String[] processLineRegex(final String line) { 
     * Matcher matcher = KEY_VALUE_PATTERN.matcher(line);
     * if (matcher.find()) { 
     *    String key = matcher.group(1);
     *    String value = matcher.group(2);
     *    return new String[] { key, value }; } 
     * else { 
     *    return new String[] { line.trim() }; }
     * }
     */

    private String[] processLineLeadingWhiteSpace(final String line) {
        if (line.startsWith("     ")) {
        	String value = line;
        	if (maintainSpaceFields.contains(currentFieldName)){
        		value = value + "\n";
        	} else {
        		value = StringUtils.strip(value);
        	}
        	return new String[] { value };
        } else {
        	String tline = StringUtils.strip(line);
            //int idx = tline.indexOf(': ');
            int idx = tline.indexOf(' ');
            if (idx < 3) {
                //System.err.println(" Error: " + tline);
                return new String[] { tline };
            }
            String key = tline.substring(0, idx);
            if (!isValidKey(key)) {
                // System.err.println(" Error: '" + key + "' : " + tline);
                return new String[] { tline };
            }

            String value = tline.substring(idx, tline.length());
            if (maintainSpaceFields.contains(key.toUpperCase())){
            	value = value + "\n";
            } else {
            	value = StringUtils.strip(value);
            }
            return new String[] { key, value };
        }
    }

    /**
     * Check Key is valid
     * 
     * Criteria: 
     * 1) 3 or 4 characters long 
     * 2) First character Uppercase ASCII 
     * 3) characters Uppercase ASCII or number
     * 
     * @param key
     * @return
     */
    public boolean isValidKey(String key) {
        if (key.length() > 5 || key.length() < 2) {
            return false;
        }

        char ch = key.charAt(0);
        if (!(ch >= 'A' && ch <= 'Z')) {
            return false;
        }

        for (int i = 1; i < key.length(); i++) {
            ch = key.charAt(i);
            if (!(ch >= '0' && ch <= '9') && !(ch >= 'A' && ch <= 'Z')) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) throws PatentReaderException, IOException {

        File inputFile = new File(args[0]);

        List<FieldGroup> fieldGroups = new ArrayList<FieldGroup>();
        fieldGroups.add(new FieldGroup("APPLICANT").setAncorField("AANM").addField("AACI", "AAST", "AAZP", "AACO",
                "AATX", "AAGP", "AAAT"));
        fieldGroups.add(new FieldGroup("INVENTOR").setAncorField("INNM").addField("INSA", "INCI", "INST", "INZP",
                "INCO", "INTX", "INGP"));
        fieldGroups.add(new FieldGroup("ASSIGNEE").setAncorField("ASNM").addField("ASSA", "ASCI", "ASST", "ASCO",
                "ASPC", "ASTC", "ASZP", "ASTX", "ASGP"));

        fieldGroups.add(new FieldGroup("AGENT").addField("LRCN", "FIRM", "PATT", "AATT", "ATTY", "ATTN", "LRNM", "LRSA", "LRFW",
                "LRCI", "LRST", "LRZC", "LREA", "LRTX"));

        fieldGroups.add(
                new FieldGroup("UREF").setAncorField("URPN").addField("URNM", "URPD", "URCL", "URCP", "URGP"));

        fieldGroups.add(new FieldGroup("FREF").setAncorField("FRCO").addField("FRPN", "FRPD", "FRCL",
                "FRCP", "FRGP"));

        fieldGroups.add(new FieldGroup("CORRESPONDENCE").setAncorField("COCN").addField("CODR"));

        fieldGroups.add(new FieldGroup("PRIOR").setAncorField("PDID").addField("PPNR", "PPPD", "PPKC", "PPCC"));

        fieldGroups.add(new FieldGroup("CLAIMS").addField("CLST", "CLPR", "CLTX"));
        fieldGroups.add(new FieldGroup("ABSTRACT").addField("ABPR"));
        fieldGroups.add(new FieldGroup("EXAMINERS").addField("ART", "EXP", "EXA"));

        fieldGroups.add(new FieldGroup("RELATED").setAncorField("COND").addField("RLPY", "RLGY", "RLCY", "RLAN", "RLFD",
                "RLPN", "RLCN", "RLPK", "RLGK", "RLPM", "RLGM", "RLCM", "RLHD", "RLPD", "RLPP", "RLPC", "RLTC", "RLTX", "RLRP"));
        
        fieldGroups.add(new FieldGroup("DESC_CROSSREF").addField("CRTX"));
        fieldGroups.add(new FieldGroup("DESC_SUMMARY").addField("BSTX"));
        fieldGroups.add(new FieldGroup("DESC_DETAILED").addField("DETX"));

        fieldGroups.add(new FieldGroup("FIELD_OF_SEARCH_CPC").setAncorField("FSCP").addField("FSCL", "FSCS"));
        fieldGroups.add(new FieldGroup("FIELD_OF_SEARCH_USPC").setAncorField("CFSC").addField("CFSS"));
        //fieldGroups.add(new FieldGroup("CLASSIFICATION").addField("CIFS", "FSCP", "FSCL"));

        
        //fieldGroups.add(new FieldGroup("PCT").addField("PCAN", true).addField("PCAC", "PCAK", "PCAD", "PCDV",
        //       "PCCO", "PCPN", "PCKC", "PCPD", "TDID"));

        //fieldGroups.add(new FieldGroup("CITATION").addField("ASNM", true).addField("RFKC", "RFPD", "RFCO", "RFNM",
        //        "RFNR", "RFON", "RFAD", "RFIP", "RFRS", "RFNP"));
        
        Reader reader = new InputStreamReader(new FileInputStream(inputFile), "UTF-8");

        KvReader kvf = new KvReader();
        // kvf.setSectionSequence(sections);

        List<KeyValue> keyValues = kvf.parse(reader);

        for (KeyValue kv : keyValues) {
            System.out.println(kv.toString());
        }

        Document xmlDoc = kvf.genXml(keyValues, fieldGroups);

        System.out.println(xmlDoc.asXML());
    }
}
