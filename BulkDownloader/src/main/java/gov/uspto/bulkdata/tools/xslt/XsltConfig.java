package gov.uspto.bulkdata.tools.xslt;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.common.base.Preconditions;

import gov.uspto.bulkdata.BulkReaderArguments;
import joptsimple.OptionParser;

/**
 * Xslt Config from Command-line args
 * 
 * <code><pre>
 * XsltConfig config = new XsltConfig();
 * config.buildArgs();
 * config.parseArgs(args);
 * config.readOptions();
 *</pre></code>
 * 
 * @author Brian G. Feldman <brian.feldman@uspto.gov>
 *
 */
public class XsltConfig extends BulkReaderArguments {
	private Path xsltFile;
	private Boolean prettyPrint = false;

	public XsltConfig() {
		buildArgs(new OptionParser());
	}

	public OptionParser buildArgs(OptionParser opParser) {
		super.buildArgs();
		opParser.accepts("xslt").withRequiredArg().ofType(String.class).describedAs("xslt stylesheet file path")
				.required();
		opParser.accepts("prettyPrint").withOptionalArg().ofType(Boolean.class).describedAs("Pretty print output")
				.defaultsTo(false);
		return opParser;
	}

	public void readOptions() {
		super.readOptions();

		if (options.has("xslt")) {
			String xsltFile = (String) options.valueOf("xslt");
			setXsltFile(Paths.get(xsltFile));
		}

		setPrettyPrint((Boolean) options.valueOf("prettyPrint"));
	}

	public void setXsltFile(Path xsltFile) {
		Preconditions.checkArgument(xsltFile.toFile().canRead(), "Unable to read XSLT file: " + xsltFile);
		this.xsltFile = xsltFile;
	}

	public Path getXsltFile() {
		return this.xsltFile;
	}

	public void setPrettyPrint(Boolean bool) {
		this.prettyPrint = bool;
	}

	public boolean isPrettyPrint() {
		return this.prettyPrint;
	}
}