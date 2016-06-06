package gov.uspto.patent.greenbook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;

import org.dom4j.Document;

import gov.uspto.parser.dom4j.KeyValue2Dom4j;
import gov.uspto.patent.PatentParserException;

/**
 * Convert Patent Greenbook to XML
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class Greenbook2GreenbookXml extends KeyValue2Dom4j {

	private static final Set<String> SECTIONS = new HashSet<String>(20);

	static {
		SECTIONS.add("PATN");
		SECTIONS.add("INVT");
		SECTIONS.add("ASSG");
		SECTIONS.add("PRIR");
		SECTIONS.add("REIS");
		SECTIONS.add("RLAP");
		SECTIONS.add("CLAS");
		SECTIONS.add("UREF");
		SECTIONS.add("FREF");
		SECTIONS.add("OREF");
		SECTIONS.add("LREP");
		SECTIONS.add("PCTA");
		SECTIONS.add("ABST");
		SECTIONS.add("GOVT");
		SECTIONS.add("PARN");
		SECTIONS.add("BSUM");
		SECTIONS.add("DRWD");
		SECTIONS.add("DETD");
		SECTIONS.add("CLMS");
		SECTIONS.add("DCLM");
	}

	public Greenbook2GreenbookXml() {
		super(SECTIONS);
	}

	public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException, PatentParserException {
		String filename = args[0];
		File file = new File(filename);

		Greenbook2GreenbookXml g2xml = new Greenbook2GreenbookXml();

		if (file.isDirectory()) {
			int count = 1;
			for (File subfile : file.listFiles()) {
				System.out.println(count++ + " " + subfile.getAbsolutePath());
				Document dom4jDoc = g2xml.parse(subfile);
				System.out.println(dom4jDoc.asXML());
			}
		} else {
			Document dom4jDoc = g2xml.parse(file);
			System.out.println(dom4jDoc.asXML());
		}
	}
}
