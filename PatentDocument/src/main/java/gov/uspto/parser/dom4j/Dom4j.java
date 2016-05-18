package gov.uspto.parser.dom4j;

import org.dom4j.Document;

import gov.uspto.patent.PatentParserException;
import gov.uspto.patent.model.Patent;

public interface Dom4j {
	public Patent parse(Document document) throws PatentParserException;
}	

