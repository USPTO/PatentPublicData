package gov.uspto.patent.doc.xml.fragments;

import static org.junit.Assert.*;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.junit.Test;

import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.entity.Inventor;
import gov.uspto.patent.model.entity.NamePerson;

public class InventorNodeTest {

	@Test
	public void Inventors() throws DocumentException {
		String xml = "<us-parties><inventors>\r\n" + 
				"<inventor designation=\"us-only\" sequence=\"001\">\r\n" + 
				"<addressbook>\r\n" + 
				"<last-name>Doe</last-name>\r\n" + 
				"<first-name>John</first-name>\r\n" + 
				"<address>\r\n" + 
				"<city>Portland</city>\r\n" + 
				"<state>OR</state>\r\n" + 
				"<country>US</country>\r\n" + 
				"</address>\r\n" + 
				"</addressbook>\r\n" + 
				"</inventor>\r\n" + 
				"<inventor designation=\"us-only\" sequence=\"002\">\r\n" + 
				"<addressbook>\r\n" + 
				"<last-name>Smith</last-name>\r\n" + 
				"<first-name>John Paul</first-name>\r\n" + 
				"<address>\r\n" + 
				"<city>Albany</city>\r\n" + 
				"<state>OR</state>\r\n" + 
				"<country>US</country>\r\n" + 
				"</address>\r\n" + 
				"</addressbook>\r\n" + 
				"</inventor></inventors></us-parties>";

		Document doc = DocumentHelper.parseText(xml);
		List<Inventor> inventors = new InventorNode(doc).read();

		assertEquals("Doe", ((NamePerson) inventors.get(0).getName()).getLastName());
		assertEquals(CountryCode.US, inventors.get(0).getAddress().getCountry());
		assertEquals("OR", inventors.get(0).getAddress().getState());
		assertEquals("Portland", inventors.get(0).getAddress().getCity());

		assertEquals("Smith", ((NamePerson) inventors.get(1).getName()).getLastName());
		assertEquals(CountryCode.US, inventors.get(1).getAddress().getCountry());
		assertEquals("OR", inventors.get(1).getAddress().getState());
		assertEquals("Albany", inventors.get(1).getAddress().getCity());
	}

}
