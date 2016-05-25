package gov.uspto.bulkdata;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * Read individual XML Records, wrapped within a single large XML optionally included in a zipfile.
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class DumpXmlReader implements DocumentIterator, Iterator<String> {
	private static final Logger LOGGER = LoggerFactory.getLogger(DumpXmlReader.class);

	private final File file;
	//private final String endRecordTag;
	private final String xmlBodyTag;
	private final String xmlStartTag;
	private final String xmlEndTag;

	private ZipFile zipFile;
	private BufferedReader reader;

	private int currentRecCount = 0;
	private int totalRecordCount = 0;

	private String currentXMLDoc = ""; // Entire XML Record captured in string.

	/**
	 * Constructor
	 * 
	 * @param file, ZipFile or XML File.
	 * @param endRecordTag  ending XML tag indicating the end of a record.
	 * @throws IOException
	 */
	public DumpXmlReader(File file, String xmlBodyTag) {
		Preconditions.checkNotNull(file, "File can not be Null");
		Preconditions.checkNotNull(xmlBodyTag, "XmlBodyTag can not be Null");
		Preconditions.checkArgument(file.isFile(), "File not found:" + file.getAbsolutePath());

		this.file = file;
		this.xmlBodyTag = xmlBodyTag;
		this.xmlStartTag = "<" + xmlBodyTag;
		this.xmlEndTag = "</" + xmlBodyTag;
	}
	
	public void open() throws IOException {
		if (file.getName().endsWith("zip")) {
			reader = openZip(file);
		} else if (file.getName().endsWith("xml")) {
			reader = new BufferedReader(new FileReader(file));
		}
	}

	public File getFile(){
		return file;
	}

	public BufferedReader getReader() {
		return reader;
	}

	private BufferedReader openZip(File file) throws IOException {
		Preconditions.checkArgument(file.isFile() || file.getName().endsWith("zip"),
				"Input file is not a zipfile: " + file.getAbsolutePath());

		zipFile = new ZipFile(file);

		Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();

		while (entries.hasMoreElements()) {
			ZipArchiveEntry entry = entries.nextElement();

			if (entry.getName().endsWith("xml")) {
				return new BufferedReader(
						new InputStreamReader(zipFile.getInputStream(entry), Charset.forName("UTF-8")));
			}
		}

		throw new FileNotFoundException("XML file not found in Zip File: " + file.getName());
	}

	/**
	 * First two lines are removed and later added as header onto each document.
	 * @throws IOException 
	 */
	/*
	private void readHeader() throws IOException {
		StringBuilder top = new StringBuilder().append(reader.readLine()).append('\n') // xml line.
				.append(reader.readLine()).append('\n'); // doc type, dtd line.
		header = top.toString();
	}
	*/

	@Override
	public String next() {
		currentXMLDoc = advance();
		return currentXMLDoc;
	}

	@Override
	public InputStream nextDocument() {
		// TODO: Change class to u
		currentXMLDoc = advance();
		return new ByteArrayInputStream(currentXMLDoc.getBytes(StandardCharsets.UTF_8));
	}

	public boolean isStartTag(String line) {
		return line.trim().startsWith(xmlStartTag);
	}

	public boolean isEndTag(String line) {
		return line.trim().startsWith(xmlEndTag);
	}

	private String advance() {

		StringBuilder content = new StringBuilder();

		try {
			String line;
			while (reader.ready() && (line = reader.readLine()) != null) {

				if (isStartTag(line)) {
					content = new StringBuilder();
				} else if (isEndTag(line)) {
					content.append(line).append('\n');
					currentRecCount++;
					return content.toString();
				}

				content.append(line).append('\n');
			}
		} catch (IOException e) {
			LOGGER.error("Error while reading file: {}; record: {}", file, currentRecCount, e);
		}

		throw new NoSuchElementException();
	}

	@Override
	public boolean hasNext() {
		return currentXMLDoc != null;
	}

	@Override
	public void reset() {
		throw new UnsupportedOperationException("Remove not supported");
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Remove not supported");
	}

	/**
	 * Skip forward specified number of documents.
	 * 
	 * @param skipCount
	 */
	public void skip(int skipCount) {
		for (int i = 1; i <= skipCount; i++) {
			next();
		}
	}

	/**
	 * Jump forward specified count to retrieve record.
	 * 
	 * @param recCount
	 * 
	 * @return xml string
	 */
	public String jumpTo(int recCount) {
		for (int i = 1; i < recCount; i++) {
			next();
		}
		return next();
	}

	/**
	 * Get Number of Records in the Dump XML file.
	 * 
	 * @return record count
	 * @throws IOException
	 */
	public int recordCount() throws IOException {
		if (totalRecordCount == 0) {
			DumpXmlReader countReader = new DumpXmlReader(file, xmlBodyTag);
			countReader.open();
			int recordCount = 0;
			try {
				String line;
				boolean started = false;
				while (countReader.getReader().ready() && (line = countReader.getReader().readLine()) != null) {
					if (isStartTag(line)) {
						started = true;
					} else if (isEndTag(line)) {
						recordCount++;
					}
				}
			} finally {
				countReader.close();
			}
			totalRecordCount = recordCount;
		}
		return totalRecordCount;
	}

	/**
	 * Get Current Record Number
	 * 
	 * @return
	 */
	public int getCurrentRecCount() {
		return currentRecCount;
	}

	public void close() throws IOException {
		reader.close();
		zipFile.close();
	}
}
