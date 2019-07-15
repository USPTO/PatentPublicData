package gov.uspto.patent.doc.greenbook.items;

import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;
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

	private static final XPath CITYXP = DocumentHelper.createXPath("CTY");
	private static final XPath STATEXP = DocumentHelper.createXPath("STA");
	private static final XPath CNTRYXP = DocumentHelper.createXPath("CNT");
	
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
		/*
		Node streetN = itemNode.selectSingleNode("STR");
		String street = streetN != null ? streetN.getText() : null;

		Node zipcodeN = itemNode.selectSingleNode("ZIP");
		String zipcode = zipcodeN != null ? zipcodeN.getText() : null;
		*/

		Node cityN = CITYXP.selectSingleNode(itemNode);
		String city = cityN != null ? cityN.getText() : null;

		Node stateN = STATEXP.selectSingleNode(itemNode);
		String state = stateN != null ? stateN.getText() : null;

		Node countryN = CNTRYXP.selectSingleNode(itemNode);
		CountryCode countryCode = getCountryCode(countryN);
		if (CountryCode.UNDEFINED.equals(countryCode)) {
			countryCode = defaultCountryCode;
		}

		//Address address = new Address(street, city, state, zipcode, countryCode);
		Address address = new Address(city, state, countryCode);

		return address;
	}

	/**
	 * Country Code
	 * 
	 * Fix 3 digit country codes, in two digit country code field, by removing the
	 * trailing "X" or number (0-9) example: (DE is DEX, NL is NLX, GB1, GB2, GB3).
	 * 
	 * @param country
	 * @return
	 */
	public static CountryCode getCountryCode(Node countryNode) {
		if (countryNode == null) {
			return CountryCode.UNDEFINED;
		}

		String country = countryNode.getText();

		if (country.length() == 3) {
			country = country.replaceFirst("(?:X|[0-9])$", "");
		}

		CountryCode countryCode = CountryCode.UNKNOWN;
		try {
			countryCode = CountryCode.fromString(country);
		} catch (InvalidDataException e) {
			// LOGGER.warn("{} : {}", country, countryNode.getParent().asXML());
			countryCode = AddressNode.getCountryCodeHistoric(countryNode);
		}

		return countryCode;
	}

	/**
	 * Country Code
	 * 
	 * <p>
	 * Fix 3 digit country codes, in two digit country code field, by removing the
	 * trailing "X" or number (0-9) example: (DE is DEX, NL is NLX, GB1, GB2, GB3).
	 * </p>
	 * 
	 * @param country
	 * @return
	 */
	public static CountryCode getCountryCodeHistoric(Node countryNode) {
		if (countryNode == null) {
			return CountryCode.UNDEFINED;
		}

		String country = countryNode.getText();

		if (country.length() == 3) {
			country = country.replaceFirst("(?:X|[0-9])$", "");
		}

		CountryCode countryCode = CountryCodeHistory.getCurrentCode(country);

		LOGGER.debug("Historic Country Code: '{}' maps to '{}'", country, countryCode);

		return countryCode;
	}

}
