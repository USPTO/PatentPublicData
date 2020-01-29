package gov.uspto.parser.keyvalue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

import org.dom4j.Document;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.google.common.base.Preconditions;

import gov.uspto.parser.dom4j.Dom4j;
import gov.uspto.patent.PatentReaderException;
import gov.uspto.patent.model.Patent;

public abstract class KvParser implements Dom4j {
	// private static final Logger LOGGER = LoggerFactory.getLogger(KvParser.class);

	private final KvReader kvReader;
	private final KeyValue2Dom4j kvWriter;

	public KvParser() {
		kvReader = new SimpleKvReader();
		kvWriter = new KeyValue2Dom4j();
	}

	public KvParser(Collection<String> maintainSpaceFields, Collection<String> paragraphFields,
			Collection<String> headerFields, Collection<String> tableFields) {
		kvReader = new SimpleKvReader();
		kvReader.setMaintainSpaceFields(maintainSpaceFields);

		kvWriter = new KeyValue2Dom4j();
		kvWriter.setFieldsForId(paragraphFields, headerFields, tableFields);
	}

	public Patent parse(Path docPath) throws PatentReaderException, IOException {
		return parse(docPath.toFile());
	}

	public Patent parse(File file) throws PatentReaderException, IOException {
		Preconditions.checkArgument(file.isFile(), "File is not plain file: " + file.getAbsolutePath());
		try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "UTF-8")) {
			return parse(reader);
		}
	}

	/**
	 * Parse Reader text and generate XML document
	 */
	@Override
	public Patent parse(Reader reader) throws PatentReaderException {
		/*
		 * try { LOGGER.info("RAW: {}", IOUtils.toString(reader)); reader.reset(); }
		 * catch (IOException e) { e.printStackTrace(); }
		 */

		List<KeyValue> keyValues = kvReader.parse(reader);
		// LOGGER.info("KeyValues: {}", keyValues);

		Document document = kvWriter.genXml(keyValues);
		// LOGGER.info("XML: {}", document.asXML());

		return parse(document);
	}

	public String getSource() {
		// %X{SOURCE}:%X{RECNUM}:%X{DOCID}
		StringBuilder stb = new StringBuilder();
		stb.append(MDC.get("SOURCE"));
		stb.append(":");
		stb.append(MDC.get("RECNUM"));
		stb.append(":");
		stb.append(MDC.get("DOCID"));
		return stb.toString();
	}
}
