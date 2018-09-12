package gov.uspto.parser.dom4j;

import java.io.Reader;

import org.dom4j.Document;

import gov.uspto.patent.PatentReaderException;
import gov.uspto.patent.model.Patent;

public interface Dom4j {
	public Patent parse(Reader reader) throws PatentReaderException;
	public Patent parse(Document document) throws PatentReaderException;
}

