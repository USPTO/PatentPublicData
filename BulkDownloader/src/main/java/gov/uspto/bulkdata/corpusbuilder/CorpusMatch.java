package gov.uspto.bulkdata.corpusbuilder;

import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import gov.uspto.patent.PatentReaderException;
import gov.uspto.patent.PatentType;

public interface CorpusMatch<Q extends CorpusMatch<?>> {
	public void setup() throws XPathExpressionException;
	public Q on(String xmlDocStr, PatentType patentType) throws PatentReaderException, IOException;
	public boolean match();
	public String getLastMatchPattern();
}
