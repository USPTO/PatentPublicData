package gov.uspto.patent.doc.xml.fragments;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.util.Set;
import java.util.SortedSet;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.junit.Test;

import gov.uspto.patent.model.classification.ClassificationType;
import gov.uspto.patent.model.classification.PatentClassification;

public class ClassificationNodeTest {

	@Test
	public void test() throws DocumentException, ParseException {
		String xml = "<xml><classifications-cpc>\r\n" + 
				"<main-cpc>\r\n" + 
				"<classification-cpc>\r\n" + 
				"<cpc-version-indicator><date>20130101</date></cpc-version-indicator>\r\n" + 
				"<section>F</section>\r\n" + 
				"<class>25</class>\r\n" + 
				"<subclass>D</subclass>\r\n" + 
				"<main-group>23</main-group>\r\n" + 
				"<subgroup>028</subgroup>\r\n" + 
				"<symbol-position>F</symbol-position>\r\n" + 
				"<classification-value>I</classification-value>\r\n" + 
				"<action-date><date>20180102</date></action-date>\r\n" + 
				"<generating-office><country>US</country></generating-office>\r\n" + 
				"<classification-status>B</classification-status>\r\n" + 
				"<classification-data-source>H</classification-data-source>\r\n" + 
				"<scheme-origination-code>C</scheme-origination-code>\r\n" + 
				"</classification-cpc>\r\n" + 
				"</main-cpc>\r\n" + 
				"</classifications-cpc>\r\n" + 
				"<classification-locarno>\r\n" + 
				"<edition>11</edition>\r\n" + 
				"<main-classification>1507</main-classification>\r\n" + 
				"</classification-locarno>\r\n" + 
				"<classification-national>\r\n" + 
				"<country>US</country>\r\n" + 
				"<main-classification>D15 86</main-classification>\r\n" + 
				"</classification-national>\r\n" +
				"<classifications-ipcr>\r\n" + 
				"<classification-ipcr>\r\n" + 
				"<ipc-version-indicator><date>20060101</date></ipc-version-indicator>\r\n" + 
				"<classification-level>A</classification-level>\r\n" + 
				"<section>A</section>\r\n" + 
				"<class>01</class>\r\n" + 
				"<subclass>H</subclass>\r\n" + 
				"<main-group>5</main-group>\r\n" + 
				"<subgroup>00</subgroup>\r\n" + 
				"<symbol-position>F</symbol-position>\r\n" + 
				"<classification-value>I</classification-value>\r\n" + 
				"<action-date><date>20180102</date></action-date>\r\n" + 
				"<generating-office><country>US</country></generating-office>\r\n" + 
				"<classification-status>B</classification-status>\r\n" + 
				"<classification-data-source>H</classification-data-source>\r\n" + 
				"</classification-ipcr>\r\n" + 
				"</classifications-ipcr></xml>";

		Document doc = DocumentHelper.parseText(xml);
		Set<PatentClassification> clazs = new ClassificationNode(doc).read();
		//clazs.forEach(System.out::println);

		SortedSet<PatentClassification> uspcClazs = PatentClassification.filterByType(clazs, ClassificationType.USPC);
		assertEquals("D15 86", uspcClazs.iterator().next().getTextOriginal());

		SortedSet<PatentClassification> cpcClazs = PatentClassification.filterByType(clazs, ClassificationType.CPC);
		assertEquals("F25D 23/028", cpcClazs.iterator().next().getTextNormalized());

		SortedSet<PatentClassification> ipcClazs = PatentClassification.filterByType(clazs, ClassificationType.IPC);
		assertEquals("A01H 5/00", ipcClazs.iterator().next().getTextNormalized());

		SortedSet<PatentClassification> locarnClazs = PatentClassification.filterByType(clazs, ClassificationType.LOCARNO);
		assertEquals("15-07", locarnClazs.iterator().next().getTextNormalized());
	}

}
