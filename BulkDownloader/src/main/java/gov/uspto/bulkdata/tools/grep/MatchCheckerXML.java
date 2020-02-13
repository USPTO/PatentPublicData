package gov.uspto.bulkdata.tools.grep;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.common.base.Preconditions;

public class MatchCheckerXML implements Match<MatchPattern> {

	protected Set<MatchPattern> matchPatterns;

	public MatchCheckerXML(Set<MatchPattern> matchPatterns) {
		setMatchPatterns(matchPatterns);
	}

	@Override
	public void setMatchPatterns(Set<MatchPattern> matchPatterns) {
		Preconditions.checkNotNull(matchPatterns);
		Preconditions.checkArgument(!matchPatterns.isEmpty());
		this.matchPatterns = matchPatterns;
	}

	public boolean match(String source, CharSequence string, Writer writer, boolean stopOnFirstMatch)
			throws IOException, DocumentException {
		return match(source, new StringReader(string.toString()), writer, stopOnFirstMatch);
	}

	@Override
	public boolean match(String source, Reader reader, Writer writer, boolean stopOnFirstMatch)
			throws IOException, DocumentException {
		Document w3cdom = createDOM(reader);
		return match(source, w3cdom, writer, stopOnFirstMatch);
	}

	public boolean match(String source, Document w3cdom, Writer writer, boolean stopOnFirstMatch)
			throws IOException, DocumentException {
		boolean matched = false;
		for (MatchPattern matchPattern : matchPatterns) {
			if (matchPattern instanceof MatchPatternXPath) {
				if (((MatchPatternXPath) matchPattern).writeMatches(source, w3cdom, writer)) {
					matched = true;
					if (stopOnFirstMatch) {
						return true;
					}
				}
			} else if (matchPattern instanceof MatchXPathExpression) {
				if (((MatchXPathExpression) matchPattern).writeMatches(source, w3cdom, writer)) {
					matched = true;
					if (stopOnFirstMatch) {
						return true;
					}
				}
			} else if (matchPattern instanceof MatchXPathNodeValues) {
				if (((MatchXPathNodeValues) matchPattern).writeMatches(source, w3cdom, writer)) {
					matched = true;
					if (stopOnFirstMatch) {
						return true;
					}
				}
			}
		}
		return matched;
	}

	@Override
	public boolean match(Reader reader) throws DocumentException {
		DocumentBuilderFactory dbfact = DocumentBuilderFactory.newInstance();
		dbfact.setAttribute("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		InputSource inputSource = new InputSource(reader);

		try {
			DocumentBuilder builder = dbfact.newDocumentBuilder();
			Document document = builder.parse(inputSource);
			if (match(document)) {
				return true;
			}
			return false;

		} catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
			throw new DocumentException(e);
		}
	}

	public boolean match(Document w3cdom) throws XPathExpressionException, DocumentException {
		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();
		for (MatchPattern matchPattern : matchPatterns) {
			if (matchPattern instanceof MatchPatternXPath) {
				MatchPatternXPath matchPattern2 = (MatchPatternXPath) matchPattern;
				String value = (String) xpath.evaluate(matchPattern2.getXpathNodePath(), w3cdom, XPathConstants.STRING);
				if (matchPattern.hasMatch(value)) {
					return true;
				}
			} else if (matchPattern instanceof MatchXPathExpression) {
				if (matchPattern.hasMatch(w3cdom)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean match(String rawStr) throws DocumentException {
		for (MatchPattern matchPattern : matchPatterns) {
			if (matchPattern.hasMatch(rawStr)) {
				return true;
			}
		}
		return false;
	}

	private Document createDOM(Reader reader) throws DocumentException {
		DocumentBuilderFactory dbfact = DocumentBuilderFactory.newInstance();
		dbfact.setNamespaceAware(true);
		dbfact.setValidating(false);
		dbfact.setAttribute("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		InputSource inputSource = new InputSource(reader);

		Document document;
		try {
			DocumentBuilder builder = dbfact.newDocumentBuilder();
			document = builder.parse(inputSource);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new DocumentException(e);
		}
		return document;
	}

}
