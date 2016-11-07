package gov.uspto.patent.doc.sgml;

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

public class SgmlTest {

    @Test
    public void readSamples() throws PatentReaderException, IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Sgml sgml = new Sgml();
        Path dirPath = Paths.get("resources/samples/sgml");
        Preconditions.checkArgument(dirPath.toFile().isDirectory(), "SGML sample dir does not exist.");
        for (File file : dirPath.toFile().listFiles()) {
            Patent patent = sgml.parse(file);
            ValidatePatent.methodsReturnNonNull(patent);
            //System.out.println(patent.getDocumentId().toText() + " - " + patent.getTitle());
        }
    }

}
