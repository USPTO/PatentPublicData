package gov.uspto.patent.xml.items;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dom4j.Node;

import gov.uspto.common.text.StringCaseUtil;
import gov.uspto.parser.dom4j.ItemReader;
import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.entity.Address;
import gov.uspto.patent.model.entity.Name;
import gov.uspto.patent.model.entity.NameOrg;
import gov.uspto.patent.model.entity.NamePerson;

/**
 * Addressbook
 * 
 *<p><pre>
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
 *</pre></p>
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class AddressBookNode extends ItemReader<Name> {
	private static final String ITEM_NODE_NAME = "addressbook";
	private static final String ITEM_ELSE_PREFIX = "-examiner";

	public AddressBookNode(Node itemNode) {
		super(itemNode, ITEM_NODE_NAME, ITEM_ELSE_PREFIX);
	}

	/**
	 * Get Names.
	 * 
	 * PersonName can also have an associated Orgname, or Orgname can be by itself.
	 * 
	 * @return
	 * @throws InvalidDataException 
	 */
	public Name getPersonName() throws InvalidDataException {
		Node prefixN = itemNode.selectSingleNode("prefix");
		String prefix = prefixN != null ? prefixN.getText() : null;

		Node firstN = itemNode.selectSingleNode("first-name");
		String firstName = firstN != null ? firstN.getText() : null;

		Node lastN = itemNode.selectSingleNode("last-name");
		String lastName = lastN != null ? lastN.getText() : null;

		Node middleN = itemNode.selectSingleNode("middleName");
		String middleName = middleN != null ? middleN.getText() : null;

		Node suffixN = itemNode.selectSingleNode("suffix");
		String suffix = suffixN != null ? suffixN.getText() : null;

		@SuppressWarnings("unchecked")
		List<Node> synonymNodes = itemNode.selectNodes("synonym");
		Set<String> synonyms = new HashSet<String>(synonymNodes.size());
		for (Node synonymN : synonymNodes) {
			synonyms.add(synonymN.getText());
		}

		NamePerson name = null;
		if (lastName != null || firstName != null) {
			name = new NamePerson(firstName, middleName, lastName);
			name.setPrefix(prefix);
			name.setSuffix(suffix);
			name.setSynonyms(synonyms);
		}

		return name;
	}

	public NameOrg getOrgName() throws InvalidDataException {
		Node orgnameN = itemNode.selectSingleNode("orgname");

		@SuppressWarnings("unchecked")
		List<Node> synonymNodes = itemNode.selectNodes("synonym");
		Set<String> synonyms = new HashSet<String>(synonymNodes.size());
		for (Node synonymN : synonymNodes) {
			synonyms.add(synonymN.getText());
		}

		NameOrg name = null;
		if (orgnameN != null) {
			String orgName = StringCaseUtil.toTitleCase(orgnameN.getText());
			name = new NameOrg(orgName);
			name.setSynonyms(synonyms);
		}
		return name;
	}

	public Address getAddress() throws InvalidDataException {
		Node addressN = itemNode.selectSingleNode("address");
		if (addressN == null) {
			return null;
		}

		/*
		 * Contact Info
		 */
		Node phoneN = itemNode.selectSingleNode("phone");
		String phone = phoneN != null ? phoneN.getText() : null;

		Node faxN = itemNode.selectSingleNode("fax");
		String fax = faxN != null ? faxN.getText() : null;

		Node emailN = itemNode.selectSingleNode("email");
		String email = emailN != null ? emailN.getText() : null;

		/*
		 * Address
		 */
		Node streetN = addressN.selectSingleNode("street");
		String street = streetN != null ? streetN.getText() : null;

		Node pboxN = addressN.selectSingleNode("pobox");
		String pbox = pboxN != null ? pboxN.getText() : null;
		if (pbox != null && street == null) {
			street = pbox;
		}

		Node cityN = addressN.selectSingleNode("city");
		String city = cityN != null ? cityN.getText() : null;

		Node stateN = addressN.selectSingleNode("state");
		String state = stateN != null ? stateN.getText() : null;

		Node zipCodeN = addressN.selectSingleNode("postcode");
		String zipCode = zipCodeN != null ? zipCodeN.getText() : null;

		Node countryN = addressN.selectSingleNode("country");
		CountryCode countryCode = countryN != null ? CountryCode.fromString(countryN.getText()) : null;

		Address address = new Address(street, city, state, zipCode, countryCode);
		address.setPhoneNumber(phone);
		address.setFaxNumber(fax);
		address.setEmail(email);

		return address;
	}

	@Override
	public Name read() {
		throw new IllegalArgumentException("Function not Used.");
	}

}
