package gov.uspto.patent;

import java.io.File;
import java.io.FileNotFoundException;

import org.dom4j.Document;

import gov.uspto.parser.dom4j.Dom4JParser;
import gov.uspto.patent.model.Patent;
import gov.uspto.patent.pap.PatentAppPubParser;
import gov.uspto.patent.sgml.Sgml;
import gov.uspto.patent.xml.ApplicationParser;
import gov.uspto.patent.xml.GrantParser;

/**
 * PatentParser 
 * 
 * detects and initializes parser for XML and SGML Patent Formats.
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class PatentXmlParser extends Dom4JParser {

	@Override
	public Patent parse(Document document) throws PatentParserException {
		if (document.selectSingleNode(ApplicationParser.XML_ROOT) != null) {
			return new ApplicationParser().parse(document);
		} else if (document.selectSingleNode(GrantParser.XML_ROOT) != null) {
			return new GrantParser().parse(document);
		} else if (document.selectSingleNode(Sgml.SGML_ROOT) != null) {
			return new Sgml().parse(document);
		} else if (document.selectSingleNode(PatentAppPubParser.XML_ROOT) != null) {
			return new PatentAppPubParser().parse(document);
		} else {
			// ((Element)
			// document.getRootElement().elements().get(0)).getUniquePath()
			throw new PatentParserException("Invalid or Unknown Document Type");
		}
	}

	public static void main(String[] args) throws FileNotFoundException, PatentParserException {

		String filename = args[0];

		File file = new File(filename);

		if (file.isDirectory()) {
			int count = 1;
			for (File subfile : file.listFiles()) {
				System.out.println(count++ + " " + subfile.getAbsolutePath());
				PatentXmlParser patentParser = new PatentXmlParser();
				Patent patent = patentParser.parse(subfile);
				if (patent.getAbstract().getProcessedText().length() < 90) {
					System.err.println("Abstract too small.");
				}
				if (patent.getDescription().getAllProcessedText().length() < 400) {
					System.err.println("Description to small.");
				}
				//System.out.println(patent.toString());
			}
		} else {
			PatentXmlParser patentParser = new PatentXmlParser();
			Patent patent = patentParser.parse(file);
			System.out.println(patent.toString());
		}

	}

}
