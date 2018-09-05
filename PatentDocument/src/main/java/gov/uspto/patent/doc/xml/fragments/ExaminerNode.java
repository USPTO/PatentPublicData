package gov.uspto.patent.doc.xml.fragments;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.doc.xml.items.AddressBookNode;
import gov.uspto.patent.model.ExaminerType;
import gov.uspto.patent.model.entity.Examiner;
import gov.uspto.patent.model.entity.Name;

public class ExaminerNode extends DOMFragmentReader<List<Examiner>> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExaminerNode.class);

	private static final String PRIMARY = "//us-bibliographic-data-grant/examiners/primary-examiner";
	private static final String ASSISTANT = "//us-bibliographic-data-grant/examiners/assistant-examiner";

	public ExaminerNode(Document document) {
		super(document);
	}

	@Override
	public List<Examiner> read() {
		List<Examiner> examinerList = new ArrayList<Examiner>();

		Examiner primary = readPrimaryExaminer();
		if (primary != null) {
			examinerList.add(primary);
		}

		Examiner assistant = readAssistantExaminer();
		if (assistant != null) {
			if (assistant.getDepartment() == null) {
				assistant.setDepartment(primary.getDepartment());
			}
			examinerList.add(assistant);
		}

		return examinerList;
	}

	public Examiner readPrimaryExaminer() {
		Node primaryN = document.selectSingleNode(PRIMARY);
		if (primaryN == null) {
			return null;
		}

		Node departmentN = primaryN.selectSingleNode("department");
		String department = departmentN != null ? departmentN.getText() : null;

		Name name = new AddressBookNode(primaryN).getPersonName();
		if (name != null){
			return new Examiner(name, department, ExaminerType.PRIMARY);
		}
		return null;
	}

	public Examiner readAssistantExaminer() {
		Node assistantN = document.selectSingleNode(ASSISTANT);
		if (assistantN == null) {
			return null;
		}

		Node departmentN = assistantN.selectSingleNode("department");
		String department = departmentN != null ? departmentN.getText() : null;

		Name name = new AddressBookNode(assistantN).getPersonName();
		if (name != null){
			return new Examiner(name, department, ExaminerType.ASSISTANT);
		}
		return null;
	}

}
