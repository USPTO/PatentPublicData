package gov.uspto.bulkdata.corpusbuilder;

import java.util.List;
import java.util.SortedSet;

import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.bulkdata.find.PatternMatcher;
import gov.uspto.bulkdata.find.PatternXPath;
import gov.uspto.patent.PatentDocFormat;
import gov.uspto.patent.model.classification.PatentClassification;
import gov.uspto.patent.model.classification.ClassificationType;
import gov.uspto.patent.model.classification.CpcClassification;
import gov.uspto.patent.model.classification.UspcClassification;

/**
 * Match Patents by only looking at Classifications stored in each Patent's XML.
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class MatchClassificationXPath implements CorpusMatch<MatchClassificationXPath> {
	private static final Logger LOGGER = LoggerFactory.getLogger(MatchClassificationXPath.class);

	private final List<PatentClassification> wantedClasses;
	private PatternMatcher matcher;
	private String xmlDocStr;

	private PatentDocFormat patentDocFormat;

	public MatchClassificationXPath(List<PatentClassification> wantedClasses){
		this.wantedClasses = wantedClasses;
	}

	@Override
	public void setup() throws XPathExpressionException{
		matcher = new PatternMatcher();

		@SuppressWarnings("unchecked")
		SortedSet<PatentClassification> cpcClasses = PatentClassification.filterByType(wantedClasses, ClassificationType.CPC);
		for (PatentClassification cpcClass : cpcClasses) {
			CpcClassification cpc = (CpcClassification) cpcClass;
			String CPCXpathStr = buildCPCxPathString(cpc);
			LOGGER.debug("CPC xPath: {}", CPCXpathStr);
			PatternXPath CPC = new PatternXPath(CPCXpathStr);
			matcher.add(CPC);
		}

		@SuppressWarnings("unchecked")
		SortedSet<PatentClassification> uspcClasses = PatentClassification.filterByType(wantedClasses, ClassificationType.USPC);
		for (PatentClassification uspcClass : uspcClasses) {
			UspcClassification uspc = (UspcClassification) uspcClass;
			String UspcXpathStr = buildUSPCxPathString(uspc);
			LOGGER.debug("USPC xPath: {}", UspcXpathStr);
			PatternXPath USPC = new PatternXPath(UspcXpathStr);
			matcher.add(USPC);
		}
	}

	@Override
	public MatchClassificationXPath on(String xmlDocStr, PatentDocFormat patentDocFormat) {
		this.xmlDocStr = xmlDocStr;
		this.patentDocFormat = patentDocFormat;
		return this;
	}

	@Override
	public boolean match() {
		return matcher.match(xmlDocStr);
	}

	@Override
	public String getLastMatchPattern() {
		return matcher.getLastMatchedPattern().toString();
	}	

	/**
	 * 
	 * Note matches on Patent Classification as well any Cited Patent Classifications (Citations are only publicly available within Grants).
	 * 
	 * @param uspcClass
	 * @return
	 * @throws XPathExpressionException
	 */
	public String buildUSPCxPathString(UspcClassification uspcClass) throws XPathExpressionException {
		StringBuilder stb = new StringBuilder();
		stb.append("//classification-national/main-classification");
		stb.append("[starts-with(.,'").append(uspcClass.getMainClass()).append("')]");
		return stb.toString();
	}

	/**
	 * 
	 * Build XPath Expression for CPC Classification lookup.
	 * 
	 * "//classifications-cpc/main-cpc/classification-cpc[section/text()='H' and class/text()='04' and subclass/text()='N' and main-group[starts-with(.,'21')]]"
	 * 
	 * @param cpcClass
	 * @return
	 * @throws XPathExpressionException
	 */
	public String buildCPCxPathString(CpcClassification cpcClass) throws XPathExpressionException {

		StringBuilder stb = new StringBuilder();
		stb.append("//classifications-cpc/main-cpc/classification-cpc");
		stb.append("[");
		stb.append("section/text()='").append(cpcClass.getSection()).append("'");
		stb.append(" and ");
		stb.append("class/text()='").append(cpcClass.getMainClass()).append("'");
		stb.append(" and ");
		stb.append("subclass/text()='").append(cpcClass.getSubClass()).append("'");
		stb.append(" and ");
		stb.append("main-group[starts-with(.,'").append(cpcClass.getMainGroup()).append("')]");
		stb.append("]");

		return stb.toString();
	}

}
