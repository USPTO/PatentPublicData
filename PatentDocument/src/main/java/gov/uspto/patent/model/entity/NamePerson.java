package gov.uspto.patent.model.entity;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;

import gov.uspto.common.text.StringCaseUtil;
import gov.uspto.patent.InvalidDataException;

/*
* TODO: need more research... name and entity disambiguation.
* 	Does the middle name or initial sometimes appear in the first-name field?
* 
* 		Function to get a list of name variants:
* 			first word in first name field,  with lastname field.
* 			first name last name
* 			first name, last name joined no space.
* 			first initial last name.
* 
* 		Function to get normalized "best guess" name.
* 
* 		Function to score uniqueness of names, need to create a domain name dictionary which contains uniqueness score within the corpus.
* 			john  0.0875
* 			doe	  0.0093
*			likely hood of word being lastname or first name... likely hood of finding first name and last name together...
*/
public class NamePerson extends Name {

    private String prefix; // Title: Mr., Mrs., Dr.
    private final String firstName;
    private final String middleName;
    private final String lastName;

    public NamePerson(String firstName, String lastName) throws InvalidDataException {
        this(firstName, null, lastName);
    }

    public NamePerson(String firstName, String middleName, String lastName) throws InvalidDataException {
        super(buildFullName(firstName, middleName, lastName));

        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;

        validate();
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public boolean validate() throws InvalidDataException {
        String fullName = super.getName();

        if (Strings.isNullOrEmpty(lastName)) {
            throw new InvalidDataException("Invalid PersonName, lastname can not be blank");
        }

        if (Strings.isNullOrEmpty(fullName) || fullName.length() < 2) {
            throw new InvalidDataException("Invalid PersonName, fullName can not be blank or smaller than 2.");
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

    public String getMiddleName() {
        return middleName;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
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
        return "PersonName[prefix=" + prefix + ", firstName=" + firstName + ", middleName=" + middleName + ", lastName="
                + lastName + ", fullName=" + super.getName() + ", suffix=" + super.getSuffix() + ", synonym="
                + super.getSynonyms() + "]";
    }
}
