package gov.uspto.patent.greenbook.fragments;

import java.util.ArrayList;
import java.util.List;

import javax.naming.directory.InvalidAttributesException;

import org.dom4j.Document;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.greenbook.items.NameNode;
import gov.uspto.patent.model.ExaminerType;
import gov.uspto.patent.model.entity.Examiner;
import gov.uspto.patent.model.entity.Name;

public class ExaminerNode extends DOMFragmentReader<List<Examiner>> {
	private static final Logger LOGGER = LoggerFactory.getLogger(NameNode.class);

	private static final String PRIMARY = "/DOCUMENT/PATN/EXP";
	private static final String ASSISTANT = "/DOCUMENT/PATN/EXA";

	public ExaminerNode(Document document) {
		super(document);
	}

	@Override
	public List<Examiner> read() {
		List<Examiner> examinerList = new ArrayList<Examiner>();

		Examiner primary = getPrimaryExaminer();
		if (primary != null){
			examinerList.add(primary);
		}
		
		Examiner assistant = getAssistantExaminer();
		if (assistant != null){
			examinerList.add(assistant);
		}

		return examinerList;
	}

	public Examiner getPrimaryExaminer(){
		Node primaryN = document.selectSingleNode(PRIMARY);
		if (primaryN != null) {
			String fullName = primaryN != null ? primaryN.getText() : null;
			try {
				Name name = new NameNode(primaryN).createName(fullName);
				return new Examiner(name, null, ExaminerType.PRIMARY);
			} catch (InvalidAttributesException e) {
				LOGGER.warn("Invalid Name: {}", fullName, e);
			}
		}
		return null;
	}

	public Examiner getAssistantExaminer(){
		Node assistantN = document.selectSingleNode(ASSISTANT);
		if (assistantN != null) {
			String fullName = assistantN != null ? assistantN.getText() : null;
			try {
				Name name = new NameNode(assistantN).createName(fullName);
				return new Examiner(name, null, ExaminerType.ASSISTANT);
			} catch (InvalidAttributesException e) {
				LOGGER.warn("Invalid Name: {}", fullName, e);
			}
		}
		return null;
	}
}
