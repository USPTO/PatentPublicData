package gov.uspto.patent.model.entity;

import javax.naming.directory.InvalidAttributesException;

import gov.uspto.common.text.StringCaseUtil;
import gov.uspto.patent.InvalidDataException;

public class NameLocation extends Name {

	public Address address;

	public NameLocation(Address address) throws InvalidAttributesException {
		this.address = address;
	}

	@Override
	public String getName() {
		return address.toText();
	}

	@Override
	public String getNameNormalizeCase() {
		return StringCaseUtil.toTitleCase(getName());
	}

	@Override
	public String getInitials() {
		//throw new RuntimeException("Not Implemented");
		return null;
	}

	@Override
	public boolean validate() throws InvalidDataException {
		return true;
	}

	@Override
	public String toString() {
		return "NameLocation[" + super.toString() + "]";
	}

}
