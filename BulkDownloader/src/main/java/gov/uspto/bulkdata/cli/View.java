package gov.uspto.bulkdata.cli;

import java.io.IOException;
import java.io.Writer;

import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Priority;

import gov.uspto.bulkdata.RecordReader;
import gov.uspto.bulkdata.tools.grep.DocumentException;
import gov.uspto.bulkdata.tools.view.ViewConfig;
import gov.uspto.bulkdata.tools.view.ViewRecordProcessor;
import gov.uspto.patent.PatentReader;
import gov.uspto.patent.PatentReaderException;
import gov.uspto.patent.bulk.DumpReader;

/**
 * VIEW is a CLI tool to view patents within Patent Bulk File.
 * 
 * --input="../download/ipg180102.zip"  --skip=2 --limit=4 --type="text"
 *
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class View {

	private ViewConfig config;
	private RecordReader recordReader;
	private PatentReader patentReader;

	public View(ViewConfig config) {
    	this.config = config;
    	this.recordReader = new RecordReader(config);
    	this.patentReader = recordReader.getPatentReader();
    }
 
    public void view() throws XPathExpressionException, PatentReaderException, IOException, DocumentException {
    	recordReader.read(new ViewRecordProcessor(config, patentReader));
    }
    
    public void view(DumpReader dumpReader, Writer writer) throws XPathExpressionException, PatentReaderException, IOException, DocumentException {
    	recordReader.read(dumpReader, new ViewRecordProcessor(config, patentReader), writer);
    }

    public static void main(String[] args) throws PatentReaderException, IOException, DocumentException, XPathExpressionException {
	  /*
	   * Disable Logging except for Errors.
	   */
	  Logger.getRootLogger().getLoggerRepository().resetConfiguration();
	  ConsoleAppender console = new ConsoleAppender();
	  String PATTERN = "%d [%p|%c|%C{1}] %m%n";
	  console.setLayout(new PatternLayout(PATTERN)); 
	  console.setThreshold(Priority.ERROR);
	  console.activateOptions();
	  Logger.getRootLogger().addAppender(console);

	  ViewConfig config = new ViewConfig();
	  config.parseArgs(args);
	  config.buildArgs();
	  config.readOptions();

	  View view = new View(config);
	  view.view();
    }

}
