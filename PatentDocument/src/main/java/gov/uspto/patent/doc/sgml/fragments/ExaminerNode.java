package gov.uspto.patent.doc.sgml.fragments;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.doc.sgml.items.NameNode;
import gov.uspto.patent.model.ExaminerType;
import gov.uspto.patent.model.entity.Examiner;
import gov.uspto.patent.model.entity.Name;

public class ExaminerNode extends DOMFragmentReader<List<Examiner>> {

	private static final XPath PARENTXP = DocumentHelper.createXPath("/PATDOC/SDOBI/B700/B745");
	private static final XPath P_EXAMINERXP = DocumentHelper.createXPath("B746");
	private static final XPath A_EXAMINERXP = DocumentHelper.createXPath("B747");
	private static final XPath ARTUNITXP = DocumentHelper.createXPath("B748US/PDAT");
	private static final XPath NAMEXP = DocumentHelper.createXPath("PARTY-US");

	public ExaminerNode(Document document) {
		super(document);
	}

	@Override
	public List<Examiner> read() {
		List<Examiner> examinerList = new ArrayList<Examiner>();

		Node examiner = PARENTXP.selectSingleNode(document);
		if (examiner == null) {
			return examinerList;
		}

		Node primaryExaminerN = P_EXAMINERXP.selectSingleNode(examiner);
		Examiner primaryExaminer = getExaminer(primaryExaminerN, ExaminerType.PRIMARY);
		if (primaryExaminer != null) {
			examinerList.add(primaryExaminer);
		}

		Node assistantExaminerN = A_EXAMINERXP.selectSingleNode(examiner);
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

		Node dataNode = NAMEXP.selectSingleNode(examinerNode);
		Name name = new NameNode(dataNode).read();

		Node artUnitN = ARTUNITXP.selectSingleNode(examinerNode.getParent());
		String artUnit = artUnitN != null ? artUnitN.getText() : null;

		Examiner examiner = new Examiner(name, artUnit, type);
		
		return examiner;
	}

}
