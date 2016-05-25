package gov.uspto.bulkdata;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;

import com.google.common.base.Preconditions;

public class PatternXpathValueRegex implements XPathMatch {

	private List<Pattern> regexs = new ArrayList<Pattern>();
	private Pattern pattern = Pattern.compile("");
	private XPathExpression xPathExpression;

	public PatternXpathValueRegex(String xPathExpression, String... regexPattern) throws XPathExpressionException {
		Preconditions.checkNotNull(xPathExpression, "xPathExpression can not be null");
		Preconditions.checkNotNull(regexPattern, "Regex can not be null");

		XPathFactory fact = XPathFactory.newInstance();
		XPath xpath = fact.newXPath();
		this.xPathExpression = xpath.compile(xPathExpression);

		for (String regex : regexPattern) {
			Pattern cRegex = Pattern.compile(regex);
			regexs.add(cRegex);
		}
	}

	@Override
	public boolean match(Document document) throws XPathExpressionException {
		String value = (String) xPathExpression.evaluate(document, XPathConstants.STRING);
		Matcher matcher = pattern.matcher(value);
		for(Pattern regex: regexs){
			matcher.reset().usePattern(regex);
			if (matcher.matches()){
				return true;
			}
		}
		return false;
	}
}
