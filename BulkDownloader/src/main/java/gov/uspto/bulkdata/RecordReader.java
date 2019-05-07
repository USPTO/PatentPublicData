package gov.uspto.bulkdata;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.NoSuchElementException;

import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import gov.uspto.bulkdata.tools.grep.DocumentException;
import gov.uspto.common.filter.FileFilterChain;
import gov.uspto.common.io.DummyWriter;
import gov.uspto.patent.PatentDocFormat;
import gov.uspto.patent.PatentDocFormatDetect;
import gov.uspto.patent.PatentReader;
import gov.uspto.patent.PatentReaderException;
import gov.uspto.patent.bulk.DumpFileAps;
import gov.uspto.patent.bulk.DumpFileXml;
import gov.uspto.patent.bulk.DumpReader;

public class RecordReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(RecordReader.class);

	private final BulkReaderArguments bulkReaderArgs;

	public RecordReader(BulkReaderArguments args) {
		this.bulkReaderArgs = args;
	}

	public RunStats read(RecordProcessor processor) throws PatentReaderException, IOException, DocumentException {
		return this.read(bulkReaderArgs.getInputFile().toFile(), processor, bulkReaderArgs.getOutputFile());
	}

	/**
	 * Read, processor does not write (i.e. using Grep for hasMatch only)
	 * 
	 * @param inputFile
	 * @param processor
	 * @return
	 * @throws PatentReaderException
	 * @throws DocumentException
	 * @throws IOException
	 */
	public RunStats read(File inputFile, RecordProcessor processor)
			throws PatentReaderException, DocumentException, IOException {
		this.bulkReaderArgs.setInputFile(inputFile.toPath());
		return this.read(inputFile, processor, new DummyWriter());
	}

	/**
	 * Read, processor writes to STDOUT or FILE.
	 * 
	 * @param inputFile
	 * @param processor
	 * @param outputFilePath
	 * @return
	 * @throws PatentReaderException
	 * @throws DocumentException
	 * @throws IOException
	 */
	public RunStats read(File inputFile, RecordProcessor processor, Path outputFilePath)
			throws PatentReaderException, DocumentException, IOException {

		Writer writer = null;
		if (outputFilePath != null) {
			writer = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(outputFilePath.toFile()), Charset.forName("UTF-16")));
		} else {
			writer = new BufferedWriter(new OutputStreamWriter(System.out, Charset.forName("UTF-16")));
		}

		return read(inputFile, processor, writer);
	}

	public RunStats read(File inputFile, RecordProcessor processor, Writer writer)
			throws PatentReaderException, IOException {

		if (inputFile.isDirectory()) {
			return readDirectory(inputFile, processor, writer);
		}

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

		return read(dumpReader, processor, writer);
	}

	public RunStats readDirectory(File inputDirectory, RecordProcessor processor, Writer writer){

		RunStats runStats = new RunStats("directory:" + inputDirectory.getName());

		DirectoryStream.Filter<Path> filter = new DirectoryStream.Filter<Path>() {
			// regular files modified over 20 seconds ago
			public boolean accept(Path file) throws IOException {
				long twentySecsAgo = System.currentTimeMillis() - 20000;
				long lastModified = file.toFile().lastModified();
				return (file.getFileName().toString().endsWith(".zip") && Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS) && lastModified < twentySecsAgo);
			}
		};

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(inputDirectory.toPath(), filter)) {
			for (Path filePath : stream) {
				String filename = filePath.getFileName().toString();
				MDC.put("DOCID", filename);
				LOGGER.info("--- Reading File: {}", filename);
				try {
					RunStats fileStats = read(filePath.toFile(), processor, writer);
					runStats.add(fileStats);
				} catch (PatentReaderException | IOException e) {
					 LOGGER.error("Failed on Bulk File: {}", filename, e);
				}
				LOGGER.info("--- Done Reading File: {}", filename);
			}
		} catch (IOException e1) {
			runStats.incrementFailure(inputDirectory.toString());
			LOGGER.error("Failed to read directory: {}", inputDirectory, e1);
		}

		return runStats;
	}

	public RunStats read(DumpReader dumpReader, RecordProcessor processor, Writer writer)
			throws PatentReaderException, IOException {

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

			if (dumpReader.getCurrentRecCount() % 1 == 0) {
				LOGGER.info("Records Processed {} : {}", runStats.getTaskName(), dumpReader.getCurrentRecCount());
			}

			String sourceTxt = currentFileName + ":" + dumpReader.getCurrentRecCount();

			MDC.put("DOCID", sourceTxt);

			String rawRecord;
			try {
				rawRecord = dumpReader.next();
			} catch (NoSuchElementException e) {
				break;
			}

			try {
				Boolean success = processor.process(sourceTxt, rawRecord, writer);
				if (success) {
					runStats.incrementSucess();
				} else {
					runStats.incrementFailure(sourceTxt);
				}
			} catch (DocumentException | IOException e) {
				LOGGER.error("Exception occured on {}", sourceTxt, e);
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
