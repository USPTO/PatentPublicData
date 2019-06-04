package gov.uspto.patent.model.classification;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Strings;

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

	private final static Pattern REGEX = Pattern.compile("^([^/]{3}|[^/]{1,2}(?=/))/?([^/]{3})\\.?([^\\.]{0,4})$");
	private final static Pattern SUBGROUP_RANGE = Pattern.compile("-([^/]{3})\\.?([^\\.]{0,4})$");

	private final static Set<String> MAIN_CLASSES = new HashSet<String>(Arrays.asList("002", "004", "005", "007", "008",
			"012", "014", "015", "016", "019", "023", "024", "026", "027", "028", "029", "030", "033", "034", "036",
			"037", "038", "040", "042", "043", "044", "047", "048", "049", "051", "052", "053", "054", "055", "056",
			"057", "059", "060", "062", "063", "065", "066", "068", "069", "070", "071", "072", "073", "074", "075",
			"076", "079", "081", "082", "083", "084", "086", "087", "089", "091", "092", "095", "096", "099", "100",
			"101", "102", "104", "105", "106", "108", "109", "110", "111", "112", "114", "116", "117", "118", "119",
			"122", "123", "124", "125", "126", "127", "128", "131", "132", "134", "135", "136", "137", "138", "139",
			"140", "141", "142", "144", "147", "148", "149", "150", "152", "156", "157", "159", "160", "162", "163",
			"164", "165", "166", "168", "169", "171", "172", "173", "174", "175", "177", "178", "180", "181", "182",
			"184", "185", "186", "187", "188", "190", "191", "192", "193", "194", "196", "198", "199", "200", "201",
			"202", "203", "204", "205", "206", "208", "209", "210", "211", "212", "213", "215", "216", "217", "218",
			"219", "220", "221", "222", "223", "224", "225", "226", "227", "228", "229", "231", "232", "234", "235",
			"236", "237", "238", "239", "241", "242", "244", "245", "246", "248", "249", "250", "251", "252", "254",
			"256", "257", "258", "260", "261", "264", "266", "267", "269", "270", "271", "273", "276", "277", "278",
			"279", "280", "281", "283", "285", "289", "290", "291", "292", "293", "294", "295", "296", "297", "298",
			"299", "300", "301", "303", "305", "307", "310", "312", "313", "314", "315", "318", "320", "322", "323",
			"324", "326", "327", "329", "330", "331", "332", "333", "334", "335", "336", "337", "338", "340", "341",
			"342", "343", "345", "346", "347", "348", "349", "351", "352", "353", "355", "356", "358", "359", "360",
			"361", "362", "363", "365", "366", "367", "368", "369", "370", "372", "373", "374", "375", "376", "377",
			"378", "379", "380", "381", "382", "383", "384", "385", "386", "388", "392", "396", "398", "399", "400",
			"401", "402", "403", "404", "405", "406", "407", "408", "409", "410", "411", "412", "413", "414", "415",
			"416", "417", "418", "419", "420", "422", "423", "424", "425", "426", "427", "428", "429", "430", "431",
			"432", "433", "434", "435", "436", "438", "439", "440", "441", "442", "445", "446", "449", "450", "451",
			"452", "453", "454", "455", "460", "462", "463", "464", "470", "472", "473", "474", "475", "476", "477",
			"482", "483", "492", "493", "494", "501", "502", "503", "504", "505", "506", "507", "508", "510", "512",
			"514", "516", "518", "520", "521", "522", "523", "524", "525", "526", "527", "528", "530", "532", "534",
			"536", "540", "544", "546", "548", "549", "552", "554", "556", "558", "560", "562", "564", "568", "570",
			"585", "588", "600", "601", "602", "604", "606", "607", "623", "700", "701", "702", "703", "704", "705",
			"706", "707", "708", "709", "710", "711", "712", "713", "714", "715", "716", "717", "718", "719", "720",
			"725", "726", "800", "850", "901", "902", "903", "930", "968", "976", "977", "984", "987", "D01", "D02",
			"D03", "D04", "D05", "D06", "D07", "D08", "D09", "D10", "D11", "D12", "D13", "D14", "D15", "D16", "D17",
			"D18", "D19", "D20", "D21", "D22", "D23", "D24", "D25", "D26", "D27", "D28", "D29", "D30", "D32", "D34",
			"D99", "G9B", "PLT"));

	private String mainClass;
	private String[] subClass;

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

	@Override
	public String[] getParts() {
		List<String> parts = new ArrayList<String>();
		parts.add(Strings.padStart(mainClass, 3, '0'));
		for(String claz: subClass) {
			parts.add(Strings.padStart(claz, 3, '0'));
		}
		return parts.toArray(new String[parts.size()]);
		//return new String[] { mainClass, subClass.toString() }; // multiple nesting.
		// return null;
	}

	@Override
	public int getDepth() {
		int classDepth = 0;

		if (subClass != null && subClass.length > 0) {
			classDepth = 2;
		}
		else if (mainClass != null) {
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
			String[] facets = ClassificationTokenizer.partsToFacet(Strings.padStart(mainClass, 3, '0'), Strings.padStart(subRange, 3, '0'));
			retFacets.addAll(Arrays.asList(facets));
		}

		return retFacets.toArray(new String[retFacets.size()]);
	}

	/**
	 * 
	 * Converts: " 602031" => [060, 060/203, 060/203.1] " 60204" => [060, 060/204]
	 * "148602" => [148, 148/602]
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
		Matcher rangeMatcher = SUBGROUP_RANGE.matcher(input);
		String subClassRangLmt = null;
		if (rangeMatcher.find()) {
			input = input.substring(0, rangeMatcher.start());
			subClassRangLmt = rangeMatcher.group(1);
			subClassRangLmt = subClassRangLmt.replaceFirst("^0+", "");

			String subClassIndent = rangeMatcher.group(2);
			if (subClassIndent != null) {
				if ("FOR".equals(subClassRangLmt) || "DIG".equals(subClassRangLmt)) {
					subClassRangLmt += " " + subClassIndent.replaceFirst("^0+", "");
				}
				if (subClassIndent.matches("^\\d+$")) {
					subClassRangLmt += "." + subClassIndent;
				} else if (subClassIndent.startsWith(".")) {
					subClassRangLmt += subClassIndent.replaceFirst("000", "");
				} else if (subClassIndent.matches("^\\d+$")) {
					subClassRangLmt += "." + subClassIndent;
				} else {
					subClassRangLmt += subClassIndent;
				}
			}
		}

		Matcher matcher = REGEX.matcher(input);
		if (matcher.matches()) {
			String mainClass = matcher.group(1);
			mainClass = mainClass.replaceFirst("^0+", "");
			mainClass = mainClass.replaceFirst("^D0", "D");
			setMainClass(mainClass);

			String subClass = matcher.group(2);
			String subClassIndent = matcher.group(3); // keep leading zeros.
			subClass = subClass.replaceFirst("^0+", "");

			if ("000".equals(subClassIndent)) {
				subClassIndent = "";
			}

			if (subClassIndent != null) {
				// System.out.println(input + " - " + subClassIndent);
				if ("FOR".equals(subClass) || "DIG".equals(subClass)) {
					subClass += " " + subClassIndent.replaceFirst("^0+", "");
				} else if (subClassIndent.startsWith(".")) {
					subClass += subClassIndent.replaceFirst("000", "");
				} else if (subClassIndent.matches("^\\d+$")) {
					subClass += "." + subClassIndent;
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

		throw new ParseException(
				"Failed to regex parse USPC Classification: '" + classificationStr + "' evaluated as: '" + input + "'",
				0);
	}

	@Override
	public boolean validate() throws InvalidDataException {
		if (mainClass.length() < 3 && mainClass.startsWith("D")){
			mainClass = mainClass.replaceFirst("D", "D0");
		}
		if (!MAIN_CLASSES.contains(Strings.padStart(mainClass, 3, '0'))) {
		 throw new InvalidDataException("Invalid MainClass: " + mainClass);
		}
		return true;
	}

	@Override
	public boolean isContained(PatentClassification check) {
		if (check == null || !(check instanceof IpcClassification)) {
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
