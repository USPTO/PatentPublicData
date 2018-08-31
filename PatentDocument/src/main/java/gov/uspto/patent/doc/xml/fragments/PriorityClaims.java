package gov.uspto.patent.doc.xml.fragments;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.doc.xml.items.DocumentIdNode;
import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.DocumentDate;
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

	private static final Logger LOGGER = LoggerFactory.getLogger(DocumentIdNode.class);
	
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

                // priority claim attribute "kind" (national | regional | international )
                Element element = (Element) fragNode;
                String priorityKind = element.attributeValue("kind");
                DocumentIdType docIdType;
                switch (priorityKind) {
                	case "national":
                		docIdType = DocumentIdType.NATIONAL_FILING;
                		break;
                	case "regional":
                		docIdType = DocumentIdType.REGIONAL_FILING;
                		break;
                	case "international":
                		docIdType = DocumentIdType.INTERNATIONAL_FILING;
                		break;
                	default:
                		docIdType = DocumentIdType.NATIONAL_FILING;
                		break;               		
                }

        		Node docNumN = fragNode.selectSingleNode("doc-number");
        		if (docNumN == null) {
        			continue;
        		}

        		Node countryN = fragNode.selectSingleNode("country");
        		CountryCode countryCode = CountryCode.US;
        		String country = countryN != null ? countryN.getText() : null;
        		try {
        			countryCode = CountryCode.fromString(country);
        		} catch (InvalidDataException e2) {
        			LOGGER.warn("Invalid CountryCode '{}', from : {}", country, fragNode.asXML(), e2);
        		}

        		Node kindN = fragNode.selectSingleNode("kind");
        		String kindCode = kindN != null ? kindN.getText() : null;

        		DocumentId documentId = new DocumentId(countryCode, docNumN.getText(), kindCode);
        		documentId.setType(docIdType);
        		
        		Node dateN = fragNode.selectSingleNode("date");
        		if (dateN != null) {
        			try {
        				documentId.setDate(new DocumentDate(dateN.getText()));
        			} catch (InvalidDataException e) {
        				LOGGER.warn("Failed to parse date from : {}", fragNode.asXML(), e);
        			}
        		}

                priorityDocIds.add(documentId);                
            }
        }
        
        return priorityDocIds;
    }

}
