package gov.uspto.bulkdata.cli;

import java.io.IOException;
import java.io.Writer;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Priority;

import gov.uspto.bulkdata.RecordReader;
import gov.uspto.bulkdata.tools.grep.DocumentException;
import gov.uspto.bulkdata.tools.xslt.XsltConfig;
import gov.uspto.bulkdata.tools.xslt.XsltRecordProcessor;
import gov.uspto.patent.PatentReaderException;
import gov.uspto.patent.bulk.DumpReader;

/**
 * XSLT is a tool to transform Raw patent XML documents within a Patent Bulk File.
 * 
 * --input="../download/ipg180102.zip"  --skip=0 --limit=1 --xslt="example.xslt" --prettyPrint=true
 *
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class Xslt {

	private XsltConfig config;
	private RecordReader recordReader;

	public Xslt(XsltConfig config) {
    	this.config = config;
    	this.recordReader = new RecordReader(config);
    }
 
    public void transform() throws TransformerConfigurationException, PatentReaderException, IOException, DocumentException {
    	recordReader.read(new XsltRecordProcessor(config));
    }

    public void transform(DumpReader dumpReader, Writer writer) throws TransformerConfigurationException, PatentReaderException, IOException, DocumentException  {
    	recordReader.read(dumpReader, new XsltRecordProcessor(config), writer);
    }

    public static void main(String[] args) throws PatentReaderException, IOException, DocumentException, XPathExpressionException, TransformerConfigurationException {
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

	  XsltConfig config = new XsltConfig();
	  config.buildArgs();
	  config.parseArgs(args);
	  config.readOptions();

	  Xslt xslt = new Xslt(config);
	  xslt.transform();
    }

}
