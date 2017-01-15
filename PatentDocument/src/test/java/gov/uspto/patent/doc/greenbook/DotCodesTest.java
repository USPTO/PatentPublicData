package gov.uspto.patent.doc.greenbook;

import static org.junit.Assert.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

public class DotCodesTest {

	private static Map<String, String> dotCodes = new LinkedHashMap<String, String>();
	static {
		dotCodes.put("Java (.TM.)", "Java (\u8482)");
		dotCodes.put(".alpha. .beta. .-+. .0.", "\u03B1 \u03B2 \u2213 \u00F8");
	}
	
	private static Map<String, String> dotSubCodes = new LinkedHashMap<String, String>();
	static {
		dotSubCodes.put("h.sub.2.O", "h<sub>2</sub>O");
		dotSubCodes.put("D.sub.2, D.sub.3, D.sub.4 .", "D<sub>2</sub>, D<sub>3</sub>, D<sub>4</sub> .");
		dotSubCodes.put("between D.sub.13 and D.sub.12, ", "between D<sub>13</sub> and D<sub>12</sub>, ");
		dotSubCodes.put(" (D.sub.n-1), (D.sub.n-2)", " (D<sub>n-1</sub>), (D<sub>n-2</sub>)");
		dotSubCodes.put(" (D.sub.n-1), (D.sub.n-2)", " (D<sub>n-1</sub>), (D<sub>n-2</sub>)");
		dotSubCodes.put("QUERY.sub.-- STRING PATH.sub.-- INFO", "QUERY<sub>--</sub> STRING PATH<sub>--</sub> INFO");
	}

	@Test
	public void replace(){
		for (Entry<String,String> check: dotCodes.entrySet()){
			String actual = DotCodes.replace(check.getKey());
			assertEquals( check.getValue(), actual);
		}
	}
	
	@Test
	public void replaceSubSupHTML() {
		for (Entry<String,String> check: dotSubCodes.entrySet()){
			String actual = DotCodes.replaceSubSupHTML(check.getKey());
			assertEquals( check.getValue(), actual);
		}
	}

}
