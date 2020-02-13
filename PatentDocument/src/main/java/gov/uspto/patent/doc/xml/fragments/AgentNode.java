package gov.uspto.patent.doc.xml.fragments;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.doc.xml.items.AddressBookNode;
import gov.uspto.patent.model.entity.Address;
import gov.uspto.patent.model.entity.Agent;
import gov.uspto.patent.model.entity.AgentRepType;
import gov.uspto.patent.model.entity.Name;
import gov.uspto.patent.model.entity.RelationshipType;

public class AgentNode extends DOMFragmentReader<List<Agent>> {
	private static final Logger LOGGER = LoggerFactory.getLogger(AgentNode.class);

	private static final String FRAGMENT_PATH = "//us-parties/agents/agent|//parties/agents/agent";

	private static final String FRAGMENT_PATH2 = "//correspondence-address";

	public AgentNode(Document document) {
		super(document);
	}

	@Override
	public List<Agent> read() {
		List<Agent> agentList = new ArrayList<Agent>();

		List<Node> agents = document.selectNodes(FRAGMENT_PATH);

		int interationSquence = 0;
		for (Node node : agents) {
			interationSquence++;

			AddressBookNode addressBook = new AddressBookNode(node);

			Node sequenceN = node.selectSingleNode("@sequence");
			String sequence = sequenceN != null ? sequenceN.getText() : Integer.toString(interationSquence);

			Node repTypeN = node.selectSingleNode("@rep-type");
			String repType = repTypeN != null ? repTypeN.getText() : null;

			AgentRepType agentRepType;
			if (repType != null) {
				agentRepType = AgentRepType.valueOf(repType.trim().toUpperCase());
			} else {
				agentRepType = AgentRepType.UNDEFINED;
			}

			Name name = addressBook.getName();
			if (name != null) {

				Address address = addressBook.getAddress();
				/*
				 * try { address.validate(); } catch (InvalidDataException e) {
				 * LOGGER.warn("{} : {}", e.getMessage(), node.asXML()); }
				 */

				Agent agent = new Agent(name, address, agentRepType);
				agent.setSequence(sequence);

				if (addressBook.getOrgName() != null) {
					agent.addRelationship(addressBook.getOrgName(), RelationshipType.REPRESENTATIVE);
				}

				agentList.add(agent);
			}

		}

		if (agentList.size() > 0) {
			return agentList;
		}

		/*
		 * If Agents are not defined then use correspondence-address if available.
		 */
		List<Node> correspondenceNodes = document.selectNodes(FRAGMENT_PATH2);
		for (Node node : correspondenceNodes) {
			AddressBookNode addressBook = new AddressBookNode(node);

			Name name;
			if (addressBook.getPersonName() != null) {
				name = addressBook.getPersonName();
			} else {
				name = addressBook.getOrgName();
			}

			if (name != null) {

				Address address = addressBook.getAddress();
				try {
					address.validate();
				} catch (InvalidDataException e) {
					LOGGER.warn("{} : {}", e.getMessage(), node.asXML());
				}

				Agent agent = new Agent(name, address, AgentRepType.AGENT);

				if (addressBook.getOrgName() != null) {
					agent.addRelationship(addressBook.getOrgName(), RelationshipType.REPRESENTATIVE);
				}

				agentList.add(agent);
			}

		}
		return agentList;
	}

}
