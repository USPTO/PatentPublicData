package gov.uspto.patent.doc.xml.fragments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.junit.Test;

import gov.uspto.patent.model.classification.ClassificationType;
import gov.uspto.patent.model.classification.CpcClassification;
import gov.uspto.patent.model.classification.PatentClassification;

public class ClassificationNodeTest {

	//@Test FIXME
	public void test() throws DocumentException, ParseException {
		String xml = "<xml><biblio><classifications-cpc>\r\n" + 
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
				"</classifications-ipcr></biblio></xml>";

		Document doc = DocumentHelper.parseText(xml);
		Set<PatentClassification> clazs = new ClassificationNode(doc).read();
		clazs.forEach(System.out::println);
		
		assertEquals(4, clazs.size());

		SortedSet<PatentClassification> uspcClazs = PatentClassification.filterByType(clazs, ClassificationType.USPC);
		assertEquals("D15 86", uspcClazs.iterator().next().getTextOriginal());

		SortedSet<PatentClassification> cpcClazs = PatentClassification.filterByType(clazs, ClassificationType.CPC);
		assertEquals("F25D 23/028", cpcClazs.iterator().next().getTextNormalized());

		SortedSet<PatentClassification> ipcClazs = PatentClassification.filterByType(clazs, ClassificationType.IPC);
		assertEquals("A01H 5/00", ipcClazs.iterator().next().getTextNormalized());

		SortedSet<PatentClassification> locarnClazs = PatentClassification.filterByType(clazs, ClassificationType.LOCARNO);
		assertEquals("15-07", locarnClazs.iterator().next().getTextNormalized());
	}

	//@Test FIXME
	public void CPC() throws DocumentException, ParseException {
		String xml = "<xml><biblio><classifications-cpc>\r\n" + 
				"<main-cpc>\r\n" + 
				"<classification-cpc>\r\n" + 
				"<cpc-version-indicator><date>20130101</date></cpc-version-indicator>\r\n" + 
				"<section>A</section>\r\n" + 
				"<class>01</class>\r\n" + 
				"<subclass>F</subclass>\r\n" + 
				"<main-group>12</main-group>\r\n" + 
				"<subgroup>444</subgroup>\r\n" + 
				"<symbol-position>F</symbol-position>\r\n" + 
				"<classification-value>I</classification-value>\r\n" + 
				"<action-date><date>20170328</date></action-date>\r\n" + 
				"<generating-office><country>US</country></generating-office>\r\n" + 
				"<classification-status>B</classification-status>\r\n" + 
				"<classification-data-source>H</classification-data-source>\r\n" + 
				"<scheme-origination-code>C</scheme-origination-code>\r\n" + 
				"</classification-cpc>\r\n" + 
				"<classification-cpc>\r\n" + 
				"<cpc-version-indicator><date>20130101</date></cpc-version-indicator>\r\n" + 
				"<section>A</section>\r\n" + 
				"<class>01</class>\r\n" + 
				"<subclass>F</subclass>\r\n" + 
				"<main-group>12</main-group>\r\n" + 
				"<subgroup>222</subgroup>\r\n" + 
				"<symbol-position>F</symbol-position>\r\n" + 
				"<classification-value>I</classification-value>\r\n" + 
				"<action-date><date>20170328</date></action-date>\r\n" + 
				"<generating-office><country>US</country></generating-office>\r\n" + 
				"<classification-status>B</classification-status>\r\n" + 
				"<classification-data-source>H</classification-data-source>\r\n" + 
				"<scheme-origination-code>C</scheme-origination-code>\r\n" + 
				"</classification-cpc>\r\n" + 
				"</main-cpc>\r\n" + 
				"<further-cpc>\r\n" + 
				"<classification-cpc>\r\n" + 
				"<cpc-version-indicator><date>20130101</date></cpc-version-indicator>\r\n" + 
				"<section>A</section>\r\n" + 
				"<class>01</class>\r\n" + 
				"<subclass>B</subclass>\r\n" + 
				"<main-group>71</main-group>\r\n" + 
				"<subgroup>08</subgroup>\r\n" + 
				"<symbol-position>L</symbol-position>\r\n" + 
				"<classification-value>I</classification-value>\r\n" + 
				"<action-date><date>20170328</date></action-date>\r\n" + 
				"<generating-office><country>US</country></generating-office>\r\n" + 
				"<classification-status>B</classification-status>\r\n" + 
				"<classification-data-source>H</classification-data-source>\r\n" + 
				"<scheme-origination-code>C</scheme-origination-code>\r\n" + 
				"</classification-cpc>\r\n" + 
				"<classification-cpc>\r\n" + 
				"<cpc-version-indicator><date>20130101</date></cpc-version-indicator>\r\n" + 
				"<section>B</section>\r\n" + 
				"<class>07</class>\r\n" + 
				"<subclass>B</subclass>\r\n" + 
				"<main-group>1</main-group>\r\n" + 
				"<subgroup>12</subgroup>\r\n" + 
				"<symbol-position>L</symbol-position>\r\n" + 
				"<classification-value>I</classification-value>\r\n" + 
				"<action-date><date>20170328</date></action-date>\r\n" + 
				"<generating-office><country>US</country></generating-office>\r\n" + 
				"<classification-status>B</classification-status>\r\n" + 
				"<classification-data-source>H</classification-data-source>\r\n" + 
				"<scheme-origination-code>C</scheme-origination-code>\r\n" + 
				"</classification-cpc>\r\n" + 
				"<classification-cpc>\r\n" + 
				"<cpc-version-indicator><date>20130101</date></cpc-version-indicator>\r\n" + 
				"<section>B</section>\r\n" + 
				"<class>07</class>\r\n" + 
				"<subclass>B</subclass>\r\n" + 
				"<main-group>1</main-group>\r\n" + 
				"<subgroup>526</subgroup>\r\n" + 
				"<symbol-position>L</symbol-position>\r\n" + 
				"<classification-value>I</classification-value>\r\n" + 
				"<action-date><date>20170328</date></action-date>\r\n" + 
				"<generating-office><country>US</country></generating-office>\r\n" + 
				"<classification-status>B</classification-status>\r\n" + 
				"<classification-data-source>H</classification-data-source>\r\n" + 
				"<scheme-origination-code>C</scheme-origination-code>\r\n" + 
				"</classification-cpc>\r\n" + 
				"<classification-cpc>\r\n" + 
				"<cpc-version-indicator><date>20130101</date></cpc-version-indicator>\r\n" + 
				"<section>B</section>\r\n" + 
				"<class>08</class>\r\n" + 
				"<subclass>B</subclass>\r\n" + 
				"<main-group>1</main-group>\r\n" + 
				"<subgroup>008</subgroup>\r\n" + 
				"<symbol-position>L</symbol-position>\r\n" + 
				"<classification-value>I</classification-value>\r\n" + 
				"<action-date><date>20170328</date></action-date>\r\n" + 
				"<generating-office><country>US</country></generating-office>\r\n" + 
				"<classification-status>B</classification-status>\r\n" + 
				"<classification-data-source>H</classification-data-source>\r\n" + 
				"<scheme-origination-code>C</scheme-origination-code>\r\n" + 
				"</classification-cpc>\r\n" + 
				"</further-cpc>\r\n" + 
				"</classifications-cpc></biblio></xml>";

		Document doc = DocumentHelper.parseText(xml);
		Set<PatentClassification> clazs = new ClassificationNode(doc).read();
		clazs.forEach(System.out::println);
	
		SortedSet<PatentClassification> cpcClazs = PatentClassification.filterByType(clazs, ClassificationType.CPC);
		Iterator<PatentClassification> it = cpcClazs.iterator();

		assertEquals(6, cpcClazs.size());
		 
		CpcClassification cpc1 = (CpcClassification) it.next();
		CpcClassification cpc2 = (CpcClassification) it.next();
		CpcClassification cpc3 = (CpcClassification) it.next();
		CpcClassification cpc4 = (CpcClassification) it.next();
		CpcClassification cpc5 = (CpcClassification) it.next();
		CpcClassification cpc6 = (CpcClassification) it.next();

		//cpcClazs.forEach(System.out::println);
		
		assertEquals("A01B 71/08", cpc1.getTextNormalized());
		assertFalse(cpc1.isMainOrInventive());

		assertEquals("A01F 12/222", cpc2.getTextNormalized());
		assertTrue(cpc2.isMainOrInventive());

		assertEquals("A01F 12/444", cpc3.getTextNormalized());
		assertTrue(cpc3.isMainOrInventive());

		assertEquals("B07B 1/12", cpc4.getTextNormalized());
		assertFalse(cpc4.isMainOrInventive());

		assertEquals("B07B 1/526", cpc5.getTextNormalized());
		assertFalse(cpc5.isMainOrInventive());

		assertEquals("B08B 1/008", cpc6.getTextNormalized());
		assertFalse(cpc6.isMainOrInventive());
	}

	// @Test FIXME
	public void IPC() throws DocumentException, ParseException {
		String xml = "<xml><biblio><classifications-ipcr>\r\n" + 
				"<classification-ipcr>\r\n" + 
				"<ipc-version-indicator><date>20060101</date></ipc-version-indicator>\r\n" + 
				"<classification-level>A</classification-level>\r\n" + 
				"<section>A</section>\r\n" + 
				"<class>01</class>\r\n" + 
				"<subclass>D</subclass>\r\n" + 
				"<main-group>41</main-group>\r\n" + 
				"<subgroup>12</subgroup>\r\n" + 
				"<symbol-position>F</symbol-position>\r\n" + 
				"<classification-value>I</classification-value>\r\n" + 
				"<action-date><date>20170328</date></action-date>\r\n" + 
				"<generating-office><country>US</country></generating-office>\r\n" + 
				"<classification-status>B</classification-status>\r\n" + 
				"<classification-data-source>H</classification-data-source>\r\n" + 
				"</classification-ipcr>\r\n" + 
				"<classification-ipcr>\r\n" + 
				"<ipc-version-indicator><date>20060101</date></ipc-version-indicator>\r\n" + 
				"<classification-level>A</classification-level>\r\n" + 
				"<section>A</section>\r\n" + 
				"<class>01</class>\r\n" + 
				"<subclass>F</subclass>\r\n" + 
				"<main-group>12</main-group>\r\n" + 
				"<subgroup>44</subgroup>\r\n" + 
				"<symbol-position>L</symbol-position>\r\n" + 
				"<classification-value>I</classification-value>\r\n" + 
				"<action-date><date>20170328</date></action-date>\r\n" + 
				"<generating-office><country>US</country></generating-office>\r\n" + 
				"<classification-status>B</classification-status>\r\n" + 
				"<classification-data-source>H</classification-data-source>\r\n" + 
				"</classification-ipcr>\r\n" + 
				"<classification-ipcr>\r\n" + 
				"<ipc-version-indicator><date>20060101</date></ipc-version-indicator>\r\n" + 
				"<classification-level>A</classification-level>\r\n" + 
				"<section>B</section>\r\n" + 
				"<class>07</class>\r\n" + 
				"<subclass>B</subclass>\r\n" + 
				"<main-group>1</main-group>\r\n" + 
				"<subgroup>12</subgroup>\r\n" + 
				"<symbol-position>L</symbol-position>\r\n" + 
				"<classification-value>I</classification-value>\r\n" + 
				"<action-date><date>20170328</date></action-date>\r\n" + 
				"<generating-office><country>US</country></generating-office>\r\n" + 
				"<classification-status>B</classification-status>\r\n" + 
				"<classification-data-source>H</classification-data-source>\r\n" + 
				"</classification-ipcr>\r\n" + 
				"<classification-ipcr>\r\n" + 
				"<ipc-version-indicator><date>20060101</date></ipc-version-indicator>\r\n" + 
				"<classification-level>A</classification-level>\r\n" + 
				"<section>B</section>\r\n" + 
				"<class>07</class>\r\n" + 
				"<subclass>B</subclass>\r\n" + 
				"<main-group>1</main-group>\r\n" + 
				"<subgroup>52</subgroup>\r\n" + 
				"<symbol-position>L</symbol-position>\r\n" + 
				"<classification-value>I</classification-value>\r\n" + 
				"<action-date><date>20170328</date></action-date>\r\n" + 
				"<generating-office><country>US</country></generating-office>\r\n" + 
				"<classification-status>B</classification-status>\r\n" + 
				"<classification-data-source>H</classification-data-source>\r\n" + 
				"</classification-ipcr>\r\n" + 
				"<classification-ipcr>\r\n" + 
				"<ipc-version-indicator><date>20060101</date></ipc-version-indicator>\r\n" + 
				"<classification-level>A</classification-level>\r\n" + 
				"<section>B</section>\r\n" + 
				"<class>08</class>\r\n" + 
				"<subclass>B</subclass>\r\n" + 
				"<main-group>1</main-group>\r\n" + 
				"<subgroup>00</subgroup>\r\n" + 
				"<symbol-position>L</symbol-position>\r\n" + 
				"<classification-value>I</classification-value>\r\n" + 
				"<action-date><date>20170328</date></action-date>\r\n" + 
				"<generating-office><country>US</country></generating-office>\r\n" + 
				"<classification-status>B</classification-status>\r\n" + 
				"<classification-data-source>H</classification-data-source>\r\n" + 
				"</classification-ipcr>\r\n" + 
				"<classification-ipcr>\r\n" + 
				"<ipc-version-indicator><date>20060101</date></ipc-version-indicator>\r\n" + 
				"<classification-level>A</classification-level>\r\n" + 
				"<section>A</section>\r\n" + 
				"<class>01</class>\r\n" + 
				"<subclass>B</subclass>\r\n" + 
				"<main-group>71</main-group>\r\n" + 
				"<subgroup>08</subgroup>\r\n" + 
				"<symbol-position>L</symbol-position>\r\n" + 
				"<classification-value>I</classification-value>\r\n" + 
				"<action-date><date>20170328</date></action-date>\r\n" + 
				"<generating-office><country>US</country></generating-office>\r\n" + 
				"<classification-status>B</classification-status>\r\n" + 
				"<classification-data-source>H</classification-data-source>\r\n" + 
				"</classification-ipcr>\r\n" + 
				"</classifications-ipcr></biblio></xml>";

		Document doc = DocumentHelper.parseText(xml);
		Set<PatentClassification> clazs = new ClassificationNode(doc).read();
		//clazs.forEach(System.out::println);

		SortedSet<PatentClassification> cpcClazs = PatentClassification.filterByType(clazs, ClassificationType.IPC);
		//cpcClazs.forEach(System.out::println);

		assertEquals(6, cpcClazs.size());

		Iterator<PatentClassification> it = cpcClazs.iterator();
		assertEquals("A01B 71/08", it.next().getTextNormalized());
		assertEquals("A01D 41/12", it.next().getTextNormalized());
		assertEquals("A01F 12/44", it.next().getTextNormalized());
		assertEquals("B07B 1/12", it.next().getTextNormalized());
		assertEquals("B07B 1/52", it.next().getTextNormalized());
		assertEquals("B08B 1/00", it.next().getTextNormalized());
	}

}
