package gov.uspto.patent.doc.sgml.items;

import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.ItemReader;
import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.entity.Address;

/**
 *<h3>ADR Individual or organization address</h3>
 *<p> 
 *<li>NAM Name, organization, if part of address
 *<li>OMC PDAT Organization mail code
 *<li>PBOX PDAT Post office box number
 *<li>STR PDAT Street, house number or name, district (of city), apt. number, etc.
 *<li>CITY PDAT City or town
 *<li>CNTY PDAT County, parish, department, etc.
 *<li>STATE PDAT Region of country (state, province, etc.)
 *<li>CTRY PDAT Country
 *<li>PCODE PDAT Postal code
 *<li>EAD PDAT Electronic address (e.g., e-mail)
 *<li>TEL PDAT Telephone number, including area or regional code
 *<li>FAX PDAT Facsimile telephone number 
 *</p>
 *<p>
 *<pre>
 *{@code
 * <!ELEMENT ADR - - (OMC?,PBOX?,STR*,CITY?,CNTY?,STATE?,CTRY?,PCODE?,EAD*,TEL*,FAX*) > 
 *}
 *</pre>
 *</p>
 *
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class AddressNode extends ItemReader<Address> {
	private static final Logger LOGGER = LoggerFactory.getLogger(AddressNode.class);

	private static final String ITEM_NODE_NAME = "ADR";

	public AddressNode(Node itemNode) {
		super(itemNode, ITEM_NODE_NAME);
	}

	@Override
	public Address read() {
		return readAddress(itemNode);
	}

	public Address readAddress(Node addrNode) {
		if (addrNode == null) {
			return null;
		}

		Node streetN = addrNode.selectSingleNode("STR/PDAT");
		String street = streetN != null ? streetN.getText() : null;

		Node pBoxN = addrNode.selectSingleNode("PBOX/PDAT");
		String pbox = pBoxN != null ? pBoxN.getText() : null;
		if (pbox != null && street == null) {
			street = pbox;
		}

		Node cityN = addrNode.selectSingleNode("CITY/PDAT");
		String city = cityN != null ? cityN.getText() : null;

		Node stateN = addrNode.selectSingleNode("STATE/PDAT");
		String state = stateN != null ? stateN.getText() : null;

		Node zipcodeN = addrNode.selectSingleNode("PCODE/PDAT");
		String zipcode = zipcodeN != null ? zipcodeN.getText() : null;

		Node countryN = addrNode.selectSingleNode("CTRY/PDAT");
		String country = countryN != null ? countryN.getText() : null;
		CountryCode countryCode = CountryCode.UNDEFINED;
		try {
			countryCode = CountryCode.fromString(country);
		} catch (InvalidDataException e1) {
			LOGGER.warn("Invalid Country Code: '{}'", country);
		}

		Node emailN = addrNode.selectSingleNode("EAD/PDAT");
		String email = emailN != null ? emailN.getText() : null;

		Node telN = addrNode.selectSingleNode("TEL/PDAT");
		String tel = telN != null ? telN.getText() : null;

		Node faxN = addrNode.selectSingleNode("FAX/PDAT");
		String fax = faxN != null ? faxN.getText() : null;

		Address address = new Address(street, city, state, zipcode, countryCode);
		address.setEmail(email);
		address.setPhoneNumber(tel);
		address.setFaxNumber(fax);

		try {
			address.validate();
		} catch (InvalidDataException e) {
			LOGGER.warn("Address Invalid: {}", addrNode.getParent().asXML(), e);
		}

		return address;
	}
}
