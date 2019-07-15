package gov.uspto.patent.doc.greenbook.fragments;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.doc.greenbook.items.AddressNode;
import gov.uspto.patent.doc.greenbook.items.NameNode;
import gov.uspto.patent.model.entity.Address;
import gov.uspto.patent.model.entity.Agent;
import gov.uspto.patent.model.entity.AgentRepType;
import gov.uspto.patent.model.entity.Name;

/**
 * Agent / Legal Rep
 *
 * <p>
 * 
 * <pre>
 * {@code
 * <LREP>
 *  <FR2>Lawyer Name 1</FR2>
 *  <FR2>Lawyer Name 2</FR2>
 * 	<FRM>Legal Firm Name Here</FRM>
 * <LREP>
 * }
 * </pre>
 * </p>
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class AgentNode extends DOMFragmentReader<List<Agent>> {
	private static final Logger LOGGER = LoggerFactory.getLogger(AgentNode.class);

	private static final XPath AGENTXP = DocumentHelper.createXPath("/DOCUMENT/LREP");
	private static final XPath FR2XP = DocumentHelper.createXPath("FR2");
	private static final XPath AATXP = DocumentHelper.createXPath("AAT");
	private static final XPath ATTXP = DocumentHelper.createXPath("ATT");
	private static final XPath AGTXP = DocumentHelper.createXPath("AGT");
	private static final XPath NAMXP = DocumentHelper.createXPath("NAM");
	private static final XPath FRMXP = DocumentHelper.createXPath("FRM");

	private static NameNode nameParser = new NameNode(null);

	public AgentNode(Document document) {
		super(document);
	}

	@Override
	public List<Agent> read() {
		List<Agent> agentList = new ArrayList<Agent>();

		Node legalRep = AGENTXP.selectSingleNode(document);
		if (legalRep == null) {
			return agentList;
		}

		Address address = new AddressNode(legalRep).read();
		/*try {
			address.validate();
		} catch (InvalidDataException e) {
			LOGGER.warn("{} : {}", e.getMessage(), legalRep.asXML());
		}
		*/

		/*
		 * Attorney Principle Name
		 */
		List<Node> attorneyPrinciples = FR2XP.selectNodes(legalRep); // Multiple can exist.
		for (Node attyPN : attorneyPrinciples) {
			Name name = parseName(attyPN);
			if (name != null) {
				Agent agent = new Agent(name, address, AgentRepType.ATTORNEY);
				agentList.add(agent);
			}
		}

		/*
		 * Attorney Associate Name
		 */
		List<Node> attorneyAssociates = AATXP.selectNodes(legalRep);
		for (Node attyAN : attorneyAssociates) {
			Name name = parseName(attyAN);
			if (name != null) {
				Agent agent = new Agent(name, address, AgentRepType.ATTORNEY);
				agentList.add(agent);
			}
		}

		/*
		 * Attorney Name
		 */
		List<Node> attorneyNames = ATTXP.selectNodes(legalRep);
		for (Node attorneyNameN : attorneyNames) {
			Name name = parseName(attorneyNameN);
			if (name != null) {
				Agent agent = new Agent(name, address, AgentRepType.ATTORNEY);
				agentList.add(agent);
			}
		}

		/*
		 * Agent Name
		 */
		List<Node> agents = AGTXP.selectNodes(legalRep);
		for (Node agentN : agents) {
			Name name = parseName(agentN);
			if (name != null) {
				Agent agent = new Agent(name, address, AgentRepType.AGENT);
				agentList.add(agent);
			}
		}

		/*
		 * Representative Name
		 */
		List<Node> nameNs = NAMXP.selectNodes(legalRep);
		for (Node nameN : nameNs) {
			Name name = parseName(nameN);
			if (name != null) {
				Agent agent = new Agent(name, address, AgentRepType.COMMON_REPRESENTATIVE);
				agentList.add(agent);
			}
		}

		/*
		 * Law Firm Name
		 */
		List<Node> lawFirmNs = FRMXP.selectNodes(legalRep);
		for (Node lawFirmN : lawFirmNs) {
			Name name = parseName(lawFirmN);
			if (name != null) {
				Agent agent = new Agent(name, address, AgentRepType.ATTORNEY);
				agentList.add(agent);
			}
		}


		return agentList;
	}

	private Name parseName(Node fullNameNode) {
		Name name = null;

		try {
			name = nameParser.createName(fullNameNode.getText());
		} catch (InvalidDataException e) {
			LOGGER.warn("{} : {}", e.getMessage(), fullNameNode.asXML());
		}

		if (name != null) {
			try {
				name.validate();
			} catch (InvalidDataException e) {
				LOGGER.warn("{} : {}", e.getMessage(), fullNameNode.asXML());
			}
		}

		return name;
	}
}
