package gov.uspto.patent.doc.xml.fragments;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.doc.xml.items.ClassificationCpcNode;
import gov.uspto.patent.doc.xml.items.ClassificationIPCNode;
import gov.uspto.patent.doc.xml.items.ClassificationLocarnoNode;
import gov.uspto.patent.doc.xml.items.ClassificationNationalNode;
import gov.uspto.patent.model.classification.PatentClassification;

public class ClassificationNode extends DOMFragmentReader<Set<PatentClassification>> {
	
	private static final XPath IPCXP = DocumentHelper.createXPath("/*/*/classifications-ipcr/classification-ipcr|/*/*/classification-ipc");
	private static final XPath USPCXP = DocumentHelper.createXPath("/*/*/classification-national");
	private static final XPath LOCARNOXP = DocumentHelper.createXPath("/*/*/classification-locarno");
	private static final XPath CPCMAINXP = DocumentHelper.createXPath("/*/*/classifications-cpc/main-cpc/classification-cpc");
	private static final XPath CPCFURTHERXP = DocumentHelper.createXPath("/*/*/classifications-cpc/further-cpc/classification-cpc");

    public ClassificationNode(Document document) {
        super(document);
    }

    @Override
    public Set<PatentClassification> read() {
        Set<PatentClassification> classifications = new LinkedHashSet<PatentClassification>();

        List<Node> uspc = USPCXP.selectNodes(document);
        for (Node usclass : uspc) {
        	classifications.addAll(new ClassificationNationalNode(usclass).read());
        }

        List<Node> cpc = CPCMAINXP.selectNodes(document);
        for (Node cpclass : cpc) {
        	classifications.addAll(new ClassificationCpcNode(cpclass, true).read());
        }

        List<Node> cpc2 = CPCFURTHERXP.selectNodes(document);
        for (Node cpclass : cpc2) {
        	classifications.addAll(new ClassificationCpcNode(cpclass, false).read());
        }

        List<Node> ipc = IPCXP.selectNodes(document);
        for (Node ipclass : ipc) {
        	classifications.addAll(new ClassificationIPCNode(ipclass).read());
        }

        List<Node> locarnos = LOCARNOXP.selectNodes(document);
        for (Node locarnoClass : locarnos) {
        	classifications.addAll(new ClassificationLocarnoNode(locarnoClass).read());
        }

        return classifications;
    }

}
