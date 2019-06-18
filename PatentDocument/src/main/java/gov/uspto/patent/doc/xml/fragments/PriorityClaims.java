package gov.uspto.patent.doc.xml.fragments;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	private static final Logger LOGGER = LoggerFactory.getLogger(PriorityClaims.class);
	
	private static final Pattern SHORT_YEAR = Pattern.compile("^([09])[0-9][/-]\\d+");

    private static final String FRAGMENT_PATH = "//priority-claims/priority-claim";

    private static final String PROVISIONAL_PATH = "/*/*/us-related-documents/us-provisional-application";

    public PriorityClaims(Document document) {
        super(document);
    }

    @Override
    public List<DocumentId> read() {
        List<DocumentId> priorityDocIds = new ArrayList<DocumentId>();

        Node provisionalNode = document.selectSingleNode(PROVISIONAL_PATH);
        if (provisionalNode != null) {
        	DocumentId provisionalDocId = new DocumentIdNode(provisionalNode).read();
        	provisionalDocId.setType(DocumentIdType.PROVISIONAL);
        	priorityDocIds.add(provisionalDocId);
        }

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
        		CountryCode countryCode = CountryCode.UNDEFINED;
        		String country = countryN != null ? countryN.getText() : null;
        		if (country == null || country.trim().isEmpty()) {
        			LOGGER.warn("Invalid CountryCode missing, using default countryCode 'US': {}", fragNode.asXML());
        			countryCode = CountryCode.US;
        		} else {
	        		try {
	        			countryCode = CountryCode.fromString(country);
	        		} catch (InvalidDataException e2) {
	        			LOGGER.warn("{} : {}", e2.getMessage(), fragNode.asXML());
	        		}
        		}

        		String docNumber = docNumN.getText();
        		
        		if (docNumber.substring(0,2).toLowerCase().equals(countryCode.toString().toLowerCase())) {
        			docNumber = docNumber.substring(2).trim();
        			LOGGER.debug("Removed duplicate CountryCode '{}' doc-number: {} => {}", countryCode.toString(), docNumN.getText(), docNumber);
        		}

        		// Seems application number format changed in 2004 from short year to long year.
        		Matcher matcher = SHORT_YEAR.matcher(docNumber);
        		if (matcher.matches()) {
        			if (matcher.group(1).equals("0")) {
        				docNumber = "20" + docNumber;
        				LOGGER.debug("Expanded Short Year, doc-number: {} => {}{}", matcher.group(0), countryCode, docNumber);
        			}
        			else if (matcher.group(1).equals("9")) {
        				docNumber = "19" + docNumber;
        				LOGGER.debug("Expanded Short Year, doc-number: {} => {}{}", matcher.group(0), countryCode, docNumber);
        			}
        		}

        		if (!docNumber.startsWith("PCT/")) {
        			docNumber = docNumber.replace("/", "");
        		}

        		docNumber = docNumber.replaceAll("[\\s-]", "");        		
        		
        		
        		Node kindN = fragNode.selectSingleNode("kind");
        		String kindCode = kindN != null ? kindN.getText() : null;

        		DocumentId documentId = new DocumentId(countryCode, docNumber, kindCode);
        		documentId.setRawText(docNumN.getText());
        		documentId.setType(docIdType);

        		Node dateN = fragNode.selectSingleNode("date");
        		if (dateN != null) {
        			try {
        				documentId.setDate(new DocumentDate(dateN.getText()));
        			} catch (InvalidDataException e) {
        				LOGGER.warn("{} : {}", e.getMessage(), fragNode.asXML());
        			}
        		}

                priorityDocIds.add(documentId);                
            }
        }
        
        return priorityDocIds;
    }

}
