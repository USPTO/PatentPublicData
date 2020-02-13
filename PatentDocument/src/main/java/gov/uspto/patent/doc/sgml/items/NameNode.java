package gov.uspto.patent.doc.sgml.items;

import java.util.List;

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

	private static final XPath NAMEXP = DocumentHelper.createXPath("NAM");
	private static final XPath ORGNAMEXP = DocumentHelper.createXPath("ONM/STEXT");
	private static final XPath FNAMEXP = DocumentHelper.createXPath("FNM/PDAT");
	private static final XPath LNAMEXP = DocumentHelper.createXPath("SNM/STEXT");
	private static final XPath VALUESXP = DocumentHelper.createXPath("descendant::PDAT");

	public NameNode(Node itemNode) {
		super(itemNode);
	}

	@Override
	public Name read() {
		Name name = null;

		Node nameNode = NAMEXP.selectSingleNode(itemNode);

		Node orgNameN = ORGNAMEXP.selectSingleNode(nameNode);
		if (orgNameN != null) {
			String orgName = readSTEXT(orgNameN);
			name = new NameOrg(orgName);
			try {
				((NameOrg)name).validate();
			} catch (InvalidDataException e) {
				LOGGER.warn("{} : {}", e.getMessage(), nameNode.getParent().asXML());
			}
		} else {
			Node firstNameN = FNAMEXP.selectSingleNode(nameNode);
			String firstName = firstNameN != null ? firstNameN.getText() : "";

			Node lastNameN = LNAMEXP.selectSingleNode(nameNode);
			String lastName = readSTEXT(lastNameN);
			name = new NamePerson(firstName, lastName);
			try {
				((NamePerson)name).validate();
			} catch (InvalidDataException e) {
				LOGGER.warn("{} : {}", e.getMessage(), nameNode.getParent().asXML());
			}
		}

		return name;
	}

	/**
	 * Get plain text from STEXT , ignore stylized tags (bold, italic, superscript, subscript)
	 * 
	 * @param stextNode
	 * @return
	 */
	public String readSTEXT(Node stextNode){
		List<Node> namePartN = VALUESXP.selectNodes(stextNode); //stextNode.selectNodes("descendant::PDAT");

		String orgName;
		if (namePartN.size() == 1){
			orgName = namePartN.get(0).getText();
		} else {
			// Multiple PDATs when text has styling.
			StringBuilder stb = new StringBuilder();
			for(Node node: namePartN){
				stb.append(node.getText());
			}
			orgName = stb.toString();
		}

		return orgName;
	}
}
