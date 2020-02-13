package gov.uspto.parser.keyvalue;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import com.google.common.base.Strings;

import gov.uspto.parser.dom4j.keyvalue.config.FieldGroup;
import gov.uspto.parser.dom4j.keyvalue.config.FieldIndex;
import gov.uspto.parser.dom4j.keyvalue.config.IndexEntry;

public class KeyValue2Dom4j {

	private static final Pattern QNAME_INVALID = Pattern.compile("^[0-9].+$");
	
	private List<String> paragraphFields = new ArrayList<String>();
	private List<String> headerFields = new ArrayList<String>();
	private List<String> tableFields = new ArrayList<String>();	

	/**
	 * Paragraph Fields, used to add num and id.
	 */
	public void setFieldsForId(Collection<String> paragraphFields, Collection<String> headerFields,
			Collection<String> tableFields) {
		this.paragraphFields.addAll(paragraphFields);
		this.headerFields.addAll(headerFields);
		this.tableFields.addAll(tableFields);
	}

	/**
	 * Generate XML which is either flat, or sections of matching key which are one
	 * level deep.
	 * 
	 * @param keyValues
	 * @param sections
	 * @return Document
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
	 * <p>
	 * Generate XML from fields which are flat, but sequencial fields can be mapped
	 * to individual entities.
	 * </p>
	 * 
	 * <pre>
	 * Example use case: 
	 *  -- Data from database is flat. 
	 *  -- to capture an Inventor there is an inventor name and inventor address field
	 *  -- when multiple inventors are present, the fields repeats for each inventor in sequence.
	 * </pre>
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
	 * <p>
	 * new FieldGroup("INVENTOR").setAncorField("INNM").addField("INSA", "INCI",
	 * "INST");
	 * </p>
	 * 
	 * <pre>
	 * Notes: 
	 *  -- All fields not defined within a field group will be added to root xml node. 
	 *  -- If a field belongs to an entity it needs to be added to that field group else it might split the group apart.
	 * </pre>
	 * 
	 * @param keyValues
	 * @param fieldGroup
	 * @return Document
	 */
	public Document genXml(List<KeyValue> keyValues, List<FieldGroup> fieldGroup) {
		FieldIndex index = new FieldIndex(fieldGroup);

		System.out.println(index.toString());
		
		//System.out.println(keyValues);

		Document document = DocumentHelper.createDocument();
		Element rootNode = document.addElement("DOCUMENT");
		Element currentSection = rootNode;
		FieldGroup currentFieldGroup = null;

		for (KeyValue kv : keyValues) {
			IndexEntry entry = index.getEntry(kv.getKey());

			if (entry == null) {
				//continue;
				if (currentSection != rootNode) {
					rootNode.add(currentSection);
					currentFieldGroup = null;
				}
				currentSection = rootNode;

				Element field = DocumentHelper.createElement(kv.getKey());
				field.setText(kv.getValue());
				currentSection.add(field);
			}
			/*
			 * Single-instance FieldGroup
			 */
			else if (!entry.getFieldGroup().isMultivalued()) {
				if (currentFieldGroup != entry.getFieldGroup()) {

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
	 * <p>
	 * Generate XML which is either flat, or sections of matching key which are one
	 * level deep.
	 * </p>
	 * 
	 * <p>
	 * Sections are auto detected by being a field without a value.
	 * 
	 * <pre>
	 * INVT
	 * NAM  Doe; John
	 * STR  1 Main St
	 * CTY  Springfield
	 * </pre>
	 * </p>
	 * 
	 * @param keyValues
	 * @return Document
	 */
	public Document genXml(List<KeyValue> keyValues) {
		Document document = DocumentHelper.createDocument();
		Element rootNode = document.addElement("DOCUMENT");
		Element currentSection = rootNode;

		int pCount = 1;
		int hCount = 1;
		int tCount = 1;

		for (KeyValue kv : keyValues) {
			if (kv.getKey().trim().isEmpty()) {
				continue;
			}

			String keyName = cleanXMLElementName(kv.getKey());

			if (kv.getValue().trim().isEmpty()) { // auto detect section.
				if (currentSection != rootNode) {
					rootNode.add(currentSection);
				}
				currentSection = DocumentHelper.createElement(keyName);
			} else {
				Element field = DocumentHelper.createElement(keyName);

				/*
				 * Add field ids
				 */
				if (paragraphFields.contains(kv.getKey().toUpperCase())) {
					String idValue = "p-" + Strings.padStart(String.valueOf(pCount), 4, '0');
					field.addAttribute("id", idValue);
					pCount++;
				} else if (headerFields.contains(kv.getKey().toUpperCase())) {
					String idValue = "h-" + Strings.padStart(String.valueOf(hCount), 4, '0');
					field.addAttribute("id", idValue);
					hCount++;
				} else if (tableFields.contains(kv.getKey().toUpperCase())) {
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

	private String cleanXMLElementName(String name) {
		if (QNAME_INVALID.matcher(name).matches()) {
			name = "_" + name;
		}
		return name.trim();
	}

	public static void serializeDom(Writer writer, Document document, Charset outCharset, boolean prettyPrint) throws IOException {
		//Charset outCharset = StandardCharsets.UTF_8;
		//new FileWriter(outDir.resolve(outFileName).toFile()
		OutputFormat outFormat;
		if (prettyPrint) {
			outFormat = OutputFormat.createPrettyPrint();
		} else {
			outFormat = OutputFormat.createCompactFormat();
		}
		outFormat.setEncoding(outCharset.name());
		XMLWriter xmlWriter = new XMLWriter(writer, outFormat);
		xmlWriter.write(document);
	}
}
