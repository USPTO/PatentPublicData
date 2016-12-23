package gov.uspto.patent.model;

import static org.junit.Assert.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.DocumentId;

public class DocumentIdTest {

    private static Map<String, String> validFromTo = new LinkedHashMap<String, String>();
    static {
        validFromTo.put("US2002/0097953A1", "US97953A1");
        validFromTo.put("US5973683A", "US5973683A");
        validFromTo.put("WO2004/021600A1", "WO21600A1");
        validFromTo.put("DE101 47 913C1", "DE10147913C1");
        validFromTo.put("EP 470 185B1", "EP470185B1");
    }
    
    @Test
    public void supportXPatents() throws InvalidDataException {
        String docIdStr = "USX9876I1";
        DocumentId docId = DocumentId.fromText(docIdStr, 1836);
        assertEquals(docIdStr, docId.getId());
    }

    @Test
    public void testParse() throws InvalidDataException {
        for (Entry<String, String> entry : validFromTo.entrySet()) {
            DocumentId docId = DocumentId.fromText(entry.getKey(), 1990);
            assertEquals(entry.getValue(), docId.getId());
        }
    }

    @Test
    public void testPadding() throws InvalidDataException {
        DocumentId docId = new DocumentId(CountryCode.US, "123456", "A1");
        String actualValue = docId.toText(8);
        String expectValue = "US00123456A1";
        assertEquals(expectValue, actualValue);
    }
}
