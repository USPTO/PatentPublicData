package gov.uspto.patent.bulk;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.google.common.base.Preconditions;

import gov.uspto.common.file.archive.ZipReader;
import gov.uspto.patent.PatentDocFormat;
import gov.uspto.patent.PatentDocFormatDetect;

public abstract class DumpFile implements Iterator<String>, Closeable, DumpReader {
	private static final Logger LOGGER = LoggerFactory.getLogger(DumpFile.class);

	private final File file;
	private PatentDocFormat patentDocFormat;

	private ZipReader zipFile;
	private BufferedReader reader;

	private FileFilter fileFilter;
	private String currentRawDoc;

	public DumpFile(File file) {
		Preconditions.checkNotNull(file, "File can not be Null");
		Preconditions.checkArgument(file.isFile(), "File not found:" + file.getAbsolutePath());
		MDC.put("SOURCE", String.valueOf(file.getName()));
		this.file = file;
	}

	/**
	 * @param name   - Name use for tracking purposes.
	 * @param reader
	 */
	public DumpFile(String name, BufferedReader reader) {
		this.file = new File(name);
		this.reader = reader;
	}

	public void setPatentDocFormat(PatentDocFormat patentDocFormat) {
		this.patentDocFormat = patentDocFormat;
	}

	public void setFileFilter(FileFilter filter) {
		this.fileFilter = filter;
	}

	public void open() throws IOException {
		if (file.getName().endsWith(".zip")) {
			zipFile = new ZipReader(file, fileFilter, StandardCharsets.UTF_8);
			try {
				reader = zipFile.open().next();
			} catch (NoSuchElementException e) {
				LOGGER.error("Failed to Read Zip File '{}' ; no matching '{}'", file.getName(), fileFilter, e);
				throw e;
			}
		} else if (reader != null) {
			// use defined reader.
		} else {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
		}

		patentDocFormat = new PatentDocFormatDetect().fromContent(reader);
		currentRawDoc = read();
	}

	@Override
	public void close() throws IOException {
		if (zipFile != null) {
			zipFile.close();
		}
		if (reader != null) {
			reader.close();
		}
		MDC.clear();
	}

	@Override
	public boolean hasNext() {
		return currentRawDoc != null;
	}

	@Override
	public String next() {
		String doc = currentRawDoc;
		if (doc != null) {
			currentRawDoc = read();
			return doc;
		} else {
			throw new NoSuchElementException();
		}
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Remove not supported");
	}

	@Override
	public InputStream nextDocument() {
		return new ByteArrayInputStream(next().getBytes(StandardCharsets.UTF_8));
	}

	@Override
	public PatentDocFormat getPatentDocFormat() {
		return patentDocFormat;
	}

	protected BufferedReader getReader() {
		return reader;
	}

	@Override
	public File getFile() {
		return file;
	}

}