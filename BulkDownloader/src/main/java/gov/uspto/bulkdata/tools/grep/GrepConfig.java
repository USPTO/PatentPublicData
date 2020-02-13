package gov.uspto.bulkdata.tools.grep;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import com.google.common.base.Preconditions;

import gov.uspto.bulkdata.BulkReaderArguments;
import gov.uspto.bulkdata.tools.grep.OutputMatchConfig.OUTPUT_MATCHING;
import joptsimple.OptionParser;

/**
 * Grep Arguments
 * 
 * <code><pre>
 * GrepArguments grepArgs = new GrepArguments();
 * grepArgs.buildArgs();
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
	private Set<String> values;
	private Path containsFile;
	private Path outputFile;
	private Path outputDir;

	public OptionParser buildArgs() {
		return buildArgs(new OptionParser());
	}

	public OptionParser buildArgs(OptionParser opParser) {
		opParser = super.buildArgs(opParser);

		//opParser.accepts("outputDir").withOptionalArg().ofType(String.class)
		//.describedAs("Directory to write output files");

		opParser.accepts("xpath").withOptionalArg().ofType(String.class)
				.describedAs("XPath - XML node to perform match on");

		opParser.accepts("contains").withOptionalArg().ofType(String.class)
		.describedAs("list of values, separated by pipe '|' ");

		opParser.accepts("containsFromFile").withOptionalArg().ofType(String.class)
				.describedAs("File containing list of values, single value per line");

		opParser.acceptsAll(asList("regex", "regexp", "e")).withOptionalArg().ofType(String.class)
				.describedAs("Regex Pattern; multiple patterns are allowed with multiple occurance, flags 'i' for ignore-case and 'f' for full-match; \"'regex1~i','\regex2~if' ");

		opParser.accepts("regexs").withOptionalArg().ofType(String.class).describedAs(
				"Regex Patterns; multiple patterns seperated with comma; flags 'i' for ignore-case and 'f' for full-match; \"'regex1~i','\regex2~if' ");

		opParser.acceptsAll(asList("not", "invert-match", "v")).withOptionalArg().ofType(Boolean.class)
				.describedAs("Records NOT matching").defaultsTo(false);

		opParser.acceptsAll(asList("c", "count", "only-count")).withOptionalArg().ofType(Boolean.class)
				.describedAs("Only output count of matching records").defaultsTo(false);

		opParser.acceptsAll(asList("only-matching", "o")).withOptionalArg().ofType(Boolean.class)
				.describedAs("Print only parts of the content covered by the regex.").defaultsTo(false);

		opParser.acceptsAll(asList("max", "max-count", "m")).withOptionalArg().ofType(Integer.class)
				.describedAs("Stop reading after total num record matches.").defaultsTo(-1);

		opParser.acceptsAll(asList("matching-record-location", "l")).withOptionalArg().ofType(Boolean.class)
				.describedAs("Only output record/iteration numbers which match").defaultsTo(false);

		opParser.acceptsAll(asList("matching-record", "matching-records")).withOptionalArg().ofType(Boolean.class)
				.describedAs("Only full records which match").defaultsTo(false);

		opParser.acceptsAll(asList("matching-node-xml", "node-xml", "matching-xml")).withOptionalArg()
				.ofType(Boolean.class).describedAs("Print matching node's xml").defaultsTo(false);

		opParser.acceptsAll(asList("nosource", "no-source", "no-filename", "no-location", "h")).withOptionalArg()
				.ofType(Boolean.class)
				.describedAs("Do not output counts as well as source and location of match; only outputs matches")
				.defaultsTo(false);

		return opParser;
	}

	public void readOptions() {
		super.readOptions();

		String xpath = (String) options.valueOf("xpath");
		setXPath(xpath);

		if (options.has("contains")) {
			String valueStr = (String) options.valueOf("contains");
			String[] values = valueStr.split("\\|");
			Set<String> valueSet = new HashSet<String>(Arrays.asList(values));
			setValues(valueSet);
		} 
		else if (options.has("containsFromFile")){
			String filePathStr = (String) options.valueOf("containsFromFile");
			Path filePath = Paths.get(filePathStr);
			Preconditions.checkArgument(filePath.toAbsolutePath().toFile().canRead(),
					"Unable to read contains file: " + filePath.toAbsolutePath());
			try {
				List<String> lines = Files.readAllLines(filePath, Charset.defaultCharset());
				System.out.println("Loaded contains lst file, size: " + lines.size());
				Set<String> valueSet = new HashSet<String>(lines);
				System.out.println("Loaded contains lst file, unique size: " + valueSet.size());
				setValues(valueSet);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/* FIXME
		if (options.has("outputDir")) {
			String filePathStr = (String) options.valueOf("outputDir");
			Path filePath = Paths.get(filePathStr);
			setOutputFile(filePath);
		}
		*/

		/*
		 * Regex Options
		 * 
		 * --regexs="'regex1~i','regex2'"
		 */
		if (options.has("regexs")) {
			String regexStr = (String) options.valueOf("regex");
			setRegex(RegexArguments.parseString(regexStr));
		}
		if (options.has("regex")) {
			@SuppressWarnings("unchecked")
			List<String> regexs = (List<String>) options.valuesOf("regex");
			setRegex(RegexArguments.parseString(regexs));
		}

		if (options.has("max-count")) {
			int maxCount = (Integer) options.valueOf("max-count");
			setSucessLimit(maxCount);
		}

		OutputMatchConfig outputConfig = null;
		if (options.has("matching-record")) {
			outputConfig = new OutputMatchConfig(OUTPUT_MATCHING.RECORD);
		} else if (options.has("only-matching")) {
			outputConfig = new OutputMatchConfig(OUTPUT_MATCHING.PATTERN_COVERED);
		} else if (options.has("only-count")) {
			outputConfig = new OutputMatchConfig(OUTPUT_MATCHING.TOTAL_COUNT);
		} else if (options.has("matching-record-location")) {
			outputConfig = new OutputMatchConfig(OUTPUT_MATCHING.RECORD_LOCATION);
		} else if (options.has("matching-node-xml")) {
			outputConfig = new OutputMatchConfig(OUTPUT_MATCHING.NODE_XML);
		} else {
			outputConfig = new OutputMatchConfig(OUTPUT_MATCHING.PATTERN_CONTAINED);
		}

		outputConfig.setNoSource(options.has("no-source"));
		setOutputConfig(outputConfig);
	}

	public void setOutputDir(Path path) {
		Preconditions.checkArgument(path.toAbsolutePath().toFile().isDirectory() && path.toAbsolutePath().toFile().canWrite(),
				"Output Directory is not a writable directory: " + path.toAbsolutePath());
		this.outputDir = path;
	}

	public Path getOutputDir() {
		return outputDir;
	}

	public void setValues(Set<String> values) {
		this.values = values;
	}

	public Set<String> getValues() {
		return values;
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

		if (XPath != null && regexList != null) {
			RegexArguments regex = regexList.get(0);
			MatchPatternXPath matchPatternXpath = new MatchPatternXPath(regex.getRegex(), XPath);
			if (outputConfig.getOutputType() == OUTPUT_MATCHING.PATTERN_COVERED) {
				matchPatternXpath.onlyMatching();
			}
			if (outputConfig.getOutputType() == OUTPUT_MATCHING.NODE_XML) {
				matchPatternXpath.onlyMatchingNode();
			}
			if (outputConfig.isNoSource()) {
				matchPatternXpath.doNotPrintSource();
			}
			if (outputConfig.isInvertMatch()) {
				matchPatternXpath.negate();
			}
			patterns.add(matchPatternXpath);
			return new MatchCheckerXML(patterns);
		} else if (XPath != null && values != null) {
			MatchXPathNodeValues matchPatternXV = new MatchXPathNodeValues(XPath, values);
			if (outputConfig.isNoSource()) {
				matchPatternXV.doNotPrintSource();
			}
			if (outputConfig.getOutputType() == OUTPUT_MATCHING.NODE_XML) {
				matchPatternXV.onlyMatchingNode();
			}
			patterns.add(matchPatternXV);
			return new MatchCheckerXML(patterns);
		} else if (regexList != null) {
			RegexArguments regex = regexList.get(0);
			MatchRegexBase matchPatternBase = new MatchRegexBase(regex.getRegex(), regex.isIgnoreCase());
			if (outputConfig.getOutputType() == OUTPUT_MATCHING.PATTERN_COVERED) {
				matchPatternBase.onlyMatching();
			}
			if (outputConfig.isNoSource()) {
				matchPatternBase.doNotPrintSource();
			}
			if (outputConfig.isInvertMatch()) {
				matchPatternBase.negate();
			}
			patterns.add(matchPatternBase);
			return new MatchChecker(patterns);
		} else if (XPath != null) {
			MatchXPathExpression matchXPathPattern = new MatchXPathExpression(XPath);
			if (outputConfig.isNoSource()) {
				matchXPathPattern.doNotPrintSource();
			}
			if (outputConfig.getOutputType() == OUTPUT_MATCHING.NODE_XML) {
				matchXPathPattern.onlyMatchingNode();
			}
			patterns.add(matchXPathPattern);
			return new MatchCheckerXML(patterns);
		} else {
			// System.err.println("Not Pattern Defined, xpath and/or regex");
			// System.exit(-1);
			return null;
		}

	}

}