package gov.uspto.bulkdata.corpusbuilder;

import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import gov.uspto.patent.PatentDocFormat;
import gov.uspto.patent.PatentReaderException;

public interface CorpusMatch<Q extends CorpusMatch<?>> {
	public void setup() throws XPathExpressionException;
	public Q on(String xmlDocStr, PatentDocFormat patentDocFormat) throws PatentReaderException, IOException;
	public boolean match();
	public String getLastMatchPattern();
}
