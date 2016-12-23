package gov.uspto.patent.doc.xml.fragments;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.Node;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.doc.xml.items.ClassificationCpcNode;
import gov.uspto.patent.doc.xml.items.ClassificationIPCNode;
import gov.uspto.patent.doc.xml.items.ClassificationLocarnoNode;
import gov.uspto.patent.doc.xml.items.ClassificationNationalNode;
import gov.uspto.patent.model.classification.PatentClassification;
import gov.uspto.patent.model.classification.CpcClassification;
import gov.uspto.patent.model.classification.IpcClassification;
import gov.uspto.patent.model.classification.LocarnoClassification;
import gov.uspto.patent.model.classification.UspcClassification;

public class ClassificationNode extends DOMFragmentReader<Set<PatentClassification>> {
    private static final String IPC_PATH2 = "//classifications-ipcr/classification-ipcr";
    private static final String IPC_PATH = "//classification-ipc";
    private static final String CPC_PATH = "//classifications-cpc"; // has sub classification-cpc
    private static final String USPC_PATH = "//classification-national";
    private static final String LOCARNO_PATH = "//classification-locarno";

    public ClassificationNode(Document document) {
        super(document);
    }

    @Override
    public Set<PatentClassification> read() {
        Set<PatentClassification> classifications = new LinkedHashSet<PatentClassification>();

        @SuppressWarnings("unchecked")
        List<Node> uspc = document.selectNodes(USPC_PATH);
        for (Node usclass : uspc) {
            UspcClassification classification = (UspcClassification) new ClassificationNationalNode(usclass).read();
            if (classification != null) {
                classifications.add(classification);
            }
        }

        @SuppressWarnings("unchecked")
        List<Node> cpc = document.selectNodes(CPC_PATH + "/main-cpc");
        for (Node cpclass : cpc) {
            CpcClassification classification = (CpcClassification) new ClassificationCpcNode(cpclass).read();
            if (classification != null) {
                classification.setIsMainClassification(true);
                classifications.add(classification);
            }
        }

        @SuppressWarnings("unchecked")
        List<Node> cpc2 = document.selectNodes(CPC_PATH + "/further-cpc");
        for (Node cpclass : cpc2) {
            CpcClassification classification = (CpcClassification) new ClassificationCpcNode(cpclass).read();
            if (classification != null) {
                classification.setIsMainClassification(false);
                classifications.add(classification);
            }
        }

        @SuppressWarnings("unchecked")
        List<Node> ipc = document.selectNodes(IPC_PATH);
        for (Node ipclass : ipc) {
            IpcClassification classification = (IpcClassification) new ClassificationIPCNode(ipclass).read();
            if (classification != null) {
                classifications.add(classification);
            }
        }

        @SuppressWarnings("unchecked")
        List<Node> ipc2 = document.selectNodes(IPC_PATH2);
        for (Node ipclass : ipc2) {
            IpcClassification classification = (IpcClassification) new ClassificationIPCNode(ipclass).read();
            if (classification != null) {
                classifications.add(classification);
            }
        }

        @SuppressWarnings("unchecked")
        List<Node> locarnos = document.selectNodes(LOCARNO_PATH);
        for (Node locarnoClass : locarnos) {
            LocarnoClassification classification = (LocarnoClassification) new ClassificationLocarnoNode(locarnoClass).read();
            if (classification != null) {
                classifications.add(classification);
            }
        }

        return classifications;
    }

}
