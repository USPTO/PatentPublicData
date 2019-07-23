package gov.uspto.patent.doc.greenbook;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.StringReader;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;
import org.junit.Test;

import gov.uspto.parser.dom4j.keyvalue.KeyValue;
import gov.uspto.parser.dom4j.keyvalue.KvReader;
import gov.uspto.patent.PatentReaderException;

public class KvReaderTest {

	@Test
	public void titleWraps() throws PatentReaderException {

		String text = "PATN\n" + 
				"WKU  040248011\n" + 
				"SRC  5\n" + 
				"APN  1891537\n" + 
				"APT  1\n" + 
				"ART  341\n" + 
				"APD  19711014\n" + 
				"TTL  EXTENDED INSULATED HOT HEAD PISTON WITH EXTENDED INSULATED HOT CYLINDER\n" + 
				"      WALLS\n" + 
				"ISD  19770524\n" + 
				"NCL  7\n" + 
				"EXP  Cohen; Irwin C.\n" + 
				"NDR  3\n" + 
				"NFG  3\n" + 
				"INVT\n" + 
				"NAM  Hudson; Perry David\n" + 
				"CTY  Corpus Christi\n" + 
				"STA  TX\n";


		 KvReader kvReader = new KvReader();
		 List<KeyValue> keyValues = kvReader.parse(new StringReader(text));
		 System.out.println(keyValues);
		 
		 Document document = kvReader.genXml(keyValues);
		 System.out.println(document.asXML());
		 
		 Node ttlN = document.selectSingleNode("/DOCUMENT/PATN/TTL");
		 assertNotNull(ttlN);

		 String titleActual = ttlN.getText();
		 String titleExpect = "EXTENDED INSULATED HOT HEAD PISTON WITH EXTENDED INSULATED HOT CYLINDER WALLS";
	 
		 assertEquals(titleExpect, titleActual);
	}

}
