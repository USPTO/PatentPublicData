package gov.uspto.patent.doc.pap.fragments;

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
import gov.uspto.patent.doc.pap.items.AddressNode;
import gov.uspto.patent.model.entity.Address;
import gov.uspto.patent.model.entity.Agent;
import gov.uspto.patent.model.entity.AgentRepType;
import gov.uspto.patent.model.entity.Name;
import gov.uspto.patent.model.entity.NameOrg;

public class AgentNode extends DOMFragmentReader<List<Agent>> {
	private static final Logger LOGGER = LoggerFactory.getLogger(AgentNode.class);

	private static final XPath AGENTXP = DocumentHelper.createXPath("/patent-application-publication/subdoc-bibliographic-information/correspondence-address");
	private static final XPath AGENTNAMEXP = DocumentHelper.createXPath("name-1|name-2");

    public AgentNode(Document document) {
        super(document);
    }

    @Override
    public List<Agent> read() {
        List<Agent> agents = new ArrayList<Agent>();

        Node correspondenceN = AGENTXP.selectSingleNode(document);
        if (correspondenceN == null){
        	return agents;
        }

        /*
         * Following name elements are name used for mailing.
         * name-2 is often empty.
         * company name can occur in either name-1 or name-2
         * 
         * Variations:
         *   <name-1>PERSON OR ENTITY NAME</name-1>
         *   <name-2>COMPANY NAME</name-2>
         *   
         *   <name-1>COMPANY NAME</name-1>
         *   <name-2>FLOOR</name-2> 
         */
        List<Node> namesN = AGENTNAMEXP.selectNodes(correspondenceN);
        StringBuilder stb = new StringBuilder();
        for (int i=0; namesN.size() > i; i++) {
        	if (namesN.get(i).getText().trim().length() > 1) {
            	if (i > 0) {
            		stb.append(" ; ");  // using semi-colon since commas may occur within name.
            	}
        		stb.append(namesN.get(i).getText());
        	}
        }
        String addressName = stb.toString();

        Address address = new AddressNode(correspondenceN).read();
		try {
			address.validate();
		} catch (InvalidDataException e) {
			LOGGER.warn("{} : {}", e.getMessage(), correspondenceN.asXML());
		}

        Name name = new NameOrg(addressName); // FIXME, need to disambiguate between Person and Organization.
        if (name != null){
        	try {
        		name.validate();
    		} catch (InvalidDataException e) {
    			LOGGER.warn("{} : {}", e.getMessage(), correspondenceN.asXML());
    		}

            agents.add(new Agent(name, address, AgentRepType.AGENT));
        }

        return agents;
    }
}
