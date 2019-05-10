package gov.uspto.patent.doc.greenbook.fragments;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.doc.greenbook.items.NameNode;
import gov.uspto.patent.model.ExaminerType;
import gov.uspto.patent.model.entity.Examiner;
import gov.uspto.patent.model.entity.Name;

public class ExaminerNode extends DOMFragmentReader<List<Examiner>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(NameNode.class);

	private static final String ARTUNIT = "/DOCUMENT/PATN/ART";
	private static final String PRIMARY = "/DOCUMENT/PATN/EXP";
	private static final String ASSISTANT = "/DOCUMENT/PATN/EXA";

	public ExaminerNode(Document document) {
		super(document);
	}

	@Override
	public List<Examiner> read() {
		List<Examiner> examinerList = new ArrayList<Examiner>();

		String artUnit = getArtUnit();

		Node primaryNode = document.selectSingleNode(PRIMARY);

		Examiner primary = getExaminer(primaryNode, ExaminerType.PRIMARY, artUnit);
		if (primary != null) {
			examinerList.add(primary);
		}

		Node assistantNode = document.selectSingleNode(ASSISTANT);
		Examiner assistant = getExaminer(assistantNode, ExaminerType.ASSISTANT, artUnit);
		if (assistant != null) {
			examinerList.add(assistant);
		}

		return examinerList;
	}

	public String getArtUnit() {
		Node artN = document.selectSingleNode(ARTUNIT);
		return artN != null ? artN.getText() : null;
	}

	public Examiner getExaminer(Node examinerNode, ExaminerType type, String artUnit) {

		if (examinerNode != null) {
			String fullName = examinerNode != null ? examinerNode.getText() : null;

			Name name = null;
			try {
				name = new NameNode(examinerNode).createName(fullName);
			} catch (InvalidDataException e) {
				LOGGER.warn("{} : {}", e.getMessage(), examinerNode.asXML());
			}

			if (name != null) {
				try {
					name.validate();
				} catch (InvalidDataException e) {
					LOGGER.warn("{} : {}", e.getMessage(), examinerNode.asXML());
				}

				return new Examiner(name, artUnit, type);
			}
		}

		return null;
	}
}
