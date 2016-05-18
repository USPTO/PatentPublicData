package gov.uspto.patent.xml.fragments;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.Node;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.model.classification.Classification;
import gov.uspto.patent.xml.items.ClassificationCpcNode;
import gov.uspto.patent.xml.items.ClassificationNationalNode;

public class ClassificationSearchNode extends DOMFragmentReader<Set<Classification>> {
		private static final String FRAGMENT_PATH = "//us-field-of-classification-search"; // Only PGPub.

		private Node parentPath;

		public ClassificationSearchNode(Document document){
			super(document);
			
			Node parentPath = document.selectSingleNode(FRAGMENT_PATH);
			if (parentPath != null){
				this.parentPath = parentPath;
			} else {
				this.parentPath = document.getRootElement();
			}
		}

		@Override
		public Set<Classification> read() {
			Set<Classification> classifications = new HashSet<Classification>();
			
			@SuppressWarnings("unchecked")
			List<Node> nationalN = parentPath.selectNodes("classification-national");
			for (Node classNode: nationalN){
				classifications.add( new ClassificationNationalNode(classNode).read() );
			}
	
			Classification classification = new ClassificationCpcNode(parentPath).read();
			if (classification != null){
				classifications.add( classification );
			}
						
			return classifications;
		}
}
