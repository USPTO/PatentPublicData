package gov.uspto.parser.dom4j.keyvalue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;

import gov.uspto.parser.dom4j.Dom4j;
import gov.uspto.patent.PatentReaderException;
import gov.uspto.patent.model.Patent;

public abstract class KvParser implements Dom4j {

    private KvReader kvReader = new KvReader();

    public Patent parse(Path docPath) throws PatentReaderException, IOException {
        return parse(docPath.toFile());
    }

    public Patent parse(File file) throws PatentReaderException, IOException {
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "UTF-8")) {
            return parse(reader);
        }
    }

    public Patent parse(Reader reader) throws PatentReaderException {
        List<KeyValue> keyValues = kvReader.parse(reader);
        Document document = kvReader.genXml(keyValues);
        return parse(document);
    }
}
