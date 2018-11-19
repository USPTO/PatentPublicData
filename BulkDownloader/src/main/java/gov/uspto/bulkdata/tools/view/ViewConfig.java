package gov.uspto.bulkdata.tools.view;

import static java.util.Arrays.asList;

import gov.uspto.bulkdata.BulkReaderArguments;
import joptsimple.OptionParser;

/**
 * View Config from Command-line args
 * 
 * <code><pre>
 * ViewConfig config = new ViewConfig();
 * config.parseArgs(args);
 * config.readOptions();
 *</pre></code>
 * 
 * @author Brian G. Feldman <brian.feldman@uspto.gov>
 *
 */
public class ViewConfig extends BulkReaderArguments {
	private String[] fields;
	private int recordLocation = -1;
	private String patentId;
	private String outputType = "fields";

	public ViewConfig() {
		buildArgs(new OptionParser());
	}

	public OptionParser buildArgs(OptionParser opParser) {
		super.buildArgs(opParser);

		opParser.accepts("type").withOptionalArg().ofType(String.class)
				.describedAs("types options: [raw,xml,json,json_flat,patft,object,text]").defaultsTo("object");

		opParser.accepts("fields").withOptionalArg().ofType(String.class)
				.describedAs("comma seperated list of fields; --fields=? will return all available field names")
				.defaultsTo("object");

		opParser.acceptsAll(asList("num", "record-location")).withOptionalArg().ofType(Integer.class)
				.describedAs("Record Number to retrive");

		opParser.accepts("id").withOptionalArg().ofType(String.class).describedAs("Patent Id");

		return opParser;
	}

	public void readOptions() {
		super.readOptions();

		if (options.has("num")) {
			int loc = (Integer) options.valueOf("num");
			setRecordLocation(loc);
		}

		if (options.has("id")) {
			String patentId = (String) options.valueOf("id");
			setPatentId(patentId);
		}

		if (options.has("type")) {
			String type = (String) options.valueOf("type");
			setOutputType(type.toLowerCase());
		}

		String[] fields = ((String) options.valueOf("fields")).split(",");
		setFields(fields);

		if (getRecordReadLimit() == -1) {
			setRecordReadLimit(1); // Default for view is one record.
		}
	}

	public void setOutputType(String outputType) {
		this.outputType = outputType;
	}

	public String getOutputType() {
		return this.outputType;
	}

	public void setPatentId(String patentId) {
		this.patentId = patentId;
	}

	public String getPatentId() {
		return this.patentId;
	}

	public void setRecordLocation(int loc) {
		this.recordLocation = loc;
		int skip = loc - 1;
		int limit = 1;
		setSkipRecordCount(skip);
		setRecordReadLimit(limit);
	}

	public int getRecordLocation() {
		return this.recordLocation;
	}

	public void setFields(String[] fields) {
		this.fields = fields;
	}

	public String[] getFields() {
		return this.fields;
	}

}