package gov.uspto.patent.model.entity;

import static org.junit.Assert.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.OrgSynonymGenerator;
import gov.uspto.patent.model.CountryCode;

public class NameOrgTest {

	private static Map<String, String> variations = new LinkedHashMap<String, String>();
	{
		variations.put("KNG","KNK"); // 3 letters, stay the same.
		variations.put("KN","KN"); // under 3 letters, stay the same.
		variations.put("K","K"); // 1 letter, no error.
		variations.put("",""); // under 1 letter, no error.
		variations.put("KONG Company","KNK");
		variations.put("Woodstream Corporation","WTS");
		variations.put("Meiji University","MJ-UN");
		variations.put("Battelle UK Limited","BTL-UK");
		variations.put("Mitsui AgriScience International", "MTS-AKR");
		variations.put("Glash\u00fctte", "GLK");
		variations.put("Berggiesshubel", "BRK");
		variations.put("Berggie\u00DFh\\u00fcbel", "BRK");
		variations.put("Meijir\u014d", "MJR");
		variations.put("OneHundred-TwentyTwo Company", "ONT-TNT");
		variations.put("United States President", "UNT-STS");
		variations.put("United States President Council", "UNT-STS");
		variations.put("The Regents of the University of California", "RKN-UNF");
		variations.put("School Supplies Inc", "SKL-SPL");
	}

	@Test
	public void initializeName() throws InvalidDataException {
		for(Entry<String, String> entry: variations.entrySet()) {
			NameOrg name = new NameOrg(entry.getKey());
			Entity entity = new Assignee(name, new Address("","", CountryCode.UNKNOWN));
			new OrgSynonymGenerator().computeSynonyms(entity);
			String abbrev = entity.getName().getInitials();
			assertEquals(entry.getValue(), abbrev);
		}
	}

	@Test
	public void getShortestSynonym() throws InvalidDataException {
		NameOrg name = new NameOrg("School Supplies Inc");
		name.addSynonym("School Supplies");
		name.addSynonym("School Supplies Incorperated");
		String actual = name.getShortestSynonym();
		assertEquals("School Supplies", actual);
	}

	@Test
	public void getLongestSynonym() throws InvalidDataException {
		NameOrg name = new NameOrg("School Supplies Inc");
		name.addSynonym("School Supplies");
		name.addSynonym("School Supplies Incorperated");
		String actual = name.getLongestSynonym();
		assertEquals("School Supplies Incorperated", actual);
	}
}
