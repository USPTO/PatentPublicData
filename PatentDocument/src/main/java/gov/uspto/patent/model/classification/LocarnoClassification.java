package gov.uspto.patent.model.classification;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocarnoClassification extends Classification {

	private static Pattern PATTERN = Pattern.compile("^([0-9]{2})[-/]?([0-9]{2})$");

	private String mainClass;
	private String subClass;
	
	public LocarnoClassification(String originalText) {
		super(ClassificationType.LOCARNO, originalText);
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

    public String standardize() {
  	  
    	StringBuilder sb =  new StringBuilder()
				.append(mainClass)
				.append("-")
				.append(subClass);

        return sb.toString();
    }

	@Override
	public String toString() {
		return "LocarnoClassification [mainClass=" + mainClass + ", subClass=" + subClass + ", standardize()="
				+ standardize() + "]";
	}

	public static LocarnoClassification fromText(String classificationStr) throws ParseException{
		Matcher matcher = PATTERN.matcher(classificationStr);
		if (matcher.matches()){
			String mainClass = matcher.group(1);
			String subClass = matcher.group(2);
			LocarnoClassification classification = new LocarnoClassification(classificationStr);
			classification.setMainClass(mainClass);
			classification.setSubClass(subClass);
			return classification;
		}

		throw new ParseException("Failed to regex parse Locarno Classification: " + classificationStr, 0);
	}
}
