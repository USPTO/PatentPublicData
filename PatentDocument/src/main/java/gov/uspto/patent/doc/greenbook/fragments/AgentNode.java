package gov.uspto.patent.doc.greenbook.fragments;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;
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

	private static final String FRAGMENT_PATH = "/DOCUMENT/LREP";

	private static NameNode nameParser = new NameNode(null);

	public AgentNode(Document document) {
		super(document);
	}

	@Override
	public List<Agent> read() {
		List<Agent> agentList = new ArrayList<Agent>();

		Node legalRep = document.selectSingleNode(FRAGMENT_PATH);
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
		List<Node> attorneyPrinciples = legalRep.selectNodes("FR2"); // Multiple can exist.
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
		List<Node> attorneyAssociates = legalRep.selectNodes("AAT");
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
		List<Node> attorneyNames = legalRep.selectNodes("ATT");
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
		List<Node> agents = legalRep.selectNodes("AGT");
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
		List<Node> nameNs = legalRep.selectNodes("NAM");
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
		List<Node> lawFirmNs = legalRep.selectNodes("FRM");
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
