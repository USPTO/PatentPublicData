package gov.uspto.patent.doc.greenbook.items;

import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.ItemReader;
import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.CountryCodeHistory;
import gov.uspto.patent.model.entity.Address;

/**
 * <h3>ADR Individual or organization address</h3>
 * <p>
 * </p>
 * <p>
 * 
 * <pre>
 *{@code
 *   <STR>1 Main St</STR>
 *   <CTY>Somecity</CTY>
 *   <STA>IL</STA>
 *   <ZIP>60544</ZIP>
 *   <CNT>US</CNT>
 *}
 * </pre>
 * </p>
 *
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class AddressNode extends ItemReader<Address> {
	private static final Logger LOGGER = LoggerFactory.getLogger(AddressNode.class);

	private static final CountryCode DEFAULT_COUNTRYCODE = CountryCode.US;
	private CountryCode defaultCountryCode;

	public AddressNode(Node itemNode) {
		this(itemNode, DEFAULT_COUNTRYCODE);
	}

	public AddressNode(Node itemNode, CountryCode defaultCountryCode) {
		super(itemNode);
		this.defaultCountryCode = defaultCountryCode;
	}

	@Override
	public Address read() {
		Node streetN = itemNode.selectSingleNode("STR");
		String street = streetN != null ? streetN.getText() : null;

		Node cityN = itemNode.selectSingleNode("CTY");
		String city = cityN != null ? cityN.getText() : null;

		Node stateN = itemNode.selectSingleNode("STA");
		String state = stateN != null ? stateN.getText() : null;

		Node zipcodeN = itemNode.selectSingleNode("ZIP");
		String zipcode = zipcodeN != null ? zipcodeN.getText() : null;

		Node countryN = itemNode.selectSingleNode("CNT");
		String countryCodeStr = countryN != null ? countryN.getText() : null;

		CountryCode countryCode = getCountryCode(countryCodeStr);

		Address address = new Address(street, city, state, zipcode, countryCode);

		try {
			address.validate();
		} catch (InvalidDataException e) {
			LOGGER.warn("Invalid Address: {}", itemNode.getParent().asXML(), e);
		}

		return address;
	}

	/**
	 * Country Code
	 * 
	 * Fix 3 digit country codes, in two digit country code field, by removing
	 * the trailing "X" or number (0-9) example: (DE is DEX, NL is NLX, GB1,
	 * GB2).
	 * 
	 * @param country
	 * @return
	 */
	public static CountryCode getCountryCode(String country) {
		if (country == null) {
			return CountryCode.UNDEFINED;
		}

		if (country.length() == 3) {
			country = country.replaceFirst("(?:X|[0-9])$", "");
		}

		CountryCode countryCode = CountryCode.UNKNOWN;
		try {
			countryCode = CountryCode.fromString(country);
		} catch (InvalidDataException e) {
			LOGGER.warn("Invalid Country Code: '{}'", country);
		}

		if (countryCode == CountryCode.UNKNOWN) {
			countryCode = AddressNode.getCountryCodeHistoric(country);
		}

		return countryCode;
	}

	/**
	 * Country Code
	 * 
	 * Fix 3 digit country codes, in two digit country code field, by removing
	 * the trailing "X" or number (0-9) example: (DE is DEX, NL is NLX, GB1,
	 * GB2).
	 * 
	 * @param country
	 * @return
	 */
	public static CountryCode getCountryCodeHistoric(String country) {
		if (country == null) {
			return CountryCode.UNDEFINED;
		}

		if (country.length() == 3) {
			country = country.replaceFirst("(?:X|[0-9])$", "");
		}

		CountryCode countryCode = CountryCodeHistory.getCurrentCode(country);

		LOGGER.warn("Historic Country Code: '{}' maps to '{}'", country, countryCode);

		return countryCode;
	}

}
