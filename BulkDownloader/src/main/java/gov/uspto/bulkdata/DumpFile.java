package gov.uspto.bulkdata;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import gov.uspto.bulkdata.zip.FileFilter;
import gov.uspto.bulkdata.zip.ZipReader;
import gov.uspto.patent.PatentType;
import gov.uspto.patent.PatentTypeDetect;

public abstract class DumpFile implements Iterator<String>, Closeable, DumpReader {
	private static final Logger LOGGER = LoggerFactory.getLogger(DumpFile.class);

	private final File file;
	private PatentType patentType;

	private ZipReader zipFile;
	private BufferedReader reader;

	private FileFilter fileFilter = new FileFilter();
	private String currentRawDoc = "";

	public DumpFile(File file){
		Preconditions.checkNotNull(file, "File can not be Null");
		Preconditions.checkArgument(file.isFile(), "File not found:" + file.getAbsolutePath());

		this.file = file;
	}

	public void setPatentType(PatentType patentType){
		this.patentType = patentType;
	}

	public void setFileFilter(FileFilter filter){
		this.fileFilter   = filter;
	}

	public void open() throws IOException {
		currentRawDoc = "";

		if (file.getName().endsWith("zip")) {
			zipFile = new ZipReader(file, fileFilter);
			reader = zipFile.open().next();
		} else {
			reader = new BufferedReader(new FileReader(file));
		}

		patentType = new PatentTypeDetect().fromContent(reader);
	}
	
	@Override
	public void close() throws IOException {
		if (zipFile != null) {
			zipFile.close();
		}
		reader.close();
	}

	@Override
	public boolean hasNext() {
		return currentRawDoc != null;
	}

	@Override
	public String next() {
		currentRawDoc = read().toString();
		return currentRawDoc;
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
	public PatentType getPatentType() {
		return patentType;
	}

	protected BufferedReader getReader(){
		return reader;
	}

	@Override
	public File getFile() {
		return file;
	}

}
