package gov.uspto.patent.doc.sgml.fragments;

import java.util.ArrayList;
import java.util.List;

import javax.naming.directory.InvalidAttributesException;

import org.dom4j.Document;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.doc.sgml.items.NameNode;
import gov.uspto.patent.model.ExaminerType;
import gov.uspto.patent.model.entity.Examiner;
import gov.uspto.patent.model.entity.Name;
import gov.uspto.patent.model.entity.NamePerson;

public class ExaminerNode extends DOMFragmentReader<List<Examiner>> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExaminerNode.class);

	private static final String FRAGMENT_PATH = "/PATDOC/SDOBI/B700/B745";

	public ExaminerNode(Document document) {
		super(document);
	}

	@Override
	public List<Examiner> read() {
		Node examiner = document.selectSingleNode(FRAGMENT_PATH);
		if (examiner == null) {
			return null;
		}

		List<Examiner> examinerList = new ArrayList<Examiner>();

		Node primaryExaminerN = examiner.selectSingleNode("B746");
		Examiner primaryExaminer = getExaminer(primaryExaminerN, ExaminerType.PRIMARY);
		examinerList.add(primaryExaminer);

		Node assistantExaminerN = examiner.selectSingleNode("B747");
		Examiner assistantExaminer = getExaminer(assistantExaminerN, ExaminerType.ASSISTANT);
		if (assistantExaminer != null) {
			examinerList.add(assistantExaminer);
		}

		return examinerList;
	}

	public Examiner getExaminer(Node examinerNode, ExaminerType type) {
		if (examinerNode == null) {
			return null;
		}

		Node dataNode = examinerNode.selectSingleNode("PARTY-US");
		Name name = new NameNode(dataNode).read();

		Node artUnitN = examinerNode.getParent().selectSingleNode("B748US");
		String artUnit = artUnitN != null ? artUnitN.getText() : null;

		Examiner examiner = new Examiner(name, artUnit, type);
		return examiner;
	}

}
