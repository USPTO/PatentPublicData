package gov.uspto.patent.model.classification;

import java.text.ParseException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *<h3>International Patent Classification (IPC)</h3>
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
	
	private final static Pattern REGEX = Pattern.compile("^([A-HY])(\\d\\d)([A-Z])\\s?(\\d{1,4})/?(\\d{2,})$");

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

	@Override
	public String toString() {
		return "IpcClassification["
				+ "section=" + section
				+ ", mainClass=" + mainClass
				+ ", subClass=" + subClass
				+ ", mainGroup=" + mainGroup
				+ ", subGroup=" + subGroup
				+ ", toText()=" + toText()
				+ ", standardize()=" + standardize()
				+ ", originalText=" + super.getText()
				+ "]";
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
			String mainGroup = matcher.group(4);
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
