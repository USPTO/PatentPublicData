package gov.uspto.patent.model.classification;

import java.text.ParseException;
import java.util.List;
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
 * CpcClassification cpc = new CpcClassification();
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
public class CpcClassification extends PatentClassification {

	private final static Pattern REGEX = Pattern.compile("^([A-HY])(\\d\\d)([A-Z])\\s?(\\d{1,4})/?(\\d{2,})$"); // test added ?-mark to  /?

	private final static Pattern REGEX_LEN3 = Pattern.compile("^([A-HY])(\\d\\d)$");

	private final static Pattern REGEX_LEN4 = Pattern.compile("^([A-HY])(\\d\\d)([A-Z])$");

	private String section;
	private String mainClass;
	private String subClass;
	private String mainGroup;
	private String subGroup;
	private Boolean isMainClassification = false;

	@Override
	public ClassificationType getType() {
		return ClassificationType.CPC;
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

	@Override
	public String[] getParts() {
		return new String[]{section, mainClass, subClass, mainGroup, subGroup};
	}

	/**
	 * Text Representation normalized.
	 * 
	 * "D07B2201/2051" => "D07B 2201/2051"
	 * 
	 */
	@Override
	public String getTextNormalized() {
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

		StringBuilder sb = new StringBuilder().append(section).append(mainClass);

		if (subClass != null) {
			sb.append(subClass).append("0");

			if (mainGroup != null && mainGroup.matches("^[0-9]+$")) {
				if (Integer.valueOf(mainGroup) < 10) {
					sb.append("0");
				}
				sb.append(mainGroup);

				if (subGroup != null) {
					sb.append(subGroup);
				}
			}
		}
		String changed = sb.toString();
		changed = String.format("%1$-9s", changed);

		return changed;
	}
	
	/**
	 * Classification depth 
	 * 
	 * (1=section, 2=mainclass, 3=subclass, 4=mainGroup, 5=subGroup)
	 * 
	 */
	@Override
	public int getDepth(){
		int classDepth = 0;
		if (subGroup != null && !subGroup.isEmpty()){
			classDepth = 5;
		}
		else if (mainGroup != null && !mainGroup.isEmpty()){
			classDepth = 4;
		}
		else if (subClass != null && !subClass.isEmpty()){
			classDepth = 3;
		}
		else if (mainClass != null && !mainClass.isEmpty()){
			classDepth = 2;
		}
		else if (section != null && !section.isEmpty()){
			classDepth = 1;
		}
		return classDepth;
	}

	@Override
	public boolean isContained(PatentClassification check){
		if (check == null || !(check instanceof CpcClassification)) {
			return false;
		}
		CpcClassification cpc = (CpcClassification) check;
			
		int depth = getDepth();

		if (depth == cpc.getDepth()){
			if (this.getTextNormalized().equals(cpc.getTextNormalized())){
				return true;
			} else {
				return false;
			}
		}
		if (depth == 5){
			if (section.equals(cpc.getSection()) 
					&& mainClass.equals(cpc.getMainClass()) 
					&& subClass.equals(cpc.getSubClass()) 
					&& mainGroup.equals(cpc.getMainGroup()) 
					&& subGroup.equals(cpc.getSubGroup())){
				return true;
			}
		}
		else if (depth == 4){
			if (section.equals(cpc.getSection()) 
					&& mainClass.equals(cpc.getMainClass()) 
					&& subClass.equals(cpc.getSubClass()) 
					&& mainGroup.equals(cpc.getMainGroup())){
					return true;
			}
		}
		else if (depth == 3){
			if (section.equals(cpc.getSection()) 
					&& mainClass.equals(cpc.getMainClass()) 
					&& subClass.equals(cpc.getSubClass())){
					return true;
			}
		}
		else if (depth == 2){
			if (section.equals(cpc.getSection()) 
					&& mainClass.equals(cpc.getMainClass())){
					return true;
			}
		}
		else if (depth == 1){
			if (section.equals(cpc.getSection())){
					return true;
			}
		}
		return false;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final CpcClassification other = (CpcClassification) obj;
		
		if (other.getDepth() == getDepth() && isContained(other)){
			return true;
		}

		return false;
	}

	/**
	 * Parse Text to generate CpcClassifiation
	 * 
	 * @param classificationStr
	 * @return
	 * @throws ParseException
	 */
	@Override
	public void parseText(final String classificationStr) throws ParseException {

		super.setTextOriginal(classificationStr);
		
		Matcher matcher = REGEX.matcher(classificationStr);
		if (matcher.matches()) {
			String section = matcher.group(1);
			String mainClass = matcher.group(2);
			String subClass = matcher.group(3);
			String mainGroup = matcher.group(4);
			String subGroup = matcher.group(5);

			setSection(section);
			setMainClass(mainClass);
			setSubClass(subClass);
			setMainGroup(mainGroup);
			setSubGroup(subGroup);
		} else if (classificationStr.length() == 3) {
			Matcher matchL3 = REGEX_LEN3.matcher(classificationStr);
			if (matchL3.matches()) {
				String section = matchL3.group(1);
				String mainClass = matchL3.group(2);
				setSection(section);
				setMainClass(mainClass);
			}
		} else if (classificationStr.length() == 4) {
			Matcher matchL4 = REGEX_LEN4.matcher(classificationStr);
			if (matchL4.matches()) {
				String section = matchL4.group(1);
				String mainClass = matchL4.group(2);
				String subClass = matchL4.group(3);

				setSection(section);
				setMainClass(mainClass);
				setSubClass(subClass);
			}
		} else {
			throw new ParseException("Failed to regex parse USPC Classification: " + classificationStr, 0);
		}
	}

	@Override
	public String toString() {
		return "CpcClassification [section=" + section + ", mainClass=" + mainClass + ", subClass=" + subClass
				+ ", mainGroup=" + mainGroup + ", subGroup=" + subGroup + ", isMainClassification="
				+ isMainClassification + ", getTextNormalized()=" + getTextNormalized() + ", standardize()="
				+ standardize() + ", toText()=" + toText() + ", originalText()=" + super.getTextOriginal() + "]";
	}

    /**
     * Parse Facet back into Classifications
     */
    public static List<CpcClassification> fromFacets(List<String> facets) {
        return ClassificationTokenizer.fromFacets(facets, CpcClassification.class);
    }
}
