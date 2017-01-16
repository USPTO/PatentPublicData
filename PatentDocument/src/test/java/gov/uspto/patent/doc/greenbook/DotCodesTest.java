package gov.uspto.patent.doc.greenbook;

import static org.junit.Assert.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

public class DotCodesTest {

	private static Map<String, String> dotCodes = new LinkedHashMap<String, String>();
	static {
		dotCodes.put(".-+.", "\u2213");
		dotCodes.put(".dblquote.", "\"");
		dotCodes.put("Java (.TM.)", "Java (\u2122)");
		dotCodes.put(".alpha. .beta. .-+. .0.", "\u03B1 \u03B2 \u2213 \u2205");
		dotCodes.put("CTY Verneuil.En.Halatte", "CTY Verneuil\u2025Halatte");
		dotCodes.put("endo-.alpha.-1", "endo-\u03B1-1");
		dotCodes.put("6-.beta.-glucanase", "6-\u03B2-glucanase");
	}

	private static Map<String, String> dotSubCodes = new LinkedHashMap<String, String>();
	static {
		dotSubCodes.put("h.sub.2.O", "h<sub>2</sub>O");
		dotSubCodes.put("D.sub.2, D.sub.3, D.sub.4 .", "D<sub>2</sub>, D<sub>3</sub>, D<sub>4</sub> .");
		dotSubCodes.put("between D.sub.13 and D.sub.12, ", "between D<sub>13</sub> and D<sub>12</sub>, ");
		dotSubCodes.put(" (D.sub.n-1), (D.sub.n-2)", " (D<sub>n-1</sub>), (D<sub>n-2</sub>)");
		dotSubCodes.put("QUERY.sub.-- STRING PATH.sub.-- INFO", "QUERY<sub>--</sub> STRING PATH<sub>--</sub> INFO");
		dotSubCodes.put(" .sub.3/4 ", " <sub>3/4</sub> ");
		dotSubCodes.put(".sup.\" ", "<sup>\"</sup> ");
		dotSubCodes.put(".sup.( ", "<sup>(</sup> ");
		dotSubCodes.put(".sup.1+2 ", "<sup>1+2</sup> ");
	}

	private static Map<String, String> combined = new LinkedHashMap<String, String>();
	static {
		combined.put(".sup..TM. ", "<sup>\u2122</sup> ");
		combined.put(".sup..alpha. ", "<sup>\u03B1</sup> ");
		combined.put(".sup..fwdarw. ", "<sup>\u2192</sup> ");
	}

	@Test
	public void replace() {
		for (Entry<String, String> check : dotCodes.entrySet()) {
			String actual = DotCodes.replace(check.getKey());
			assertEquals(check.getValue(), actual);
		}
	}

	@Test
	public void replaceSubSupHTML() {
		for (Entry<String, String> check : dotSubCodes.entrySet()) {
			String actual = DotCodes.replaceSubSupHTML(check.getKey());
			assertEquals(check.getValue(), actual);
		}
	}

	@Test
	public void combined() {
		for (Entry<String, String> check : combined.entrySet()) {
			String update1 = DotCodes.replace(check.getKey());
			String update2 = DotCodes.replaceSubSupHTML(update1);
			assertEquals(check.getValue(), update2);
		}
	}

}
