package gov.uspto.patent.doc.xml.fragments;

import static org.junit.Assert.*;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.junit.Test;

import gov.uspto.patent.model.ExaminerType;
import gov.uspto.patent.model.entity.Examiner;
import gov.uspto.patent.model.entity.NamePerson;

public class ExaminerNodeTest {

	@Test
	public void Examiner() throws DocumentException {
		String xml = "<xml><us-bibliographic-data-grant><examiners><primary-examiner>\r\n" + 
				"<last-name>Doe</last-name>\r\n" + 
				"<first-name>John</first-name>\r\n" + 
				"<department>1711</department>\r\n" + 
				"</primary-examiner>\r\n" + 
				"<assistant-examiner>\r\n" + 
				"<last-name>Smith</last-name>\r\n" + 
				"<first-name>Benjamin L</first-name>\r\n" + 
				"</assistant-examiner>\r\n" + 
				"</examiners></us-bibliographic-data-grant></xml>";

		Document doc = DocumentHelper.parseText(xml);

		List<Examiner> examiners = new ExaminerNode(doc).read();
		//examiners.forEach(System.out::println);

		assertEquals("Doe", ((NamePerson) examiners.get(0).getName()).getLastName());
		assertEquals("1711", examiners.get(0).getDepartment());
		assertEquals(ExaminerType.PRIMARY, examiners.get(0).getExaminerType());

		assertEquals("Smith", ((NamePerson) examiners.get(1).getName()).getLastName());
		assertEquals("1711", examiners.get(1).getDepartment());
		assertEquals(ExaminerType.ASSISTANT, examiners.get(1).getExaminerType());
	}

}
