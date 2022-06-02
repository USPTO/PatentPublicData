package gov.uspto.patent.doc.sgml;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import gov.uspto.patent.model.DocumentId;
import gov.uspto.patent.model.DocumentIdType;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Preconditions;

import gov.uspto.document.test.ValidatePatent;
import gov.uspto.patent.PatentReaderException;
import gov.uspto.patent.model.Patent;

public class SgmlTest {

	private Path samplePath = Paths.get("resources/samples/sgml");

	@Before
	public void setUp() throws Exception {
		Preconditions.checkArgument(samplePath.toFile().isDirectory(), "SGML sample dir does not exist.");
	}

	@Test
	public void readSamples() throws PatentReaderException, IOException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		Sgml sgml = new Sgml();
		for (File file : samplePath.toFile().listFiles()) {
			Patent patent = sgml.parse(file);
			ValidatePatent.validateGrant(patent);
			// System.out.println(patent.getDocumentId().toText() + " - " +
			// patent.getTitle());
		}
	}

	@Test
	public void multipleAssignee() throws PatentReaderException, IOException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		Sgml sgml = new Sgml();
		Path filePath = samplePath.resolve("USD435854S1.xml");
		Patent patent = sgml.parse(filePath.toFile());
		assertEquals("Multiple Assignees", 3, patent.getAssignee().size());
	}

	@Test
	public void multipleInventor() throws PatentReaderException, IOException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		Sgml sgml = new Sgml();
		Path filePath = samplePath.resolve("US06337117.xml");
		Patent patent = sgml.parse(filePath.toFile());
		assertEquals("Multiple Inventors", 5, patent.getInventors().size());
	}

	@Test
	public void shouldExtractContinuationRelations() throws PatentReaderException, IOException {
		Sgml sgml = new Sgml();
		Path filePath = samplePath.resolve("US06336130.xml");
		Patent patent = sgml.parse(filePath.toFile());
		List<String> continuationDocNumbers =
				patent.getRelationIds().stream()
						.filter(r -> r.getType() == DocumentIdType.CONTINUATION)
						.map(DocumentId::getDocNumber)
						.collect(Collectors.toList());
		assertEquals("Continuations not extracted", 2, continuationDocNumbers.size());
		assertTrue(
				"Continuation child missing from " + continuationDocNumbers,
				continuationDocNumbers.contains("09413215"));
		assertTrue(
				"Continuation parent missing from " + continuationDocNumbers,
				continuationDocNumbers.contains("PCTNO9800107"));
	}
}
