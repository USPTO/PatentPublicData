package gov.uspto.patent.doc.greenbook.fragments;

import static org.junit.Assert.*;

import org.junit.Test;

public class ClassificationSearchNodeTest {

	@Test
	public void subclass_dash_dot() {
		ClassificationSearchNode classSearch = new ClassificationSearchNode(null);
		String actual = classSearch.normSubClass("15-15.8");
		String expect = "015-015.8";
		assertEquals(expect, actual);
	}

	@Test
	public void subclass_slash() {
		ClassificationSearchNode classSearch = new ClassificationSearchNode(null);
		String actual = classSearch.normSubClass("21/00");
		String expect = "021/00";
		assertEquals(expect, actual);
	}	

}
