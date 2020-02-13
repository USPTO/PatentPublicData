package gov.uspto.patent.doc.xml.items;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.ItemReader;
import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.entity.Address;
import gov.uspto.patent.model.entity.Name;
import gov.uspto.patent.model.entity.NameOrg;
import gov.uspto.patent.model.entity.NamePerson;

/**
 * Address Book Node
 * 
 * <p>
 * 
 * <pre>
 * {@code
 * <!ELEMENT addressbook ((%name_group;, address?, phone*, fax*, email*, url*, ead*, dtext?) | text)>
 * <!ELEMENT address (%address_group;)>
 * <!ENTITY % name_group "((name | (prefix? , (last-name | orgname) , first-name? , middle-name? , suffix? , iid? , role? , orgname? , department? , synonym*)) , registered-number?)">
 * <!ENTITY % address_group "((address-1? , address-2? , address-3? , mailcode? , pobox? , room? , address-floor? , building? , street? , city? , county? , state? , postcode? , country) | text)">
 * <!ELEMENT synonym (#PCDATA)>
 * <!ELEMENT phone (#PCDATA)>
 * <!ELEMENT fax (#PCDATA)>
 * <!ELEMENT email (#PCDATA)>
 * }
 * </pre>
 * </p>
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class AddressBookNode extends ItemReader<Name> {
	private static final Logger LOGGER = LoggerFactory.getLogger(AddressBookNode.class);

	private static final XPath ADDRESSXP = DocumentHelper.createXPath("address");
	private static final XPath CITYXP = DocumentHelper.createXPath("city");
	private static final XPath STATEXP = DocumentHelper.createXPath("state");
	private static final XPath CNTRYXP = DocumentHelper.createXPath("country");

	private static final XPath ORGNAMEXP = DocumentHelper.createXPath("orgname|name");
	private static final XPath PREFIXXP = DocumentHelper.createXPath("prefix");
	private static final XPath FNAMEXP = DocumentHelper.createXPath("first-name");
	private static final XPath MNAMEXP = DocumentHelper.createXPath("middleName");
	private static final XPath LNAMEXP = DocumentHelper.createXPath("last-name");
	private static final XPath SUFFIXXP = DocumentHelper.createXPath("prefix");

	private static final String ITEM_NODE_NAME = "addressbook";
	private static final String ITEM_ELSE_PREFIX = "-examiner";

	public static final Set<String> COMMON_SUFFIXES = new HashSet<String>(
			Arrays.asList("JR", "SR", "II", "III", "IV", "ESQ"));

	public AddressBookNode(Node itemNode) {
		super(itemNode, ITEM_NODE_NAME, ITEM_ELSE_PREFIX);
	}

	public Name getName() {
		if (LNAMEXP.selectSingleNode(itemNode) != null) {
			return getPersonName();
		} else {
			return getOrgName();
		}
	}

	/**
	 * Get Names.
	 * 
	 * PersonName can also have an associated Orgname, or Orgname can be by itself.
	 * 
	 * @return
	 * @throws InvalidDataException
	 */
	public Name getPersonName() {

		Node prefixN = PREFIXXP.selectSingleNode(itemNode);
		String prefix = prefixN != null ? prefixN.getText() : null;

		Node firstN = FNAMEXP.selectSingleNode(itemNode);
		String firstName = firstN != null ? firstN.getText() : null;

		/*
		 *  Middle Name is occasionally within middle-name field,
		 *  it may also occur within the first-name field
		 */
		Node middleN = MNAMEXP.selectSingleNode(itemNode);
		String middleName = middleN != null ? middleN.getText() : null;

		Node lastN = LNAMEXP.selectSingleNode(itemNode);
		String lastName = lastN != null ? lastN.getText() : null;

		Node suffixN = SUFFIXXP.selectSingleNode(itemNode);
		String suffix = suffixN != null ? suffixN.getText() : null;

		/*
		 * Parse Common Suffixes from Lastname
		 */
		if (suffix == null && lastName != null && lastName.contains(",")) {
			String[] parts = lastName.split(",");
			if (parts.length == 2) {
				String suffixCheck = parts[1].trim().replaceFirst("\\.$", "").toUpperCase();
				if (suffixCheck.length() < 4 && COMMON_SUFFIXES.contains(suffixCheck)) {
					LOGGER.debug("Suffix Fixed, parsed common suffix '{}' from lastname: '{}'", suffixCheck, lastName);
					lastName = parts[0];
					suffix = suffixCheck;
				}
				// else if (suffixCheck.length() < 4) {
				// LOGGER.warn("Possible Suffix, not found in common suffixes: {}",
				// suffixCheck);
				// }
			}
		}

		/* Synonyms; not included in Public Data.
		List<Node> synonymNodes = itemNode.selectNodes("synonym");
		Set<String> synonyms = new HashSet<String>(synonymNodes.size());
		for (Node synonymN : synonymNodes) {
			synonyms.add(synonymN.getText());
		}
		*/

		NamePerson name = null;
		if (lastName != null || firstName != null) {
			name = new NamePerson(firstName, middleName, lastName);
			name.setPrefix(prefix);
			name.setSuffix(suffix);
			//name.setSynonyms(synonyms);
		}

		return name;
	}

	public NameOrg getOrgName() {
		Node orgnameN = ORGNAMEXP.selectSingleNode(itemNode);

		/* Synonyms; not included in Public Data.
		List<Node> synonymNodes = itemNode.selectNodes("synonym");
		Set<String> synonyms = new HashSet<String>(synonymNodes.size());
		for (Node synonymN : synonymNodes) {
			synonyms.add(synonymN.getText());
		}
		*/

		NameOrg name = null;
		if (orgnameN != null) {
			String orgName = orgnameN.getText();
			name = new NameOrg(orgName);
			//name.setSynonyms(synonyms);
		}

		return name;
	}

	public Address getAddress() {
		Node addressN = ADDRESSXP.selectSingleNode(itemNode);
		if (addressN == null) {
			return null;
		}

		/*
		 * Contact Info; Not within Public Data.
		 *
		Node phoneN = itemNode.selectSingleNode("phone");
		String phone = phoneN != null ? phoneN.getText() : null;

		Node faxN = itemNode.selectSingleNode("fax");
		String fax = faxN != null ? faxN.getText() : null;

		Node emailN = itemNode.selectSingleNode("email");
		String email = emailN != null ? emailN.getText() : null;
		*/

		/*
		 * Address; only City, State, Country within Public Data.
		 *
		Node streetN1 = addressN.selectSingleNode("address-1");
		Node streetN2 = addressN.selectSingleNode("street");
		String street = null;
		if (streetN1 != null) {
			street = streetN1.getText();
			Node streetN12 = addressN.selectSingleNode("address-2");
			if (streetN12 != null) {
				street = street + ", " + streetN12.getText();
			}
		} else if (streetN2 != null) {
			street = streetN2.getText();
		}

		Node pboxN = addressN.selectSingleNode("pobox");
		String pbox = pboxN != null ? pboxN.getText() : null;
		if (pbox != null && street == null) {
			street = pbox;
		}
		
		Node zipCodeN = addressN.selectSingleNode("postcode");
		String zipCode = zipCodeN != null ? zipCodeN.getText() : null;
		*/

		Node cityN = CITYXP.selectSingleNode(addressN);
		String city = cityN != null ? cityN.getText() : null;

		Node stateN = STATEXP.selectSingleNode(addressN);
		String state = stateN != null ? stateN.getText() : null;

		Node countryN = CNTRYXP.selectSingleNode(addressN);
		CountryCode countryCode = CountryCode.UNDEFINED;
		if (countryN != null) {
			try {
				countryCode = CountryCode.fromString(countryN.getText());
			} catch (InvalidDataException e) {
				if ("UNKNOWN".equalsIgnoreCase(countryN.getText())) {
					LOGGER.debug("{} : {}", e.getMessage(), addressN.getParent().asXML());
				} else {
					LOGGER.warn("{} : {}", e.getMessage(), addressN.getParent().asXML());
				}
			}
		}

		Address address = new Address(city, state, countryCode);
		/*
		address.setPhoneNumber(phone);
		address.setFaxNumber(fax);
		address.setEmail(email);
		 */
		return address;

	}

	@Override
	public Name read() {
		throw new IllegalArgumentException("Function not Used.");
	}

}
