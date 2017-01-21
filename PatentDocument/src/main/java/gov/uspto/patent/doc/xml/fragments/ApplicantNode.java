package gov.uspto.patent.doc.xml.fragments;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.doc.xml.items.AddressBookNode;
import gov.uspto.patent.model.entity.Applicant;
import gov.uspto.patent.model.entity.Name;

public class ApplicantNode extends DOMFragmentReader<List<Applicant>> {

	/*
	 * CURRENT: //us-parties/us-applicants/us-applicant
	 * PRE-2012: //parties/applicants/applicant
	 */
	private static final String FRAGMENT_PATH = "//us-parties/us-applicants/us-applicant|//parties/applicants/applicant";

	public ApplicantNode(Document document) {
		super(document);
	}

	@Override
	public List<Applicant> read() {
		List<Applicant> applicantList = new ArrayList<Applicant>();

		@SuppressWarnings("unchecked")
		List<Node> applNodes = document.selectNodes(FRAGMENT_PATH);
		applicantList.addAll(readApplicants(applNodes));

		return applicantList;
	}

	private List<Applicant> readApplicants(List<Node> applicants) {
		List<Applicant> applicantList = new ArrayList<Applicant>();

		for (Node node : applicants) {
			AddressBookNode addressBook = new AddressBookNode(node);

			Name applicantName;
			if (addressBook.getPersonName() != null) {
				applicantName = addressBook.getPersonName();
			} else {
				applicantName = addressBook.getOrgName();
			}

			if (applicantName != null) {
				Applicant applicant = new Applicant(applicantName, addressBook.getAddress());
				applicantList.add(applicant);
			}
		}

		return applicantList;
	}

}
