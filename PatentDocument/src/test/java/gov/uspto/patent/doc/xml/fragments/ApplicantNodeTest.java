package gov.uspto.patent.doc.xml.fragments;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.junit.Test;

import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.entity.Applicant;
import gov.uspto.patent.model.entity.Inventor;
import gov.uspto.patent.model.entity.NameOrg;
import gov.uspto.patent.model.entity.NamePerson;

public class ApplicantNodeTest {

	@Test
	public void ApplicantOrg() throws DocumentException {
		String xml = "<us-parties><us-applicants>\r\n" + 
				"<us-applicant app-type=\"applicant\" designation=\"us-only\" sequence=\"001\">\r\n" + 
				"<addressbook>\r\n" + 
				"<orgname>JACQUET DOE DISTRIBUTION</orgname>\r\n" + 
				"<address>\r\n" + 
				"<city>Paris</city>\r\n" + 
				"<country>FR</country>\r\n" + 
				"</address>\r\n" + 
				"</addressbook>\r\n" + 
				"<residence>\r\n" + 
				"<country>FR</country>\r\n" + 
				"</residence>\r\n" + 
				"</us-applicant>\r\n" + 
				"</us-applicants></us-parties>";

		Document doc = DocumentHelper.parseText(xml);

		List<Applicant> appls = new ApplicantNode(doc).read();
		//appls.forEach(System.out::println);

		assertEquals("JACQUET DOE DISTRIBUTION", ((NameOrg) appls.get(0).getName()).getName());
		assertEquals("Jacquet Doe Distribution", ((NameOrg) appls.get(0).getName()).getNameNormalizeCase());
		assertEquals(CountryCode.FR, appls.get(0).getAddress().getCountry());
		assertEquals("Paris", appls.get(0).getAddress().getCity());
	}

	@Test
	public void ApplicantInventor() throws DocumentException {
		String xml = "<us-parties><us-applicants>\r\n" + 
				"<us-applicant app-type=\"applicant-inventor\" applicant-authority-category=\"inventor\" designation=\"us-only\" sequence=\"001\">\r\n" + 
				"<addressbook>\r\n" + 
				"<last-name>Doe</last-name>\r\n" + 
				"<first-name>John Steven</first-name>\r\n" + 
				"<address>\r\n" + 
				"<city>Torrance</city>\r\n" + 
				"<state>CA</state>\r\n" + 
				"<country>US</country>\r\n" + 
				"</address>\r\n" + 
				"</addressbook>\r\n" + 
				"<residence>\r\n" + 
				"<country>US</country>\r\n" + 
				"</residence>\r\n" + 
				"</us-applicant>\r\n" + 
				"<us-applicant app-type=\"applicant-inventor\" applicant-authority-category=\"inventor\" designation=\"us-only\" sequence=\"002\">\r\n" + 
				"<addressbook>\r\n" + 
				"<last-name>Hatake</last-name>\r\n" + 
				"<first-name>Hiroyuki</first-name>\r\n" + 
				"<address>\r\n" + 
				"<city>Tokyo</city>\r\n" + 
				"<country>JP</country>\r\n" + 
				"</address>\r\n" + 
				"</addressbook>\r\n" + 
				"<residence>\r\n" + 
				"<country>JP</country>\r\n" + 
				"</residence>\r\n" + 
				"</us-applicant></us-applicants></us-parties>";

		Document doc = DocumentHelper.parseText(xml);

		List<Applicant> appls = new ApplicantNode(doc).read();
		//appls.forEach(System.out::println);

		assertEquals("Doe", ((NamePerson) appls.get(0).getName()).getLastName());
		assertEquals("John Steven", ((NamePerson) appls.get(0).getName()).getFirstName());
		assertEquals(CountryCode.US, appls.get(0).getAddress().getCountry());
		assertEquals("CA", appls.get(0).getAddress().getState());
		assertEquals("Torrance", appls.get(0).getAddress().getCity());

		assertEquals("Hatake", ((NamePerson) appls.get(1).getName()).getLastName());
		assertEquals("Hiroyuki", ((NamePerson) appls.get(1).getName()).getFirstName());
		assertEquals(CountryCode.JP, appls.get(1).getAddress().getCountry());
		assertEquals("Tokyo", appls.get(1).getAddress().getCity());

		
		List<Inventor> inventors = new InventorNode(doc).read();
		//inventors.forEach(System.out::println);

		assertEquals("Doe", ((NamePerson) inventors.get(0).getName()).getLastName());
		assertEquals("John Steven", ((NamePerson) inventors.get(0).getName()).getFirstName());
		assertEquals(CountryCode.US, inventors.get(0).getAddress().getCountry());
		assertEquals("CA", inventors.get(0).getAddress().getState());
		assertEquals("Torrance", inventors.get(0).getAddress().getCity());

		assertEquals("Hatake", ((NamePerson) inventors.get(1).getName()).getLastName());
		assertEquals("Hiroyuki", ((NamePerson) inventors.get(1).getName()).getFirstName());
		assertEquals(CountryCode.JP, inventors.get(1).getAddress().getCountry());
		assertEquals("Tokyo", inventors.get(1).getAddress().getCity());
	}	
	
}
