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
import gov.uspto.bulkdata.tools.grep.GrepConfig;
import gov.uspto.bulkdata.tools.grep.GrepRecordProcessor;
import gov.uspto.patent.PatentReaderException;
import gov.uspto.patent.bulk.DumpReader;

/**
 * GREP is a CLI tool to find/view raw values within Bulk Patent XML.
 *
 * Use Cases:
 * 1) Find patents which match
 * 2) See or dump all raw values within a field
 *
 * Text Regex Search
 * --regex="<br/>"
 * --regex="classification-cpc-text"
 *
 * Only Display Matching Record Count
 * --regex="\D\D09856571" --count
 * 
 * Only Display Matching Record iteration/locations
 * --regex="\D\D09856571" --matching-record-location
 * 
 * Only Display Matching Record
 * --regex="\D\D09856571" --matching-record --max-count=1
 *
 * Parse XML using Xpath and Regex
 * --xpath="//doc-number/text()" --regex="D0806350" --max-count=1
 * 
 * --xpath="//description/p[contains(descendant-or-self::text(),'computer')]"
 * 
 * --xpath="//description/descendant::text()" --regex="computer"
 * --xpath="//p/descendant::text()" --regex="computer"
 * --xpath="//table/descendant::text()" --regex="[Tt]omato"
 * --xpath="//p/descendant::text()|//table/descendant::text()" --regex="[Tt]omato"\
 * --xpath="//classification-cpc-text/text()" --regex="^A21C"
 * --xpath="//invention-title/text()" --regex="Food" --matching-xml
 *
 * Dump all:
 *    Transitional phrases: --xpath="//description//text()" --regex="\b[A-Z][a-z ]{10,35}," --only-matching --no-source
 *    Brace Codes: --regex="\{[A-z ]+ \(.+?\)\}" --only-matching --no-source
 * 	  Company Names: --xpath="//orgname/text()" --no-source
 * 	  Company Names with CountryCode 'DE': --xpath="//addressbook/orgname[../address/country[text()='DE']]/text()" --no-source
 *    Last Names:  --xpath="//last-name/text()" --no-source
 *    First Names: --xpath="//first-name/text()" --no-source
 *    Trademarks: --xpath="//p/descendant::text()|//table/descendant::text()" --regex="\b((?:[a-z]-?)?[A-Z][\w'\/]{1,13}[\d\-_®™\/ ]*){1,5}\W?[®™]{1,2}" --only-matching --no-source
 *    NPL Patent/Application Citations: --xpath="//nplcit/othercit/text()" --regex="\b([Pp]at\.|[P]atent\b|[Aa]pp\.|[Aa]pplication\b|PCT)" --no-source 
 *
 * Full XPath Lookup
 * --xpath="//document-id/*[text() = 'D0806350']" --max-count=1
 * --xpath="//document-id/*[contains(text(),'D0806350')]" --max-count=1
 * --xpath="count(//claim-text/*[contains(text(),' consisting ')]) > 3" --matching-record
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class Grep {

	private GrepConfig config;
	private RecordReader recordReader;

	public Grep(GrepConfig config) {
    	this.config = config;
    	this.recordReader = new RecordReader(config);
    }
 
    public void find() throws XPathExpressionException, PatentReaderException, IOException, DocumentException {
    	recordReader.read(new GrepRecordProcessor(config));
    }
    
    public void find(DumpReader dumpReader, Writer writer) throws XPathExpressionException, PatentReaderException, IOException, DocumentException {
    	recordReader.read(dumpReader, new GrepRecordProcessor(config), writer);
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

	  GrepConfig grepConfig = new GrepConfig();
	  grepConfig.parseArgs(args);
	  grepConfig.readOptions();

	  Grep grep = new Grep(grepConfig);
	  grep.find();
    }

}
