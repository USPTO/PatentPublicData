package gov.uspto.patent.doc.xml.fragments;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.junit.Test;

import gov.uspto.patent.model.entity.Agent;
import gov.uspto.patent.model.entity.AgentRepType;
import gov.uspto.patent.model.entity.NameOrg;
import gov.uspto.patent.model.entity.NamePerson;

public class AgentNodeTest {

	@Test
	public void Agents() throws DocumentException {
		String xml ="<us-parties><agents>\r\n" +
				"<agent rep-type=\"attorney\" sequence=\"01\">\r\n" + 
				"<addressbook>\r\n" + 
				"<last-name>Smith</last-name>\r\n" + 
				"<first-name>Jane W.</first-name>\r\n" + 
				"<address>\r\n" + 
				"<country>unknown</country>\r\n" + 
				"</address>\r\n" + 
				"</addressbook>\r\n" + 
				"</agent>\r\n" + 
				"<agent rep-type=\"attorney\" sequence=\"02\">\r\n" + 
				"<addressbook>\r\n" + 
				"<orgname>Doe, John LLC</orgname>\r\n" + 
				"<address>\r\n" + 
				"<country>unknown</country>\r\n" + 
				"</address>\r\n" + 
				"</addressbook>\r\n" + 
				"</agent>\r\n" + 
				"</agents></us-parties>\r\n";

		Document doc = DocumentHelper.parseText(xml);

		List<Agent> agents = new AgentNode(doc).read();
		//agents.forEach(System.out::println);

		assertEquals("Smith", ((NamePerson) agents.get(0).getName()).getLastName());
		assertEquals("Jane W.", ((NamePerson) agents.get(0).getName()).getFirstName());
		assertEquals(AgentRepType.ATTORNEY, agents.get(0).getRepType());

		assertEquals("Doe, John LLC", ((NameOrg) agents.get(1).getName()).getName());
		assertEquals(AgentRepType.ATTORNEY, agents.get(1).getRepType());
	}

}
