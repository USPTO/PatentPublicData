package gov.uspto.patent.model.classification;

import java.text.ParseException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Strings;

/**
 *<h3>USPC Classification</h3>
 *<p>
 *<h3>Structure Breakout: "417/161.1A"</h3>
 *<li>identifies Class 417, Subclass 161.1A
 *<li>5-6 digits, with left padding zeros i.e: "002".
 *<li>Section I (Class) : first three digits:  002-987, D01-D99, G9B, PLT
 *<li>Section II (Subclass): Next 2-3 digits; may have trailing decimal and digits.
 *</p>
 *<p>
 * Parse classification string:
 *<pre>
 * {@code
 *  UspcClassification uspc = UspcClassification.fromText(originalText);
 * }
 *</pre>
 *</p>
 *
 *<p>
 * Create Classification by its individual parts:
 *<pre>
 * {@code
 * UspcClassification uspc = new UspcClassification(originalText);
 * uspc.setMainClass(mainClass);
 * uspc.setSubClass(subClass);
 * }
 *</pre>
 *</p>
 *
 *  @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class UspcClassification extends Classification {

	// 074 89140
	private final static Pattern REGEX = Pattern.compile("^([0-9DGP][0-9L][0-9BT])/?([0-9A-Z]{5,6})$");

	private String mainClass;
	private String subClass;

    public UspcClassification(String originalText) {
		super(ClassificationType.USPC, originalText.replaceAll("\\s+", ""));
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

	public String toText(){
		if (super.getText() != null && super.getText().length() > 3){
			return super.getText();
		} else {
			return toTextNormalized();
		}
	}

	/**
	 * Normalized text in format mainClass/subClass
	 * 
	 * @return
	 */
	public String toTextNormalized(){
			return new StringBuilder()
				.append(mainClass)
				.append("/")
				.append(subClass).toString();
	}

	/**
	 * Facets used for Search
	 * 
	 * PLT101 => [0/PLT, 1/PLT/PLT101000]
	 * 
	 */
	public List<String> toFacet() {
		return Classification.partsToFacet(mainClass , subClass);
	}

	/**
	 * 
	 * Converts:
	 * " 602031" => [060, 060/203, 060/203.1]
	 * " 60204"  => [060, 060/204]
	 * "148602"  => [148, 148/602]
	 *  
	 * @return
	 */
	public Set<String> toSet(){
		Set<String> formats = new LinkedHashSet<String>();
		formats.add(mainClass);

		if (subClass.length() >= 3){
			String format2 = new StringBuilder()
					.append(mainClass)
					.append("/")
					.append(subClass.substring(0, 3)).toString();
			formats.add(format2);
		}

		if (subClass.length() > 3){
			String format3 = new StringBuilder()
					.append(mainClass)
					.append("/")
					.append(subClass.substring(0, 3))
					.append(".")
					.append(subClass.substring(3, subClass.length()))
					.toString();
			
			// Trim trailing Zeros.
			format3 = format3.replaceFirst("\\.0*$|(\\.\\d*?)0+$", "$1");

			formats.add(format3);
		}
		
		return formats;
	}
	
	@Override
	public String toString() {
		return "UspcClassification [mainClass=" + mainClass
				+ ", subClass=" + subClass
				+ ", toText()=" + toText()
				+ ", toSet()=" + toSet()
				+ ", originalText=" + super.getText()
				+ "]";
	}

	/**
	 * Parse classification text to create USPC Classification.
	 * 
	 * @param classificationStr
	 * @return
	 * @throws ParseException
	 */
	public static UspcClassification fromText(final String classificationStr) throws ParseException {
		if (Strings.isNullOrEmpty(classificationStr)){
			return null;
		}

		String input = classificationStr.replaceAll("\\s", "0");
		input = Strings.padEnd(input, 9, '0');

		// Handle Leading Space " D2907" --> "D02/907000"
		if (classificationStr.startsWith(" ")){ 
		    Pattern pattern = Pattern.compile("^0(\\D)(\\d)");
		    Matcher match = pattern.matcher(input);
		    if (match.find()){
		    	input = match.replaceFirst( match.group(1) + "0" + match.group(2));
		    }
		}

		// Handling Range "D11143-144"  @FIXME update to return both items in range, currently only first.
		String[] classRange = classificationStr.split("-", 2);
		if (classRange.length == 2){
			int len2 = classRange[1].length();
			Pattern pattern2 = Pattern.compile("(\\d{"+ len2 +"})-(\\d{"+ len2 + "})$");
			Matcher match = pattern2.matcher(classificationStr);
			if (match.find()){
		    	input = match.replaceFirst( match.group(1) );
		    	input = input.replaceAll("\\s", "0");
		    	input = Strings.padEnd(input, 9, '0');
		    	// String input2 = match.replaceFirst( match.group(2) );
			}
		}
		

		Matcher matcher = REGEX.matcher(input);
		if ( matcher.matches() ){
			String mainClass = matcher.group(1);
			String subClass = matcher.group(2);

		    UspcClassification classification = new UspcClassification(classificationStr);
		    classification.setMainClass(mainClass);
		    classification.setSubClass(subClass);

		    return classification;
		}

		throw new ParseException("Failed to regex parse USPC Classification: '" + classificationStr + "' transposed to: '" + input + "'", 0);

	}
}
