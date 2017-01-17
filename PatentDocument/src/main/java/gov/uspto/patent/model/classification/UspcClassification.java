package gov.uspto.patent.model.classification;

import java.text.ParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Strings;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;

/**
 * <h3>USPC Classification</h3>
 * <p>
 * <h3>Structure Breakout: "417/161.1A"</h3>
 * <li>identifies Class 417, Subclass 161.1A
 * <li>5-6 digits, with left padding zeros i.e: "002".
 * <li>Section I (Class) : first three digits: 002-987, D01-D99, G9B, PLT
 * <li>Section II (Subclass): Next 2-3 digits; may have trailing decimal and
 * digits, may also be a range.
 * </p>
 * <p>
 * Parse classification string:
 * 
 * <pre>
 * {
 * 	&#64;code
 *  UspcClassification uspc = new UspcClassification();
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
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class UspcClassification extends PatentClassification {

	// 074 89140
	private final static Pattern REGEX = Pattern.compile("^([0-9DGP][0-9L][0-9BT])/?([0-9A-Z]{1,9})$");
	private final static Pattern RANGE_REGEX = Pattern.compile("-([0-9A-Z]{1,9})$");

	private String mainClass;
	private SortedSet<String> subClass = new TreeSet<String>();

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

	public SortedSet<String> getSubClass() {
		return subClass;
	}

	public void setSubClass(String subClass) {
		this.subClass.add(subClass);
	}

	public void setSubClass(SortedSet<String> subClass) {
		this.subClass = subClass;
	}

	@Override
	public String[] getParts() {
		//return new String[]{mainClass, subClass}; // multiple nesting.
		return null;
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

	/**
	 * Normalized text in format mainClass/subClass
	 * 
	 * @return
	 */
	@Override
	public String getTextNormalized() {
		StringBuilder stb = new StringBuilder().append(mainClass).append('/');
		for (String subRange : subClass) {
			stb.append(subRange).append(',');
		}
		stb.deleteCharAt(stb.length() - 1);
		return stb.toString();
	}

	/**
	 * Facets used for Search
	 * 
	 * PLT101 => [0/PLT, 1/PLT/PLT101000]
	 * 
	 */
	@Override
	public String[] toFacet() {
		Set<String> retFacets = new HashSet<String>();
		for (String subRange : subClass) {
			String[] facets = ClassificationTokenizer.partsToFacet(mainClass, subRange);
			retFacets.addAll(Arrays.asList(facets));
		}

		return retFacets.toArray(new String[retFacets.size()]);
	}

	/**
	 * 
	 * Converts: " 602031" => [060, 060/203, 060/203.1] " 60204" => [060,
	 * 060/204] "148602" => [148, 148/602]
	 * 
	 * @return
	 */
	public Set<String> toSet() {
		Set<String> formats = new LinkedHashSet<String>();
		formats.add(mainClass);

		for (String subRange : subClass) {
			if (subRange.length() >= 3) {
				String format2 = new StringBuilder().append(mainClass).append("/").append(subRange.substring(0, 3))
						.toString();
				formats.add(format2);
			}

			if (subRange.length() > 3) {
				String format3 = new StringBuilder().append(mainClass).append("/").append(subRange.substring(0, 3))
						.append(".").append(subRange.substring(3, subRange.length())).toString();

				// Trim trailing Zeros.
				format3 = format3.replaceFirst("\\.0*$|(\\.\\d*?)0+$", "$1");

				formats.add(format3);
			}
		}

		return formats;
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
		super.setTextOriginal(classificationStr);

		String input = classificationStr.toUpperCase().replace(' ', '0');

		// Match USPC Classification Range.
		Matcher rangeMatcher = RANGE_REGEX.matcher(input);
		String backRange = null;
		if (rangeMatcher.find()) {
			input = input.substring(0, rangeMatcher.start());
			backRange = rangeMatcher.group(1);
		}

		// Handle Leading Space " D2907" --> "D02/907000"
		if (classificationStr.startsWith(" ")) {
			Pattern pattern = Pattern.compile("^0(\\D)(\\d)");
			Matcher match = pattern.matcher(input);
			if (match.find()) {
				input = match.replaceFirst(match.group(1) + "0" + match.group(2));
			}
		}

		input = input.replaceFirst("^0+D", "D");

		Matcher matcher = REGEX.matcher(input);
		if (matcher.matches()) {
			String mainClass = matcher.group(1);
			String subClass = matcher.group(2);

			setMainClass(mainClass);

			SortedSet<String> subClassRange = new TreeSet<String>();
			subClassRange.add(Strings.padEnd(subClass, 9, '0'));

			if (backRange != null) {
				String rangeSubclass;
				String frontRange;

				if (subClass.length() > backRange.length()) {
					frontRange = subClass.substring(0, subClass.length() - backRange.length());
					rangeSubclass = subClass.substring(0, subClass.length() - backRange.length()) + backRange;
				} else {
					frontRange = subClass;
					rangeSubclass = backRange;
				}

				// Expansion of Numeric Range.
				if (frontRange.matches("^\\d+$") && backRange.matches("^\\d+$")
						&& Integer.valueOf(frontRange) - 100 < Integer.valueOf(backRange)
						&& Integer.valueOf(frontRange) < Integer.valueOf(backRange)) {
					Integer range1 = Integer.valueOf(frontRange);
					Integer range2 = Integer.valueOf(backRange);

					if (range2 - range1 > 0 && range2 - range1 < 100) {
						Iterator<Integer> range = (Iterator<Integer>) ContiguousSet
								.create(Range.closed(range1, range2), DiscreteDomain.integers()).iterator();
						while (range.hasNext()) {
							String subRange = Strings.padStart(String.valueOf(range.next()), 3, '0');
							subRange = Strings.padEnd(subRange, 9, '0');
							subClassRange.add(subRange);
						}
					} else {
						throw new ParseException("Failed to parse USPC Classification RANGE: '" + classificationStr
								+ "' Range: '" + range1 + "-" + range2 + "'", 0);
					}

				} else {
					subClassRange.add(Strings.padEnd(rangeSubclass, 9, '0'));
				}
			}

			setSubClass(subClassRange);
			return;
		}

		throw new ParseException(
				"Failed to regex parse USPC Classification: '" + classificationStr + "' transposed to: '" + input + "'",
				0);

	}

	@Override
	public boolean isContained(PatentClassification check){
		if (check == null || !(check instanceof IpcClassification)) {
			return false;
		}

		UspcClassification uspc = (UspcClassification) check;
		if (uspc.getSubClass().isEmpty()) {
			return getMainClass().equals(((UspcClassification) check).getMainClass());
		} else {
			for (String subClass : this.getSubClass()) {
				for (String checkSubClass : uspc.getSubClass()) {
					if (subClass.equals(checkSubClass)){
						return true;
					}
				}
			}
			return false;
		}
	}

	@Override
	public String toString() {
		return "UspcClassification [mainClass=" + mainClass + ", subClass=" + subClass + ", toText()=" + toText()
		// + ", toSet()=" + toSet()
				+ ", originalText=" + super.getTextOriginal()
				// + ", range=" + range
				+ "]";
	}

    /**
     * Parse Facet back into Classifications
     */
    public static List<UspcClassification> fromFacets(List<String> facets) {
        return ClassificationTokenizer.fromFacets(facets, UspcClassification.class);
    }
}
