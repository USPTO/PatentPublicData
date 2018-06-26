package gov.uspto.patent.bulk;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DumpFileXml extends DumpFile {
    private static final Logger LOGGER = LoggerFactory.getLogger(DumpFileXml.class);

    private String xmlStartTag;
    private String xmlEndTag;

    private int currentRecCount;

    private static final String DEFAULT_HEADER = "";
    private String header = "";

    public DumpFileXml(File file) {
        super(file);
    }

    public DumpFileXml(String name, BufferedReader reader) {
        super(name, reader);
    }

    @Override
    public void open() throws IOException {
        super.open();
    }

    @Override
    public String read() {
        String xmlTag = super.getPatentDocFormat().getParentElement();
        this.xmlStartTag = "<" + xmlTag;
        this.xmlEndTag = "</" + xmlTag;

        StringBuilder content = new StringBuilder();
        content.append(header);

        try {
            String line;
            while (super.getReader().ready() && (line = super.getReader().readLine()) != null) {
                if (isStartTag(line)) {
                    content = new StringBuilder();
                    content.append(header);
                } else if (isEndTag(line)) {
                    // Fix for Patent PAP with trailing XML tag.  '</patent-application-publication><?xml version="1.0" encoding="UTF-8"?>'
                    if (line.contains("<?xml")) {
                        String[] parts = line.split("<\\?xml");
                        line = parts[0];
                    }

                    content.append(line).append('\n');
                    currentRecCount++;

                    String docString = content.toString();
                    
                    //LOGGER.trace(docString);
                    
                    return docString;
                }

                content.append(line).append('\n');
            }
        } catch (IOException e) {
            LOGGER.error("Error while reading file: {}; record: {}", super.getFile(), currentRecCount, e);
        }

        return null;
        //throw new NoSuchElementException();
    }

    @Override
    public void skip(int skipCount) throws IOException {
        for (int i = 1; i < skipCount; i++) {
            super.next();
            currentRecCount++;
        }
    }

    @Override
    public int getCurrentRecCount() {
        return currentRecCount;
    }

    /**
     * Add Html Entities DTD to header
     * Fix for Patent PAP document formats (years 2001-2004).
     */
    public void addHTMLEntities() {
    	LOGGER.info("Inserting HTML Entities DTD");
        //header = DEFAULT_HEADER + "\n<!DOCTYPE simple SYSTEM \"html-entities.dtd\">\n";
        header = DEFAULT_HEADER + "<!DOCTYPE simple PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n";
    }

    private boolean isXMLHeader(final String line) {
    	return line.trim().startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    }

    private boolean isStartTag(String line) {
        return line.trim().startsWith(xmlStartTag);
    }

    private boolean isEndTag(String line) {
        return line.trim().startsWith(xmlEndTag);
    }
}
