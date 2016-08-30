package gov.uspto.bulkdata.corpusbuilder;

import java.io.IOException;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.patent.PatentDocFormat;
import gov.uspto.patent.PatentReader;
import gov.uspto.patent.PatentReaderException;
import gov.uspto.patent.model.Patent;
import gov.uspto.patent.model.classification.Classification;
import gov.uspto.patent.model.classification.ClassificationType;
import gov.uspto.patent.model.classification.CpcClassification;
import gov.uspto.patent.model.classification.UspcClassification;

/**
 *  Match Patents by instantiating each Patent and then matching on Classification.
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class MatchClassificationPatent implements CorpusMatch<MatchClassificationPatent> {
	private static final Logger LOGGER = LoggerFactory.getLogger(MatchClassificationPatent.class);

	private final List<Classification> wantedClasses;

	private List<Classification> wantedCPC;
	private List<Classification> wantedUSPC;
	private Patent patent;
	private String lastPatternMatch;

	public MatchClassificationPatent(List<Classification> wantedClasses) {
		this.wantedClasses = wantedClasses;
	}

	@Override
	public void setup() throws XPathExpressionException {
		wantedCPC = Classification.getByType(wantedClasses, ClassificationType.CPC);
		wantedUSPC = Classification.getByType(wantedClasses, ClassificationType.USPC);

	}

	@Override
	public String getLastMatchPattern() {
		return lastPatternMatch;
	}

	@Override
	public MatchClassificationPatent on(String xmlDocStr, PatentDocFormat patentDocFormat) throws PatentReaderException, IOException {
		try(PatentReader patentReader = new PatentReader(xmlDocStr, patentDocFormat)){
			patent = patentReader.read();
		}
		return this;
	}

	@Override
	public boolean match() {
		if (patent == null) {
			return false;
		}

		List<Classification> patentCPC = Classification.getByType(patent.getClassification(), ClassificationType.CPC);
		for (Classification wantedCpcClass : wantedCPC) {
			CpcClassification wantedCpc = (CpcClassification) wantedCpcClass;

			for (Classification cpcClass : patentCPC) {
				CpcClassification cpc = (CpcClassification) cpcClass;
				if (cpc.getSection() == wantedCpc.getSection() && cpc.getMainClass() == wantedCpc.getMainClass()
						&& cpc.getSubClass() == wantedCpc.getSubClass()
						&& cpc.getMainGroup() == wantedCpc.getMainGroup()) {
					lastPatternMatch="cpc";
					return true;
				}
			}
		}

		List<Classification> patentUSPC = Classification.getByType(patent.getClassification(), ClassificationType.USPC);
		for (Classification wantedUspcClass : wantedUSPC) {
			UspcClassification wantedUspc = (UspcClassification) wantedUspcClass;

			for (Classification usclass : patentUSPC) {
				UspcClassification uspc = (UspcClassification) usclass;

				if (uspc.getMainClass().equals(wantedUspc.getMainClass())) {
					lastPatternMatch="uspc";
					return true;
				}
			}
		}

		return false;
	}

}
