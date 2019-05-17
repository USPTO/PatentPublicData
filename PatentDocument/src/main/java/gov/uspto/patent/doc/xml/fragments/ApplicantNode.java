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
import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.entity.Address;
import gov.uspto.patent.model.entity.Applicant;
import gov.uspto.patent.model.entity.Name;

public class ApplicantNode extends DOMFragmentReader<List<Applicant>> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicantNode.class);

	/*
	 * CURRENT: //us-parties/us-applicants/us-applicant PRE-2012:
	 * //parties/applicants/applicant
	 */
	private static final String FRAGMENT_PATH = "//us-parties/us-applicants/us-applicant|//parties/applicants/applicant";

	public ApplicantNode(Document document) {
		super(document);
	}

	@Override
	public List<Applicant> read() {
		List<Applicant> applicantList = new ArrayList<Applicant>();

		List<Node> applNodes = document.selectNodes(FRAGMENT_PATH);
		applicantList.addAll(readApplicants(applNodes));

		return applicantList;
	}

	private List<Applicant> readApplicants(List<Node> applicants) {
		List<Applicant> applicantList = new ArrayList<Applicant>();

		for (Node node : applicants) {
			AddressBookNode addressBook = new AddressBookNode(node);

			Name personName = addressBook.getPersonName();
			Name orgName = addressBook.getPersonName();
			Name applicantName = personName != null ? personName : orgName;

			if (applicantName == null) {
				continue;
			}

			if (personName != null) {
				try {
					applicantName.validate();
				} catch (InvalidDataException e) {
					LOGGER.warn("{} : {}", e.getMessage(), node.asXML());
				}
			}

			Address address = addressBook.getAddress();
			if (address == null) {
				address = new Address("", "", CountryCode.UNDEFINED);
				LOGGER.warn("Missing address : {}", node.asXML());
			} else {
				try {
					address.validate();
				} catch (InvalidDataException e) {
					LOGGER.warn("{} : {}", e.getMessage(), node.asXML());
				}
			}

			if (applicantName != null) {
				Applicant applicant = new Applicant(applicantName, address);
				applicantList.add(applicant);
			}
		}

		return applicantList;
	}

}
