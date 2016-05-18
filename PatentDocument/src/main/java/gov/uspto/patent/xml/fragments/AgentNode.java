package gov.uspto.patent.xml.fragments;

import java.util.ArrayList;
import java.util.List;

import javax.naming.directory.InvalidAttributesException;

import org.dom4j.Document;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.model.entity.Agent;
import gov.uspto.patent.model.entity.AgentRepType;
import gov.uspto.patent.model.entity.RelationshipType;
import gov.uspto.patent.xml.items.AddressBookNode;

public class AgentNode extends DOMFragmentReader<List<Agent>> {
	private static final Logger LOGGER = LoggerFactory.getLogger(AgentNode.class);
	
	private static final String FRAGMENT_PATH = "//us-parties/agents/agent";
	
	public AgentNode(Document document) {
		super(document);
	}

	@Override
	public List<Agent> read(){
		List<Agent> agentList = new ArrayList<Agent>();
		
		@SuppressWarnings("unchecked")
		List<Node> agents = document.selectNodes(FRAGMENT_PATH);

		for(Node node: agents){
			AddressBookNode addressBook = new AddressBookNode(node);

			Node sequenceN = node.selectSingleNode("@sequence");
			String sequence = sequenceN != null ? sequenceN.getText() : null;

			Node repTypeN = node.selectSingleNode("@rep-type");
			String repType = repTypeN != null ? repTypeN.getText() : null;

			AgentRepType agentRepType = AgentRepType.valueOf(repType.trim().toUpperCase());
		
			try {
				Agent agent = new Agent(addressBook.getPersonName(), addressBook.getAddress(), agentRepType);
				agent.setSequence(sequence);

				if (addressBook.getOrgName() != null){
					agent.addRelationship(addressBook.getOrgName(), RelationshipType.REPRESENTATIVE);
				}

				agentList.add( agent );
			} catch (InvalidAttributesException e) {
				LOGGER.warn("Invalid Agent: {}", node.asXML(), e);
			}

		}

		return agentList;
	}
	
}
