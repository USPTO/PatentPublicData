package gov.uspto.patent.doc.sgml.items;

import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.parser.dom4j.ItemReader;
import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.DocumentDate;
import gov.uspto.patent.model.DocumentId;

/**
 *<h3>Patent Document Identification</h3>
 *<p> 
 *<li>DNUM PDAT Document number
 *<li>DATE PDAT Document date
 *<li>CTRY PDAT Publishing country or organization (ST.3)
 *<li>KIND PDAT Document kind (ST.16)
 *<li>BNUM PDAT Bulletin number
 *<li>DTXT STEXT Descriptive text
 *</p>
 *<p>
 *<pre>
 *{@code
 *<DOC>
 *	<DNUM><PDAT>12345678</PDAT></DNUM>
 *	<DATE><PDAT>19990212</PDAT></DATE>
 *	<CTRY><PDAT>US</PDAT></CTRY>
 *	<KIND><PDAT>A1</PDAT></KIND>
 *</DOC>
 *}
 *</pre>
 *</p>
 *
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class DocNode extends ItemReader<DocumentId> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocNode.class);

    private static final CountryCode DEFAULT_COUNTRYCODE = CountryCode.US;

    private CountryCode fallbackCountryCode;

    public DocNode(Node itemNode) {
        this(itemNode, DEFAULT_COUNTRYCODE);
    }

    /**
     * Constuctor
     * 
     * @param itemNode
     * @param fallbackCountryCode - CountryCode to use when country is Null or Invalid.
     */
    public DocNode(Node itemNode, CountryCode fallbackCountryCode) {
        super(itemNode);
        this.fallbackCountryCode = fallbackCountryCode != null ? fallbackCountryCode : DEFAULT_COUNTRYCODE;
    }

    @Override
    public DocumentId read() {
        return readDocId(itemNode);
    }

    public DocumentId readDocId(Node docIdNode) {
        Node docNumN = docIdNode.selectSingleNode("DNUM/PDAT");
        if (docNumN == null) {
            LOGGER.warn("Invalid document id can not be Null, from: {}", docIdNode.asXML());
            return null;
        }

        CountryCode countryCode = fallbackCountryCode;
        Node countryCodeN = docIdNode.selectSingleNode("CTRY/PDAT");
        if (countryCodeN != null) {
            String countryCodeStr = countryCodeN.getText();

            try {
                countryCode = CountryCode.fromString(countryCodeStr);
            } catch (InvalidDataException e1) {
                LOGGER.warn("Invalid Country Code: {} from: {}", countryCodeStr, docIdNode.asXML(), e1);
            }
        }

        Node kindCodeN = docIdNode.selectSingleNode("KIND/PDAT");
        String kindCode = kindCodeN != null ? kindCodeN.getText() : null;

        DocumentId docId = new DocumentId(countryCode, docNumN.getText().replaceAll("/", ""), kindCode);

        Node dateN = docIdNode.selectSingleNode("DATE/PDAT");

        if (dateN != null) {
            try {
                DocumentDate docDate = new DocumentDate(dateN.getText());
                docId.setDate(docDate);
            } catch (InvalidDataException e) {
                LOGGER.warn("Unable to parse date: {} from: {}", dateN.getText(), docIdNode.asXML(), e);
            }
        }

        return docId;
    }
}
