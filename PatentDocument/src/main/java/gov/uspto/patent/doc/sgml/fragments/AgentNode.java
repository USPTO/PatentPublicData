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

public class AgentNode extends DOMFragmentReader<List<Agent>> {

	private static final String AGENT_LIST = "/PATDOC/SDOBI/B700/B740";

	private static final String AGENT = "B741/PARTY-US";

	public AgentNode(Document document) {
		super(document);
	}

	@Override
	public List<Agent> read() {
		List<Agent> agentList = new ArrayList<Agent>();

		@SuppressWarnings("unchecked")
		List<Node> agents = document.selectNodes(AGENT_LIST);
		for (Node agentNode : agents) {

			Node dataNode = agentNode.selectSingleNode(AGENT);

			Agent agent = readAgent(dataNode);
			if (agent != null) {
				agentList.add(agent);
			}
		}

		return agentList;
	}

	public Agent readAgent(Node agentNode) {
		Name name = new NameNode(agentNode).read();

		if (name != null) {
			Address address = new AddressNode(agentNode).read();
			return new Agent(name, address, null);
		}

		return null;
	}

}
