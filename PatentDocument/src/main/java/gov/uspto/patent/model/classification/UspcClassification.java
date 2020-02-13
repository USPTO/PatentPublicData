package gov.uspto.patent.model.classification;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import gov.uspto.common.tree.Node;
import gov.uspto.common.tree.Tree;
import gov.uspto.patent.InvalidDataException;

/**
 * <h3>USPC Classification</h3>
 * <p>
 * <h3>Structure Breakout: "417/161.1A"</h3>
 * <li>identifies Class 417, Subclass 161.1A
 * <li>5-6 digits, with left padding zeros i.e: "002".
 * <li>Class : first three digits: 002-987, D01-D99, G9B, PLT
 * <li>Subclass : Next 2-3 digits;
 * <li>Subclass indent : trailing decimal and digits, may also be a range or
 * "combination"
 * </p>
 * <p>
 * Parse classification string:
 * 
 * <pre>
 * {
 * 	&#64;code
 * 	UspcClassification uspc = new UspcClassification();
 * 	uspc.parseText(uspcTextString);
 * }
 * </pre>
 * </p>
 *
 * <p>
 * Create Classification by its individual parts:
 * 
 * <pre>
 * {
 * 	&#64;code
 * 	UspcClassification uspc = new UspcClassification();
 * 	uspc.setMainClass(mainClass);
 * 	uspc.setSubClass(subClass);
 * }
 * </pre>
 * </p>
 * 
 * <p>
 * Patent classification mostly phased out by the USPTO on June 1st , 2015.
 * </p>
 * 
 * <p>
 * Plant Patents and Design Patents are still classified within USPC.
 * </p>
 *
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class UspcClassification extends PatentClassification {

	private static Logger LOGGER = LoggerFactory.getLogger(UspcClassification.class);

	private final static Pattern REGEX = Pattern
			.compile("^(?<mainClass>[^/]{3}|[^/]{1,2}(?=/))/?(?<subClass>[^/]{3})\\.?(?<subClassIndent>[^\\.]{0,5})$");
	private final static Pattern SUBGROUP_RANGE = Pattern.compile("-([^/]{3})\\.?([^\\.]{0,4})$");

	private String mainClass;
	private String[] subClass;
	private boolean parseFailed = false;

	public UspcClassification(String originalText, boolean mainOrInventive) {
		super(originalText, mainOrInventive);
	}

	public boolean isParseFailed() {
		return parseFailed;
	}

	@Override
	public ClassificationType getType() {
		return ClassificationType.USPC;
	}

	public void setMainClass(String mainClass) {
		this.mainClass = mainClass;
	}

	public String getMainClass() {
		return mainClass;
	}

	public String[] getSubClass() {
		return subClass;
	}

	public void setSubClass(String subClass) {
		this.subClass = new String[] { subClass };
	}

	/**
	 * Sub Class Range
	 * 
	 * @param subClass
	 */
	public void setSubClass(String[] subClass) {
		this.subClass = subClass;
	}

	/**
	 * Normalized text in format mainClass/subClass
	 * 
	 * @return
	 */
	@Override
	public String getTextNormalized() {
		if (parseFailed) {
			return super.getTextOriginal() + "__parseFailed";
		}

		StringBuilder stb = new StringBuilder();
		if (subClass != null) {
			if (subClass.length == 1) {
				stb.append(mainClass).append('/').append(subClass[0]);
			} else if (subClass.length == 2) {
				stb.append(mainClass).append('/').append(subClass[0]);
				stb.append("-");
				// stb.append(mainClass).append('/');
				stb.append(subClass[1]);
			} else if (mainClass != null) {
				stb.append(mainClass);
			}
		}

		/*
		 * for (String subRange : subClass) { stb.append(subRange).append(','); }
		 * stb.deleteCharAt(stb.length() - 1);
		 */

		return stb.toString();
	}

	public List<String> getSearchTokens() {
		// List<String> list = new ArrayList<String>(1);
		// list.add(getTextNormalized());
		// return list;

		return getTree().getLeafPaths("").stream().map(c -> String.format("%1$-9s", c).replace(' ', '0'))
				.collect(Collectors.toList());		
	}

	public int getDepth() {
		int classDepth = 0;

		if (subClass != null && subClass.length > 0) {
			classDepth = 2;
		} else if (mainClass != null) {
			classDepth = 1;
		}

		return classDepth;
	}

	public Tree getTree() {
		Tree tree = new Tree();
		if (parseFailed) {
			return tree;
		}

		String mainClassPad = Strings.padStart(mainClass, 3, '0');
		Node parent = tree.addChild(mainClassPad);
		for (int i = 0; i < subClass.length; i++) {
			String subClassPad = Strings.padStart(subClass[i], 3, '0');
			parent.addChild(subClassPad);
		}
		return tree;
	}

	/**
	 * Parse classification text to create USPC Classification.
	 * 
	 * @param classificationStr
	 * @return
	 * @throws ParseException
	 */
	@Override
	public void parseText(final String classificationStr) throws ParseException {

		String input = classificationStr.toUpperCase().replace(' ', '0').replaceAll("(DIG|FOR)0", "$1");

		// Match USPC Classification Range.
		Matcher rangeMatcher = SUBGROUP_RANGE.matcher(input);
		String subClassRangLmt = null;
		if (rangeMatcher.find()) {
			input = input.substring(0, rangeMatcher.start());
			subClassRangLmt = rangeMatcher.group(1);
			subClassRangLmt = subClassRangLmt.replaceFirst("^0+", "");

			String subClassIndent = rangeMatcher.group(2);

			if (subClassIndent != null) {
				if ("FOR".equals(subClass) || "DIG".equals(subClass)) {
					subClassRangLmt += " " + subClassIndent.replaceFirst("^0+", "").replaceFirst("0([A-Z]{1,2})$", "$1");
				} else if (subClassIndent.matches("^0[A-Z]{1,2}$")) {
					subClassRangLmt += subClassIndent.replaceFirst("^0", "");
				} else if (subClassIndent.startsWith(".")) {
					subClassRangLmt += subClassIndent.replaceFirst("000", "");
				} else if (subClassIndent.matches("^\\d+(0[A-Z]{1,2})?$")) {
					subClassRangLmt += "." + subClassIndent.replaceFirst("0([A-Z]{1,2})$", "$1");
				} else {
					subClassRangLmt += subClassIndent;
				}
			}
		}

		Matcher matcher = REGEX.matcher(input);
		if (matcher.matches()) {
			String mainClass = matcher.group("mainClass");
			mainClass = mainClass.replaceFirst("^0+", "");
			mainClass = mainClass.replaceFirst("^D0", "D");
			setMainClass(mainClass);

			String subClass = matcher.group("subClass");
			String subClassIndent = matcher.group("subClassIndent"); // keep leading zeros.
			subClass = subClass.replaceFirst("^0+", "");

			if ("000".equals(subClassIndent)) {
				subClassIndent = "";
			}

			LOGGER.trace(
					input + " --> main(" + mainClass + ") sub(" + subClass + ") indent(" + subClassIndent + ")");

			if (subClassIndent != null) {
				if ("FOR".equals(subClass) || "DIG".equals(subClass)) {
					subClass += " " + subClassIndent.replaceFirst("^0+", "").replaceFirst("0([A-Z]{1,2})$", "$1");
				} else if (subClassIndent.matches("^0[A-Z]{1,2}$")) {
					subClass += subClassIndent.replaceFirst("^0", "");
				} else if (subClassIndent.startsWith(".")) {
					subClass += subClassIndent.replaceFirst("000", "");
				} else if (subClassIndent.matches("^\\d+(0[A-Z]{1,2})?$")) {
					subClass += "." + subClassIndent.replaceFirst("0([A-Z]{1,2})$", "$1");
				} else {
					subClass += subClassIndent;
				}
			}

			String[] subClassRange;
			if (subClassRangLmt != null) {
				subClassRange = new String[] { subClass, subClassRangLmt };
			} else {
				subClassRange = new String[] { subClass };
			}
			setSubClass(subClassRange);

			return;
		}

		parseFailed = true;
		LOGGER.debug("USPC parse failed '{}' as '{}'", classificationStr, input);
		throw new ParseException(
				"Failed to regex parse USPC Classification: '" + classificationStr + "' evaluated as: '" + input + "'",
				0);
	}

	@Override
	public boolean validate() throws InvalidDataException {
		if (parseFailed) {
			return false;
		}

		return true;
	}

	@Override
	public boolean isContained(PatentClassification check) {
		if (parseFailed || check == null || !(check instanceof IpcClassification)) {
			return false;
		}

		UspcClassification uspc = (UspcClassification) check;
		if (uspc.getSubClass().length != 0) {
			return getMainClass().equals(((UspcClassification) check).getMainClass());
		} else {
			for (String subClass : this.getSubClass()) {
				for (String checkSubClass : uspc.getSubClass()) {
					if (subClass.equals(checkSubClass)) {
						return true;
					}
				}
			}
			return false;
		}
	}

	@Override
	public String toString() {
		return "UspcClassification [mainClass=" + mainClass + ", subClass=" + Arrays.toString(subClass) + ", toText()="
				+ toText() + ", originalText=" + super.getTextOriginal() + "]";
	}

}
