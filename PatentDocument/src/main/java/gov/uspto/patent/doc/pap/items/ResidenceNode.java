package gov.uspto.patent.doc.pap.items;

import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.ItemReader;
import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.entity.Address;

/**
 * Residence or Address
 *
 *<p><pre>
 * {@code}
 * <residence-us>
 *   <city>SALT LAKE CITY</city>
 *   <state>UT</state>
 *   <country-code>US</country-code>
 * </residence-us>
 * }
 *</pre></p>
 * 
 *<p><pre>
 * {@code
 * <!ELEMENT residence  (military-service | ( (residence-us | residence-non-us), citizenship?) ) >
 * <!ELEMENT residence-us  (city,state,country-code) >
 * <!ELEMENT residence-non-us (city,state?,country-code) >
 * }
 *</pre><p>
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class ResidenceNode extends ItemReader<Address> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ResidenceNode.class);

	private static final String ITEM_NAME_NODE = "residence";
	private static final String US_RESIDENT = "residence-us";
	private static final String NONUS_RESIDENT = "residence-non-us";

	public ResidenceNode(Node itemNode) {
		super(itemNode, ITEM_NAME_NODE);
	}

	@Override
	public Address read() {

		Node usResident = itemNode.selectSingleNode(US_RESIDENT);
		Node nonUS = itemNode.selectSingleNode(NONUS_RESIDENT);

		try {
			if (usResident != null) {
				return readResidence(usResident);
			} else if (nonUS != null) {
				return readResidence(nonUS);
			}
		} catch (InvalidDataException e) {
			LOGGER.error("Invalid Address from {}", itemNode, e);
		}

		return null;
	}

	private Address readResidence(Node residence) throws InvalidDataException {

		Node cityN = residence.selectSingleNode("city");
		String city = cityN != null ? cityN.getText() : null;

		Node stateN = residence.selectSingleNode("state");
		String state = stateN != null ? stateN.getText() : null;

		CountryCode countryCode = null;
		if (residence.getParent().matches("//residence-non-us")) {
			Node countryN = residence.selectSingleNode("country-code");
			String country = countryN != null ? countryN.getText() : null;

			try {
				countryCode = CountryCode.fromString(country);
			} catch (InvalidDataException e) {
				LOGGER.error("Invalid CountryCode: {} from: {}", country, residence.asXML());
			}
		} else {
			countryCode = CountryCode.US;
		}

		Address address = new Address(city, state, countryCode);

		return address;
	}

}
