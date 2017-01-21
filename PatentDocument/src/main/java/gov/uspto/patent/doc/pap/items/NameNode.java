package gov.uspto.patent.doc.pap.items;

import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.ItemReader;
import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.model.entity.Name;
import gov.uspto.patent.model.entity.NameOrg;
import gov.uspto.patent.model.entity.NamePerson;

/**
 * Parse Name (Inventor)
 *
 *<p><pre>
 * {@code
 * <!ELEMENT name  (name-prefix?,given-name?,middle-name?,family-name,name-suffix?) >
 * }
 *</pre></p>
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class NameNode extends ItemReader<gov.uspto.patent.model.entity.Name> {
	private static final Logger LOGGER = LoggerFactory.getLogger(NameNode.class);

	private static final String ITEM_NODE_NAME = "name";

	public NameNode(Node itemNode) {
		super(itemNode, ITEM_NODE_NAME);
	}

	@Override
	public Name read() {

		NameOrg orgName = getOrgName(itemNode);
		if (orgName != null) {
			return orgName;
		}

		return getPersonName(itemNode);
	}

	public NameOrg getOrgName(Node node) {
		Node orgNameN = node.selectSingleNode("organization-name");
		NameOrg name = orgNameN != null ? new NameOrg(orgNameN.getText()) : null;

		if (name != null) {
			try {
				name.validate();
			} catch (InvalidDataException e) {
				LOGGER.warn("Org Name Invalid: {}", node.getParent().asXML(), e);
			}
		}

		return name;
	}

	public NamePerson getPersonName(Node node) {
		Node prefixN = node.selectSingleNode("name-prefix");
		String prefix = prefixN != null ? prefixN.getText() : null;

		Node firstN = node.selectSingleNode("given-name");
		String firstName = firstN != null ? firstN.getText() : null;

		Node middleN = node.selectSingleNode("middle-name");
		String middleName = middleN != null ? middleN.getText() : null;

		Node lastN = node.selectSingleNode("family-name");
		String lastName = lastN != null ? lastN.getText() : null;

		Node suffixN = node.selectSingleNode("name-suffix");
		String suffix = suffixN != null ? suffixN.getText() : null;

		NamePerson name = null;
		if (lastName != null || firstName != null) {
			name = new NamePerson(firstName, middleName, lastName);
			name.setPrefix(prefix);
			name.setSuffix(suffix);

			try {
				name.validate();
			} catch (InvalidDataException e) {
				LOGGER.warn("Unable to create NamePerson from {}", node.asXML(), e);
			}
		}

		return name;
	}

}
