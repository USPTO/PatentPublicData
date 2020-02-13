package gov.uspto.patent.doc.xml.fragments;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.Node;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.doc.xml.items.ClassificationCpcNode;
import gov.uspto.patent.doc.xml.items.ClassificationNationalNode;
import gov.uspto.patent.model.classification.PatentClassification;

public class ClassificationSearchNode extends DOMFragmentReader<Set<PatentClassification>> {
	private static final String FRAGMENT_PATH = "//us-field-of-classification-search"; // Only PGPub.

	private Node parentPath;

	public ClassificationSearchNode(Document document) {
		super(document);

		Node parentPath = document.selectSingleNode(FRAGMENT_PATH);
		if (parentPath != null) {
			this.parentPath = parentPath;
		} else {
			this.parentPath = document.getRootElement();
		}
	}

	@Override
	public Set<PatentClassification> read() {
		Set<PatentClassification> classifications = new HashSet<PatentClassification>();

		List<Node> nationalN = parentPath.selectNodes("classification-national");
		for (Node classNode : nationalN) {
			classifications.addAll(new ClassificationNationalNode(classNode).read());
		}

		List<Node> cpcN = parentPath.selectNodes("classification-cpc-text|classification-cpc");
		for (Node cpc : cpcN) {
			classifications.addAll(new ClassificationCpcNode(cpc, true).read());
		}

		return classifications;
	}
}
