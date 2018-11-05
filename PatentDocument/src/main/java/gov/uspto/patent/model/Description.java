package gov.uspto.patent.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import gov.uspto.patent.doc.simplehtml.FreetextConfig;

/**
 * Patent Description
 * 
 * <p>
 * <ul>
 * These are the common sections, and the order they appear within the Patent
 * Description. Any number of the following may or may not appear, since it is
 * up to the patent drafter and the type of patent. Design Patents often contain
 * nothing more than a single sentence, to reference the included drawings.
 * 
 * <li>Other Patent Relations "RELAPP"
 * <li>Brief Summary "BRFSUM"
 * <li>Brief Description of Drawings "DRWDESC"
 * <li>Detailed Description "DETDESC"
 * </ul>
 * </p>
 *
 */
public class Description {

	private List<DescriptionSection> sections = new ArrayList<DescriptionSection>();
	private List<Figure> figures = new ArrayList<Figure>(); // from Brief Description of Drawings "DRWDESC"

	public List<DescriptionSection> getSections() {
		return sections;
	}

	public List<Figure> getFigures() {
		return figures;
	}

	public void addFigures(Collection<Figure> figures) {
		this.figures.addAll(figures);
	}

	public Figure getFigure(String id) {
		for (Figure fig : figures) {
			if (fig.hasId(id)) {
				return fig;
			}
		}
		return null;
	}

	public DescriptionSection getSection(DescSection section) {
		for (DescriptionSection sec : sections) {
			if (sec.getSection().equals(section)) {
				return sec;
			}
		}
		return null;
	}

	public void addSection(DescriptionSection section) {
		sections.add(section);
	}

	public void setSections(List<DescriptionSection> sections) {
		this.sections = sections;
	}

	public DescSection[] getSectionsAvailable() {
		DescSection[] descSections = new DescSection[sections.size()];
		for (int i = 0; i > sections.size(); i++) {
			descSections[i] = sections.get(i).getSection();
		}
		return descSections;
	}

	public String getAllRawText() {
		return getRawText(getSectionsAvailable());
	}

	public String getRawText(DescSection... descSections) {
		StringBuilder stb = new StringBuilder();

		for (DescSection decSec : descSections) {
			for (DescriptionSection sec : sections) {
				if (sec.getSection().equals(decSec)) {
					stb.append(sec.getRawText()).append("\n");
				}
			}
		}

		return stb.toString();
	}

	public String getAllPlainText(FreetextConfig config) {
		return getPlainText(config, getSectionsAvailable());
	}

	public String getAllPlainText() {
		StringBuilder stb = new StringBuilder();

		for (DescriptionSection sec : sections) {
			stb.append(sec.getPlainText()).append("\n");
		}

		return stb.toString();
	}

	public String getPlainText(FreetextConfig config, DescSection... descSections) {
		StringBuilder stb = new StringBuilder();

		for (DescSection decSec : descSections) {
			for (DescriptionSection sec : sections) {
				if (sec.getSection().equals(decSec)) {
					stb.append(sec.getPlainText(config));
					if (config.isPrettyPrint()) {
						stb.append("\n");
					} else {
						stb.append("\\n");
					}
				}
			}
		}

		return stb.toString();
	}

	public String getPlainText(DescSection... descSections) {
		StringBuilder stb = new StringBuilder();

		for (DescSection decSec : descSections) {
			for (DescriptionSection sec : sections) {
				if (sec.getSection().equals(decSec)) {
					stb.append(sec.getPlainText()).append("\n");
				}
			}
		}

		return stb.toString();
	}

	public String getSimpleHtml() {		
		StringBuilder stb = new StringBuilder();

		for (DescriptionSection sec : sections) {
			stb.append(sec.getSimpleHtml());
		}

		return stb.toString();
	}

	public String getSimpleHtml(DescSection... descSections) {
		StringBuilder stb = new StringBuilder();

		for (DescSection decSec : descSections) {
			for (DescriptionSection sec : sections) {
				if (sec.getSection().equals(decSec)) {
					stb.append(sec.getSimpleHtml());
				}
			}
		}

		return stb.toString();
	}

	@Override
	public String toString() {
		return "Description [sections=" + sections + ", figures=" + figures + "]";
	}

}
