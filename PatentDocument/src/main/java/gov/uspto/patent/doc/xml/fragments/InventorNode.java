package gov.uspto.patent.doc.xml.fragments;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.doc.xml.items.AddressBookNode;
import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.entity.Inventor;
import gov.uspto.patent.model.entity.Name;
import gov.uspto.patent.model.entity.RelationshipType;

public class InventorNode extends DOMFragmentReader<List<Inventor>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(InventorNode.class);

    private static final String FRAGMENT_PATH = "//us-parties/inventors/inventor"; // current.

    private static final String FRAGMENT_PATH2 = "//parties/inventors/inventor"; // pre-2012.

    private static final String FRAGMENT_PATH_APPLICANT = "//us-parties/us-applicants/us-applicant"; // current.

    private static final String FRAGMENT_PATH_APPLICANT2 = "//parties/applicants/applicant"; // pre-2012.

    public InventorNode(Document document) {
        super(document);
    }

    @Override
    public List<Inventor> read() {
        List<Inventor> inventorList = new ArrayList<Inventor>();

        @SuppressWarnings("unchecked")
        List<Node> applicants = document.selectNodes(FRAGMENT_PATH_APPLICANT);
        List<Inventor> applicantInventors = readApplicantInventors(applicants);
        inventorList.addAll(applicantInventors);
        //if (!applicantInventors.isEmpty()) {
        //    return applicantInventors;
        //}

        @SuppressWarnings("unchecked")
        List<Node> applicants2 = document.selectNodes(FRAGMENT_PATH_APPLICANT2);
        List<Inventor> applicantInventors2 = readApplicantInventors(applicants2);
        inventorList.addAll(applicantInventors2);
        //if (!applicantInventors2.isEmpty()) {
        //    return applicantInventors2;
        //}

        @SuppressWarnings("unchecked")
        List<Node> inventors = document.selectNodes(FRAGMENT_PATH);
        List<Inventor> currentLocInventors = readInventors(inventors);
        inventorList.addAll(currentLocInventors);
        //if (!currentLocInventors.isEmpty()) {
        //    return currentLocInventors;
        //}

        @SuppressWarnings("unchecked")
        List<Node> inventors2 = document.selectNodes(FRAGMENT_PATH2);
        List<Inventor> pre2012Inventors = readInventors(inventors2);
        inventorList.addAll(pre2012Inventors);
        //return pre2012Inventors;

        return inventorList;
    }

    /**
     * Applicants which have attribute "app-type" value of "applicant-inventor"
     * 
     * @return
     */
    private List<Inventor> readApplicantInventors(List<Node> applicants) {
        List<Inventor> inventorList = new ArrayList<Inventor>();

        for (Node node : applicants) {
            String appType = ((Element) node).attribute("app-type").getValue();
            if ("applicant-inventor".equalsIgnoreCase(appType)) {
                Inventor inventor = readInventor(node);
                if (inventor != null) {
                    inventorList.add(inventor);
                }
            }
        }

        return inventorList;
    }

    private List<Inventor> readInventors(List<Node> inventors) {
        List<Inventor> inventorList = new ArrayList<Inventor>();

        for (Node node : inventors) {
            Inventor inventor = readInventor(node);
            if (inventor != null) {
                inventorList.add(inventor);
            }
        }

        return inventorList;
    }

    private Inventor readInventor(Node inventorNode) {
        AddressBookNode addressBook = new AddressBookNode(inventorNode);

        Node residenceN = inventorNode.selectSingleNode("residence/country");
        CountryCode residenceCC = null;
        try {
            residenceCC = residenceN != null ? CountryCode.fromString(residenceN.getText()) : null;
        } catch (InvalidDataException e1) {
            LOGGER.warn("Invalid Residence Country Code", e1);
        }

    	Name name = addressBook.getPersonName() != null ? addressBook.getPersonName() : addressBook.getOrgName();
        Inventor inventor = new Inventor(name, addressBook.getAddress());
        inventor.setResidency(residenceCC);
        if (addressBook.getOrgName() != null) {
            inventor.addRelationship(addressBook.getOrgName(), RelationshipType.EMPLOYEE);
        }
        return inventor;
    }

}
