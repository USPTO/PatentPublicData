package gov.uspto.patent.doc.xml.items;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.ItemReader;
import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.OrgSynonymGenerator;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(AddressBookNode.class);
  
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
    public Name getPersonName() {
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

            try {
				name.validate();
			} catch (InvalidDataException e) {
				LOGGER.warn("Person Name Invalid: {}", itemNode.getParent().getParent().asXML(), e);
			}
        }

        return name;
    }

    public NameOrg getOrgName() {
        // @FIXME Note: for now, treat name as a org name. 
        Node orgnameN = itemNode.selectSingleNode("orgname") != null ? itemNode.selectSingleNode("orgname")
                : itemNode.selectSingleNode("name");

        @SuppressWarnings("unchecked")
        List<Node> synonymNodes = itemNode.selectNodes("synonym");
        Set<String> synonyms = new HashSet<String>(synonymNodes.size());
        for (Node synonymN : synonymNodes) {
            synonyms.add(synonymN.getText());
        }
        
        NameOrg name = null;
        if (orgnameN != null) {
            String orgName = orgnameN.getText();
            name = new NameOrg(orgName);
            name.setSynonyms(synonyms);

            OrgSynonymGenerator.computeSynonyms(name);

            try {
				name.validate();
			} catch (InvalidDataException e) {
				LOGGER.warn("Org Name Invalid: {}", orgnameN.getParent().getParent().asXML(), e);
			}
        }
        
        return name;
    }
  
    public Address getAddress() {
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

        Node cityN = addressN.selectSingleNode("city");
        String city = cityN != null ? cityN.getText() : null;

        Node stateN = addressN.selectSingleNode("state");
        String state = stateN != null ? stateN.getText() : null;

        Node zipCodeN = addressN.selectSingleNode("postcode");
        String zipCode = zipCodeN != null ? zipCodeN.getText() : null;

        Node countryN = addressN.selectSingleNode("country");
        CountryCode countryCode = CountryCode.UNDEFINED;
        if (countryN != null) {
            try {
                countryCode = CountryCode.fromString(countryN.getText());
            } catch (InvalidDataException e) {
                LOGGER.warn("Invalid CountryCode: {} from: {}", countryN.getText(), addressN.asXML());
            }
        }

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
