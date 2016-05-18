package gov.uspto.patent.model.classification;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *<h3>Cooperative Patent Classification</h3>
 *<p>
 * Partnership between EPO and USPTO, as a joint effort to develop a common, 
 * internationally compatible classification system  for technical documents, 
 * in particular patent publications, which will be used by both offices in the patent granting process.
 *</p>
 *<p>
 * Classification code/symbol format same as IPC, but the groups differ in areas.
 * The CPC has many more subdivisions than the IPC, that is, many more subgroups. These internal subgroups
 * start with the symbol of an IPC group (not necessarily of the current edition), followed by a further
 * combination of numbers. In some cases, an IPC group is not used in the CPC structure. In this situation,
 * the IPC group will not be present in the CPC structure. 
 *</p>
 *<p> 
 * The CPC is substantially based on the previous European classification system (ECLA) of which
 * closely mirrored the IPC Classification System.  
 *</p>
 *<li>1 January 2013, EPO replaced European Classification (ECLA).
 *<li>1 January 2015, USPTO replaced most of the USPC System.
 *
 *<p>
 *<h3>Structure Breakout: "A01B33/00"</h3>
 *<li>Section A (one letter A-H and Y)
 *<li>Class 01 (two digits)
 *<li>Sub Class B (1-3 digits)
 *<li>Main Group 33 (1-3 digits)
 *<li>Sub group 00 (at least 2 digits; CPC specific since the IPC does not have subgroups)
 *</p>
 *</br>
 *<p>
 *<h3>Parse classification string:</h3>
 *<pre>
 * {@code
 * CpcClassification cpc = CpcClassification.fromText(originalText);
 * cpc.setIsMainClassification(true);
 * }
 *</pre>
 *</p>
 *
 *<p>
 *<h3>Create Classification by its individual parts:</h3>
 *<pre>
 * {@code
 * CpcClassification cpc = new CpcClassification(originalText);
 * cpc.setSection(section);
 * cpc.setMainClass(mainClass);
 * cpc.setSubClass(subClass);
 * cpc.setMainGroup(mainGroup);
 * cpc.setSubGroup(subGroup);
 * cpc.setIsMainClassification(true);
 * }
 *</pre>
 *</p> 
 *
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 * @see http://www.cooperativepatentclassification.org/index.html
 */
public class CpcClassification extends Classification {

	private final static Pattern REGEX = Pattern.compile("^([A-HY])(\\d\\d)([A-Z])\\s?(\\d{1,4})/?(\\d{2,})$"); // test added ?-mark to  /?

	private final static Pattern REGEX_LEN3 = Pattern.compile("^([A-HY])(\\d\\d)$");

	private final static Pattern REGEX_LEN4 = Pattern.compile("^([A-HY])(\\d\\d)([A-Z])$");

	private String section;
	private String mainClass;
	private String subClass;
	private String mainGroup;
	private String subGroup;
	private Boolean isMainClassification = false;

	public CpcClassification(String originalText) {
		super(ClassificationType.CPC, originalText);
	}

	public String getSection() {
		return section;
	}

	public void setSection(String section) {
		this.section = section;
	}

	public String getMainClass() {
		return mainClass;
	}

	public void setMainClass(String mainClass) {
		this.mainClass = mainClass;
	}

	public String getSubClass() {
		return subClass;
	}

	public void setSubClass(String subClass) {
		this.subClass = subClass;
	}

	public String getMainGroup() {
		return mainGroup;
	}

	public void setMainGroup(String mainGroup) {
		this.mainGroup = mainGroup;
	}

	public String getSubGroup() {
		return subGroup;
	}

	public void setSubGroup(String subGroup) {
		this.subGroup = subGroup;
	}

	/**
	 * Facets used for Search
	 * 
	 * D07B2201/2051 => [0/D, 1/D/D07, 2/D/D07/D07B, 3/D/D07/D07B/D07B2201, 4/D/D07/D07B/D07B2201/D07B22012051]
	 * 
	 */
	public List<String> toFacet() {
		return Classification.partsToFacet(section, mainClass, subClass, mainGroup, subGroup);
	}

	/**
	 * Classification Tree, permutation of all classification parts.
	 * 
	 * D07B2201/2051 => ["D", "D07", "D07B", "D07B2201/00", "D07B2201/2051"] 
	 * 
	 */
	public Set<String> getClassTree() {
		Set<String> cpcClasses = new LinkedHashSet<String>();
		cpcClasses.add(section);
		cpcClasses.add(section + mainClass);
		cpcClasses.add(section + mainClass + subClass);

		if (mainGroup != null) {
			//cpcClasses.add(section + mainClass + subClass + mainGroup);
			cpcClasses.add(section + mainClass + subClass + mainGroup + "/00");

			if (subGroup != null) {
				cpcClasses.add(section + mainClass + subClass + mainGroup + "/" + subGroup);
			}
		}

		return cpcClasses;
	}

	/**
	 * Text Representation normalized.
	 * 
	 * "D07B2201/2051" => "D07B 2201/2051"
	 * 
	 */
	public String toTextNormalized() {
		StringBuilder sb = new StringBuilder().append(section).append(mainClass);

		if (subClass != null) {
			sb.append(subClass);

			if (mainGroup != null) {
				sb.append(" ").append(mainGroup);

				if (subGroup != null) {
					sb.append("/").append(subGroup);
				}
			}
		}

		return sb.toString();
	}

	/**
	 * Text Representation Standardized; padded with zeros.
	 * 
	 * "D07B2201/2051" => "D07B022012051"
	 * 
	 */
	public String standardize() {

		StringBuilder sb = new StringBuilder().append(section).append(mainClass).append(subClass).append("0");

		if (Integer.valueOf(mainGroup) < 10) {
			sb.append("0");
		}

		sb.append(mainGroup).append(subGroup);

		String changed = sb.toString();
		changed = String.format("%1$-9s", changed);

		return changed;
	}

	/**
	 * Text Representation
	 * 
	 * Either the original text when created from parsing of text else generates normalized form from calling toTextNormalized().
	 * 
	 * @return
	 */
	public String toText() {
		if (super.getText() != null && super.getText().length() > 3) {
			return super.getText();
		} else {
			return toTextNormalized();
		}
	}

	@Override
	public String toString() {
		return "CpcClassification [section=" + section + ", mainClass=" + mainClass + ", subClass=" + subClass
				+ ", mainGroup=" + mainGroup + ", subGroup=" + subGroup + ", isMainClassification="
				+ isMainClassification + ", toFacet()=" + toFacet() + ", toText()=" + toText() + ", toTextNormalized()="
				+ toTextNormalized() + ", standardize()=" + standardize() + ", originalText=" + super.getText() + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			System.out.println("null");
			return false;
		}
		if (getClass() != obj.getClass()) {
			System.out.println("objClass");
			return false;
		}
		final CpcClassification other = (CpcClassification) obj;
		if ((this.section == null) ? (other.section != null) : !this.section.equals(other.section)) {
			System.out.println("section");
			return false;
		} else if ((this.mainClass == null) ? (other.mainClass != null) : !this.mainClass.equals(other.mainClass)) {
			System.out.println("mainclass");
			return false;
		} else if ((this.subClass == null) ? (other.subClass != null) : !this.subClass.equals(other.subClass)) {
			System.out.println("subClass");
			return false;
		} else if ((this.mainGroup == null) ? (other.mainGroup != null) : !this.mainGroup.equals(other.mainGroup)) {
			System.out.println("mainGroup");
			return false;
		} else if ((this.subGroup == null) ? (other.subGroup != null) : !this.subGroup.equals(other.subGroup)) {
			System.out.println("subGroup");
			return false;
		}
		return true;
	}

	/**
	 * Generate List of CpcClassifications from list of Facets.
	 * 
	 * @param classificationFacets
	 */
	public static List<CpcClassification> fromFacets(final List<String> classificationFacets) {
		List<String> specificClasses = getMostSpecificClasses(classificationFacets);
		List<CpcClassification> retClasses = new ArrayList<CpcClassification>();
		for (String textClass : specificClasses) {
			CpcClassification cpcClass;
			try {
				cpcClass = fromText(textClass);
				retClasses.add(cpcClass);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return retClasses;
	}

	/**
	 * Collapse Classification Facets to list of Specific Classifications
	 * 
	 * Returns the classification list extracted from the faceted string stored in solr
	 * 
	 * @param cpcVal // e.g. {0/A45B, 0/E04H, 1/A45B/A45B17, 1/E04H/E04H12, 2/A45B/A45B17/A45B1700, 2/E04H/E04H12/E04H122284 }
	 * @return       // {A45B1700, E04H122284}
	 */
	public static List<String> getMostSpecificClasses(List<String> cpcFacets) {
		List<String> leafClasses = new ArrayList<String>();

		String largestNode = cpcFacets.get(cpcFacets.size() - 1).split("/")[0];
		for (int i = cpcFacets.size() - 1; i > 0; i--) {
			String nodes[] = cpcFacets.get(i).split("/");
			if (!nodes[0].equals(largestNode)) {
				break;
			}
			leafClasses.add(nodes[nodes.length - 1]);
		}

		return leafClasses;
	}

	/**
	 * Parse Text to generate CpcClassifiation
	 * 
	 * @param classificationStr
	 * @return
	 * @throws ParseException
	 */
	public static CpcClassification fromText(final String classificationStr) throws ParseException {

		Matcher matcher = REGEX.matcher(classificationStr);
		if (matcher.matches()) {
			String section = matcher.group(1);
			String mainClass = matcher.group(2);
			String subClass = matcher.group(3);
			String mainGroup = matcher.group(4);
			String subGroup = matcher.group(5);

			CpcClassification classification = new CpcClassification(classificationStr);
			classification.setSection(section);
			classification.setMainClass(mainClass);
			classification.setSubClass(subClass);
			classification.setMainGroup(mainGroup);
			classification.setSubGroup(subGroup);

			return classification;
		} else if (classificationStr.length() == 3) {
			Matcher matchL3 = REGEX_LEN3.matcher(classificationStr);
			if (matchL3.matches()) {
				String section = matchL3.group(1);
				String mainClass = matchL3.group(2);
				CpcClassification classification = new CpcClassification(classificationStr);
				classification.setSection(section);
				classification.setMainClass(mainClass);

				return classification;
			}
		} else if (classificationStr.length() == 4) {
			Matcher matchL4 = REGEX_LEN4.matcher(classificationStr);
			if (matchL4.matches()) {
				String section = matchL4.group(1);
				String mainClass = matchL4.group(2);
				String subClass = matchL4.group(3);

				CpcClassification classification = new CpcClassification(classificationStr);
				classification.setSection(section);
				classification.setMainClass(mainClass);
				classification.setSubClass(subClass);

				return classification;
			}
		}

		throw new ParseException("Failed to regex parse USPC Classification: " + classificationStr, 0);
	}
}
