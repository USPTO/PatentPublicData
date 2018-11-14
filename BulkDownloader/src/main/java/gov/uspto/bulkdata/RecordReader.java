package gov.uspto.bulkdata;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.NoSuchElementException;

import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.slf4j.MDC;

import gov.uspto.bulkdata.tools.grep.DocumentException;
import gov.uspto.common.filter.FileFilterChain;
import gov.uspto.patent.PatentDocFormat;
import gov.uspto.patent.PatentDocFormatDetect;
import gov.uspto.patent.PatentReader;
import gov.uspto.patent.PatentReaderException;
import gov.uspto.patent.bulk.DumpFileAps;
import gov.uspto.patent.bulk.DumpFileXml;
import gov.uspto.patent.bulk.DumpReader;

public class RecordReader {

	private final BulkReaderArguments bulkReaderArgs;

	public RecordReader(BulkReaderArguments args) {
		this.bulkReaderArgs = args;
	}

	public RunStats read(RecordProcessor processor) throws PatentReaderException, IOException, DocumentException {
		return this.read(bulkReaderArgs.getInputFile().toFile(), processor, bulkReaderArgs.getOutputFile());
	}

	public RunStats read(File inputFile, RecordProcessor processor)
			throws PatentReaderException, DocumentException, IOException {
		this.bulkReaderArgs.setInputFile(inputFile.toPath());
		return this.read(inputFile, processor, null);
	}

	public RunStats read(File inputFile, RecordProcessor processor, Path outputFilePath)
			throws PatentReaderException, DocumentException, IOException {
		FileFilterChain filters = new FileFilterChain();
		DumpReader dumpReader;
		if (bulkReaderArgs.isApsPatent()) {
			dumpReader = new DumpFileAps(inputFile);
			// filter.addRule(new SuffixFileFilter("txt"));
		} else {
			PatentDocFormat patentDocFormat = new PatentDocFormatDetect().fromFileName(inputFile);
			processor.setPatentDocFormat(patentDocFormat);
			switch (patentDocFormat) {
			case Greenbook:
				// aps = true;
				dumpReader = new DumpFileAps(inputFile);
				// filters.addRule(new PathFileFilter(""));
				// filters.addRule(new SuffixFilter("txt"));
				break;
			default:
				// DumpFileXml2 dumpXml = new DumpFileXml2(inputFile);
				DumpFileXml dumpXml = new DumpFileXml(inputFile);
				if (PatentDocFormat.Pap.equals(patentDocFormat) || bulkReaderArgs.addHtmlEntities()) {
					dumpXml.addHTMLEntities();
				}
				dumpReader = dumpXml;
				filters.addRule(new SuffixFileFilter(new String[] { "xml", "sgm", "sgml" }));
			}
			dumpReader.setFileFilter(filters);
		}

		Writer writer = null;
		if (outputFilePath != null) {
			writer = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(outputFilePath.toFile()), Charset.forName("UTF-8")));
		} else {
			writer = new BufferedWriter(new OutputStreamWriter(System.out, Charset.forName("UTF-8")));
		}

		return read(dumpReader, processor, writer);
	}

	public RunStats read(DumpReader dumpReader, RecordProcessor processor, Writer writer)
			throws PatentReaderException, DocumentException, IOException {

		dumpReader.open();
		dumpReader.skip(bulkReaderArgs.getSkipRecordCount());
		
		String currentFileName = dumpReader.getFile().getName();
		RunStats runStats = new RunStats(currentFileName);

		try {
			processor.initialize(writer);
		} catch (Exception e1) {
			throw new PatentReaderException(e1);
		}

		for (int checked = 1; dumpReader.hasNext(); checked++) {
			runStats.incrementRecord();

			String sourceTxt = currentFileName + ":" + dumpReader.getCurrentRecCount();

			MDC.put("DOCID", sourceTxt);

			String rawRecord;
			try {
				rawRecord = dumpReader.next();
			} catch (NoSuchElementException e) {
				break;
			}

			Boolean success = processor.process(sourceTxt, rawRecord, writer);
			if (success) {
				runStats.incrementSucess();
			} else {
				runStats.incrementFailure(sourceTxt);
			}

			if (checked == bulkReaderArgs.getRecordReadLimit() 
					|| runStats.getSuccess() == bulkReaderArgs.getSucessLimit()
					|| runStats.getFailure() == bulkReaderArgs.getFailLimit()) {
				break;
			}
		}

		try {
			processor.finish(writer);
		} catch (Exception e1) {
			throw new PatentReaderException(e1);
		}

		writer.close();
		dumpReader.close();
		
		return runStats;
	}

	public PatentReader getPatentReader() {
		return new PatentReader(getPatentDocFormat());
	}

	public PatentDocFormat getPatentDocFormat() {
		File inputFile = bulkReaderArgs.getInputFile().toFile();
		return new PatentDocFormatDetect().fromFileName(inputFile);
	}
}
