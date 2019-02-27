package gov.uspto.patent.model.entity;

import org.apache.commons.text.WordUtils;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;

import gov.uspto.common.text.StringCaseUtil;
import gov.uspto.patent.InvalidDataException;

public class NamePerson extends Name {

	private final String firstName;
	private final String middleName;
	private final String lastName;

	public NamePerson(String firstName, String lastName) {
		this(firstName, null, lastName);
	}

	public NamePerson(String firstName, String middleName, String lastName) {
		super(buildFullName(firstName, middleName, lastName));

		this.firstName = firstName;
		this.middleName = middleName;
		this.lastName = lastName;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getMiddleName() {
		return middleName;
	}

	public String getLastName() {
		return lastName;
	}

	public boolean validate() throws InvalidDataException {
		String fullName = super.getName();

		if (Strings.isNullOrEmpty(lastName)) {
			throw new InvalidDataException("Invalid NamePerson, lastname can not be blank");
		}

		if (Strings.isNullOrEmpty(fullName) || fullName.length() < 2) {
			throw new InvalidDataException("Invalid NamePerson, fullName can not be blank or smaller than 2.");
		}

		return true;
	}

	/**
	 * Fix case for Name part when in all capitals.
	 * 
	 * @param part
	 * @return
	 */
	private String normalizeCase(String part) {
		if (part.matches("^[A-Z]+$")) {
			return StringCaseUtil.toTitleCase(part);
		} else {
			return part;
		}
	}

	/**
	 * Get abbreviated version of name, with normalized case.
	 * 
	 * @return Last Name, first initial.
	 */
	public String getAbbreviatedName() {
		if (!Strings.isNullOrEmpty(firstName)) {
			StringBuilder stb = new StringBuilder();
			stb.append(normalizeCase(lastName));
			stb.append(", ");
			stb.append(firstName.substring(0, 1).toUpperCase());
			stb.append(".");

			return stb.toString();
		} else {
			return normalizeCase(lastName);
		}
	}

	public String getInitials() {
		StringBuilder stb = new StringBuilder();
		if (!Strings.isNullOrEmpty(firstName)) {
			stb.append(firstName);
			stb.append(" ");
		}

		if (!Strings.isNullOrEmpty(middleName)) {
			stb.append(middleName);
			stb.append(" ");
		}

		stb.append(lastName);
		return WordUtils.initials(stb.toString());
	}

	private static String buildFullName(String firstName, String middleName, String lastName) {
		StringBuilder sb = new StringBuilder();
		if (!Strings.isNullOrEmpty(lastName)) {
			sb.append(lastName).append(", ");
		}

		if (!Strings.isNullOrEmpty(firstName)) {
			sb.append(Joiner.on(" ").skipNulls().join(firstName, middleName));
		}

		return sb.toString().trim();
	}

	@Override
	public String toString() {
		return "PersonName[firstName=" + firstName + ", middleName=" + middleName + ", lastName=" + lastName
				+ ", fullName=" + super.getName() + ", prefix=" + super.getPrefix() + ", suffix=" + super.getSuffix()
				+ ", synonym=" + super.getSynonyms() + "]";
	}
}
