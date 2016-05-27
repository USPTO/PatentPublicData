package gov.uspto.bulkdata.corpusbuilder;

import javax.xml.xpath.XPathExpressionException;

import gov.uspto.patent.PatentParserException;

public interface CorpusMatch<Q extends CorpusMatch<?>> {
	public void setup() throws XPathExpressionException;
	public Q on(String xmlDocStr) throws PatentParserException;
	public boolean match();
	public String getLastMatchPattern();
}
