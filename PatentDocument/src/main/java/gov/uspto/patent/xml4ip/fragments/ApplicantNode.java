package gov.uspto.patent.xml4ip.fragments;

import java.util.ArrayList;
import java.util.List;

import javax.naming.directory.InvalidAttributesException;

import org.dom4j.Document;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.model.entity.Applicant;
import gov.uspto.patent.model.entity.Name;
import gov.uspto.patent.xml.items.AddressBookNode;

public class ApplicantNode extends DOMFragmentReader<List<Applicant>>{
	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicantNode.class);

	private static final String FRAGMENT_PATH = "//Applicant";

	private List<Applicant> applicantList;
	
	public ApplicantNode(Document document){
		super(document);
	}

	@Override
	public List<Applicant> read() {
		applicantList = new ArrayList<Applicant>();
		
		@SuppressWarnings("unchecked")
		List<Node> applicants = document.selectNodes(FRAGMENT_PATH);
		readApplicants(applicants);
		
		return applicantList;
	}

	private void readApplicants(List<Node> applicants){
		for(Node node: applicants){
			AddressBookNode addressBook = new AddressBookNode(node);

			try {
				Name applicantName;
				if (addressBook.getPersonName() != null){
					applicantName = addressBook.getPersonName();
				} else {
					applicantName = addressBook.getOrgName();
				}

				Applicant applicant = new Applicant(applicantName, addressBook.getAddress());				
				applicantList.add( applicant );
			} catch (InvalidAttributesException e) {
				LOGGER.warn("Invalid Applicant: {}", node.asXML(), e);
			}

		}
	}
	
}
