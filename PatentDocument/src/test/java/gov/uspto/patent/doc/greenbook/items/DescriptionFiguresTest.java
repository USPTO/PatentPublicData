package gov.uspto.patent.doc.greenbook.items;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import gov.uspto.patent.model.Figure;

public class DescriptionFiguresTest {

	@Test
	public void extractFigDesc1() {
		String pargraphText = "FIG. 1 is a partial perspective view of a person wearing a suitcoat.";
		List<Figure> figures = new ArrayList<Figure>();
		DescriptionFigures.findFigures(pargraphText, figures);
		Figure fig1 = figures.get(0);
		Set<String> ids = fig1.getIds();
		String fig1Text = fig1.getRawText();
		assertEquals("FIG. 1", ids.iterator().next());
		assertEquals("is a partial perspective view of a person wearing a suitcoat.", fig1Text);
	}

	@Test
	public void extractFigDesc2() {
		String pargraphText = "Image 1 is a partial perspective view of a person wearing a suitcoat.";
		List<Figure> figures = new ArrayList<Figure>();
		DescriptionFigures.findFigures(pargraphText, figures);
		assertTrue(figures.isEmpty());
	}

	@Test
	public void extractFigDesc3() {
		String pargraphText = "";
		List<Figure> figures = new ArrayList<Figure>();
		DescriptionFigures.findFigures(pargraphText, figures);
		assertTrue(figures.isEmpty());
	}

	@Test
	public void extractFigDesc4() {
		String pargraphText = "FIG. 1";
		List<Figure> figures = new ArrayList<Figure>();
		DescriptionFigures.findFigures(pargraphText, figures);
		assertTrue(figures.isEmpty());
	}
	
	@Test
	public void mentionalAnother() {
		String pargraphText = "FIG. 3 is a cross-sectional view similar to that shown in FIG. 2 wherein the";
		List<Figure> figures = new ArrayList<Figure>();
		DescriptionFigures.findFigures(pargraphText, figures);
		Figure fig1 = figures.get(0);
		Set<String> ids = fig1.getIds();
		String fig1Text = fig1.getRawText();
		assertEquals("FIG. 3", ids.iterator().next());
		assertEquals("is a cross-sectional view similar to that shown in FIG. 2 wherein the", fig1Text);
	}
}
