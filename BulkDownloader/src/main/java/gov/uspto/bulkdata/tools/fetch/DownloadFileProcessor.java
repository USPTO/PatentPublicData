package gov.uspto.bulkdata.tools.fetch;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import gov.uspto.bulkdata.RecordProcessor;
import gov.uspto.bulkdata.RecordReader;
import gov.uspto.bulkdata.RunStats;
import gov.uspto.bulkdata.tools.grep.DocumentException;
import gov.uspto.patent.PatentReaderException;

public class DownloadFileProcessor {

	private final DownloadConfig config;
	private final RecordProcessor[] recordProcessors;

	public <T extends RecordProcessor> DownloadFileProcessor(DownloadConfig config, T... recordProcessors) {
		this.config = config;
		this.recordProcessors = recordProcessors;
	}

	public void initialize(Writer writer) throws Exception {
		for (RecordProcessor processor : recordProcessors) {
			processor.initialize(writer);
		}
	}

	public RunStats process(File inBulkFile) throws IOException, DocumentException, PatentReaderException {
		RecordReader reader = new RecordReader(config);
		RunStats runStats = new RunStats(inBulkFile.getName());
		for (RecordProcessor processor : recordProcessors) {
			RunStats processStats = reader.read(inBulkFile, processor);
			processStats.setTaskName(processor.getClass().getName());
			runStats.add(processStats);
		}
		return runStats;
	}

	public void finish(Writer writer) throws IOException {
		for (RecordProcessor processor : recordProcessors) {
			processor.finish(writer);
		}
	}
}
