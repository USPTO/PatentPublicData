package gov.uspto.patent.model.classification;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import gov.uspto.common.tree.Node;
import gov.uspto.common.tree.Tree;
import gov.uspto.patent.InvalidDataException;

/**
 * <h3>Cooperative Patent Classification</h3>
 * <p>
 * Partnership between EPO and USPTO, as a joint effort to develop a common,
 * internationally compatible classification system for technical documents, in
 * particular patent publications, which will be used by both offices in the
 * patent granting process.
 * </p>
 * <p>
 * Classification code/symbol format same as IPC, but the groups differ in
 * areas. The CPC has many more subdivisions than the IPC, that is, many more
 * subgroups. These internal subgroups start with the symbol of an IPC group
 * (not necessarily of the current edition), followed by a further combination
 * of numbers. In some cases, an IPC group is not used in the CPC structure. In
 * this situation, the IPC group will not be present in the CPC structure.
 * </p>
 * <p>
 * The CPC is substantially based on the previous European classification system
 * (ECLA) of which closely mirrored the IPC Classification System.
 * </p>
 * <li>1 January 2013, EPO replaced European Classification (ECLA).
 * <li>1 January 2015, USPTO replaced most of the USPC System.
 *
 * <p>
 * <h3>Structure Breakout: "A01B33/00"</h3>
 * <li>Section A (one letter A-H and Y)
 * <li>Class 01 (two digits)
 * <li>Sub Class B (1-3 digits)
 * <li>Main Group 33 (1-3 digits)
 * <li>Sub group 00 (at least 2 digits; CPC specific since the IPC does not have
 * subgroups)
 * </p>
 * </br>
 * <p>
 * <h3>Parse classification string:</h3>
 * 
 * <pre>
 * {
 * 	&#64;code
 * 	CpcClassification cpc = CpcClassification.fromText(originalText);
 * 	cpc.setIsMainClassification(true);
 * }
 * </pre>
 * </p>
 *
 * <p>
 * <h3>Create Classification by its individual parts:</h3>
 * 
 * <pre>
 * {
 * 	&#64;code
 * 	CpcClassification cpc = new CpcClassification();
 * 	cpc.setSection(section);
 * 	cpc.setMainClass(mainClass);
 * 	cpc.setSubClass(subClass);
 * 	cpc.setMainGroup(mainGroup);
 * 	cpc.setSubGroup(subGroup);
 * 	cpc.setIsMainClassification(true);
 * 	cpc.setType(CpcClassification.TYPE.INVENTIVE);
 * }
 * </pre>
 * </p>
 *
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 * @see http://www.cooperativepatentclassification.org/index.html
 */
public class CpcClassification extends PatentClassification {

	private static Logger LOGGER = LoggerFactory.getLogger(CpcClassification.class);

	private final static Pattern REGEX = Pattern.compile(
			"^(?<section>[A-HY])(?<mainClass>\\d\\d)(?<subClass>[A-Z])\\s?(?<mainGroup1>\\d{1,4})/?(?<subGroup1>\\d{2,6})(-(?<mainGroup2>\\d{1,4})/(?<subGroup2>\\d{2,6})|-(?<subGroup2SameMain>\\d{2,6}))?$");

	private final static Pattern REGEX_LEN3 = Pattern.compile("^([A-HY])(\\d\\d)$");

	private final static Pattern REGEX_LEN4 = Pattern.compile("^([A-HY])(\\d\\d)([A-Z])$");

	private String section;
	private String mainClass;
	private String subClass;
	private String[] mainGroup;
	private String[] subGroup;
	private boolean parseFailed = false;

	public CpcClassification(String originalText, boolean mainOrInventive) {
		super(originalText, mainOrInventive);
	}

	public boolean isParseFailed() {
		return parseFailed;
	}

	@Override
	public ClassificationType getType() {
		return ClassificationType.CPC;
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

	public String[] getMainGroup() {
		return mainGroup;
	}

	public void setMainGroup(String[] mainGroup) {
		this.mainGroup = mainGroup;
	}

	public String[] getSubGroup() {
		return subGroup;
	}

	public void setSubGroup(String[] subGroup) {
		this.subGroup = subGroup;
	}

	/**
	 * Text Representation normalized.
	 * 
	 * "D07B2201/2051" => "D07B 2201/2051"
	 * 
	 */
	@Override
	public String getTextNormalized() {
		if (parseFailed) {
			return super.getTextOriginal() + "__parseFailed";
		}

		StringBuilder sb = new StringBuilder().append(section).append(mainClass);

		if (subClass != null) {
			sb.append(subClass);

			if (mainGroup != null) {
				sb.append(" ").append(mainGroup[0]);

				if (subGroup != null) {
					sb.append("/").append(subGroup[0]);
				}
			}

			// Handle Ranges
			if (mainGroup != null && subGroup != null) {
				if (mainGroup.length == 1 && subGroup.length == 2 && subGroup[1] != null) {
					sb.append("-");
					// sb.append(mainGroup[0]);
					// sb.append("/");
					sb.append(subGroup[1]);
				} else if (mainGroup.length == 2 && subGroup.length == 2 && mainGroup[1] != null) {
					sb.append("-");
					if (mainGroup[0] == mainGroup[1]) {
						sb.append(subGroup[1]);
					} else {
						sb.append(mainGroup[1]);
						sb.append("/");
						sb.append(subGroup[1]);
					}
				}
			}
		}

		return sb.toString();
	}

	@Override
	public List<String> getSearchTokens() {
		return getTree().getLeafPaths("").stream().map(c -> String.format("%1$-16s", c).replace(' ', '0'))
				.collect(Collectors.toList());
	}

	/**
	 * Text Representation Standardized; padded with zeros.
	 * 
	 * "D07B2201/2051" => "D07B022012051"
	 * 
	 */
	public String standardize() {
		StringBuilder sb = new StringBuilder().append(section).append(mainClass);
		if (subClass != null) {
			sb.append(subClass).append("0");

			if (mainGroup != null) {
				sb.append(mainGroup[0]);

				if (subGroup != null) {
					sb.append(subGroup[0]);
				}
			}

			if (mainGroup != null && subGroup != null) {
				if (mainGroup.length == 1 && subGroup.length == 2 && subGroup[1] != null) {
					sb.append("-").append(subGroup[1]);
				} else if (mainGroup.length == 2 && subGroup.length == 2 && mainGroup[1] != null) {
					sb.append("-").append(mainGroup[1]).append(subGroup[1]);
				}
			}
		}

		/*
		 * if (subClass != null) { sb.append(subClass).append("0");
		 * 
		 * if (mainGroup != null) { for(String mGroup: mainGroup) { if (mGroup != null
		 * && mGroup.matches("^[0-9]+$")) { if (Integer.valueOf(mGroup) < 10) {
		 * sb.append("0"); } sb.append(mGroup);
		 * 
		 * if (subGroup != null) { for(String sGroup: subGroup) { if (subGroup != null)
		 * { sb.append(sGroup); } } } } } } }
		 */

		String changed = sb.toString();
		changed = String.format("%1$-9s", changed);

		return changed;
	}

	@Override
	public Tree getTree() {
		Tree tree = new Tree();
		if (parseFailed) {
			return tree;
		}

		Node pnode = tree.addChild(section);
		pnode = pnode.addChild(Strings.padStart(mainClass, 2, '0'));
		if (subClass != null) {
			pnode = pnode.addChild(Strings.padEnd(subClass, 3, '0'));

			if (mainGroup != null && subGroup != null) {
				if (mainGroup.length == 1 && subGroup.length == 1) {
					pnode.addChild(Strings.padStart(mainGroup[0], 3, '0'))
							.addChild(Strings.padEnd(subGroup[0], 4, '0'));
				} else if (mainGroup.length == 2 && subGroup.length == 2) {
					pnode.addChild(Strings.padStart(mainGroup[0], 3, '0'))
							.addChild(Strings.padEnd(subGroup[0], 4, '0'));
					pnode.addChild(Strings.padStart(mainGroup[1], 3, '0'))
							.addChild(Strings.padEnd(subGroup[1], 4, '0'));
				} else if (mainGroup.length == 1 && subGroup.length > 1) {
					List<String> children = Arrays.asList(subGroup).stream().map(s -> Strings.padEnd(s, 4, '0'))
							.collect(Collectors.toList());
					pnode.addChild(Strings.padStart(mainGroup[0], 3, '0')).addChildren(children);
				}
			}
		}

		return tree;
	}

	/**
	 * Classification depth
	 * 
	 * (1=section, 2=mainclass, 3=subclass, 4=mainGroup, 5=subGroup)
	 * 
	 */
	@Override
	public int getDepth() {
		int classDepth = 0;
		if (subGroup != null && subGroup[0] != null) {
			classDepth = 5;
		} else if (mainGroup != null && mainGroup[0] != null) {
			classDepth = 4;
		} else if (subClass != null && !subClass.isEmpty()) {
			classDepth = 3;
		} else if (mainClass != null && !mainClass.isEmpty()) {
			classDepth = 2;
		} else if (section != null && !section.isEmpty()) {
			classDepth = 1;
		}
		return classDepth;
	}

	@Override
	public boolean isContained(PatentClassification check) {
		if (parseFailed || check == null || !(check instanceof CpcClassification)) {
			return false;
		}
		CpcClassification cpc = (CpcClassification) check;

		int depth = getTree().getMaxDepth();

		if (depth == cpc.getTree().getDepth()) {
			if (this.getTextNormalized().equals(cpc.getTextNormalized())) {
				return true;
			} else {
				return false;
			}
		}
		if (depth == 5) {
			if (section.equals(cpc.getSection()) && mainClass.equals(cpc.getMainClass())
					&& subClass.equals(cpc.getSubClass()) && mainGroup.equals(cpc.getMainGroup())
					&& subGroup.equals(cpc.getSubGroup())) {
				return true;
			}
		} else if (depth == 4) {
			if (section.equals(cpc.getSection()) && mainClass.equals(cpc.getMainClass())
					&& subClass.equals(cpc.getSubClass()) && mainGroup.equals(cpc.getMainGroup())) {
				return true;
			}
		} else if (depth == 3) {
			if (section.equals(cpc.getSection()) && mainClass.equals(cpc.getMainClass())
					&& subClass.equals(cpc.getSubClass())) {
				return true;
			}
		} else if (depth == 2) {
			if (section.equals(cpc.getSection()) && mainClass.equals(cpc.getMainClass())) {
				return true;
			}
		} else if (depth == 1) {
			if (section.equals(cpc.getSection())) {
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

		if (this == other || other.toText().equals(toText()) ) {
			return true;
		}

		return false;
	}

	/**
	 * Parse Text to generate CpcClassifiation
	 * 
	 * @param classificationStr
	 * @return
	 * @throws ParseException
	 */
	@Override
	public void parseText(final String classificationStr) throws ParseException {

		Matcher matcher = REGEX.matcher(classificationStr);
		if (classificationStr.length() > 4 && matcher.matches()) {
			String section = matcher.group("section");
			String mainClass = matcher.group("mainClass");
			String subClass = matcher.group("subClass");
			String mainGroup1 = matcher.group("mainGroup1");
			String subGroup1 = matcher.group("subGroup1");

			String[] mainGroup = null;
			String[] subGroup = null;

			String mainGroup2 = matcher.group("mainGroup2");
			String subGroup2 = matcher.group("subGroup2");
			String subGroup2SameMain = matcher.group("subGroup2SameMain");

			if (mainGroup2 != null && !mainGroup2.equals(mainGroup1)) {
				mainGroup = new String[] { mainGroup1, mainGroup2 };
			} else if (mainGroup2 == null && subGroup2 != null) {
				mainGroup = new String[] { mainGroup1, mainGroup1 };
			} else {
				mainGroup = new String[] { mainGroup1 };
			}

			if (subGroup2 != null) {
				subGroup = new String[] { subGroup1, subGroup2 };
			} else if (subGroup2SameMain != null) {
				subGroup = new String[] { subGroup1, subGroup2SameMain };
			} else {
				subGroup = new String[] { subGroup1 };
			}

			LOGGER.trace(classificationStr + " " + Arrays.toString(mainGroup) + " " + Arrays.toString(subGroup));

			setSection(section);
			setMainClass(mainClass);
			setSubClass(subClass);
			setMainGroup(mainGroup);
			setSubGroup(subGroup);
			
			LOGGER.debug("'{}','{}'", classificationStr, this.toText());
		} else if (classificationStr.length() == 3) {
			Matcher matchL3 = REGEX_LEN3.matcher(classificationStr);
			if (matchL3.matches()) {
				String section = matchL3.group(1);
				String mainClass = matchL3.group(2);
				setSection(section);
				setMainClass(mainClass);
			}
		} else if (classificationStr.length() == 4) {
			Matcher matchL4 = REGEX_LEN4.matcher(classificationStr);
			if (matchL4.matches()) {
				String section = matchL4.group(1);
				String mainClass = matchL4.group(2);
				String subClass = matchL4.group(3);

				setSection(section);
				setMainClass(mainClass);
				setSubClass(subClass);
			}
		} else {
			parseFailed = true;
			LOGGER.debug("CPC parse failed '{}'", classificationStr);
			throw new ParseException("Failed to regex parse CPC Classification: " + classificationStr, 0);
		}
	}

	@Override
	public boolean validate() throws InvalidDataException {
		if (parseFailed) {
			return false;
		}
		if (StringUtils.isEmpty(mainClass)) {
			throw new InvalidDataException("Invalid SubSection");
		}
		return true;
	}

	@Override
	public String toString() {
		return "CpcClassification [section=" + section + ", mainClass=" + mainClass + ", subClass=" + subClass
				+ ", mainGroup=" + Arrays.toString(mainGroup) + ", subGroup=" + Arrays.toString(subGroup)
				+ ", parseFailed=" + parseFailed + ", getTextOriginal()=" + getTextOriginal() + ", isMainOrInventive()="
				+ isMainOrInventive() + ", toText()=" + toText() + ", toFacet()=" + getTree().getLeafFacets() + "]";
	}

	/**
	 * CpcClassifications from list of PatentClassifications, grouped by inventive
	 * or additional.
	 * 
	 * @param classes
	 * @return
	 */
	public static <T extends PatentClassification> Map<String, List<CpcClassification>> filterCpc(
			Collection<T> classes) {

		return classes.stream().filter(CpcClassification.class::isInstance).map(CpcClassification.class::cast)
				.collect(Collectors.groupingBy(s -> (s.isMainOrInventive() ? "inventive" : "additional"), TreeMap::new,
						Collectors.toList()));
	}

}
