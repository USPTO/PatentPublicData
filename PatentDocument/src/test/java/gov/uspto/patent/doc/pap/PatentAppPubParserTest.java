package gov.uspto.patent.doc.pap;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import com.google.common.base.Preconditions;

import gov.uspto.document.test.ValidatePatent;
import gov.uspto.patent.PatentReaderException;
import gov.uspto.patent.model.Patent;
import gov.uspto.patent.validate.PatentValidationError;
import gov.uspto.patent.validate.PatentValidator;
import junit.framework.AssertionFailedError;

public class PatentAppPubParserTest {

    @Test
    public void readSamples() throws PatentReaderException, IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, PatentValidationError {
        PatentAppPubParser pap = new PatentAppPubParser();
        Path dirPath = Paths.get("resources/samples/pap");
        Preconditions.checkArgument(dirPath.toFile().isDirectory(), "PAP sample dir does not exist.");
        for (File file : dirPath.toFile().listFiles()) {
            Patent patent = pap.parse(file);
            ValidatePatent.methodsReturnNonNull(patent);
            //System.out.println(patent.getDocumentId().toText() + " - " + patent.getTitle());
        }
    }

    @Test
    public void validateSamples() throws PatentReaderException, IOException, PatentValidationError {
        PatentAppPubParser pap = new PatentAppPubParser();
        Path dirPath = Paths.get("resources/samples/pap");
        Preconditions.checkArgument(dirPath.toFile().isDirectory(), "PAP sample dir does not exist.");
        for (File file : dirPath.toFile().listFiles()) {
        	Patent patent = pap.parse(file);
			try {
				PatentValidator.allRules().verify(patent);
			} catch (PatentValidationError e) {
				System.err.println(patent.getDocumentId().toText() + " : " + e.getMessage());
				throw e;
			}
        }
    }

}
