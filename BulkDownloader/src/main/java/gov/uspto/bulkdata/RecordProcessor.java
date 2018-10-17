package gov.uspto.bulkdata;

import java.io.IOException;
import java.io.Writer;

import gov.uspto.bulkdata.grep.DocumentException;

public interface RecordProcessor {
	//public void process(String sourceTxt, String rawRecord) throws DocumentException, IOException;
	public Boolean process(String sourceTxt, String rawRecord, Writer writer) throws DocumentException, IOException;

	/**
	 * Indicates that the processing task is complete
	 * @throws IOException 
	 */
	public void finish(Writer writer) throws IOException;
}
