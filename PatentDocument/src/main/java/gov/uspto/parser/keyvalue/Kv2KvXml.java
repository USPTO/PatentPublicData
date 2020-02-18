package gov.uspto.parser.keyvalue;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

public class Kv2KvXml extends KvWriter {

	private static String ROOT_NODE_NAME = "DOCUMENT";
	private static final Pattern QNAME_INVALID = Pattern.compile("^[0-9].+$");

	private final Writer writer;
	private final boolean useOriginalKeyName;
	private final Charset outCharSet;
	private final boolean prettyPrint;	
	
	private Document doc;
	private Element rootNode;
	private Element currentSection;

	public Kv2KvXml(Writer writer) {
		this(true, writer, StandardCharsets.UTF_8, false);
	}

	public Kv2KvXml(boolean useOriginalKeyName, Writer writer) {
		this(useOriginalKeyName, writer, StandardCharsets.UTF_8, false);
	}

	public Kv2KvXml(boolean useOriginalKeyName, Writer writer, boolean prettyPrint) {
		this(useOriginalKeyName, writer, StandardCharsets.UTF_8, prettyPrint);
	}

	public Kv2KvXml(boolean useOriginalKeyName, Writer writer, Charset outCharset, boolean prettyPrint) {
		this.useOriginalKeyName = useOriginalKeyName;
		this.writer = writer;
		this.outCharSet = outCharset;
		this.prettyPrint = prettyPrint;
	}
	
	@Override
	public String getRootElementName() {
		return ROOT_NODE_NAME;
	}

	@Override
	public Kv2KvXml startRecord() {
		doc = DocumentHelper.createDocument();
		rootNode = doc.addElement(ROOT_NODE_NAME);
		currentSection = rootNode;
		return this;
	}

	@Override
	public Kv2KvXml startSection(KeyValue kv) {
		Element newSection = DocumentHelper.createElement(useOriginalKeyName ? kv.getKeyOriginal() : kv.getKey());
		currentSection.add(newSection);
		currentSection = newSection;
		return this;
	}

	@Override
	public Kv2KvXml endCurrentSection() {
		Element el = currentSection.getParent();
		if (el != null) {
			currentSection = el;
		} else {
			currentSection = rootNode;
		}
		return this;
	}

	@Override
	public Kv2KvXml addField(KeyValue kv) {
		Element el = DocumentHelper
				.createElement(cleanXMLElementName(useOriginalKeyName ? kv.getKeyOriginal() : kv.getKey()))
				.addText(kv.getValue());
		currentSection.add(el);
		return this;
	}

	@Override
	public void writeRecord() throws IOException {
		OutputFormat outFormat;
		if (prettyPrint) {
			outFormat = OutputFormat.createPrettyPrint();
		} else {
			outFormat = OutputFormat.createCompactFormat();
		}
		outFormat.setEncoding(outCharSet.name());
		XMLWriter xmlWriter = new XMLWriter(writer, outFormat);
		xmlWriter.write(doc);
	}

	@Override
	public void close() throws Exception {
		if (writer != null) {
			writer.close();
		}
	}

	private String cleanXMLElementName(String name) {
		if (QNAME_INVALID.matcher(name).matches()) {
			name = "_" + name;
		}
		return name.trim();
	}

}
