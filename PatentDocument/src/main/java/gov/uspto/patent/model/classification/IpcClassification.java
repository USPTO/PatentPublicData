package gov.uspto.patent.model.classification;

import java.text.ParseException;
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

import gov.uspto.common.tree.Tree;
import gov.uspto.patent.InvalidDataException;

/**
 * <h3>International Patent Classification (IPC)</h3>
 *
 * <p>
 * The effort to build an international classification system began in the
 * 1970s. CPC and IPC classes and subclasses are historically related to United
 * Nations HS, ISIC, and SITC classification schemes.
 * </p>
 *
 * <p>
 * <h3>Structure Breakout: "H01S 3/00"</h3>
 * <li>H = Electricity section.
 * <li>01 = class symbol.
 * <li>S = subclass symbol.
 * <li>3/00 = main group has 00, else its a subgroup of main group.
 * </p>
 * B60R 900
 *
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 * @see http://www.wipo.int/classifications/ipc
 * @see http://www.wipo.int/export/sites/www/classifications/ipc/en/guide/guide_ipc.pdf}
 * @see http://web2.wipo.int/ipcpub}
 */
public class IpcClassification extends PatentClassification {

	private static Logger LOGGER = LoggerFactory.getLogger(IpcClassification.class);

	private final static Pattern REGEX_OLD = Pattern
			.compile("^([A-HY])\\s?(\\d\\d)([A-Z])\\s?(\\d\\s?\\d{1,3})/?(\\d{2,})$");

	private final static Pattern REGEX = Pattern.compile("^([A-HY])\\s?(\\d\\d)([A-Z])\\s{0,2}(\\d{1,4})/?(\\d{2,})$");
	private final static Pattern REGEX_LEN3 = Pattern.compile("^([A-HY])\\s{0,2}(\\d\\d)$");
	private final static Pattern REGEX_LEN4 = Pattern.compile("^([A-HY])\\s{0,2}(\\d\\d)([A-Z])$");

	private String section;
	private String mainClass;
	private String subClass;
	private String mainGroup;
	private String subGroup;
	private boolean parseFailed;

	public IpcClassification(String originalText, boolean mainOrInventive) {
		super(originalText, mainOrInventive);
	}

	public boolean isParseFailed() {
		return parseFailed;
	}

	@Override
	public ClassificationType getType() {
		return ClassificationType.IPC;
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

	public String[] getParts() {
		if (parseFailed) {
			return new String[] {};
		}
		return new String[] { section, mainClass, subClass, mainGroup, subGroup };
	}

	@Override
	public Tree getTree() {
		Tree tree = new Tree();
		if (parseFailed) {
			return tree;
		}
		tree.addChild(section).addChild(mainClass).addChild(subClass).addChild(mainGroup).addChild(subGroup);
		return tree;
	}

	@Override
	public String getTextNormalized() {
		if (parseFailed) {
			return super.getTextOriginal() + "__parseFailed";
		}

		StringBuilder sb = new StringBuilder().append(section).append(mainClass);

		if (subClass != null) {
			sb.append(subClass);

			if (mainGroup != null) {
				sb.append(" ").append(mainGroup);

				if (subGroup != null) {
					sb.append("/").append(subGroup);
				}
			}
		}

		return sb.toString();
	}

	public String standardize() {
		StringBuilder sb = new StringBuilder().append(section).append(mainClass);

		if (subClass != null) {
			sb.append(subClass).append("0");

			if (mainGroup != null && mainGroup.matches("^[0-9]+$")) {
				if (Integer.valueOf(mainGroup) < 10) {
					sb.append("0");
				}
				sb.append(mainGroup);

				if (subGroup != null) {
					sb.append(subGroup);
				}
			}
		}
		String changed = sb.toString();
		changed = String.format("%1$-9s", changed);

		return changed;
	}

	@Override
	public List<String> getSearchTokens() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Classification depth
	 * 
	 * (1=section, 2=mainClass, 3=subClass, 4=mainGroup, 5=subGroup)
	 * 
	 */
	public int getDepth() {
		int classDepth = 0;
		if (subGroup != null && !subGroup.isEmpty()) {
			classDepth = 5;
		} else if (mainGroup != null && !mainGroup.isEmpty()) {
			classDepth = 4;
		} else if (subClass != null && !subClass.isEmpty()) {
			classDepth = 3;
		} else if (mainClass != null && mainClass.isEmpty()) {
			classDepth = 2;
		} else if (section != null && section.isEmpty()) {
			classDepth = 1;
		}
		return classDepth;
	}

	@Override
	public boolean isContained(PatentClassification check) {
		if (parseFailed || check == null) {
			return false;
		}
		if (getClass() != check.getClass()) {
			return false;
		}

		IpcClassification cpc = (IpcClassification) check;

		int depth = getDepth();
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
		final IpcClassification other = (IpcClassification) obj;

		if (this == other || other.getDepth() == getDepth() && isContained(other)) {
			return true;
		}

		return false;
	}

	/**
	 * Parse classification text to create IpcClassification
	 * 
	 * @param classificationStr
	 * @return
	 * @throws ParseException
	 */
	@Override
	public void parseText(final String classificationStr) throws ParseException {

		Matcher matcher = REGEX_OLD.matcher(classificationStr);
		if (matcher.matches()) {
			String section = matcher.group(1);
			String mainClass = matcher.group(2);
			String subClass = matcher.group(3);
			String mainGroup = matcher.group(4).replace(' ', '0');
			String subGroup = matcher.group(5);

			setSection(section);
			setMainClass(mainClass);
			setSubClass(subClass);
			setMainGroup(mainGroup);
			setSubGroup(subGroup);
			return;
		}

		Matcher fullMatch = REGEX.matcher(classificationStr);
		if (fullMatch.matches()) {
			String section = fullMatch.group(1);
			String mainClass = fullMatch.group(2);
			String subClass = fullMatch.group(3);
			String mainGroup = fullMatch.group(4);
			String subGroup = fullMatch.group(5);

			setSection(section);
			setMainClass(mainClass);
			setSubClass(subClass);
			setMainGroup(mainGroup);
			setSubGroup(subGroup);
			return;
		}

		Matcher matchL3 = REGEX_LEN3.matcher(classificationStr);
		if (classificationStr.length() >= 3 && matchL3.matches()) {
			String section = matchL3.group(1);
			String mainClass = matchL3.group(2);
			setSection(section);
			setMainClass(mainClass);
			return;
		}

		Matcher matchL4 = REGEX_LEN4.matcher(classificationStr);
		if (classificationStr.length() >= 4 && matchL4.matches()) {
			String section = matchL4.group(1);
			String mainClass = matchL4.group(2);
			String subClass = matchL4.group(3);
			//System.out.println("4" + classificationStr + classificationStr.length());

			setSection(section);
			setMainClass(mainClass);
			setSubClass(subClass);
			return;
		} else {
			parseFailed = true;
			LOGGER.warn("IPC parse failed '{}'", classificationStr);
			throw new ParseException("Failed to regex parse IPC Classification: " + classificationStr, 0);
		}
	}

	@Override
	public boolean validate() throws InvalidDataException {
		if (parseFailed) {
			return false;
		}
		if (StringUtils.isEmpty(mainClass)) {
			throw new InvalidDataException("Invalid MainClass");
		}
		return true;
	}

	@Override
	public String toString() {
		return "IpcClassification [section=" + section + ", mainClass=" + mainClass + ", subClass=" + subClass
				+ ", mainGroup=" + mainGroup + ", subGroup=" + subGroup + ", inventive=" + super.isMainOrInventive()
				+ ", getTextNormalized()=" + getTextNormalized() + ", standardize()=" + standardize()
				+ ", getTextOriginal()=" + getTextOriginal() + ", toText()=" + toText() + "]";
	}

	/**
	 * IpcClassifications from collection of PatentClassifications, grouped by
	 * inventive or additional.
	 * 
	 * @param classes
	 * @return
	 */
	public static <T extends PatentClassification> Map<String, List<IpcClassification>> filterIpc(
			Collection<T> classes) {

		return classes.stream().filter(IpcClassification.class::isInstance).map(IpcClassification.class::cast)
				.collect(Collectors.groupingBy(s -> (s.isMainOrInventive() ? "inventive" : "additional"), TreeMap::new,
						Collectors.toList()));
	}

}
