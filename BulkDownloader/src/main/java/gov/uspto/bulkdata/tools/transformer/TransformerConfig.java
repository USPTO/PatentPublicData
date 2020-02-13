package gov.uspto.bulkdata.tools.transformer;

import static java.util.Arrays.asList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.common.base.Preconditions;

import gov.uspto.bulkdata.BulkReaderArguments;
import joptsimple.OptionParser;

/**
 * Transformer Config from Command-line args
 * 
 * <code><pre>
 * TransformerConfig config = new TransformerConfig();
 * config.parseArgs(args);
 * config.readOptions();
 *</pre></code>
 * 
 * @author Brian G. Feldman <brian.feldman@uspto.gov>
 *
 */
public class TransformerConfig extends BulkReaderArguments {
	private Boolean prettyPrint = false;
	private Boolean bulkOutput = true;
	private Integer bulkRecLimit = -1;
	private Boolean bulkKV = false;
	private Path outputDir;
	private String outputType = "json";

	public OptionParser buildArgs() {
		return buildArgs(new OptionParser());
	}

	public OptionParser buildArgs(OptionParser opParser) {
		super.buildArgs(opParser);
		opParser.accepts("outDir").withRequiredArg().ofType(String.class).describedAs("Output Directory").required();

		opParser.acceptsAll(asList("outBulk", "outputBulkFile")).withOptionalArg().ofType(Boolean.class)
				.describedAs("true: Output bulk file, patent record per line ; false: creates individual patent files within bulkfile named directory").defaultsTo(true);

		opParser.accepts("bulkRecLimit").withOptionalArg().ofType(Integer.class)
				.describedAs("Limit of records per bulk file").defaultsTo(-1);

		opParser.acceptsAll(asList("bulkKV", "bulkkv", "kv")).withOptionalArg().ofType(Boolean.class)
				.describedAs("Prepend each record with docid ; DOC_ID<TAB>RECORD").defaultsTo(false);

		opParser.accepts("prettyPrint").withOptionalArg().ofType(Boolean.class).describedAs("Pretty Print JSON")
				.defaultsTo(false);

		opParser.accepts("type").withOptionalArg().ofType(String.class)
				.describedAs("types options: [raw,json,json_flat,patft,solr,object,text]").defaultsTo("json");

		return opParser;
	}

	public void readOptions() {
		super.readOptions();

		String outDirStr = (String) options.valueOf("outDir");
		Path outDirPath = Paths.get(outDirStr);
		setOutputDir(outDirPath);

		if (options.has("type")) {
			String type = (String) options.valueOf("type");
			if ("?".equals(type) || type.length() < 2) {
				super.printHelp();
			}
			setOutputType(type.toLowerCase());
		}

		/*
		 * OutputBulk should never prettyPrint (single line-per-record)
		 */
		if (options.has("outBulk")) {
			setBulkOutput((Boolean) options.valueOf("outBulk"));
			setBulkRecLimit((Integer) options.valueOf("bulkRecLimit"));
			setBulkKV((Boolean) options.valueOf("bulkKV"));
		} else if (options.has("prettyPrint")) {
			setPrettyPrint((Boolean) options.valueOf("prettyPrint"));
		}

		setPrettyPrint((Boolean) options.valueOf("prettyPrint"));
	}

	public void setOutputDir(Path outDir) {
		File outDirFile = outDir.toFile();
		Preconditions.checkArgument(outDirFile.isDirectory(),
				"Output Directory does not exist: " + outDirFile.getAbsolutePath());
		Preconditions.checkArgument(outDirFile.canWrite(),
				"Can not write to output dir: " + outDirFile.getAbsolutePath());
		this.outputDir = outDir;
	}

	public Path getOutputDir() {
		return this.outputDir;
	}

	public void setOutputType(String outputType) {
		this.outputType = outputType;
	}

	public String getOutputType() {
		return this.outputType;
	}

	public void setPrettyPrint(Boolean bool) {
		this.prettyPrint = bool;
	}

	public void setBulkOutput(Boolean bool) {
		this.bulkOutput = bool;
	}

	public void setBulkRecLimit(Integer limit) {
		this.bulkRecLimit = limit;
	}

	public void setBulkKV(Boolean bool) {
		this.bulkKV = bool;
	}

	public boolean isPrettyPrint() {
		return this.prettyPrint;
	}

	public boolean isBulkOutput() {
		return this.bulkOutput;
	}

	public boolean isBulkKV() {
		return this.bulkKV;
	}

	public Integer getBulkRecLimit() {
		return this.bulkRecLimit;
	}
}