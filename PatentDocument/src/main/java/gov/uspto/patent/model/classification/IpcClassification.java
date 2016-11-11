package gov.uspto.patent.model.classification;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *<h3>International Patent Classification (IPC)</h3>
 *
 *<p>
 * The effort to build an international classification system began in the 1970s.
 * CPC and IPC classes and subclasses are historically related to United Nations HS, ISIC, and SITC classification schemes.  
 *</p>
 *
 *<p>
 *<h3>Structure Breakout: "H01S 3/00"</h3>
 *<li>H = Electricity section.
 *<li>01 = class symbol.
 *<li>S = subclass symbol.
 *<li>3/00 = main group has 00, else its a subgroup of main group.
 *</p> B60R 900
 *
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 * @see http://www.wipo.int/classifications/ipc
 * @see http://www.wipo.int/export/sites/www/classifications/ipc/en/guide/guide_ipc.pdf}
 * @see http://web2.wipo.int/ipcpub}
 */
public class IpcClassification extends Classification {
	
	private final static Pattern REGEX = Pattern.compile("^([A-HY])\\s?(\\d\\d)([A-Z])\\s?(\\d\\s?\\d{1,3})/?(\\d{2,})$");

	private String section;
	private String mainClass;
	private String subClass;
	private String mainGroup;
	private String subGroup;
	
	public IpcClassification(String originalText) {
		super(ClassificationType.IPC, originalText);
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

	public String toText(){
		if (super.getText() != null && super.getText().length() > 3){
			return super.getText();
		} else {
			return toTextNormalized();
		}
	}

	public String toTextNormalized(){
		StringBuilder sb = new StringBuilder().append(section).append(mainClass);
		
		if (subClass != null){
			sb.append(subClass);
			
			if (mainGroup != null){
				sb.append(" ").append(mainGroup);

				if (subGroup != null){
					sb.append("/").append(subGroup);
				}	
			}
		}

		return sb.toString();
	}	
	
	
    public String standardize() {
    	  
    	StringBuilder sb =  new StringBuilder()
				.append(section)
				.append(mainClass)
				.append(subClass)
				.append("0");

		if (Integer.valueOf(mainGroup) < 10){
			sb.append("0");
		}

		sb.append(mainGroup)
			.append(subGroup);

		String changed = sb.toString();
		changed = String.format("%1$-9s", changed);

        return changed;
    }

	/**
	 * Classification depth 
	 * 
	 * (1=section, 2=mainClass, 3=subClass, 4=mainGroup, 5=subGroup)
	 * 
	 */
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
		else if (mainClass != null && mainClass.isEmpty()){
			classDepth = 2;
		}
		else if (section != null && section.isEmpty()){
			classDepth = 1;
		}
		return classDepth;
	}

	public boolean equalOrUnder(CpcClassification cpc){
		if (cpc == null) {
			return false;
		}
		int depth = getDepth();
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
		
		if (other.getDepth() == getDepth() && equalOrUnder(other)){
			return true;
		}

		return false;
	}
	

	@Override
	public String toString() {
		return "IpcClassification [section=" + section + ", mainClass=" + mainClass + ", subClass=" + subClass
				+ ", mainGroup=" + mainGroup + ", subGroup=" + subGroup + ", toText()=" + toText()
				+ ", toTextNormalized()=" + toTextNormalized() + ", standardize()=" + standardize() + ", getDepth()="
				+ getDepth() + ", originalText()=" + super.getText() + "]";
	}

    /**
     * Generate List of IpcClassifications from list of Facets.
     * 
     * @param classificationFacets
     */
    public static List<IpcClassification> fromFacets(final List<String> classificationFacets) {
        List<String> specificClasses = getMostSpecificClasses(classificationFacets);
        List<IpcClassification> retClasses = new ArrayList<IpcClassification>();
        for (String textClass : specificClasses) {
            try {
                IpcClassification ipcClass = fromText(textClass);
                retClasses.add(ipcClass);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return retClasses;
    }
	
	/**
	 * Parse classification text to create IpcClassification
	 * 
	 * @param classificationStr
	 * @return
	 * @throws ParseException
	 */
	public static IpcClassification fromText(final String classificationStr) throws ParseException {

		Matcher matcher = REGEX.matcher(classificationStr);
		if ( matcher.matches() ){
			String section = matcher.group(1);
			String mainClass = matcher.group(2);
			String subClass = matcher.group(3);
			String mainGroup = matcher.group(4).replace(' ', '0');
			String subGroup = matcher.group(5);

			IpcClassification classification = new IpcClassification(classificationStr);
		    classification.setSection(section);
		    classification.setMainClass(mainClass);
		    classification.setSubClass(subClass);
		    classification.setMainGroup(mainGroup);
		    classification.setSubGroup(subGroup);

		    return classification;
		}
		
		throw new ParseException("Failed to regex parse IPC Classification: " + classificationStr, 0);
	}	
	
}
