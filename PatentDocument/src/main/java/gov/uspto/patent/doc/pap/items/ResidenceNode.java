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
	
	private static final XPath US_RESIDENT_XP = DocumentHelper.createXPath("residence-us");
	private static final XPath NONUS_RESIDENT_XP = DocumentHelper.createXPath("residence-non-us");

	private static final XPath CITYXP = DocumentHelper.createXPath("city");
	private static final XPath STATEXP = DocumentHelper.createXPath("state");
	private static final XPath COUNTRYXP = DocumentHelper.createXPath("country-code|country/country-code");
	
	public ResidenceNode(Node itemNode) {
		super(itemNode, ITEM_NAME_NODE);
	}

	@Override
	public Address read() {

		Node usResident = US_RESIDENT_XP.selectSingleNode(itemNode);
		
		if (usResident != null) {
			return readResidence(usResident);
		} else {
			Node nonUS = NONUS_RESIDENT_XP.selectSingleNode(itemNode);
			if (nonUS != null) {
				return readResidence(nonUS);
			}
		}

		return null;
	}

	private Address readResidence(Node residence) {

		Node cityN = CITYXP.selectSingleNode(residence);
		String city = cityN != null ? cityN.getText().trim() : null;

		Node stateN = STATEXP.selectSingleNode(residence);
		String state = stateN != null ? stateN.getText().trim() : null;

		CountryCode countryCode = null;
		if (residence.getParent().matches("//residence-non-us")) {
			Node countryN = COUNTRYXP.selectSingleNode(residence);
			String country = countryN != null ? countryN.getText().trim() : null;
			try {
				countryCode = CountryCode.fromString(country);
			} catch (InvalidDataException e) {
				LOGGER.warn("{} : {}", e.getMessage(), residence.asXML());
			}
		} else {
			countryCode = CountryCode.US;
		}

		Address address = new Address(city, state, countryCode);
		try {
			address.validate();
		} catch (InvalidDataException e) {
			LOGGER.warn("{} : {}", e.getCause(), residence.asXML());
		}

		return address;
	}

}
