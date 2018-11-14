package gov.uspto.bulkdata;

import java.io.IOException;
import java.io.Writer;

import gov.uspto.bulkdata.tools.grep.DocumentException;
import gov.uspto.patent.PatentDocFormat;

public interface RecordProcessor {
	//public void process(String sourceTxt, String rawRecord) throws DocumentException, IOException;
	public Boolean process(String sourceTxt, String rawRecord, Writer writer) throws DocumentException, IOException;

	/**
	 * Executes before processing task starts complete
	 * @throws IOException 
	 */
	public void initialize(Writer writer) throws Exception;

	/**
	 * Executes when processing task is complete
	 * @throws IOException 
	 */
	public void finish(Writer writer) throws IOException;

	/**
	 * Set PatentDocFormat used to initialize the PatentReader
	 * 
	 * @param docFormat
	 */
	void setPatentDocFormat(PatentDocFormat docFormat);
}
