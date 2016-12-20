package gov.uspto.patent.doc.xml.fragments;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.doc.xml.items.DocumentIdNode;
import gov.uspto.patent.model.DocumentId;
import gov.uspto.patent.model.DocumentIdType;

/**
 * 
 * <!ELEMENT priority-claim (country , doc-number? , date , office-of-filing? , (priority-doc-requested | priority-doc-attached)?)>
 * <!ATTLIST priority-claim  id       ID     #IMPLIED
 *                           sequence CDATA  #REQUIRED
 *                           kind            (national | regional | international )  #REQUIRED >
 *                                                     
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class PriorityClaims extends DOMFragmentReader<List<DocumentId>> {
    private static final String FRAGMENT_PATH = "//priority-claims/priority-claim";

    public PriorityClaims(Document document) {
        super(document);
    }

    @Override
    public List<DocumentId> read() {
        List<DocumentId> priorityDocIds = new ArrayList<DocumentId>();

        List<Node> fragmentNodes = document.selectNodes(FRAGMENT_PATH);
        for (Node fragNode : fragmentNodes) {
            if (fragNode != null) {
                DocumentId documentId = new DocumentIdNode(fragNode).read();
                // attribute "kind" (national | regional | international )
                documentId.setType(DocumentIdType.REGIONAL_FILING);
                priorityDocIds.add(documentId);
            }
        }
        
        return priorityDocIds;
    }

}
