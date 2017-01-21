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
 * <p>
 * 
 * <pre>
 * {@code}
 * <residence-us>
 *   <city>SALT LAKE CITY</city>
 *   <state>UT</state>
 *   <country-code>US</country-code>
 * </residence-us>
 * }
 * </pre>
 * </p>
 * 
 * <p>
 * 
 * <pre>
 * {@code
 * <!ELEMENT address  (military-address?,address-1?,address-2?,city?,state?,postalcode?,country?,email*,telephone*,fax*) >
 * }
 * </pre>
 * <p>
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class AddressNode extends ItemReader<Address> {
	private static final Logger LOGGER = LoggerFactory.getLogger(AddressNode.class);

	private static final String ITEM_NODE_NAME = "address";

	public AddressNode(Node itemNode) {
		super(itemNode, ITEM_NODE_NAME);
	}

	@Override
	public Address read() {
		return readAddress(itemNode);
	}

	public Address readAddress(Node node) {
		Node emailN = node.selectSingleNode("email");
		String email = emailN != null ? emailN.getText() : null;

		Node phoneN = node.selectSingleNode("telephone");
		String phone = phoneN != null ? phoneN.getText() : null;

		Node faxN = node.selectSingleNode("fax");
		String fax = faxN != null ? faxN.getText() : null;

		Node cityN = node.selectSingleNode("city");
		String city = cityN != null ? cityN.getText() : null;

		Node stateN = node.selectSingleNode("state");
		String state = stateN != null ? stateN.getText() : null;

		Node zipCodeN = node.selectSingleNode("postalcode");
		String zipCode = zipCodeN != null ? zipCodeN.getText() : null;

		Node countryN = node.selectSingleNode("country-code");
		CountryCode countryCode = CountryCode.UNDEFINED;
		if (countryN != null) {
			try {
				countryCode = CountryCode.fromString(countryN.getText());
			} catch (InvalidDataException e) {
				LOGGER.warn("Invalid CountryCode: {} from: {}", countryN.getText(), node.asXML());
			}
		}

		Address address = new Address(null, city, state, zipCode, countryCode);
		address.setEmail(email);
		address.setPhoneNumber(phone);
		address.setFaxNumber(fax);

		try {
			address.validate();
		} catch (InvalidDataException e) {
			LOGGER.warn("Invalid Address: {}", node.getParent().asXML(), e);
		}
		return address;
	}
}
