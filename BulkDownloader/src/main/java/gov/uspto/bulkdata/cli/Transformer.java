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
import gov.uspto.bulkdata.tools.transformer.TransformerConfig;
import gov.uspto.bulkdata.tools.transformer.TransformerRecordProcessor;
import gov.uspto.patent.PatentReader;
import gov.uspto.patent.PatentReaderException;
import gov.uspto.patent.bulk.DumpReader;

/**
 * Transformer is a CLI tool to read, normalize and export patents from within Patent Bulk Files.
 *
 *<p>
 * --input="../download/ipg180102.zip"  --skip=0 --limit=0 --type="json" --outDir="./target/output" --bulkKV=true --outputBulkFile=true
 *</p>
 *
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class Transformer {

	private TransformerConfig config;
	private RecordReader recordReader;
	private PatentReader patentReader;

	public Transformer(TransformerConfig config) {
    	this.config = config;
    	this.recordReader = new RecordReader(config);
    	this.patentReader = recordReader.getPatentReader();
    }
 
    public void exec() throws XPathExpressionException, PatentReaderException, IOException, DocumentException {
    	recordReader.read(new TransformerRecordProcessor(config, patentReader));
    }

    public void exec(DumpReader dumpReader, Writer writer) throws XPathExpressionException, PatentReaderException, IOException, DocumentException {
    	recordReader.read(dumpReader, new TransformerRecordProcessor(config, patentReader), writer);
    }

    public static void main(String[] args) throws PatentReaderException, IOException, DocumentException, XPathExpressionException {
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

      TransformerConfig config = new TransformerConfig();
	  config.parseArgs(args);
	  config.readOptions();

	  Transformer transform = new Transformer(config);
	  transform.exec();
    }

}
