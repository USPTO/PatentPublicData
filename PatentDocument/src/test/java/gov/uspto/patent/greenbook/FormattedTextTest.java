package gov.uspto.patent.greenbook;

import static org.junit.Assert.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

public class FormattedTextTest {

	private static Map<String, String> validFromTo = new LinkedHashMap<String, String>();
	static {
		validFromTo.put("in claim 1 or 2", "in Patent-Claim");
		validFromTo.put("of claim 2-3", "of Patent-Claim");
		validFromTo.put("PAR  FIG. 1 is a side elevational view", "PAR  Patent-Figure is a side elevational view");
		validFromTo.put("PAR  FIG. 3A is a perspective view", "PAR  Patent-Figure is a perspective view");
		validFromTo.put("PAR  FIG. 1a-1c is a side view", "PAR  Patent-Figure is a side view");
		validFromTo.put("PAR  FIGS. 1-2 are top views", "PAR  Patent-Figure are top views");
		//validFromTo.put("illustrated in FIGS. 2 to 5.", "illustrated in Patent-Figure.");
		//validFromTo.put("as shown in FIG. 1(b) and may be", "as shown in Patent-Figure and may be");
		//validFromTo.put("PAR  FIGS. 1(a) and 1(b) are graphs showing", "PAR  Patent-Figure are graphs showing");
		//validFromTo.put("current shown in FIG. 1(a) is", "current shown in Patent-Figure is");
	}

	@Test
	public void testCleanText() {
		FormattedText format = new FormattedText();
		for (Entry<String,String> entry: validFromTo.entrySet()){
			String actual = format.cleanText(entry.getKey());
			assertEquals( entry.getValue(), actual);
		}
	}

}
