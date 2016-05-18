package gov.uspto.parser.dom4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.util.Collection;

import org.dom4j.Document;

import gov.uspto.patent.PatentParserException;
import gov.uspto.patent.model.Patent;

public abstract class KeyValueParser implements Dom4j {

	private KeyValue2Dom4j kvParser;

	public KeyValueParser(Collection<String> sectionNames) {
		kvParser = new KeyValue2Dom4j(sectionNames);
	}

	public Patent parse(Path docPath)
			throws UnsupportedEncodingException, FileNotFoundException, PatentParserException {
		return parse(docPath.toFile());
	}

	public Patent parse(File file) throws UnsupportedEncodingException, FileNotFoundException, PatentParserException {
		return parse(new InputStreamReader(new FileInputStream(file), "UTF-8"));
	}

	public Patent parse(Reader reader) throws PatentParserException {
		Document document = kvParser.parse(reader);
		return parse(document);
	}
}
