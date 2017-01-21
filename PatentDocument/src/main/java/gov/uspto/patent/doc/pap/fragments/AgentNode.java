package gov.uspto.patent.doc.pap.fragments;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.doc.pap.items.AddressNode;
import gov.uspto.patent.model.entity.Address;
import gov.uspto.patent.model.entity.Agent;
import gov.uspto.patent.model.entity.AgentRepType;
import gov.uspto.patent.model.entity.Name;
import gov.uspto.patent.model.entity.NameOrg;

public class AgentNode extends DOMFragmentReader<List<Agent>> {

    private static final String FRAGMENT_PATH = "/patent-application-publication/subdoc-bibliographic-information/correspondence-address";

    public AgentNode(Document document) {
        super(document);
    }

    @Override
    public List<Agent> read() {
        List<Agent> agents = new ArrayList<Agent>();

        Node correspondenceN = document.selectSingleNode(FRAGMENT_PATH);
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
        Node name1N = correspondenceN.selectSingleNode("name-1");
        Node name2N = correspondenceN.selectSingleNode("name-2");

        String name1Str = name1N != null ? name1N.getText() : "";
        String name2Str = name2N != null ? name2N.getText() : "";
        String addressName = name2Str.isEmpty() ? name1Str : name1Str + "; " + name2Str; // using semi-colon since commas may occur within name.

        Address address = new AddressNode(correspondenceN).read();
        
        Name name = new NameOrg(addressName); // FIXME, need to disambiguate between Person and Organization.
        if (name != null){
            agents.add(new Agent(name, address, AgentRepType.AGENT));
        }

        return agents;
    }
}
