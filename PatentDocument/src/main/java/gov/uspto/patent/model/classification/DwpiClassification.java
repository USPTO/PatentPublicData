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
public class DwpiClassification extends PatentClassification {

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

	@Override
	public ClassificationType getType() {
		return ClassificationType.DWPI;
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

	@Override
	public String[] getParts() {
		return new String[]{section.toString(), subsection, group, subgroup, division, subdivision, extra};
	}

	@Override
	public String getTextNormalized() {
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
	
	/**
	 * Classification depth 
	 * 
	 * ( 1=section, 2=subsection, 3=group, 4=subGroup, 5=division, 6=subdivision, 7=extra)
	 * 
	 */
	public int getDepth(){
		int classDepth = 0;

		if (extra != null && extra.isEmpty()){
			classDepth = 7;
		}
		else if (subdivision != null && subdivision.isEmpty()){
			classDepth = 6;
		}
		else if (division != null && !division.isEmpty()){
			classDepth = 5;
		}
		else if (subgroup != null && !subgroup.isEmpty()){
			classDepth = 4;
		}
		else if (group != null && !group.isEmpty()){
			classDepth = 3;
		}
		else if (subsection != null && !subsection.isEmpty()){
			classDepth = 2;
		}
		else if (section != null){
			classDepth = 1;
		}

		return classDepth;
	}

	@Override
	public boolean isContained(PatentClassification check){
		if (check == null || !(check instanceof DwpiClassification)) {
			return false;
		}
		DwpiClassification dwpi = (DwpiClassification) check;
		
		int depth = getDepth();
		if (depth == 7){
			if (section.equals(dwpi.getSection()) 
					&& subsection.equals(dwpi.getSubsection()) 
					&& group.equals(dwpi.getGroup())
					&& subgroup.equals(dwpi.getSubgroup())
					&& division.equals(dwpi.getDivision())
					&& subdivision.equals(dwpi.getSubdivision()) 
					&& extra.equals(dwpi.getExtra())){
				return true;
			}
		}
		else if (depth == 6){
			if (section.equals(dwpi.getSection()) 
					&& subsection.equals(dwpi.getSubsection()) 
					&& group.equals(dwpi.getGroup())
					&& subgroup.equals(dwpi.getSubgroup())
					&& division.equals(dwpi.getDivision())
					&& subdivision.equals(dwpi.getSubgroup())){
					return true;
			}
		}
		else if (depth == 5){
			if (section.equals(dwpi.getSection()) 
					&& subsection.equals(dwpi.getSubsection()) 
					&& group.equals(dwpi.getGroup())
					&& subgroup.equals(dwpi.getSubgroup())
					&& division.equals(dwpi.getDivision())){
					return true;
			}
		}
		else if (depth == 4){
			if (section.equals(dwpi.getSection()) 
					&& subsection.equals(dwpi.getSubsection()) 
					&& group.equals(dwpi.getGroup())
					&& subgroup.equals(dwpi.getSubgroup())){
					return true;
			}
		}
		else if (depth == 3){
			if (section.equals(dwpi.getSection()) 
					&& subsection.equals(dwpi.getSubsection()) 
					&& group.equals(dwpi.getGroup())){
					return true;
			}
		}
		else if (depth == 2){
			if (section.equals(dwpi.getSection()) 
					&& subsection.equals(dwpi.getSubsection())){
					return true;
			}
		}
		else if (depth == 1){
			if (section.equals(dwpi.getSection())){
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
		final DwpiClassification other = (DwpiClassification) obj;
		
		if (other.getDepth() == getDepth() && isContained(other)){
			return true;
		}

		return false;
	}
	
	
	@Override
	public String toString() {
		return "DwpiClassification [section=" + section + ", subsection=" + subsection + ", group=" + group
				+ ", subgroup=" + subgroup + ", division=" + division + ", subdivision=" + subdivision + ", extra="
				+ extra + ", toText()=" + toText() + ", getDepth()=" + getDepth() + "]";
	}

	@Override
	public void parseText(String classificationStr) throws ParseException {
		
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
			super.setTextOriginal(classificationStr);
			setSection(section);
		    setSubsection(subsection);
		    setGroup(group);
		    setSubgroup(subgroup);
		    setDivision(division);
		    setSubdivision(subdivision);
		    setExtra(extra);
		} else {
			throw new ParseException("Failed to regex parse DWPI Classification: " + classificationStr, 0);
		}

	}

}
