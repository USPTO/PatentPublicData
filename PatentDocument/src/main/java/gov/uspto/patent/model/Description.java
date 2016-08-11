package gov.uspto.patent.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Patent Description
 * 
 *<p><ul>
 *There are three to four sections to a Patent Description:
 *<li>Other Patent Relations "RELAPP"
 *<li>Brief Summary "BRFSUM"
 *<li>Brief Description of Drawings "DRWDESC"
 *<li>Detailed Description "DETDESC"
 *</ul></p>
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

	public String getAllRawText() {
		StringBuilder stb = new StringBuilder();

		for (DescriptionSection sec : sections) {
			stb.append(sec.getRawText()).append("\n");
		}

		return stb.toString();
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

	public String getAllPlainText() {
		StringBuilder stb = new StringBuilder();

		for (DescriptionSection sec : sections) {
			stb.append(sec.getPlainText()).append("\n");
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
				stb.append(sec.getSimpleHtml()).append("\n");
		}

		return stb.toString();
	}

	
	public String getSimpleHtml(DescSection... descSections) {
		StringBuilder stb = new StringBuilder();

		for (DescSection decSec : descSections) {
			for (DescriptionSection sec : sections) {
				if (sec.getSection().equals(decSec)) {
					stb.append(sec.getSimpleHtml()).append("\n");
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
