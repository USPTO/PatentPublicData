package gov.uspto.patent.model.classification;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import gov.uspto.patent.model.classification.IpcClassification;

public class IpcClassificationTest {

	private static Map<String, String> validFromTo = new LinkedHashMap<String, String>();
	static {
		validFromTo.put("C 07D 4 3/02", "C07D 403/02");
		validFromTo.put("H01S 3/00", "H01S 3/00");
		validFromTo.put("A47L 700", "A47L 7/00");
	}

	@Test(expected = ParseException.class)
	public void failBlank() throws ParseException {		
		IpcClassification ipc = new IpcClassification();
		ipc.parseText("");
	}

	@Test
	public void validParseCheck() throws ParseException {
		for (Entry<String,String> check: validFromTo.entrySet()){
			IpcClassification ipc = new IpcClassification();
			ipc.parseText(check.getKey());
			assertEquals( check.getValue(), ipc.getTextNormalized());
		}
	}
}
