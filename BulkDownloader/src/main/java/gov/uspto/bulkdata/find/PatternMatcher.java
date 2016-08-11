package gov.uspto.bulkdata.find;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class PatternMatcher {
	private static final Logger LOGGER = LoggerFactory.getLogger(PatternMatcher.class);

	private List<XPathMatch> patterns = new ArrayList<XPathMatch>();
	private DocumentBuilder docBuilder;
	private XPathMatch lastMatchedPattern;

	public PatternMatcher() {
		DocumentBuilderFactory dbfact = DocumentBuilderFactory.newInstance();
		dbfact.setAttribute("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		try {
			docBuilder = dbfact.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			LOGGER.error("XML ParserConfigurationException", e);
			// ignore...
		}
	}

	public void add(XPathMatch pattern) {
		patterns.add(pattern);
	}

	public boolean match(String xmlDocStr) {
		if (xmlDocStr == null) {
			LOGGER.warn("xmlDocStr is null");
			return false;
		}

		docBuilder.reset();

		try {

			InputSource inputSource = new InputSource(new StringReader(xmlDocStr));

			org.w3c.dom.Document document = docBuilder.parse(inputSource);

			for (XPathMatch pattern : patterns) {
				if (pattern.match(document)) {
					lastMatchedPattern = pattern;
					return true;
				}
			}

		} catch (XPathExpressionException e) {
			LOGGER.error("XML XPathExpressionException on doc:{}", xmlDocStr, e);
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

	public XPathMatch getLastMatchedPattern() {
		return lastMatchedPattern;
	}

	@Override
	public String toString() {
		return "PatternMatcher [patterns=" + patterns + "]";
	}
}
