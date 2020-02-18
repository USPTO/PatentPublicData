package gov.uspto.parser.keyvalue;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

public class Kv2SolrXml extends KvWriter {

	private static String ROOT_NODE_NAME = "doc";

	private final Writer writer;
	private final OutputFormat outFormat;

	private Document doc;
	private Element rootNode;
	private Element currentSection;

	public Kv2SolrXml(Writer writer) {
		this(writer, defaultOutputFormat());
	}

	public Kv2SolrXml(Writer writer, OutputFormat outFormat) {
		this.writer = writer;
		this.outFormat = outFormat;
	}

	private static OutputFormat defaultOutputFormat() {
		OutputFormat outFormat = OutputFormat.createCompactFormat();
		outFormat.setSuppressDeclaration(true);
		outFormat.setEncoding(StandardCharsets.UTF_8.name());
		return outFormat;
	}

	@Override
	public String getRootElementName() {
		return ROOT_NODE_NAME;
	}

	@Override
	public Kv2SolrXml startRecord() {
		doc = DocumentHelper.createDocument();
		rootNode = doc.addElement(ROOT_NODE_NAME);
		currentSection = rootNode;
		return this;
	}

	@Override
	public Kv2SolrXml startSection(KeyValue kv) {
		// SolrXml does not have sub elements or sections.
		return this;
	}

	@Override
	public Kv2SolrXml endCurrentSection() {
		return this;
	}

	@Override
	public Kv2SolrXml addField(KeyValue kv) {
		Element el = DocumentHelper.createElement("field").addAttribute("name", kv.getKey()).addText(kv.getValue());
		currentSection.add(el);
		return this;
	}

	@Override
	public void writeRecord() throws IOException {
		XMLWriter xmlWriter = new XMLWriter(writer, outFormat);
		xmlWriter.write(doc);
	}

	@Override
	public void close() throws Exception {
		if (writer != null) {
			writer.close();
		}
	}

}
