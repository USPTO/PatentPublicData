package gov.uspto.patent.model.entity;

import java.util.LinkedHashSet;
import java.util.Set;

import com.google.common.base.Strings;

import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.model.CountryCode;

/**
 * 
 * Address and Contact Information
 *
 */
public class Address {

	private final String street;
	private final String city;
	private final String state;
	private final String zipcode;
	private final CountryCode country;
	private String phoneNumber;
	private String faxNumber;
	private String email;

	public Address(String city, String state, CountryCode country) {
		this(null, city, state, null, country);
	}

	public Address(String street, String city, String state, String zipcode, CountryCode country) {
		this.street = street;
		this.city = city;
		this.state = state;
		this.zipcode = zipcode;
		this.country = country;
	}

	public String getStreet() {
		return street;
	}

	public String getCity() {
		return city;
	}

	public String getState() {
		return state;
	}

	public String getZipCode() {
		return zipcode;
	}

	public CountryCode getCountry() {
		return country;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getFaxNumber() {
		return faxNumber;
	}

	public void setFaxNumber(String faxNumber) {
		this.faxNumber = faxNumber;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean validate() throws InvalidDataException {
		if (country == null) {
			throw new InvalidDataException("Invalid Address: country is null");
		}

		return true;
	}

	public String toText() {
		StringBuilder sb = new StringBuilder();

		for (String sub : new String[] { street, city, state, zipcode }) {
			if (!Strings.isNullOrEmpty(sub)) {
				sb.append(sub).append(", ");
			}
		}
		sb.append(country);

		return sb.toString();
	}

	/**
	 * Individual Tokens
	 * 
	 * Baltimore, MD, US => [Baltimore, MD, US, United States]
	 * 
	 * @return
	 */
	public Set<String> getTokenSet() {
		Set<String> tokens = new LinkedHashSet<String>();

		if (!Strings.isNullOrEmpty(city)) {
			tokens.add(city);
		}

		if (!Strings.isNullOrEmpty(state)) {
			tokens.add(state);
		}

		tokens.add(country.toString());
		tokens.add(country.getName());

		return tokens;
	}

	@Override
	public String toString() {
		return "Address [street=" + street + ", city=" + city + ", state=" + state + ", zipcode=" + zipcode
				+ ", country=" + country + ", phoneNumber=" + phoneNumber + ", faxNumber=" + faxNumber + ", email="
				+ email + ", toText()=" + toText() + ", getTokenSet()=" + getTokenSet() + "]";
	}

}
