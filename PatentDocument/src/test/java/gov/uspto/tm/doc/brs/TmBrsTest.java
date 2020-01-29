package gov.uspto.tm.doc.brs;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.dom4j.Document;
import org.junit.Test;

import gov.uspto.parser.keyvalue.KeyValue;
import gov.uspto.parser.keyvalue.KeyValue2Dom4j;
import gov.uspto.patent.PatentReaderException;

public class TmBrsTest {

	@Test
	public void test() throws PatentReaderException, IOException {

		String rawRec = "<WM> VCA VASCULAR CENTERS OF AMERICA         \n" + 
				"</WM>                                        \n" + 
				"</BI>                                        \n" + 
				"<CL> IC  044.                                \n" + 
				"<US>   US 100 101.                           \n" + 
				"<CP> 044 001 003 005 009 010 018 021 031 035 \n" + 
				"     036 037 038 039 040 041 042 043 045.    \n" + 
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
				"  <WM>VCA VASCULAR CENTERS OF AMERICA</WM>\n" + 
				"  <CL>IC 044.</CL>\n" + 
				"  <US>US 100 101.</US>\n" + 
				"  <CP>044 001 003 005 009 010 018 021 031 035 036 037 038 039 040 041 042 043 045.</CP>\n" + 
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
				"</DOCUMENT>\n";

		TmBrs brs = new TmBrs();
		List<KeyValue> keyValues = brs.parse(rawRec);

		//keyValues.stream().forEach(System.out::println);
		KeyValue2Dom4j kvWriter = new KeyValue2Dom4j();
		Document xmlDoc = kvWriter.genXml(keyValues);
		
		StringWriter outXmlStr = new StringWriter();
		KeyValue2Dom4j.serializeDom(outXmlStr, xmlDoc, StandardCharsets.UTF_8, true);
		String actualXML = outXmlStr.toString().replaceAll("\r", "");
		System.out.println(actualXML);

		assertEquals(xmlExpected, actualXML);
	}

}
