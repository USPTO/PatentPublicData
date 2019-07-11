package gov.uspto.patent.doc.pap.items;

import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.ItemReader;
import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.model.entity.Name;
import gov.uspto.patent.model.entity.NameOrg;
import gov.uspto.patent.model.entity.NamePerson;

/**
 * Parse Name
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

	private static final XPath ORGNAME_XP = DocumentHelper.createXPath("organization-name");
	private static final XPath NAMEPREFIX_XP = DocumentHelper.createXPath("name-prefix");
	private static final XPath FIRSTNAME_XP = DocumentHelper.createXPath("given-name");
	private static final XPath MIDDLENAME_XP = DocumentHelper.createXPath("middle-name");
	private static final XPath LASTNAME_XP = DocumentHelper.createXPath("family-name");
	private static final XPath NAMESUFFIX_XP = DocumentHelper.createXPath("name-suffix");

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
		Node orgNameN = ORGNAME_XP.selectSingleNode(node);
		NameOrg name = orgNameN != null ? new NameOrg(orgNameN.getText().trim()) : null;

		if (name != null) {
			try {
				name.validate();
			} catch (InvalidDataException e) {
				LOGGER.warn("{} : {}", e.getMessage(), node.getParent().asXML());
			}
		}

		return name;
	}

	public NamePerson getPersonName(Node node) {
		Node prefixN = NAMEPREFIX_XP.selectSingleNode(node);
		String prefix = prefixN != null ? prefixN.getText().trim() : null;

		Node firstN = FIRSTNAME_XP.selectSingleNode(node);
		String firstName = firstN != null ? firstN.getText().trim() : null;

		Node middleN = MIDDLENAME_XP.selectSingleNode(node);
		String middleName = middleN != null ? middleN.getText().trim() : null;

		Node lastN = LASTNAME_XP.selectSingleNode(node);
		String lastName = lastN != null ? lastN.getText().trim() : null;

		Node suffixN = NAMESUFFIX_XP.selectSingleNode(node);
		String suffix = suffixN != null ? suffixN.getText().trim() : null;

		NamePerson name = null;
		if (lastName != null || firstName != null) {
			name = new NamePerson(firstName, middleName, lastName);
			name.setPrefix(prefix);
			name.setSuffix(suffix);

			try {
				name.validate();
			} catch (InvalidDataException e) {
				LOGGER.warn("{} : {}", e.getMessage(), node.asXML());
			}
		}

		return name;
	}

}
