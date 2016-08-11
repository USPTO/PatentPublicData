package gov.uspto.bulkdata.find;

import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.common.base.Preconditions;

/**
 * Match XML value retrieved through XPATH against REGEX pattern.
 * <p>
 * Match Value
 * <pre>
 * {@code
 * MatchValueRegex matcher = MatchValueRegex("/doc/id", "^US12345.+$");
 * matcher.match(xmlDocString);
 * }
 * </pre>
 * 
 * Match Attribute
 * <pre>
 * {@code
 * MatchValueRegex matcher = MatchValueRegex("/doc/@id", "^US12345.+$");
 * matcher.match(xmlDocString);
 * }
 * </pre>
 * </p>
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class MatchValueRegex implements Match {
	private static final Logger LOGGER = LoggerFactory.getLogger(MatchValueRegex.class);

	private final String xPathExpression;
	private final Pattern regexPattern;

	public MatchValueRegex(final String xPathExpression, final String fullRegexPattern){
		Preconditions.checkNotNull(xPathExpression, "xPathExpression can not be null");
		Preconditions.checkNotNull(fullRegexPattern, "Regex can not be null");

		this.xPathExpression = xPathExpression;
		this.regexPattern = Pattern.compile(fullRegexPattern);
	}

	@Override
	public boolean match(String xmlDocStr){
		if (xmlDocStr == null){
			return false;
		}

		DocumentBuilderFactory dbfact = DocumentBuilderFactory.newInstance();
		dbfact.setAttribute("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

		try {

			DocumentBuilder builder = dbfact.newDocumentBuilder();
	        InputSource inputSource = new InputSource(new StringReader(xmlDocStr));
	
			org.w3c.dom.Document document = builder.parse(inputSource);
			XPathFactory fact = XPathFactory.newInstance();
			XPath xpath = fact.newXPath();

			String value = (String) xpath.evaluate(xPathExpression, document, XPathConstants.STRING);
			Matcher matcher = regexPattern.matcher(value);
	        if (matcher.matches()){
	        	return true;
	        }
		} catch (XPathExpressionException e) {
			LOGGER.error("XML XPathExpressionException on doc:{}", xmlDocStr, e);
			return false;
		} catch (ParserConfigurationException e) {
			LOGGER.error("XML ParserConfigurationException", e);
			return false;
		} catch (SAXException e) {
			LOGGER.error("XML SAXException on doc:{}", xmlDocStr, e);
			return false;
		} catch (IOException e) {
			LOGGER.error("IOException", e);
			return false;
		}

        return false;
	}
}
