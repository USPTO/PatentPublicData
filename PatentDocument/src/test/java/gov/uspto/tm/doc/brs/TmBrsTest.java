package gov.uspto.tm.doc.brs;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import org.junit.Test;

import gov.uspto.parser.keyvalue.KeyValue;
import gov.uspto.parser.keyvalue.Kv2KvXml;
import gov.uspto.parser.keyvalue.KvDocBuilder;
import gov.uspto.patent.PatentReaderException;

public class TmBrsTest {

	@Test
	public void test() throws PatentReaderException, IOException {

		String rawRec = "</BI>                                        \n" + 
				"<GS> Medical services, including treatment an\n" + 
				"     d diagnoses of cardiac and vascular dise\n" + 
				"     ase, and percutaneous interventional tre\n" + 
				"     atment for cardiac and vascular illness \n" + 
				"     and disease                             \n" + 
				"<U1> 20170101                                \n" + 
				"<U2> 20170101                                \n" + 
				"</GS>                                        \n" + 
				"<MD> 5                                       \n" + 
				"<TD> 0000                                    \n" + 
				"<SN> 88753996                                \n" + 
				"<FD> 20200110                                \n" + 
				"<OB> 1A                                      \n" + 
				"<CB> 1A                                      \n" + 
				"<RN> 0000000                                 \n" + 
				"<RD> 00000000                                \n" + 
				"<ON> (APPLICANT)                             \n" + 
				"<PN> Modern Management Center LLC            \n" + 
				"<EN> LIMITED LIABILITY COMPANY               \n" + 
				"<OW> (APPLICANT)                             \n" + 
				"<PN> Modern Management Center LLC            \n" + 
				"<EN> LIMITED LIABILITY COMPANY               \n" + 
				"<CI> MICHIGAN                                \n" + 
				"<AI> 25130 Southfield Rd                     \n" + 
				"<CY> Southfield                              \n" + 
				"<SC> MICHIGAN 48075                          \n" + 
				"</OW>                                        \n" + 
				"<DE> The color(s) Blue and grey is/are claime\n" + 
				"     d as a feature of the mark.             \n" + 
				"    1The mark consists of The stylized letter\n" + 
				"     s \"VCA\" in blue above the phrase \"Vascul\n" + 
				"     ar Centers of America\" in grey.         \n" + 
				"<TM> SERVICE MARK                            \n" + 
				"<RG> PRINCIPAL                               \n" + 
				"<LD> LIVE                                    \n" + 
				"<TP> 0000      ";

		String xmlExpected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n" +
				"<DOCUMENT>\n" + 
				"  <GS>Medical services, including treatment and diagnoses of cardiac and vascular disease, and percutaneous interventional treatment for cardiac and vascular illness and disease</GS>\n" + 
				"  <U1>20170101</U1>\n" + 
				"  <U2>20170101</U2>\n" + 
				"  <MD>5</MD>\n" + 
				"  <TD>0000</TD>\n" + 
				"  <SN>88753996</SN>\n" + 
				"  <FD>20200110</FD>\n" + 
				"  <OB>1A</OB>\n" + 
				"  <CB>1A</CB>\n" + 
				"  <RN>0000000</RN>\n" + 
				"  <RD>00000000</RD>\n" + 
				"  <ON>(APPLICANT)</ON>\n" + 
				"  <PN>Modern Management Center LLC</PN>\n" + 
				"  <EN>LIMITED LIABILITY COMPANY</EN>\n" + 
				"  <OW>(APPLICANT)</OW>\n" + 
				"  <PN>Modern Management Center LLC</PN>\n" + 
				"  <EN>LIMITED LIABILITY COMPANY</EN>\n" + 
				"  <CI>MICHIGAN</CI>\n" + 
				"  <AI>25130 Southfield Rd</AI>\n" + 
				"  <CY>Southfield</CY>\n" + 
				"  <SC>MICHIGAN 48075</SC>\n" + 
				"  <DE>The color(s) Blue and grey is/are claimed as a feature of the mark. The mark consists of The stylized letters \"VCA\" in blue above the phrase \"Vascular Centers of America\" in grey.</DE>\n" + 
				"  <TM>SERVICE MARK</TM>\n" + 
				"  <RG>PRINCIPAL</RG>\n" + 
				"  <LD>LIVE</LD>\n" + 
				"  <TP>0000</TP>\n" + 		
				"  <PN:AI:AS:CY:SC>Modern Management Center LLC, 25130 Southfield Rd,, Southfield, MICHIGAN 48075</PN:AI:AS:CY:SC>\n" + 
				"</DOCUMENT>\n\n";

		TmBrs brs = new TmBrs(false, false);
		List<KeyValue> keyValues = brs.parse(rawRec);

		//keyValues.stream().forEach(System.out::println);

		StringWriter writer = new StringWriter();
		KvDocBuilder kvWriter = new Kv2KvXml(true, true);
		kvWriter.write(keyValues, writer);
		String actualXML = writer.toString().replaceAll("\r", "");
		//System.out.println(actualXML);

		assertEquals(xmlExpected, actualXML);
	}

}
