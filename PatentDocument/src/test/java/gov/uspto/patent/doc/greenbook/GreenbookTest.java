package gov.uspto.patent.doc.greenbook;

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

public class GreenbookTest {

    @Test
    public void readSamples() throws PatentReaderException, IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Greenbook greenbook = new Greenbook();
        Path dirPath = Paths.get("resources/samples/greenbook");
        Preconditions.checkArgument(dirPath.toFile().isDirectory(), "greenbook sample dir does not exist.");
        for (File file : dirPath.toFile().listFiles()) {
            Patent patent = greenbook.parse(file);
            ValidatePatent.methodsReturnNonNull(patent);
            //System.out.println(patent.getDescription().getSimpleHtml());
            //System.out.println(patent.getDocumentId().toText() + " - " + patent.getTitle());
        }
    }
}
