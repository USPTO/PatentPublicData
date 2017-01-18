package gov.uspto.patent.thread;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import gov.uspto.patent.PatentDocReader;
import gov.uspto.patent.PatentReaderException;
import gov.uspto.patent.bulk.DumpFile;
import gov.uspto.patent.serialize.DocumentBuilder;

public class DumpFileProcessThread<T> implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(DumpFileProcessThread.class);

    private DumpFile dumpFile;
    private PatentDocReader<T> reader;
    private DocumentBuilder<T> docBuilder;
    private File outputFile;

    public DumpFileProcessThread(DumpFile dumpFile, PatentDocReader<T> reader, DocumentBuilder<T> docBuilder,
            File outputFile) {
        this.dumpFile = dumpFile;
        this.reader = reader;
        this.docBuilder = docBuilder;
        this.outputFile = outputFile;
    }

    @Override
    public void run() {
        MDC.put("DOCID", dumpFile.getFile().getName());

        try {
            dumpFile.open();
        } catch (IOException e2) {
            LOGGER.error("Error opening dump file: '{}'", dumpFile.getFile(), e2);
        }
        
        int recordNumber = 1;
        int writeCount = 0;
  
        try (Writer writer = new BufferedWriter(new FileWriter(outputFile))) {
            for(; dumpFile.hasNext(); recordNumber++){
            //while (dumpFile.hasNext()) {
                String rawDocText = dumpFile.next();
                //InputStream rawDocText = dumpFile.nextDocument();
                if (rawDocText == null) {
                    break;
                }

                //InputStreamReader rawDocReader  = new InputStreamReader(dumpFile.nextDocument());
                StringReader rawDocReader = new StringReader(rawDocText);

                T obj = null;
                try {
                    obj = reader.read(rawDocReader);
                } catch (PatentReaderException | IOException e) {
                    LOGGER.error("Reader Failed on: {}:{}", dumpFile.getFile().getName(), recordNumber, e);
                }

                if (obj != null) {
                    docBuilder.write(obj, writer);
                    writeCount++;
                }
            }
        } catch (IOException e1) {
            LOGGER.error("DumpFile Failure: '{}:{}'", dumpFile.getFile().getName(), recordNumber, e1);
        } finally {
            try {
                dumpFile.close();
            } catch (IOException e) {
                // close quietly.
            }
        }

        LOGGER.info("Completed {}, records:[{}], written:[{}]", dumpFile.getFile().getName(), recordNumber, writeCount);
    }
}
