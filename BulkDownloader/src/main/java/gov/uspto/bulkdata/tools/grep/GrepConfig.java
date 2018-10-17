package gov.uspto.bulkdata.tools.grep;

import static java.util.Arrays.asList;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import gov.uspto.bulkdata.BulkReaderArguments;
import gov.uspto.bulkdata.tools.grep.OutputMatchConfig.OUTPUT_MATCHING;

/**
 * Grep Arguments
 * 
 *<code><pre>
 * GrepArguments grepArgs = new GrepArguments();
 * grepArgs.parseArgs(args);
 * grepArgs.readOptions();
 *</pre></code>
 * 
 * @author Brian G. Feldman <brian.feldman@uspto.gov>
 *
 */
public class GrepConfig extends BulkReaderArguments {

	private String XPath;
	private List<RegexArguments> regexList;
	private OutputMatchConfig outputConfig;

	public GrepConfig() {
		buildArgs();
	}

	public void buildArgs() {
		super.buildArgs();

		opParser.accepts("xpath").withOptionalArg().ofType(String.class).describedAs("XPath - XML node to perform match on");

		opParser.acceptsAll( asList( "regex", "regexp", "e" ) )
        	.withOptionalArg()
        	.ofType(String.class)
        	.describedAs("Regex Pattern; flags 'i' for ignore-case and 'f' for full-match; \"'regex1~i','\regex2~if' ");

		opParser.acceptsAll( asList( "not", "invert-match", "v" ) )
	    	.withOptionalArg().ofType(Boolean.class)
	    	.describedAs("Records NOT matching")
	    	.defaultsTo(false);

		opParser.acceptsAll( asList( "c", "count", "only-count" ) )
        	.withOptionalArg().ofType(Boolean.class)
        	.describedAs("Only output count of matching records")
        	.defaultsTo(false);

		opParser.acceptsAll( asList( "only-matching", "o" ) )
        	.withOptionalArg()
        	.ofType(Boolean.class)
        	.describedAs("Print only parts of the content covered by the regex.")
        	.defaultsTo(false);

		opParser.acceptsAll( asList( "max", "max-count", "m" ) )
        	.withOptionalArg()
        	.ofType(Integer.class)
        	.describedAs("Stop reading after total num record matches.")
        	.defaultsTo(-1);

		opParser.acceptsAll( asList( "matching-record-location", "l" ) )
        	.withOptionalArg()
        	.ofType(Boolean.class)
        	.describedAs("Only output record/iteration numbers which match")
        	.defaultsTo(false);

		opParser.acceptsAll( asList( "matching-record", "matching-records") )
	    	.withOptionalArg()
	    	.ofType(Boolean.class)
	    	.describedAs("Only full records which match")
	    	.defaultsTo(false);

		opParser.acceptsAll( asList( "no-source", "no-filename", "no-location", "h" ) )
        	.withOptionalArg()
        	.ofType(Boolean.class)
        	.describedAs("Do not output counts as well as source and location of match; only outputs matches")
        	.defaultsTo(false);
	}
	
	public void readOptions() {
		super.readOptions();

        String xpath = (String) options.valueOf("xpath");
        setXPath(xpath);

        /*
         * Regex Options
         * 
         * "'regex1~i','regex2'
         */
        if (options.has("regex")) {
            String regexStr = (String) options.valueOf("regex");
        	setRegex(RegexArguments.parseString(regexStr));
        }

        int maxCount = (Integer) options.valueOf("max-count");
        setSucessLimit(maxCount);

        OutputMatchConfig outputConfig = null;
		if (options.has("matching-record")) {
			outputConfig = new OutputMatchConfig(OUTPUT_MATCHING.RECORD);
		}
		else if (options.has("only-matching")) {
			outputConfig = new OutputMatchConfig(OUTPUT_MATCHING.PATTERN_COVERED);
		}
		else if (options.has("only-count")) {
			outputConfig = new OutputMatchConfig(OUTPUT_MATCHING.TOTAL_COUNT);
		}
		else if (options.has("matching-record-location")) {
			outputConfig = new OutputMatchConfig(OUTPUT_MATCHING.RECORD_LOCATION);
		} else {
			outputConfig = new OutputMatchConfig(OUTPUT_MATCHING.PATTERN_CONTAINED);
		}

		outputConfig.setNoSource(options.has("no-source"));
		setOutputConfig(outputConfig);
	}

	public void setRegex(List<RegexArguments> regexs) {
		this.regexList = regexs;
	}

	public List<RegexArguments> getRegex() {
		return this.regexList;
	}

	public void setXPath(String xpathExpressionStr) {
		this.XPath = xpathExpressionStr;
	}

	public String getXPath() {
		return this.XPath;
	}

	public OutputMatchConfig getOutputConfig() {
		return outputConfig;
	}

	public void setOutputConfig(OutputMatchConfig outputConfig) {
		this.outputConfig = outputConfig;
	}

	public Match<MatchPattern> getMatcher() throws XPathExpressionException {
		 Set<MatchPattern> patterns = new LinkedHashSet<MatchPattern>();

		 if (XPath != null && regexList != null){
			 RegexArguments regex = regexList.get(0);
			 MatchPatternXPath matchPatternXpath = new MatchPatternXPath(regex.getRegex(), XPath);
			 if (outputConfig.isNoSource()){
				 matchPatternXpath.doNotPrintSource();
			 }
			 if (outputConfig.isInvertMatch()){
				 matchPatternXpath.negate();
			 }
			 patterns.add(matchPatternXpath);
			 return new MatchCheckerXML(patterns);
		 } 
		 else if (regexList != null) {
			 RegexArguments regex = regexList.get(0);
			 MatchRegexBase matchPatternBase = new MatchRegexBase(regex.getRegex(), regex.isIgnoreCase());
			 if (outputConfig.isNoSource()){
				 matchPatternBase.doNotPrintSource();
			 }
			 if (outputConfig.isInvertMatch()){
				 matchPatternBase.negate();
			 }
			 patterns.add(matchPatternBase);
			 return new MatchChecker(patterns);
		 }
		 else if (XPath != null) {
			 MatchXPathExpression matchXPathPattern = new MatchXPathExpression(XPath);
			 if (outputConfig.isNoSource()){
				 matchXPathPattern.doNotPrintSource();
			 }
			 patterns.add(matchXPathPattern);
			 return new MatchCheckerXML(patterns);
		 } 
		 else {
			 System.err.println("Not Pattern Defined, xpath and/or regex");
			 System.exit(-1);
			 return null;
		 }

	}

}