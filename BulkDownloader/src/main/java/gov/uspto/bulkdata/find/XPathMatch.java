package gov.uspto.bulkdata.find;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;

public interface XPathMatch {
	public boolean match(Document document)  throws XPathExpressionException;
	public boolean matchAll(Document document) throws XPathExpressionException;
	public boolean matchAny(Document document) throws XPathExpressionException;
}
