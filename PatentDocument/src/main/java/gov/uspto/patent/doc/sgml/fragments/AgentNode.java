package gov.uspto.patent.doc.sgml.fragments;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.doc.sgml.items.AddressNode;
import gov.uspto.patent.doc.sgml.items.NameNode;
import gov.uspto.patent.model.entity.Address;
import gov.uspto.patent.model.entity.Agent;
import gov.uspto.patent.model.entity.Name;

public class AgentNode extends DOMFragmentReader<List<Agent>>{
	private static final String FRAGMENT_PATH = "/PATDOC/SDOBI/B700/B740";

	public AgentNode(Document document){
		super(document);
	}

	@Override
	public List<Agent> read() {
		List<Agent> agentList = new ArrayList<Agent>();

		@SuppressWarnings("unchecked")
		List<Node> agents = document.selectNodes(FRAGMENT_PATH);
		for(Node agentNode: agents){

			Node dataNode = agentNode.selectSingleNode("B741/PARTY-US");

			Agent agent = readAgent(dataNode);
			agentList.add(agent);
		}

		return agentList;
	}

	public Agent readAgent(Node agentNode){
		Name name = new NameNode(agentNode).read();

		Address address = new AddressNode(agentNode).read();

		return new Agent(name, address, null);
	}
	
}
