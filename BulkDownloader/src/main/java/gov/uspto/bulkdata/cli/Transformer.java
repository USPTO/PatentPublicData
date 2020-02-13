package gov.uspto.bulkdata.cli;

import java.io.IOException;
import java.io.Writer;

import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import gov.uspto.bulkdata.RecordReader;
import gov.uspto.bulkdata.tools.grep.DocumentException;
import gov.uspto.bulkdata.tools.grep.GrepConfig;
import gov.uspto.bulkdata.tools.grep.GrepRecordProcessor;
import gov.uspto.bulkdata.tools.transformer.TransformerConfig;
import gov.uspto.bulkdata.tools.transformer.TransformerRecordProcessor;
import gov.uspto.patent.PatentReader;
import gov.uspto.patent.PatentReaderException;
import gov.uspto.patent.bulk.DumpReader;
import joptsimple.OptionParser;

/**
 * Transformer is a CLI tool to read, normalize and export patents from within Patent Bulk Files.
 *
 *<p>
 * --input="../download/ipg180102.zip"  --skip=0 --limit=0 --type="json" --outDir="./target/output" --bulkKV=true --outputBulkFile=true
 *</p>
 *
 *<h3>Pre-match Documents before transforming</h3>
 *<p>Uses Grep Tool to match documents</p>
 *<p><pre>
 * --xpath="//invention-title/text()" --regex="Food"
 * --xpath="//invention-title[contains(text(), 'Food')]"
 *
 * --xpath="//classification-cpc[./section/text() = 'A' and ./class/text() = '23' and ./subclass/text() = 'B' and ./main-group/text() = '5' and ./subgroup/text()='045']/."
 *
 * --xpath="//classification-cpc-text/text()" --regex="^A21C"
 * --xpath="//classification-cpc-text[starts-with(text(), 'A21C')]"
 * --xpath="(//classification-cpc-text[starts-with(text(), 'A21C')]|//classification-cpc[./section/text() = 'A' and ./class/text() = '21' and ./subclass/text() = 'C'])/."
 *
 *</pre></p>
 *
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class Transformer {

	private final TransformerConfig config;
	private final GrepConfig grepConfig;
	private final boolean prematch;
	private RecordReader recordReader;
	private PatentReader patentReader;

	public Transformer(TransformerConfig config) throws XPathExpressionException {
		this(config, null);
    }

	public Transformer(TransformerConfig config, GrepConfig grepConfig) throws XPathExpressionException {
    	this.config = config;
    	this.recordReader = new RecordReader(config);
    	this.patentReader = recordReader.getPatentReader();
    	this.grepConfig = grepConfig;
    	if (grepConfig == null || grepConfig.getMatcher() == null) {
    		prematch = false;
    	} else {
    		prematch = true;
    	}
    }

	public void exec() throws XPathExpressionException, PatentReaderException, IOException, DocumentException {
		TransformerRecordProcessor processor = new TransformerRecordProcessor(config);
		if (prematch) {
			processor.setMatchProcessor(new GrepRecordProcessor(grepConfig));
		}
    	recordReader.read(processor);
    }

    public void exec(DumpReader dumpReader, Writer writer) throws XPathExpressionException, PatentReaderException, IOException, DocumentException {
		TransformerRecordProcessor processor = new TransformerRecordProcessor(config);
		if (prematch) {
			processor.setMatchProcessor(new GrepRecordProcessor(grepConfig));
		}
    	recordReader.read(dumpReader, processor, writer);
    }

    public static void main(String[] args) throws PatentReaderException, IOException, DocumentException, XPathExpressionException {
	  /*
	   * Disable Logging except for Errors.
	   *
	   *
	  Logger.getRootLogger().getLoggerRepository().resetConfiguration();
	  ConsoleAppender console = new ConsoleAppender();
	  String PATTERN = "%d [%p|%c|%C{1}] %m%n";
	  console.setLayout(new PatternLayout(PATTERN)); 
	  console.setThreshold(Level.ERROR);
	  console.activateOptions();
	  Logger.getRootLogger().addAppender(console);
	  */

      TransformerConfig config = new TransformerConfig();
      OptionParser opParser = config.buildArgs();

      GrepConfig grepConfig = new GrepConfig();
      grepConfig.buildArgs(opParser);

	  config.parseArgs(args);
	  config.readOptions();

	  grepConfig.parseArgs(args);
	  grepConfig.readOptions();

	  Transformer transform = new Transformer(config, grepConfig);
	  transform.exec();
    }

}
