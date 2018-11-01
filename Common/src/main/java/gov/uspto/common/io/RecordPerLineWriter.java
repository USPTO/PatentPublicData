package gov.uspto.common.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Path;

import com.google.common.base.Preconditions;

/**
 * Record-Per-Line Writer
 * 
 * <p>
 * Create a file containing multiple records delineated by new-lines. The
 * new-lines within each record are commented out, becoming '\\n'. Optionally, a
 * max record size can be provided to partition the files.
 * </p>
 * 
 * @author Brian G. Feldman <brian.feldman@uspto.gov>
 *
 * @param <T>
 */
public class RecordPerLineWriter<T> extends Writer implements SerializeWriter<T> {

	private final String prefix;
	private final String postfix;
	private final Path outputDirPath;
	private final Charset charset;
	private final int maxRecords;

	private BufferedWriter writer;
	private int fileIncrement = 0;
	private int currentRecords = 0;
	private File currentFile;

	/**
	 * Constructor
	 * 
	 * @param prefix
	 * @param postfix
	 * @param outputDirPath
	 */
	public RecordPerLineWriter(String prefix, String postFix, Path outputDirPath) {
		this(prefix, postFix, outputDirPath, Charset.defaultCharset(), -1);
	}

	/**
	 * Constructor
	 * 
	 * @param prefix      - provided name used for file naming (preferably with
	 *                      file extension removed)
	 * @param postfix     - postfix or extension for each file created
	 * @param outputDirPath - output file directory to write files
	 * @param charset       - output file charset
	 */
	public RecordPerLineWriter(String prefix, String postFix, Path outputDirPath, Charset charset) {
		this(prefix, postFix, outputDirPath, charset, -1);
	}

	/**
	 * Constructor
	 * 
	 * @param prefix    - provided name used for file naming (preferably with
	 *                      file extension removed)
	 * @param outputDirPath - output file directory to write files
	 * @param charset       - output file charset
	 * @param maxRecords    - use to partition records; max number of records per
	 *                      file
	 */
	public RecordPerLineWriter(String prefix, String postfix, Path outputDirPath, Charset charset, int maxRecords) {
		Preconditions.checkArgument(outputDirPath.toFile().canWrite(),
				"Output Directory Not Writable: " + outputDirPath.toFile().getAbsolutePath());
		this.prefix = prefix;
		this.postfix = postfix;
		this.outputDirPath = outputDirPath;
		this.charset = charset;
		this.maxRecords = maxRecords;
	}

	@Override
	public void write(DocumentBuilder<T> docBuilder, T obj) throws IOException {
		docBuilder.write(obj, getWriter());
		currentRecords++;
	}

	@Override
	public void write(String str) throws IOException {
		getWriter();
		writer.write(str);
		writer.write('\0');
		currentRecords++;
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		getWriter();
		writer.write(cbuf, off, len);
		writer.write('\0');
		currentRecords++;
	}

	@Override
	public void close() throws IOException {
		if (writer != null) {
			writer.close();
			writer = null;
		}
	}

	@Override
	public void flush() throws IOException {
		if (writer != null) {
			writer.flush();
		}
	}

	public File getCurrentFile() {
		return this.currentFile;
	}

	private BufferedWriter getWriter() throws IOException {
		if (maxRecords != -1 && currentRecords == maxRecords) {
			writer.close();
			currentRecords = 0;
			writer = null;
		}

		if (writer == null) {
			Path filePath = outputDirPath.resolve(prefix + "-" + fileIncrement++ + "." + postfix);
			this.currentFile = filePath.toFile();
			OutputStream os = new FileOutputStream(currentFile);
			CommentNewlineOutputStream cos = new CommentNewlineOutputStream(os);
			writer = new BufferedWriter(new OutputStreamWriter(cos, charset));
		}

		return writer;
	}
}
