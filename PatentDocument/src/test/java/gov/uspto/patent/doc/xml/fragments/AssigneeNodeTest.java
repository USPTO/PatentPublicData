package gov.uspto.patent.doc.xml.fragments;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.junit.Test;

import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.entity.Assignee;
import gov.uspto.patent.model.entity.NameOrg;
import gov.uspto.patent.model.entity.NamePerson;

public class AssigneeNodeTest {

	@Test
	public void AssigneeOrg() throws DocumentException {
		String xml ="<us-patent-grant><us-bibliographic-data-grant><assignees>\r\n" + 
				"<assignee>\r\n" + 
				"<addressbook>\r\n" + 
				"<orgname>Company, Incorporated</orgname>\r\n" + 
				"<role>02</role>\r\n" + 
				"<address>\r\n" + 
				"<city>McLean</city>\r\n" + 
				"<state>VA</state>\r\n" + 
				"<country>US</country>\r\n" + 
				"</address>\r\n" + 
				"</addressbook>\r\n" + 
				"</assignee>\r\n" + 
				"</assignees></us-bibliographic-data-grant></us-patent-grant>";

		Document doc = DocumentHelper.parseText(xml);

		List<Assignee> assignees = new AssigneeNode(doc).read();
		//assignees.forEach(System.out::println);

		assertEquals(1, assignees.size());

		assertEquals(Assignee.RoleType.T2, assignees.get(0).getRole());
		assertEquals("U.S. company or corporation", assignees.get(0).getRoleDesc());

		assertEquals("Company, Incorporated", ((NameOrg) assignees.get(0).getName()).getName());
		
		assertEquals(CountryCode.US, assignees.get(0).getAddress().getCountry());
		assertEquals("McLean", assignees.get(0).getAddress().getCity());
		assertEquals("VA", assignees.get(0).getAddress().getState());
	}

	@Test
	public void AssigneePerson() throws DocumentException {
		String xml ="<us-patent-grant><us-bibliographic-data-grant><assignees>\r\n" + 
				"<assignee>\r\n" + 
				"<addressbook>\r\n" + 
				"<last-name>Doe</last-name>\r\n" + 
				"<first-name>John</first-name>\r\n" + 
				"<role>04</role>\r\n" + 
				"<address>\r\n" + 
				"<city>Orlando</city>\r\n" + 
				"<state>FL</state>\r\n" + 
				"<country>US</country>\r\n" + 
				"</address>\r\n" + 
				"</addressbook>\r\n" + 
				"</assignee>\r\n" + 
				"</assignees></us-bibliographic-data-grant></us-patent-grant>";

		Document doc = DocumentHelper.parseText(xml);

		List<Assignee> assignees = new AssigneeNode(doc).read();
		//assignees.forEach(System.out::println);

		assertEquals(1, assignees.size());
		
		assertEquals(Assignee.RoleType.T4, assignees.get(0).getRole());
		assertEquals("U.S individual", assignees.get(0).getRoleDesc());

		assertEquals("John", ((NamePerson) assignees.get(0).getName()).getFirstName());
		assertEquals("Doe", ((NamePerson) assignees.get(0).getName()).getLastName());

		assertEquals(CountryCode.US, assignees.get(0).getAddress().getCountry());
		assertEquals("Orlando", assignees.get(0).getAddress().getCity());
		assertEquals("FL", assignees.get(0).getAddress().getState());
	}	

}
