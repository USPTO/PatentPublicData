package gov.uspto.patent.model.entity;

import gov.uspto.patent.InvalidDataException;

/**
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class NameOrg extends Name {

	public NameOrg(String fullName) {
		super(fullName);
	}

	public boolean validate() throws InvalidDataException {
		String fullName = super.getName();

		if (fullName == null || fullName.length() < 2) {
	            throw new InvalidDataException("Invalid NameOrg, lastname can not be blank");
		}

		return true;
	}

	@Override
	public String toString() {
		return "OrgName[" + super.toString() + "]";
	}
}
