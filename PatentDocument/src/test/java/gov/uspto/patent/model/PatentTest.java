package gov.uspto.patent.model;

import static org.junit.Assert.*;

import java.util.Iterator;

import org.junit.Test;

import gov.uspto.patent.InvalidDataException;

public class PatentTest extends Patent {

	public PatentTest() {
		super(PatentCorpus.USPAT, new DocumentId(CountryCode.US, "1234567", "A1"), PatentType.UTILITY);
	}

	@Test
	public void DocumentIdDateOrdered() throws InvalidDataException {
		DocumentId docId1 = new DocumentId(CountryCode.US, "1234567", "A1");
		docId1.setDate(new DocumentDate("20010101"));
		addOtherId(docId1);

		DocumentId docId2 = new DocumentId(CountryCode.US, "9876543", "A1");
		docId2.setDate(new DocumentDate("19690101"));
		addOtherId(docId2);
		
		DocumentId docId3 = new DocumentId(CountryCode.US, "7654321", "A1");
		docId3.setDate(new DocumentDate("19980101"));
		addOtherId(docId3);
		
		DocumentId docId4 = new DocumentId(CountryCode.US, "6666666", "A1");
		addOtherId(docId4);

		//System.out.println(getOtherIds());
		
		Iterator<DocumentId> docIt = getOtherIds().iterator();
		assertEquals(docId2, docIt.next());
		assertEquals(docId3, docIt.next());
		assertEquals(docId1, docIt.next());
		assertEquals(docId4, docIt.next());
	}
}
