package gov.uspto.document.model.classification.ipc;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import gov.uspto.patent.model.classification.CpcClassification;
import gov.uspto.patent.model.classification.IpcClassification;

public class IpcClassificationTest {

	private static Map<String, String> validFromTo = new LinkedHashMap<String, String>();
	static {
		validFromTo.put("C 07D 4 3/02", "C07D 403/02");
	}

	@Test(expected = ParseException.class)
	public void failBlank() throws ParseException {
		CpcClassification.fromText("");
	}

	@Test
	public void validParseCheck() throws ParseException {
		for (Entry<String,String> ipc: validFromTo.entrySet()){
			IpcClassification ipcClass = IpcClassification.fromText(ipc.getKey());
			assertEquals( ipc.getValue(), ipcClass.toTextNormalized());
		}
	}
}
