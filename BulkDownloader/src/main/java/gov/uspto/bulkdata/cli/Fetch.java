package gov.uspto.bulkdata.cli;

import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Priority;

import gov.uspto.bulkdata.RecordReader;
import gov.uspto.bulkdata.tools.fetch.DownloadConfig;
import gov.uspto.bulkdata.tools.fetch.DownloadTool;
import gov.uspto.bulkdata.tools.grep.DocumentException;
import gov.uspto.bulkdata.tools.grep.GrepConfig;
import gov.uspto.bulkdata.tools.grep.GrepRecordProcessor;
import gov.uspto.bulkdata.tools.transformer.TransformerConfig;
import gov.uspto.bulkdata.tools.transformer.TransformerRecordProcessor;
import gov.uspto.patent.PatentReaderException;
import joptsimple.OptionParser;

/**
 * Fetch/Download Bulk Patent Data
 *
 *<p>
 *Download Patent Bulk files using wanted criteria [month,year,type] to a
 *directory. Optionally, each download can be processed.
 *</p>
 *
 *<h2>Example Usage</h2>
 *
 *<h3>Download</h2>
 *<p>-f="." --fetch-type="grant" --fetch-date="20181101-20181115" --outDir="./target/output"</p>
 *
 *<h3>Sequentially download and transform (Download, and Transform tools)</h3>
 *<p>-f="." --fetch-type="grant" --fetch-date="20181101-20181115" --outDir="./target/output 
 *--type="json" --bulkKV=true --outputBulkFile=true </p>
 *
 *<h3>Sequentially download, match and transform (Download, Grep, and Transform tools)</h3>
 *<p>
 * -f="." --fetch-type="grant" --outDir="./target/output"
 * --fetch-date="20181101-20181115" --type="json" --bulkKV=true
 * --outputBulkFile=true --xpath="//invention-title[starts-with(text(),
 * 'Food')]"
 *</p>
 * 
 * @author Brian G. Feldman <brian.feldman@uspto.gov>
 *
 */
public class Fetch {

	public static void main(String[] args)
			throws PatentReaderException, IOException, DocumentException, XPathExpressionException {
		/*
		 * Disable Logging except for Errors.
		 *
		 */
		Logger.getRootLogger().getLoggerRepository().resetConfiguration();
		ConsoleAppender console = new ConsoleAppender();
		String PATTERN = "%d [%p|%c|%C{1}] %m%n";
		console.setLayout(new PatternLayout(PATTERN));
		console.setThreshold(Priority.ERROR);
		console.activateOptions();
		Logger.getRootLogger().addAppender(console);

		DownloadConfig downloadConfig = new DownloadConfig();
		OptionParser opParser = downloadConfig.buildArgs();

		TransformerConfig transConfig = new TransformerConfig();
		transConfig.buildArgs(opParser);

		GrepConfig grepConfig = new GrepConfig();
		grepConfig.buildArgs(opParser);

		grepConfig.parseArgs(args);
		grepConfig.readOptions();

		transConfig.parseArgs(args);
		transConfig.readOptions();

		downloadConfig.parseArgs(args);
		downloadConfig.readOptions();

		RecordReader recordReader = new RecordReader(downloadConfig);

		TransformerRecordProcessor processor = new TransformerRecordProcessor(transConfig);
		if (grepConfig.getMatcher() != null) {
			processor.setMatchProcessor(new GrepRecordProcessor(grepConfig));
		}

		DownloadTool tool = new DownloadTool(downloadConfig, processor);
		tool.exec();
	}

}
