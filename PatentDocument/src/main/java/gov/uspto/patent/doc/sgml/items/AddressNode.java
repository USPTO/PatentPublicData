package gov.uspto.patent.doc.sgml.items;

import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;

import gov.uspto.parser.dom4j.ItemReader;
import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.entity.Address;

/**
 * <h3>ADR Individual or organization address</h3>
 * <p>
 * <li>NAM Name, organization, if part of address
 * <li>OMC PDAT Organization mail code
 * <li>PBOX PDAT Post office box number
 * <li>STR PDAT Street, house number or name, district (of city), apt. number,
 * etc.
 * <li>CITY PDAT City or town
 * <li>CNTY PDAT County, parish, department, etc.
 * <li>STATE PDAT Region of country (state, province, etc.)
 * <li>CTRY PDAT Country
 * <li>PCODE PDAT Postal code
 * <li>EAD PDAT Electronic address (e.g., e-mail)
 * <li>TEL PDAT Telephone number, including area or regional code
 * <li>FAX PDAT Facsimile telephone number
 * </p>
 * <p>
 * 
 * <pre>
 *{@code
 * <!ELEMENT ADR - - (OMC?,PBOX?,STR*,CITY?,CNTY?,STATE?,CTRY?,PCODE?,EAD*,TEL*,FAX*) > 
 *}
 * </pre>
 * </p>
 *
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class AddressNode extends ItemReader<Address> {

	private static final XPath CITYXP = DocumentHelper.createXPath("CITY/PDAT");
	private static final XPath STATEXP = DocumentHelper.createXPath("STATE/PDAT");
	private static final XPath CNTRYXP = DocumentHelper.createXPath("CTRY/PDAT");

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

		Node cityN = CITYXP.selectSingleNode(addrNode);
		String city = cityN != null ? cityN.getText() : null;

		Node stateN = STATEXP.selectSingleNode(addrNode);
		String state = stateN != null ? stateN.getText() : null;

		Node countryN = CNTRYXP.selectSingleNode(addrNode);
		String country = countryN != null ? countryN.getText() : null;
		CountryCode countryCode = CountryCode.UNDEFINED;
		try {
			countryCode = CountryCode.fromString(country);
		} catch (InvalidDataException e1) {
			countryCode = CountryCode.UNDEFINED;
		}

		if (CountryCode.UNDEFINED.equals(countryCode) && "PARTY-US".equals(addrNode.getParent().getName())) {
			countryCode = CountryCode.US;
		}

		Address address = new Address(city, state, countryCode);

		return address;
	}
}
