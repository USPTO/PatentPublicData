package gov.uspto.bulkdata.cli;

import java.io.IOException;
import java.io.Writer;

import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import gov.uspto.bulkdata.RecordReader;
import gov.uspto.bulkdata.tools.extractfields.ExtractFieldsConfig;
import gov.uspto.bulkdata.tools.extractfields.ExtractFieldsProcessor;
import gov.uspto.bulkdata.tools.grep.DocumentException;
import gov.uspto.patent.PatentReaderException;
import gov.uspto.patent.bulk.DumpReader;

/**
 * ExtractFields is a CLI tool to extract a set of values, from a list of fields
 * and their XPATH locations from within Bulk Patent XML.
 *
 * <p>
 * Use Cases: 1) Create a CSV file dumping all values within a set of fields
 * 1.1) Example: examiner art unit, cpc classes, cpc search classes
 * </p>
 *
 *<p>
 * <h3>Example to dump all CPC classes within bulk file for a single examiner art-unit</h3> 
 * --input="../download/ipg180102.zip"
 * --match="//examiners/primary-examiner/department[contains(., '1625')]" 
 * -f="art_unit://examiners/primary-examiner/department/." 
 * -f="cpc_main://classifications-cpc/main-cpc/classification-cpc/section|//classifications-cpc/main-cpc/classification-cpc/class|//classifications-cpc/main-cpc/classification-cpc/subclass|//classifications-cpc/main-cpc/classification-cpc/class|//classifications-cpc/main-cpc/classification-cpc/main-group|//classifications-cpc/main-cpc/classification-cpc/class|//classifications-cpc/main-cpc/classification-cpc/subgroup"
 * -f="cpc_further://classifications-cpc/further-cpc/classification-cpc/section|//classifications-cpc/further-cpc/classification-cpc/class|//classifications-cpc/further-cpc/classification-cpc/subclass|//classifications-cpc/further-cpc/classification-cpc/class|//classifications-cpc/further-cpc/classification-cpc/main-group|//classifications-cpc/further-cpc/classification-cpc/class|//classifications-cpc/further-cpc/classification-cpc/subgroup"
 * -f="search_cpc://us-field-of-classification-search/classification-cpc-text"
 *</p>
 *
 *<p>
 *<pre>#art_unit,cpc_main,cpc_further,search_cpc,
 *1625,A|61|K|31|4025,A|61|K|45|06,A61K 31/4025,
 *1625,A|61|K|31|4375,A|61|K|31|15|C|07|D|201|06,C07D 201/06|A61K 31/15|A61K 31/4375,
 *</pre>
 *</p>
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class ExtractFields {

	private ExtractFieldsConfig config;
	private RecordReader recordReader;

	public ExtractFields(ExtractFieldsConfig config) {
		this.config = config;
		this.recordReader = new RecordReader(config);
	}

	public void extract() throws XPathExpressionException, PatentReaderException, IOException, DocumentException {
		recordReader.read(new ExtractFieldsProcessor(config));
	}

	public void extract(DumpReader dumpReader, Writer writer)
			throws XPathExpressionException, PatentReaderException, IOException, DocumentException {
		recordReader.read(dumpReader, new ExtractFieldsProcessor(config), writer);
	}

	public static void main(String[] args)
			throws PatentReaderException, IOException, DocumentException, XPathExpressionException {
		/*
		 * Disable Logging except for Errors.
		 */
		Logger.getRootLogger().getLoggerRepository().resetConfiguration();
		ConsoleAppender console = new ConsoleAppender();
		String PATTERN = "%d [%p|%c|%C{1}] %m%n";
		console.setLayout(new PatternLayout(PATTERN));
		console.setThreshold(Level.ERROR);
		console.activateOptions();
		Logger.getRootLogger().addAppender(console);

		ExtractFieldsConfig config = new ExtractFieldsConfig();
		config.buildArgs();
		config.parseArgs(args);
		config.readOptions();

		ExtractFields processor = new ExtractFields(config);
		processor.extract();
	}

}
