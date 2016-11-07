package gov.uspto.patent.thread;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.uspto.patent.PatentDocReader;
import gov.uspto.patent.PatentReaderException;
import gov.uspto.patent.serialize.DocumentBuilder;

public class RecordProcessThread<T> implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(RecordProcessThread.class);

    private PatentDocReader<T> reader;
    private DocumentBuilder<T> docBuilder;
    private Writer writer;
    private Reader rawDocText;

    public RecordProcessThread(Reader rawDocText, PatentDocReader<T> reader, DocumentBuilder<T> docBuilder, Writer writer) {
        this.rawDocText = rawDocText;
        this.reader = reader;
        this.docBuilder = docBuilder;
        this.writer = writer;
    }

    @Override
    public void run() {
        try {
            T obj = reader.read(rawDocText);
            write(obj);
        } catch (PatentReaderException | IOException e) {
            LOGGER.error("Failed to read or write: ", e);
        }
    }

    private synchronized void write(T obj) throws IOException {
        docBuilder.write(obj, writer);
    }
}
