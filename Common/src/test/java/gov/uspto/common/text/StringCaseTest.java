package gov.uspto.common.text;

import static org.junit.Assert.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import gov.uspto.common.text.StringCaseUtil;

public class StringCaseTest {

	private static Map<String, String> TitleValidFromTo = new LinkedHashMap<String, String>();
	static {
		TitleValidFromTo.put("THE PHONE WITH BOOK", "The Phone with Book");
		TitleValidFromTo.put("FOR THE PRESEDENT OF THE CLUB", "For the Presedent of the Club");
		TitleValidFromTo.put("PRESENT FOR TALKS", "Present for Talks");
		TitleValidFromTo.put("THE COMPUTER/LAPTOP FOR THE WORK GROUP", "The Computer/Laptop for the Work Group");
		TitleValidFromTo.put("THE COMPUTER-LAPTOP FOR THE WORK GROUP", "The Computer-Laptop for the Work Group");
	}

	@Test
	public void capitalizeFirstLetterTestString() {
		assertEquals("Computer", StringCaseUtil.capitalizeFirstLetter("computer"));
	}

	@Test
	public void capitalizeFirstLetterTestArray() {
		assertEquals(new String[]{"Computer", "Laptop"}, StringCaseUtil.capitalizeFirstLetter(new String[]{"computer", "laptop"}));
	}

	@Test
	public void TitleCaseTest(){
		for (Entry<String,String> validFromTo: TitleValidFromTo.entrySet()){
			assertEquals( validFromTo.getValue(), StringCaseUtil.toTitleCase(validFromTo.getKey()));
		}
	}

}
