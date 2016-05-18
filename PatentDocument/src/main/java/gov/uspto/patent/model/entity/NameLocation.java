package gov.uspto.patent.model.entity;

import javax.naming.directory.InvalidAttributesException;

/**
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class NameLocation extends Name {

	public Address address;

	public NameLocation(Address address) throws InvalidAttributesException{
		super(address.toText());
	}

	@Override
	public String toString() {
		return "NameLocation["+ super.toString() +"]";
	}
}
