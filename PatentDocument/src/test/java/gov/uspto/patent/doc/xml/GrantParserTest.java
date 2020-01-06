package gov.uspto.patent.doc.xml;

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

public class GrantParserTest {

    @Test
    public void readSamples2004() throws PatentReaderException, IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        GrantParser xmlGrant = new GrantParser();
        Path dirPath = Paths.get("resources/samples/xml2004");
        Preconditions.checkArgument(dirPath.toFile().isDirectory(), "XML Grant 2004 sample dir does not exist.");
        for (File file : dirPath.toFile().listFiles()) {
            Patent patent = xmlGrant.parse(file);
            ValidatePatent.methodsReturnNonNull(patent);
            //System.out.println(patent.getDocumentId().toText() + " - " + patent.getTitle());
        }
    }
 
    //@Test
    public void readSamples2006() throws PatentReaderException, IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        GrantParser xmlGrant = new GrantParser();
        Path dirPath = Paths.get("resources/samples/xml2006");
        Preconditions.checkArgument(dirPath.toFile().isDirectory(), "XML Grant 2006 sample dir does not exist.");
        for (File file : dirPath.toFile().listFiles()) {
            Patent patent = xmlGrant.parse(file);
            ValidatePatent.methodsReturnNonNull(patent);
            //System.out.println(patent.getDocumentId().toText() + " - " + patent.getTitle());
        }
    }

    @Test
    public void readSamples2014() throws PatentReaderException, IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        GrantParser xmlGrant = new GrantParser();
        Path dirPath = Paths.get("resources/samples/xml2014");
        Preconditions.checkArgument(dirPath.toFile().isDirectory(), "XML Grant 2014 sample dir does not exist.");
        for (File file : dirPath.toFile().listFiles()) {
            Patent patent = xmlGrant.parse(file);
            ValidatePatent.methodsReturnNonNull(patent);
            //System.out.println(patent.getDocumentId().toText() + " - " + patent.getTitle());
        }
    }

}
