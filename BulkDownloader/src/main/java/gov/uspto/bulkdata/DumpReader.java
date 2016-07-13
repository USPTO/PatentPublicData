package gov.uspto.bulkdata;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import gov.uspto.bulkdata.zip.FileFilter;
import gov.uspto.patent.PatentType;

public interface DumpReader extends Iterator<String>, Closeable {
    /**
	 * Get the next document
	 * @return the input stream for the next document
	 */
	InputStream nextDocument();
	
	/**
	 * Whether there are anymore documents in the iterator
	 * @return whether there are anymore documents
	 * in the iterator
	 */
	boolean hasNext();

	PatentType getPatentType();
	
	void open() throws IOException;
	
	void skip(int skipCount) throws IOException;
	
	File getFile();

	void setFileFilter(FileFilter filter);

	int getCurrentRecCount();
	
	String read();
}
