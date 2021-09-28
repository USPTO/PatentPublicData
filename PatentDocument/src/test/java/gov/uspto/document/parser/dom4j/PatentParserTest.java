package gov.uspto.document.parser.dom4j;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;

import gov.uspto.patent.PatentDocFormat;
import gov.uspto.patent.PatentReader;
import gov.uspto.patent.PatentReaderException;

public class PatentParserTest {

    @Test
    public void parsePatentApplication() throws PatentReaderException, IOException {
        String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><us-patent-application><us-bibliographic-data-application><publication-reference><document-id><country-code>US</country-code><doc-number>1234567</doc-number></document-id></publication-reference><application-reference><document-id><doc-number>7654321</doc-number></document-id></application-reference></us-bibliographic-data-application></us-patent-application>";

        PatentReader patentReader = new PatentReader(PatentDocFormat.RedbookApplication);
        try (StringReader rawText = new StringReader(xmlString)) {
            patentReader.read(rawText);
        }
    }

    @Test
    public void parsePatentGrant() throws PatentReaderException, IOException {
        String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><us-patent-grant><us-bibliographic-data-application><publication-reference><document-id><country-code>US</country-code><doc-number>1234567</doc-number></document-id></publication-reference><application-reference><document-id><doc-number>7654321</doc-number></document-id></application-reference></us-bibliographic-data-application></us-patent-grant>";
        
        PatentReader patentReader = new PatentReader(PatentDocFormat.RedbookGrant);
        try (StringReader rawText = new StringReader(xmlString)) {
            patentReader.read(rawText);
        }        
    }

    @Test
    public void parsePatentSGML() throws PatentReaderException, IOException {
        String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><PATDOC><SDOBI><B100><B110><DNUM><PDAT>1234567</PDAT></DNUM></B110></B100><B200><B210><DNUM><PDAT>7654321</PDAT></DNUM></B210></B200></SDOBI></PATDOC>";

        PatentReader patentReader = new PatentReader(PatentDocFormat.Sgml);
        try (StringReader rawText = new StringReader(xmlString)) {
            patentReader.read(rawText);
        }
    }

    @Test
    public void parsePatentPAP() throws PatentReaderException, IOException {
        String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><patent-application-publication><subdoc-bibliographic-information><document-id><country-code>US</country-code><doc-number>7654321</doc-number></document-id><domestic-filing-data><application-number><doc-number>1234567</doc-number></application-number></domestic-filing-data></subdoc-bibliographic-information></patent-application-publication>";

        PatentReader patentReader = new PatentReader(PatentDocFormat.Pap);
        try (StringReader rawText = new StringReader(xmlString)) {
            patentReader.read(rawText);
        }
    }

    @Test
    public void parsePatentGreenbook() throws PatentReaderException, IOException {
        String xmlString = "HHHHHT        APS1        ISSUE - 960220\nPATN\nWKU  D03671518\nSRC  D\nAPN  0317454\nAPT  4\nART  291\nAPD  19941205\nTTL  Pasta\nISD  19960220\nNCL  1\nECL  1\nEXA  Burgess; Pamela\nEXP  Word; A. Hugo\nNDR  1\nNFG  3\nTRM  14\nINVT\nNAM  Bonucci; Roberto\nCTY  Perugia\nCNT  ITX\nASSG\nNAM  F.LLi De Cecco di Fillippo Fara S. Martino S.p.A.\nCTY  Fara S. Martino\nCNT  ITX\nCOD  03\nPRIR\nCNT  ITX\nAPD  19940609\nAPN  MI9400306\nCLAS\nOCL  D 1106\nXCL  D11 81\nFSC  D 1\nFSS  106;126;127\nFSC  D11\nFSS  44;47;81;97;99\nFSC  D21\nFSS  212\nFSC  426\nFSS  104\nUREF\nPNO  D245239\nISD  19770800\nNAM  Miller\nXCL  D11 87\nUREF\nPNO  D250134\nISD  19781000\nNAM  Baunmgartner\nOCL  D21212\nUREF\nPNO  D324753\nISD  19920300\nNAM  Meyers, Jr. et al.\nOCL  D 1106\nLREP\nFRM  Merchant, Gould, Smite, Edell, Welter & Schmidt\nDRWD\nPAL  FIG. 1 is a top plan view, in an enlarged scale, of the pasta of the\n      present invention, the bottom plan view being a mirror image thereof;\nPAL  FIG. 2 is a longitudinal cross-sectional view thereof, taken along line\n      2--2 in FIG. 1; and,\nPAL  FIG. 3 is a top plan view thereof, at a greatly reduced scale.\nDCLM\nPAL  The ornamental design for pasta, as shown and described.\n";

        PatentReader patentReader = new PatentReader(PatentDocFormat.Greenbook);
        try (StringReader rawText = new StringReader(xmlString)) {
            patentReader.read(rawText);
        }
        // (patent.getDocumentId(), "039305848");
    }
}
