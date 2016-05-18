package gov.uspto.patent.model.classification;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Derwent "DWPI" Classification, are defined as Derwent Manual Codes, since they are applied manually by analyst.
 *
 * Patents are classified into three broad technology areas: 
 * 		Chemical
 *		Engineering
 *		Electrical and Electronic Engineering   
 *
 * A01-A01A1
 *  -- Section, 1st letter
 *  -- Subsection, two digits
 *  -- Group, one letter
 *  -- subgroup, two numbers
 *  -- division, one letter
 *  -- subdivision, one numbers
 *  -- occasional training letter.
 *  
 *  -- subclass / "Fragmentation Code", series of alphnumerics.
 *  
 * Codes are applied to the inventive/significant features of the invention using the Documentation Abstract.
 * Codes are assigned by teams of Thomson Reuters DWPI analysts.
 * 
 * Can be followed by IPC code as follows:
 * 		A47, F23-5
 *		( DWPI code, IPC code)
 *
 *
 *  @author Brian G. Feldman (brian.feldman@uspto.gov)
 *  
 *  http://ip-science.thomsonreuters.com/support/patents/dwpiref/reftools/classification/
 *	http://ip-science.thomsonreuters.com/m/pdfs/DWPI_Class_Manual_2015.pdf
 * 
 */
public class DwpiClassification extends Classification {
	private DWPISection section;
	private String subsection;

	private String group;
	private String subgroup;
	private String division;
	private String subdivision;
	private String extra;

	// Regex with their lengths, note length includes the dash.
	private final static Pattern REGEX_LEN_3 = Pattern.compile("^([A-HJ-NPQS-X])(\\d{2})$"); // Section and Subsection.
	private final static Pattern REGEX_LEN_5 = Pattern.compile("^([A-HJ-NPQS-X])(\\d{2})-([A-Z])$"); // Section, Subsection, Group.
	private final static Pattern REGEX_LEN_7 = Pattern.compile("^([A-HJ-NPQS-X])(\\d{2})-([A-Z])(\\d{2})$"); // Section, Subsection, Group, Subgroup.
	private final static Pattern REGEX_LEN_8 = Pattern.compile("^([A-HJ-NPQS-X])(\\d{2})-([A-Z])(\\d{2})([A-Z])$"); // Section, Subsection, Group, Subgroup, division.
	private final static Pattern REGEX_LEN_9 = Pattern.compile("^([A-HJ-NPQS-X])(\\d{2})-([A-Z])(\\d{2})([A-Z])(\\d)$"); // Section, Subsection, Group, Subgroup, division, subdivision.
	private final static Pattern REGEX_LEN_10 = Pattern.compile("^([A-HJ-NPQS-X])(\\d{2})-([A-Z])(\\d{2})([A-Z])(\\d)([A-Z])$"); // Section, Subsection, Group, Subgroup, division, subdivision, extra letter.

	public DwpiClassification(String originalText) {
		super(ClassificationType.DWPI, originalText);
	}

	public DWPISection getSection() {
		return section;
	}

	public void setSection(String section) {
		this.section = DWPISection.valueOf(section.toUpperCase());
	}

	public void setSection(DWPISection section) {
		this.section = section;
	}

	public String getSubsection() {
		return subsection;
	}

	public void setSubsection(String subsection) {
		this.subsection = subsection;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getSubgroup() {
		return subgroup;
	}

	public void setSubgroup(String subgroup) {
		this.subgroup = subgroup;
	}

	public String getDivision() {
		return division;
	}

	public void setDivision(String division) {
		this.division = division;
	}

	public String getSubdivision() {
		return subdivision;
	}

	public void setSubdivision(String subdivision) {
		this.subdivision = subdivision;
	}

	public String getExtra() {
		return extra;
	}

	public void setExtra(String extra) {
		this.extra = extra;
	}

	public String toText(){
		if (super.getText() != null && super.getText().length() > 3){
			return super.getText();
		} else {
			StringBuilder sb = new StringBuilder()
					.append(section)
					.append(subsection);
			
				if (group != null){
					sb.append("-").append(group);
					
					if (subgroup != null){
						sb.append(subgroup);
						
						if (division != null){
							sb.append(division);
							
							if (subdivision != null){
								sb.append(subdivision);
								
								if (extra != null){
									sb.append(extra);
								}	
							}
							
						}	
						
					}
					
				}
				
				return sb.toString();
		}
	}

	public static DwpiClassification fromText(final String classificationStr) throws ParseException {
		
		String section = null;
		String subsection = null;
		String group = null;
		String subgroup = null;
		String division = null;
		String subdivision = null;
		String extra = null;
		
		if (classificationStr.length() == 3){
			Matcher matcher = REGEX_LEN_3.matcher(classificationStr);
			section = matcher.group(1);
			subsection = matcher.group(2);
		}
		else if (classificationStr.length() == 5){
			Matcher matcher = REGEX_LEN_5.matcher(classificationStr);
			if(matcher.matches()){
				section = matcher.group(1);
				subsection = matcher.group(2);
				group = matcher.group(3);
			}
		}
		else if (classificationStr.length() == 7){
			Matcher matcher = REGEX_LEN_7.matcher(classificationStr);
			if(matcher.matches()){
				section = matcher.group(1);
				subsection = matcher.group(2);
				group = matcher.group(3);
				subgroup = matcher.group(4);
			}
		}
		else if (classificationStr.length() == 8){
			Matcher matcher = REGEX_LEN_8.matcher(classificationStr);
			if(matcher.matches()){
				section = matcher.group(1);
				subsection = matcher.group(2);
				group = matcher.group(3);
				subgroup = matcher.group(4);
				division = matcher.group(5);
			}
		}
		else if (classificationStr.length() == 9){
			Matcher matcher = REGEX_LEN_9.matcher(classificationStr);
			if(matcher.matches()){
				section = matcher.group(1);
				subsection = matcher.group(2);
				group = matcher.group(3);
				subgroup = matcher.group(4);
				division = matcher.group(5);
				subdivision = matcher.group(6);
			}
		}
		else if (classificationStr.length() == 10){
			Matcher matcher = REGEX_LEN_10.matcher(classificationStr);
			if(matcher.matches()){
				section = matcher.group(1);
				subsection = matcher.group(2);
				group = matcher.group(3);
				subgroup = matcher.group(4);
				division = matcher.group(5);
				subdivision = matcher.group(6);
				extra = matcher.group(7);
			}
		}

		if (classificationStr.length() >= 1){
			DwpiClassification classification = new DwpiClassification(classificationStr);
		    classification.setSection(section);
		    classification.setSubsection(subsection);
		    classification.setGroup(group);
		    classification.setSubgroup(subgroup);
		    classification.setDivision(division);
		    classification.setSubdivision(subdivision);
		    classification.setExtra(extra);

		    return classification;
		} else {
			throw new ParseException("Failed to regex parse DWPI Classification: " + classificationStr, 0);
		}

	}

}
