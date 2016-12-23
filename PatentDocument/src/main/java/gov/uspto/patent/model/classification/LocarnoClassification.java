package gov.uspto.patent.model.classification;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocarnoClassification extends PatentClassification {

	private static Pattern PATTERN = Pattern.compile("^([0-9]{2})[-/]?([0-9]{2})$");

	private String mainClass;
	private String subClass;

	@Override
	public ClassificationType getType() {
		return ClassificationType.LOCARNO;
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

	@Override
	public String[] getParts() {
		return new String[]{mainClass, subClass};
	}

	@Override
	public int getDepth() {
		int classDepth = 0;

		if (subClass != null && subClass.isEmpty()){
			classDepth = 2;
		}
		else if (mainClass != null){
			classDepth = 1;
		}

		return classDepth;
	}

	@Override
	public String getTextNormalized() {
		StringBuilder sb = new StringBuilder().append(mainClass).append("-").append(subClass);
		return sb.toString();
	}

	@Override
	public void parseText(String text) throws ParseException {
		super.setTextOriginal(text);

		Matcher matcher = PATTERN.matcher(text);
		if (matcher.matches()) {
			String mainClass = matcher.group(1);
			String subClass = matcher.group(2);

			setMainClass(mainClass);
			setSubClass(subClass);
		} else {
			throw new ParseException("Failed to regex parse Locarno Classification: " + text, 0);
		}
	}

	@Override
	public boolean isContained(PatentClassification check){
		if (check == null || !(check instanceof LocarnoClassification)) {
			return false;
		}
		LocarnoClassification locarno = (LocarnoClassification) check;
		if (getMainClass().equals(locarno.getMainClass())) {
			if (getSubClass().equals(locarno.getSubClass())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return "LocarnoClassification [mainClass=" + mainClass + ", subClass=" + subClass + ", getTextNormalized()="
				+ getTextNormalized() + "]";
	}
}
