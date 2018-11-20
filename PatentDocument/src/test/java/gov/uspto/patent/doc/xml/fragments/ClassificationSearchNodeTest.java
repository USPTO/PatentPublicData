package gov.uspto.patent.doc.xml.fragments;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.junit.Test;

import gov.uspto.patent.model.classification.ClassificationType;
import gov.uspto.patent.model.classification.PatentClassification;

public class ClassificationSearchNodeTest {

	@Test
	public void test() throws DocumentException {
		String xml = "<xml><us-field-of-classification-search>\r\n" + 
				"<classification-national>\r\n" + 
				"<country>US</country>\r\n" + 
				"<main-classification>D 1100-130</main-classification>\r\n" + 
				"<additional-info>unstructured</additional-info>\r\n" + 
				"</classification-national>\r\n" + 
				"<classification-national>\r\n" + 
				"<country>US</country>\r\n" + 
				"<main-classification>D 1199</main-classification>\r\n" + 
				"</classification-national>\r\n" + 
				"<classification-cpc-text>A21C 3/08</classification-cpc-text>\r\n" + 
				"<classification-cpc-text>A21C 11/12</classification-cpc-text>\r\n" + 
				"<classification-cpc-text>A21C 3/00</classification-cpc-text>\r\n" + 
				"<classification-cpc-text>A21C 11/163</classification-cpc-text>\r\n" + 
				"<classification-cpc-text>A21D 13/41</classification-cpc-text>\r\n" + 
				"<classification-cpc-text>A21D 13/00</classification-cpc-text>\r\n" + 
				"<classification-cpc-text>A21D 13/19</classification-cpc-text>\r\n" + 
				"</us-field-of-classification-search></xml>";

		Document doc = DocumentHelper.parseText(xml);
		Set<PatentClassification> clazs = new ClassificationSearchNode(doc).read();
		//clazs.forEach(System.out::println);

		SortedSet<PatentClassification> uspcClazs = PatentClassification.filterByType(clazs, ClassificationType.USPC);
		assertEquals("D 1100-130", uspcClazs.iterator().next().getTextOriginal());

		SortedSet<PatentClassification> cpcClazs = PatentClassification.filterByType(clazs, ClassificationType.CPC);
		assertEquals(7, cpcClazs.size());
	}

}
