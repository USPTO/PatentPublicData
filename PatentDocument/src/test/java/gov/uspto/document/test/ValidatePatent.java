package gov.uspto.document.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import gov.uspto.patent.model.Patent;
import gov.uspto.patent.validate.AbstractRule;
import gov.uspto.patent.validate.ClassificationRule;
import gov.uspto.patent.validate.DescriptionRule;
import gov.uspto.patent.validate.TitleRule;

public class ValidatePatent {

	private static TitleRule titleTest = new TitleRule();
	private static AbstractRule abstractTest = new AbstractRule();
	private static DescriptionRule descriptionTest = new DescriptionRule();
	private static ClassificationRule classificationTest = new ClassificationRule();

	public static void validateApplication(Patent patent) {
		ValidatePatent.methodsReturnNonNull(patent);

		assertTrue(titleTest.getMessage() + " : " + patent.getDocumentId().toText(), titleTest.test(patent));
		//assertTrue(abstractTest.getMessage() + " : " + patent.getDocumentId().toText(), abstractTest.test(patent));
		assertTrue(descriptionTest.getMessage() + " : " + patent.getDocumentId().toText(), descriptionTest.test(patent));
		assertTrue(classificationTest.getMessage() + " : " + patent.getDocumentId().toText(), classificationTest.test(patent));

		assertTrue("has Inventor", !patent.getInventors().isEmpty());
		//assertTrue("has Assignee", !patent.getAssignee().isEmpty());
		assertTrue("has Agent", !patent.getAgent().isEmpty());

	}

	public static void validateGrant(Patent patent) {
		ValidatePatent.methodsReturnNonNull(patent);

		assertTrue(titleTest.getMessage() + " : " + patent.getDocumentId().toText(), titleTest.test(patent));
		//assertTrue(abstractTest.getMessage() + " : " + patent.getDocumentId().toText(), abstractTest.test(patent));
		assertTrue(descriptionTest.getMessage() + " : " + patent.getDocumentId().toText(), descriptionTest.test(patent));
		assertTrue(classificationTest.getMessage() + " : " + patent.getDocumentId().toText(), classificationTest.test(patent));
		assertTrue("has Inventor", !patent.getInventors().isEmpty());
		//assertTrue("has Assignee", !patent.getAssignee().isEmpty());
		assertTrue("has Agent", !patent.getAgent().isEmpty());

	}

	/**
	 * Test that no getter methods return null
	 * 
	 * @param patent
	 */
	public static void methodsReturnNonNull(Patent patent) {
		for (Method method : Patent.class.getMethods()) {
			if (method.getName().startsWith("get")) {
				try {
					Object ret = method.invoke(patent, null);
					assertNotNull("!! This Patent.class method is returning null: '" + method.getName()
							+ "()' for Patent id: " + patent.getDocumentId().toText(), ret);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
