package gov.uspto.patent.doc.pap.items;

import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;
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

	private static final XPath ADDRESSXP = DocumentHelper.createXPath("address");
	private static final XPath CITYXP = DocumentHelper.createXPath("city");
	private static final XPath STATEXP = DocumentHelper.createXPath("state");
	private static final XPath COUNTRYXP = DocumentHelper.createXPath("country-code|country/country-code");

	//private static final String ITEM_NODE_NAME = "address";

	public AddressNode(Node itemNode) {
		super(itemNode, ADDRESSXP);
	}

	@Override
	public Address read() {
		return readAddress(itemNode);
	}
	
	public Address readAddress(Node node) {	
		Node cityN = CITYXP.selectSingleNode(node); //node.selectSingleNode("city");
		String city = cityN != null ? cityN.getText().trim() : null;

		Node stateN = STATEXP.selectSingleNode(node); //node.selectSingleNode("state");
		String state = stateN != null ? stateN.getText().trim() : null;

		Node countryN = COUNTRYXP.selectSingleNode(node); //node.selectSingleNode("country-code|country/country-code");
		CountryCode countryCode = CountryCode.UNDEFINED;
		if (countryN != null) {
			try {
				countryCode = CountryCode.fromString(countryN.getText().trim());
			} catch (InvalidDataException e) {
				LOGGER.warn("{} : {}", e.getMessage(), node.getParent().asXML());
			}
		} else {
			if (state != null) {
				LOGGER.debug("Missing CountryCode using 'US' : {}", node.getParent().asXML());
				countryCode = CountryCode.US;
			}
		}

		Address address = new Address(city, state, countryCode);
		return address;
	}
}
