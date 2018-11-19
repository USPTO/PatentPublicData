package gov.uspto.bulkdata.tools.fetch;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;

import gov.uspto.bulkdata.BulkReaderArguments;
import gov.uspto.bulkdata.cli2.BulkDataType;
import gov.uspto.common.DateRange;
import joptsimple.OptionParser;

/**
 * Download Config from Command-line args
 * 
 * <code><pre>
 * DownloadConfig config = new DownloadConfig();
 * config.parseArgs(args);
 * config.readOptions();
 *</pre></code>
 * 
 * @author Brian G. Feldman <brian.feldman@uspto.gov>
 *
 */
public class DownloadConfig extends BulkReaderArguments {
	private Path outputDir;
	private boolean restart = false;
	private Collection<String> matchFilenames;
	private int downloadLimit;
	private boolean async;
	private BulkDataType dataType;
	private ListMultimap<String, DateRange> dateRanges;
	private boolean delete;

	public OptionParser buildArgs() {
		return buildArgs(new OptionParser());
	}

	public OptionParser buildArgs(OptionParser opParser) {
		super.buildArgs(opParser);

		opParser.accepts("outDir").withOptionalArg().ofType(String.class).describedAs("directory")
				.defaultsTo("download");

		opParser.accepts("fetch-type").withRequiredArg().ofType(String.class)
				.describedAs("Patent Document Type [grant, application, gazette] ; type=? will show available types.")
				.required();

		opParser.accepts("fetch-date").withRequiredArg().ofType(String.class)
				.describedAs("Single Date Range or list, example: 20150801-20150901,20160501-20160601").required();

		opParser.accepts("fetch-limit").withOptionalArg().ofType(Integer.class)
				.describedAs("download file limit ; 0 is unlimited").defaultsTo(0);

		opParser.accepts("fetch-async").withOptionalArg().ofType(Boolean.class).describedAs("download file async")
				.defaultsTo(false);

		opParser.accepts("fetch-filename").withOptionalArg().ofType(String.class)
				.describedAs("comma separated list of file names to match and download");

		opParser.accepts("fetch-delete").withOptionalArg().ofType(Boolean.class)
				.describedAs("Delete bulkfile after each processing download.").defaultsTo(false);

		opParser.accepts("restart").withOptionalArg().ofType(Boolean.class)
				.describedAs("Restart failed download from job file in download directory.").defaultsTo(false);

		return opParser;
	}

	public void readOptions() {
		super.readOptions();

		String outDirStr = (String) options.valueOf("outDir");
		Path outDirPath = Paths.get(outDirStr);
		setOutputDir(outDirPath);

		setDownloadLimit((Integer) options.valueOf("fetch-limit"));

		setRestart((boolean) options.valueOf("restart"));
		setAsync((boolean) options.valueOf("fetch-async"));
		setDelete((boolean) options.valueOf("fetch-delete"));

		ListMultimap<String, DateRange> yearMap = LinkedListMultimap.create();
		if (options.has("fetch-date")) {
			String dataInpuStr = (String) options.valueOf("fetch-date");
			Iterable<String> dateInputList = Splitter.on(",").omitEmptyStrings().trimResults().split(dataInpuStr);
			for (String dateStr : dateInputList) {
				List<String> dateInputRange = Splitter.on("-").omitEmptyStrings().trimResults().splitToList(dateStr);
				if (dateInputRange.size() == 2) {
					DateRange dateRange = DateRange.parse(dateInputRange.get(0), dateInputRange.get(1),
							DateTimeFormatter.BASIC_ISO_DATE);
					for (Integer year : dateRange.getYearsBetween()) {
						yearMap.put(String.valueOf(year), dateRange);
					}
				} else {
					// LOGGER.warn("Invalid DateRange has more than two dashs: {}", dateInputRange);
				}
			}
			setDateRangs(yearMap);
		}

		if (options.has("fetch-type")) {
			BulkDataType dataType = null;
			String type = (String) options.valueOf("fetch-type"); // application
			if ("?".equals(type)) {
				showAvailableTypes();
			}
			switch (type.toLowerCase()) {
			case "grant":
				dataType = BulkDataType.GRANT_REDBOOK_TEXT;
				break;
			case "application":
				dataType = BulkDataType.APPLICATION_REDBOOK_TEXT;
				for (String yearStr : yearMap.keySet()) {
					Integer year = Integer.parseInt(yearStr);
					if (year < 2001) {
						throw new IllegalArgumentException(
								"Patent Applications not publicly available before March 2001: " + yearMap.keySet());
					}
				}
				break;
			case "gazette":
				dataType = BulkDataType.GAZETTE;
				break;
			default:
				throw new IllegalArgumentException("Unknown Download Source: " + type);
			}
			setDataType(dataType);
		}

		if (options.has("fetch-filename")) {
			String filenameStr = (String) options.valueOf("fetch-filename");
			if (!filenameStr.isEmpty()) {
				List<String> filenames = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(filenameStr);
				setMatchFilenames(filenames);
			}
		}
	}

	private void showAvailableTypes() {
		System.out.println("####  Available Types ##### ");
		System.out.println("Patent Document Types [grant, application, gazette]");
		/*
		 * for (BulkDataType value : BulkDataType.values()) {
		 * System.out.println(value.name()); }
		 */
		System.exit(0);
	}

	/**
	 * Delete files after processing each; only when processing is enabled i.e.
	 * matching and/or transforming.
	 * 
	 * @param bool
	 */
	public void setDelete(boolean bool) {
		this.delete = bool;
	}

	public boolean isDelete() {
		return this.delete;
	}

	public void setDateRangs(ListMultimap<String, DateRange> dateRanges) {
		this.dateRanges = dateRanges;
	}

	public ListMultimap<String, DateRange> getDateRangs() {
		return this.dateRanges;
	}

	private void setDataType(BulkDataType dataType) {
		this.dataType = dataType;
	}

	public BulkDataType getDataType() {
		return this.dataType;
	}

	public void setRestart(boolean bool) {
		this.restart = bool;
	}

	public boolean isRestart() {
		return this.restart;
	}

	public void setAsync(boolean bool) {
		this.async = bool;
	}

	public boolean isAsync() {
		return this.async;
	}

	public void setDownloadLimit(int limit) {
		this.downloadLimit = limit;
	}

	public int getDownloadLimit() {
		return this.downloadLimit;
	}

	public void setMatchFilenames(Collection<String> filenames) {
		this.matchFilenames = filenames;
	}

	public Collection<String> getMatchFilenames() {
		return this.matchFilenames;
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

}