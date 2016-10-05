package gov.uspto.patent.doc.sgml.items;

import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.ItemReader;
import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.model.entity.Name;
import gov.uspto.patent.model.entity.NameOrg;
import gov.uspto.patent.model.entity.NamePerson;

/**
 *<h3>NAM Individual or organization name</h3>
 *<p> 
 *<li>TTL PDAT Title
 *<li>FNM PDAT Given, middle name(s) and/or Initials
 *<li>SNM PDAT Family name, last, surname, organization name
 *<li>SFX PDAT Suffix
 *<li>IID PDAT Individual ID number
 *<li>IRF PDAT Individual reference number
 *<li>SYN PDAT Synonym, cross reference
 *<li>ONM PDAT Organization name
 *<li>OID PDAT Identifying number of organization
 *<li>ODV PDAT Division of organization
 *<li>DID PDAT Identifying number of division 
 *</p>
 *<p>
 *<pre>
 *{@code
 * <!ELEMENT NAM - - ((TTL?,FNM?,SNM,SFX?,IID?,IRF?) | (ONM,SYN*,OID?,(ODV,DID?)*)) > 
 *}
 *</pre>
 *</p>
 *
 *
 *
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class NameNode extends ItemReader<Name> {
	private static final Logger LOGGER = LoggerFactory.getLogger(NameNode.class);

	public NameNode(Node itemNode) {
		super(itemNode);
	}

	@Override
	public Name read() {
		Name name = null;

		Node nameNode = itemNode.selectSingleNode("NAM");

		Node orgNameN = nameNode.selectSingleNode("ONM/STEXT/PDAT");
		if (orgNameN != null) {
			String orgName = orgNameN != null ? orgNameN.getText() : null;

			try {
				name = new NameOrg(orgName);
			} catch (InvalidDataException e) {
				LOGGER.warn("NameOrg Invalid", e);
			}
		} else {
			Node firstNameN = nameNode.selectSingleNode("FNM/PDAT");
			String firstName = firstNameN != null ? firstNameN.getText() : null;

			Node lastNameN = nameNode.selectSingleNode("SNM/STEXT/PDAT");
			String lastName = lastNameN != null ? lastNameN.getText() : null;

			try {
				name = new NamePerson(firstName, lastName);
			} catch (InvalidDataException e) {
				LOGGER.warn("NamePerson Invalid", e);
			}
		}

		return name;
	}
}
