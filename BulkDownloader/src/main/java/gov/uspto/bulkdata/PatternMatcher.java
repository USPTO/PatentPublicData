package gov.uspto.bulkdata;

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

	public void add(XPathMatch pattern){
		patterns.add(pattern);
	}

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
			
			for(XPathMatch pattern: patterns){
				if (pattern.match(document)){
					return true;
				}
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

	@Override
	public String toString() {
		return "PatternMatcher [patterns=" + patterns + "]";
	}
}
