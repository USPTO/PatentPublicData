package gov.uspto.bulkdata;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.common.base.Preconditions;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class BulkReaderArguments {

	protected OptionParser opParser = null;
	protected OptionSet options;
	private Path inputFile;
	private Path outputFile;
	private int skipRecordCount = 0;
	private int recordReadLimit = -1;
	private int sucessLimit = -1;
	private int failLimit = -1;
	private boolean htmlEntities = false;
	private boolean apsPatent;

	public OptionParser buildArgs() {
		return buildArgs(new OptionParser(true));
	}

	public OptionParser buildArgs(OptionParser opParser) {
		if (opParser == null) {
			opParser = new OptionParser(true);
		}
		this.opParser = opParser;

		opParser.acceptsAll(asList("input", "f", "file")).withRequiredArg().ofType(String.class)
				.describedAs("zip file, individual file or directory").required();

		opParser.accepts("skip").withOptionalArg().ofType(Integer.class).describedAs("records to skip").defaultsTo(0);
		opParser.accepts("limit").withOptionalArg().ofType(Integer.class).describedAs("record limit").defaultsTo(-1);

		opParser.accepts("xmlBodyTag").withOptionalArg().ofType(String.class)
				.describedAs("XML Body Tag which wrapps document: [us-patent, PATDOC, patent-application]")
				.defaultsTo("us-patent");

		opParser.accepts("addHtmlEntities").withOptionalArg().ofType(Boolean.class)
				.describedAs("Add Html Entities DTD to XML; Needed when reading Patents in PAP format.")
				.defaultsTo(false);

		opParser.accepts("aps").withOptionalArg().ofType(Boolean.class)
				.describedAs("Read APS - Greenbook Patent Document Format").defaultsTo(false);

		opParser.acceptsAll(asList("out", "output", "outfile")).withOptionalArg().ofType(String.class)
				.describedAs("out file");

		opParser.acceptsAll(asList("help", "?")).withOptionalArg().ofType(Boolean.class).describedAs("Print Help")
				.defaultsTo(false);

		return opParser;
	}

	public void parseArgs(String... args) {
		if (args.length < 1) {
			printHelp();
		}

		try {
			options = opParser.parse(args);
		} catch (Exception e) { // joptsimple.UnrecognizedOptionException is not visible.
			System.err.println(e.getMessage());
			printHelp();
		}

		if (options.has("help")) {
			printHelp();
		}
	}

	public void printHelp() {
		try {
			opParser.printHelpOn(System.out);
			System.exit(1);
		} catch (IOException e) {
			// ignore
			// e.printStackTrace();
		}
	}

	public void readOptions() {
		String inFileStr = (String) options.valueOf("input");
		setInputFile(Paths.get(inFileStr));

		if (options.has("output")) {
			String outFileStr = (String) options.valueOf("output");
			setOutputFile(Paths.get(outFileStr));
		}

		if (options.has("skip")) {
			int skip = (Integer) options.valueOf("skip");
			setSkipRecordCount(skip);
		}

		if (options.has("limit")) {
			int limit = (Integer) options.valueOf("limit");
			setRecordReadLimit(limit);
		}

		boolean addHtmlEntities = (Boolean) options.valueOf("addHtmlEntities");
		setAddHtmlEntities(addHtmlEntities);

		boolean aps = (Boolean) options.valueOf("aps");
		setApsPatent(aps);
	}

	public void setOutputFile(Path filePath) {
		Preconditions.checkArgument(filePath.toAbsolutePath().getParent().toFile().canWrite(),
				"Unable to create file, directory is not writable: " + filePath.toAbsolutePath());
		this.outputFile = filePath;
	}

	public Path getOutputFile() {
		return this.outputFile;
	}

	public void setInputFile(Path filePath) {
		Preconditions.checkArgument(filePath.toFile().canWrite(), "Unable to write to file: " + filePath);
		this.inputFile = filePath;
	}

	public Path getInputFile() {
		return this.inputFile;
	}

	public void setSkipRecordCount(int skipRecordCount) {
		this.skipRecordCount = skipRecordCount;
	}

	public int getSkipRecordCount() {
		return this.skipRecordCount;
	}

	public void setRecordReadLimit(int recordReadLimit) {
		this.recordReadLimit = recordReadLimit;
	}

	public int getRecordReadLimit() {
		return this.recordReadLimit;
	}

	public void setAddHtmlEntities(boolean addHtmlEntities) {
		this.htmlEntities = addHtmlEntities;
	}

	public Boolean addHtmlEntities() {
		return htmlEntities;
	}

	public void setApsPatent(boolean isAps) {
		this.apsPatent = isAps;
	}

	public Boolean isApsPatent() {
		return this.apsPatent;
	}

	public int getSucessLimit() {
		return sucessLimit;
	}

	public void setSucessLimit(int sucessLimit) {
		this.sucessLimit = sucessLimit;
	}

	public int getFailLimit() {
		return failLimit;
	}

	public void setFailLimit(int failLimit) {
		this.failLimit = failLimit;
	}
}
