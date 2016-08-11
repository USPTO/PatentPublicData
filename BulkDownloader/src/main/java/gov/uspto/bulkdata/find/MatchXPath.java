package gov.uspto.bulkdata.find;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;

public abstract class MatchXPath implements XPathMatch {

	public enum MatchOperator {
		ANY, ALL
	}
	
	private MatchOperator operator = MatchOperator.ANY;

	public void setMatchOperator(MatchOperator operator){
		this.operator = operator;
	}

	@Override
	public boolean match(Document document) throws XPathExpressionException{
		if (operator == MatchOperator.ALL){
			return matchAll(document);
		}
		else {
			return matchAny(document);
		}
	}

}
