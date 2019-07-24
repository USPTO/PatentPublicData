package gov.uspto.parser.keyvalue;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.junit.Test;

import gov.uspto.patent.PatentReaderException;

public class KvReaderTest {

	@Test
	public void keyValue2XML() throws PatentReaderException, IOException {

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
				"EXP  James; Kevin C.\n" + 
				"NDR  3\n" + 
				"NFG  3\n" + 
				"INVT\n" + 
				"NAM  Smith; John Doe\n" + 
				"CTY  Alexandria\n" + 
				"STA  VA\n" +
				"ABST\n" +
				"PAL  Paragraph Text 1\n" +
				"      text wrap line 2\n" +
			    "      text wrap line 3\n" +
			    "      text wrap line 4\n" +
				"PAL  Paragraph Text 2\n" +
				"      text wrap line 2\n" +
			    "      text wrap line 3\n" +
			    "      text wrap line 4\n";

		String expectXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n" +
				"<DOCUMENT>\n" + 
				"  <PATN>\n" + 
				"    <WKU>040248011</WKU>\n" + 
				"    <SRC>5</SRC>\n" + 
				"    <APN>1891537</APN>\n" + 
				"    <APT>1</APT>\n" + 
				"    <ART>341</ART>\n" + 
				"    <APD>19711014</APD>\n" + 
				"    <TTL>EXTENDED INSULATED HOT HEAD PISTON WITH EXTENDED INSULATED HOT CYLINDER WALLS</TTL>\n" + 
				"    <ISD>19770524</ISD>\n" + 
				"    <NCL>7</NCL>\n" + 
				"    <EXP>James; Kevin C.</EXP>\n" + 
				"    <NDR>3</NDR>\n" + 
				"    <NFG>3</NFG>\n" + 
				"  </PATN>\n" + 
				"  <INVT>\n" + 
				"    <NAM>Smith; John Doe</NAM>\n" + 
				"    <CTY>Alexandria</CTY>\n" + 
				"    <STA>VA</STA>\n" + 
				"  </INVT>\n" + 
				"  <ABST>\n" + 
				"    <PAL>Paragraph Text 1 text wrap line 2 text wrap line 3 text wrap line 4</PAL>\n" + 
				"    <PAL>Paragraph Text 2 text wrap line 2 text wrap line 3 text wrap line 4</PAL>\n" + 
				"  </ABST>\n" + 
				"</DOCUMENT>\n";

		 KvReader kvReader = new KvReader();
		 List<KeyValue> keyValues = kvReader.parse(new StringReader(text));
		 //System.out.println(keyValues);

		 Document document = kvReader.genXml(keyValues);
		 //System.out.println(document.asXML());

		 // Pretty Print
		 StringWriter sw = new StringWriter();
		 org.dom4j.io.OutputFormat format = OutputFormat.createPrettyPrint();
		 org.dom4j.io.XMLWriter writer = new org.dom4j.io.XMLWriter(sw, format);
		 writer.write(document);
		 String actualPrettyXml = sw.toString();
		 actualPrettyXml = actualPrettyXml.replaceAll("\r", ""); // handle windows style line-endings.
		 //System.out.println(sw.toString());

		 assertEquals(expectXml, actualPrettyXml);
	}

}
