package gov.uspto.patent.model.classification;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import gov.uspto.common.tree.Tree;
import gov.uspto.patent.InvalidDataException;

/**
 * Locarno Classification
 * 
 * <p>
 * International classification used for the purposes of the registration of
 * industrial designs.
 * </p>
 * 
 * <p>
 * Classification used within Design Patents.
 * </p>
 *
 * @author Brian G. Feldman<brian.feldman@uspto.gov>
 * 
 * @see http://www.wipo.int/classifications/locarno/en/
 *
 */
public class LocarnoClassification extends PatentClassification {

	private static Logger LOGGER = LoggerFactory.getLogger(LocarnoClassification.class);

	private static Pattern PATTERN = Pattern.compile("^([0-9]{2})[-/]?([0-9]{2})$");

	private String mainClass;
	private String subClass;
	private boolean parseFailed = false;

	public LocarnoClassification(String originalText, boolean inventiveOrMain) {
		super(originalText, inventiveOrMain);
	}

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
	public Tree getTree() {
		Tree tree = new Tree();
		if (mainClass != null && subClass != null) {
			tree.addChild(Strings.padStart(mainClass, 2, '0')).addChild(Strings.padStart(subClass, 2, '0'));
		}
		return tree;
	}

	public int getDepth() {
		int classDepth = 0;

		if (subClass != null && subClass.isEmpty()) {
			classDepth = 2;
		} else if (mainClass != null) {
			classDepth = 1;
		}

		return classDepth;
	}

	@Override
	public String getTextNormalized() {
		if (parseFailed) {
			return super.getTextOriginal() + "__parseFailed";
		}
		StringBuilder sb = new StringBuilder().append(mainClass).append("-").append(subClass);
		return sb.toString();
	}

	@Override
	public List<String> getSearchTokens() {
		List<String> list = new ArrayList<String>(1);
		StringBuilder sb = new StringBuilder();
		sb.append(Strings.padStart(mainClass, 2, '0'));
		sb.append("-");
		sb.append(Strings.padStart(subClass, 2, '0'));
		list.add(getTextNormalized());
		return list;
	}

	@Override
	public void parseText(final String text) throws ParseException {

		Matcher matcher = PATTERN.matcher(text);
		if (text.length() > 0 && text.length() <= 2) {
			String mainClass = Strings.padStart(text, 2, '0');
			setMainClass(mainClass);
			setSubClass("00");
		} else if (matcher.matches()) {
			String mainClass = matcher.group(1);
			String subClass = matcher.group(2);

			setMainClass(mainClass);
			setSubClass(subClass);
		} else {
			parseFailed = true;
			LOGGER.debug("LOCARNO parse failed '{}'", text);
			throw new ParseException("Failed to regex parse Locarno Classification: " + text, 0);
		}
	}

	@Override
	public boolean isContained(PatentClassification check) {
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
	public boolean validate() throws InvalidDataException {
		if (mainClass == null || mainClass.isEmpty()) {
			throw new InvalidDataException("Invalid MainClass");
		}
		return true;
	}

	@Override
	public String toString() {
		return "LocarnoClassification [mainClass=" + mainClass + ", subClass=" + subClass + ", getTextNormalized()="
				+ getTextNormalized() + "]";
	}
}
