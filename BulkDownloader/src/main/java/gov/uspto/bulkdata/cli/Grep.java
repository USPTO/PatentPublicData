package gov.uspto.bulkdata.cli;

import static java.util.Arrays.asList;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Priority;

import com.google.common.base.Preconditions;

import gov.uspto.bulkdata.grep.DocumentException;
import gov.uspto.bulkdata.grep.Match;
import gov.uspto.bulkdata.grep.MatchChecker;
import gov.uspto.bulkdata.grep.MatchCheckerXML;
import gov.uspto.bulkdata.grep.MatchPattern;
import gov.uspto.bulkdata.grep.MatchPatternXPath;
import gov.uspto.bulkdata.grep.MatchRegexBase;
import gov.uspto.bulkdata.grep.MatchXPathExpression;
import gov.uspto.common.filter.FileFilterChain;
import gov.uspto.common.io.ContentStream;
import gov.uspto.patent.PatentDocFormat;
import gov.uspto.patent.PatentDocFormatDetect;
import gov.uspto.patent.PatentReaderException;
import gov.uspto.patent.bulk.DumpFileAps;
import gov.uspto.patent.bulk.DumpFileXml;
import gov.uspto.patent.bulk.DumpReader;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

/**
 * GREP is a CLI tool to find/view raw values within Bulk Patent XML.
 *
 * Use Cases:
 * 1) Find patents which match
 * 2) See or dump all raw values within a field
 *
 * Text Regex Search
 * --source="../download/ipg180102.zip" --regex="<br/>"
 * --regex="classification-cpc-text"
 *
 * Only Display Matching Record Count
 * --source="../download/ipg180102.zip" --regex="\D\D09856571" --count
 * 
 * Only Display Matching Record iteration/locations
 * --source="../download/ipg180102.zip" --regex="\D\D09856571" --matching-records
 * 
 * Parse XML using Xpath and Regex
 * --source="../download/ipg180102.zip" --xpath="//doc-number/text()" --regex="D0806350" --max-count=1
 * 
 * --xpath="//description/p[contains(descendant-or-self::text(),'computer')]"
 * 
 * --xpath="//description/descendant::/text()" --regex="computer"
 * --xpath="//p/descendant::text()" --regex="computer"
 * --xpath="//table/descendant::text()" --regex="[Tt]omato"
 * --xpath="//p/descendant::text()|//table/descendant::text()" --regex="[Tt]omato"\
 * --xpath="//classification-cpc-text/text()" --regex="^A21C"
 *
 * Find transitional phrases using regex
 * --source="../download/ipg180102.zip" --xpath="//description//text()" --regex="\b[A-Z][a-z ]{10,35}," --only-matching --no-source
 * 
 * Full XPath Lookup
 * --source="../download/ipg180102.zip" --xpath="//document-id/*[text() = 'D0806350']" --max-count=1
 * --xpath="//document-id/*[contains(text(),'D0806350')]" --max-count=1
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class Grep {

    //private ContentStream contentStream;
	private String rawRecord;
    private Match<MatchPattern> matcher;
    private boolean onlyCount = false;
	private boolean onlyRecordNumber;
	private boolean showCount = true;
	private boolean negateMatch = false;
	private String recordSeperator = "\n";

    @SuppressWarnings("unchecked")
	public Grep(Set<MatchPattern> matchPatterns, boolean xmlParse) {
    	 Preconditions.checkNotNull(matchPatterns);
    	 if (xmlParse) {
    		 this.matcher = new MatchCheckerXML(matchPatterns);
    	 }
    	 else {
    		 this.matcher = new MatchChecker(matchPatterns);
    	 }
    }

    /**
     * Silent: Only report total records found.
     * 
     * @return
     */
   public Grep onlyCount() {
	   this.onlyCount = true;
	   return this;
   }
   
   public Grep setShowCount(boolean bool) {
	   this.showCount = bool;
	   return this;
   }
   
	private Grep negate() {
	   this.negateMatch = true;
	   this.onlyRecordNumber = true;
	   this.showCount = false;
	   return this;
	}

   public Grep onlyRecordNumber() {
	   this.onlyRecordNumber = true;
	   this.showCount = false;
	   return this;
   }

   public Grep zeroRecordSeperator() {
	   this.recordSeperator = "\0";
	   return this;
   }

	public boolean hasMatch(String sourceTxt, Reader reader, Writer writer) throws DocumentException, IOException {
		boolean matched;

		if (onlyCount || onlyRecordNumber) {
			matched = matcher.match(reader);
		}
		else {
			matched = matcher.match(sourceTxt, reader, writer, false);
		}

		return negateMatch ? !matched : matched;
	}

    public void find(DumpReader dumpReader, int matchLimit, Writer writer)
            throws PatentReaderException, IOException, DocumentException {

    	int matchCount = 0;
    	int checked = 1;
    	
        for (; dumpReader.hasNext(); checked++) {
            try {
            	rawRecord = dumpReader.next();
            } catch (NoSuchElementException e) {
                break;
            }

            Reader reader = new InputStreamReader(new ByteArrayInputStream(rawRecord.getBytes()), "UTF-8");
            String sourceTxt = dumpReader.getFile().getName() + ":" + dumpReader.getCurrentRecCount();

           	if (hasMatch(sourceTxt, reader, writer)) {
        		matchCount++;
        		if (onlyRecordNumber) {
        			writer.write(sourceTxt);
        			writer.write(recordSeperator);
        			writer.flush();
        		}
            	if (matchCount == matchLimit) {
            		break;
            	}
           	} else if (negateMatch){
        		writer.write(String.valueOf(checked));
        		writer.write(recordSeperator);
        		writer.flush();
        		
        		matchCount++;
            	if (matchCount == matchLimit) {
            		break;
            	}
            }
        }

        if (showCount) {
        	writer.write("\nTotal records matched: [" + matchCount + " of " + checked + "]\n");
        }

        writer.close();
        dumpReader.close();
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

        OptionParser parser = new OptionParser() {
            {
            	
            	acceptsAll( asList( "source", "f", "file" ) )
            		.withRequiredArg()
            		.ofType(String.class)
            		.describedAs("zip file").required();
            	
                accepts("skip").withOptionalArg().ofType(Integer.class).describedAs("records to skip").defaultsTo(0);
                accepts("xmlBodyTag").withOptionalArg().ofType(String.class)
                        .describedAs("XML Body Tag which wrapps document: [us-patent, PATDOC, patent-application]")
                        .defaultsTo("us-patent");
                accepts("addHtmlEntities").withOptionalArg().ofType(Boolean.class)
                        .describedAs("Add Html Entities DTD to XML; Needed when reading Patents in PAP format.")
                        .defaultsTo(false);
                accepts("aps").withOptionalArg().ofType(Boolean.class)
                        .describedAs("Read APS - Greenbook Patent Document Format").defaultsTo(false);
                
                accepts("xpath").withOptionalArg().ofType(String.class).describedAs("XPath - XML node to perform match on");

                acceptsAll( asList( "regex", "regexp", "e" ) )
                	.withOptionalArg()
                	.ofType(String.class)
                	.describedAs("Regex Pattern");

                acceptsAll( asList( "c", "count" ) )
                	.withOptionalArg().ofType(Boolean.class)
                	.describedAs("Only output count of matching records")
                	.defaultsTo(false);

                acceptsAll( asList( "not", "invert-match", "v" ) )
                	.withOptionalArg().ofType(Boolean.class)
                	.describedAs("Records NOT matching")
                	.defaultsTo(false);

                /*
                 * Current default is partial/internal matches.
                acceptsAll( asList( "partial", "word-regex", "word-regexp", "w" ) )
	            	.withOptionalArg()
	            	.ofType(Boolean.class)
	            	.describedAs("Regex matches anywhere within value or content")
	            	.defaultsTo(true);
				*/

                acceptsAll( asList( "entire", "line-regexp", "line-regex", "x" ) )
                	.withOptionalArg()
                	.ofType(Boolean.class)
                	.describedAs("Regex only matches against entire/full value or content.")
                	.defaultsTo(false);

                acceptsAll( asList( "only-matching", "o" ) )
	            	.withOptionalArg()
	            	.ofType(Boolean.class)
	            	.describedAs("Print only parts of the content covered by the regex.")
	            	.defaultsTo(false);

                acceptsAll( asList( "max", "max-count", "m" ) )
                	.withOptionalArg()
                	.ofType(Integer.class)
                	.describedAs("Stop reading after total num matches.")
                	.defaultsTo(-1);

                acceptsAll( asList( "matching-records", "records-with-matches", "files-with-matches", "l" ) )
                	.withOptionalArg()
                	.ofType(Boolean.class)
                	.describedAs("Only output record/iteration numbers which match")
                	.defaultsTo(false);

                acceptsAll( asList( "non-matching-records", "records-without-matches", "files-without-match") )
                	.withOptionalArg()
                	.ofType(Boolean.class)
                	.describedAs("Only output record/iteration numbers which do not match")
                	.defaultsTo(false);

                acceptsAll( asList( "no-source", "no-filename", "no-location", "h" ) )
	            	.withOptionalArg()
	            	.ofType(Boolean.class)
	            	.describedAs("Do not output counts as well as source and location of match; only outputs matches")
	            	.defaultsTo(false);

                acceptsAll( asList( "null", "Z", "0", "print0") )
	            	.withOptionalArg()
	            	.ofType(Boolean.class)
	            	.describedAs("Output a zero byte (the ASCII NUL character) instead of newline following record number/iteration.")
	            	.defaultsTo(false);
            }
        };

        OptionSet options = parser.parse(args);
        String inFileStr = (String) options.valueOf("source");
        File inputFile = new File(inFileStr);

        int skip = (Integer) options.valueOf("skip");
        //String xmlBodyTag = (String) options.valueOf("xmlBodyTag");
        boolean addHtmlEntities = (Boolean) options.valueOf("addHtmlEntities");
        boolean aps = (Boolean) options.valueOf("aps");

        int limit = (Integer) options.valueOf("max-count");

        Set<MatchPattern> patterns = new LinkedHashSet<MatchPattern>();
        
        MatchPattern matchPattern = null;
        if (options.has("xpath") && options.has("regex")) {
	        String regex = (String) options.valueOf("regex");
	        String xpath = (String) options.valueOf("xpath");
	        MatchPatternXPath matchPatternXpath = new MatchPatternXPath(regex, xpath);
	    	if (options.has("entire")) {
	    		matchPatternXpath.entire();
	    	}

	    	matchPattern = matchPatternXpath;
        }
        else if (options.has("regex")) {
        	String regex = (String) options.valueOf("regex");
        	MatchRegexBase matchPatternBase = new MatchRegexBase(regex);
	    	if (options.has("entire")) {
	    		matchPatternBase.entire();
	    	}

	    	matchPattern = matchPatternBase;
        }
        else if (options.has("xpath")) {
        	String xpath = (String) options.valueOf("xpath");
        	matchPattern = new MatchXPathExpression(xpath);
        } else {
        	System.err.println("Not Pattern Defined, xpath and/or regex");
        	System.exit(-1);
        }

    	if (matchPattern != null && options.has("invert-match")) {
    		matchPattern.negate();
    	}

    	if (matchPattern != null && options.has("no-source")) {
    		matchPattern.doNotPrintSource();
    	}
    	
    	if (matchPattern != null && options.has("only-matching")) {
    		matchPattern.onlyMatching();
    	}
    	
    	patterns.add(matchPattern);

    	Grep grep = new Grep(patterns, options.has("xpath"));
        
	    if (options.has("count")) {
	    	grep.onlyCount();
	    }
	    
	    if (options.has("zero")) {
	    	grep.zeroRecordSeperator();
	    }

	    if (options.has("no-source")) {
	    	grep.setShowCount(false);
	    }
	
	    if (options.has("records-with-matches")) {
	    	grep.onlyRecordNumber();
	    }

	    if (options.has("records-without-matches")) {
	    	grep.negate();
	    }
	    
        FileFilterChain filters = new FileFilterChain();

        DumpReader dumpReader;
        if (aps) {
            dumpReader = new DumpFileAps(inputFile);
            //filter.addRule(new SuffixFileFilter("txt"));
        } else {
            PatentDocFormat patentDocFormat = new PatentDocFormatDetect().fromFileName(inputFile);
            switch (patentDocFormat) {
            case Greenbook:
                aps = true;
                dumpReader = new DumpFileAps(inputFile);
                //filters.addRule(new PathFileFilter(""));
                //filters.addRule(new SuffixFilter("txt"));
                break;
            default:
                //DumpFileXml2 dumpXml = new DumpFileXml2(inputFile);
                DumpFileXml dumpXml = new DumpFileXml(inputFile);
    			if (PatentDocFormat.Pap.equals(patentDocFormat) || addHtmlEntities) {
    				dumpXml.addHTMLEntities();
    			}
                dumpReader = dumpXml;
                filters.addRule(new SuffixFileFilter(new String[] {"xml", "sgm", "sgml"}));
            }
        }

        dumpReader.setFileFilter(filters);

        dumpReader.open();
        dumpReader.skip(skip);

        Writer writer = null;
        if (options.has("out")) {
            String outStr = (String) options.valueOf("out");
            Path outFilePath = Paths.get(outStr);
            writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(outFilePath.toFile()), Charset.forName("UTF-8")));
        } else {
            writer = new BufferedWriter(new OutputStreamWriter(System.out, Charset.forName("UTF-8")));
        }

        try {
           	grep.find(dumpReader, limit, writer);
         } finally {
            dumpReader.close();
            writer.close();
        }

        System.out.println("--- Finished ---");
    }

}
