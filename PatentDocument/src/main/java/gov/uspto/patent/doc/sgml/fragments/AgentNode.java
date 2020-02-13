package gov.uspto.patent.doc.sgml.fragments;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.doc.sgml.items.AddressNode;
import gov.uspto.patent.doc.sgml.items.NameNode;
import gov.uspto.patent.model.entity.Address;
import gov.uspto.patent.model.entity.Agent;
import gov.uspto.patent.model.entity.Name;

public class AgentNode extends DOMFragmentReader<List<Agent>> {

	private static final XPath AGENTSXP = DocumentHelper.createXPath("/PATDOC/SDOBI/B700/B740");
	private static final XPath ADATAXP = DocumentHelper.createXPath("B741/PARTY-US");

	public AgentNode(Document document) {
		super(document);
	}

	@Override
	public List<Agent> read() {
		List<Agent> agentList = new ArrayList<Agent>();

		List<Node> agents = AGENTSXP.selectNodes(document);
		for (Node agentNode : agents) {

			Node dataNode = ADATAXP.selectSingleNode(agentNode);

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
