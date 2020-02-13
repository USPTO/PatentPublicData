package gov.uspto.patent.model.entity;

import java.text.Normalizer;

import gov.uspto.common.text.NameUtil;
import gov.uspto.common.text.StringCaseUtil;
import gov.uspto.common.text.WordUtil;
import gov.uspto.patent.InvalidDataException;

public class NameOrg extends Name {

	private String fullName;

	public NameOrg(String fullName) {
		this.fullName = fullName;
	}

	@Override
	public String getName() {
		return fullName;
	}

	/**
	 * Get name normalized if all capitals, else get name as is.
	 */
	@Override
	public String getNameNormalizeCase() {
		if (!WordUtil.isMixedCase(getName())){
			return NameUtil.normalizeOrgNameCase(getName());
		}
		return getName();
	}

	/**
	 * Abbreviated ngram form: first two words, first character, and two trailing
	 * non-repeating consonants.
	 * <p>
	 * United States President => UNT-STS <BR/>
	 * United States President Council => UNT-STS-PRS
	 * </p>
	 */
	@Override
	public String getInitials() {
		String shortest = super.getShortestSynonym();
		String check1 = shortest.isEmpty() ? getName() : shortest;
		String[] reduced = StringCaseUtil.removeLowercaseTitleWords(check1.split("[\\s-]+"));
		String[] words = String.join(" ", reduced).split("[\\s-]+", 2);

		StringBuilder stb = new StringBuilder();
		for(String word: words) {
			stb.append(abbreviateText(word, 3)).append("-");
		}
		if (stb.length() > 0) {
			stb.replace(stb.length() - 1, stb.length(), "");
		}

		return stb.toString();
	}

	/**
	 * Abbreviate Text
	 *
	 * <p>
	 * 1) Upper case 
	 * 3) Normalize Unicode to ASCII 
	 * 4) Remove non-alphanumeric
	 * 5) first character kept the same
	 * 6) Remove all vowels plus H and W, after first character
	 * 7) Transpose some letters and letter combinations (light weight soundex)
	 * 8) Remove duplicate characters
	 * 9) truncate to desired length
	 * </p>
	 * 
	 * @param check
	 * @return
	 */
	private String abbreviateText(String check, int len) {
		if (check.length() < 1) {
			return check;
		}

		String check1 = check.toUpperCase().trim();
		check1 = Normalizer.normalize(check1, Normalizer.Form.NFD);
		check1 = check1.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
		
		// check1 = check1.replaceAll("[^\\p{ASCII}]", "");
		check1 = check1.replaceAll("[^A-Z0-9]", "");
		
		if (check1.length() < 1) {
			return check1;
		}
		
		String firstLetter = check1.substring(0, 1);
		check1 = check1.substring(1);

		check1 = check1.replaceAll("(?:SH|CH)", "X");
		check1 = check1.replaceAll("C([IEY])", "S$1");
		check1 = check1.replaceAll("(?:C|Q|X|G)", "K");
		check1 = check1.replaceAll("D", "T");
		check1 = check1.replaceAll("Z", "S");
		check1 = check1.replaceAll("(?:V|PH)", "F");
		check1 = check1.replaceAll("B", "P");

		// Remove AaEeIiOoUuYyHhWw
		check1 = check1.replaceAll("[AaEeIiOoUuYyHhWw]", "");

		check1 = firstLetter + check1;
		check1 = check1.replaceAll("(.)\\1{1,}", "$1");

		return check1.length() < len ? check1 : check1.substring(0, len);
	}

	public boolean validate() throws InvalidDataException {
		String fullName = getName();

		if (fullName == null || fullName.trim().length() < 2) {
			throw new InvalidDataException("Invalid NameOrg: name can not be blank");
		}

		return true;
	}

	protected String nonrepeatingConstants(final String text, int num) {
		String unique = "";
		String text2 = text.replaceAll("[^A-z]", "");
		for (int i = 0; i < text.length() - 2; i++) {
			if (text2.charAt(i) != text2.charAt(i + 1)) {
				unique += text2.charAt(i);
				if (unique.length() == num) {
					break;
				}
			}
		}
		return unique;
	}

	@Override
	public String toString() {
		return "OrgName[" + super.toString() + "]";
	}

}
