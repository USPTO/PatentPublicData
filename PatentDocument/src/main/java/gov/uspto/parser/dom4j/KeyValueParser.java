package gov.uspto.parser.dom4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Path;
import java.util.Collection;

import org.dom4j.Document;

import gov.uspto.patent.PatentReaderException;
import gov.uspto.patent.model.Patent;

public abstract class KeyValueParser implements Dom4j {

	private KeyValue2Dom4j kvParser;

	public KeyValueParser(Collection<String> sectionNames) {
		kvParser = new KeyValue2Dom4j(sectionNames);
	}

	public Patent parse(Path docPath) throws PatentReaderException, IOException {
		return parse(docPath.toFile());
	}

	public Patent parse(File file) throws PatentReaderException, IOException {
		try(InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "UTF-8")){
			return parse(reader);
		}
	}

	public Patent parse(Reader reader) throws PatentReaderException {
		Document document = kvParser.parse(reader);
		return parse(document);
	}
}
