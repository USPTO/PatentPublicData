package gov.uspto.bulkdata.zip;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.zip.ZipException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.google.common.base.Preconditions;

/**
 * Read matching files from a ZipFile
 *
 * <pre><code>
 *  FileFilter filter = new FileFilter();
 *
 *  ZipReader fileInZip = new ZipReader(file, "xml");
 *  filter.addRule(new PathRule("corpus/patents/ST32-US-Grant-025xml.dtd/"));
 *  filter.addRule(new SuffixRule("xml"));
 * </pre></code>
 * </p>
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class ZipReader implements Iterator<Reader>, Closeable {
	private static final Logger LOGGER = LoggerFactory.getLogger(ZipReader.class);

	private final File file;
	private ZipFile zipFile;
	private Enumeration<ZipArchiveEntry> entries;
	private FileFilter filter;
	private int currentRecCount = 0;

	/**
	 * Constructor
	 * 
	 * @param ZipFile
	 */
	public ZipReader(File zipfile, FileFilter filter) {
		Preconditions.checkArgument(zipfile.isFile() || zipfile.getName().endsWith("zip"),
				"Input file is not a zipfile: " + zipfile.getAbsolutePath());
		this.file = zipfile;
		this.filter = filter;
	}

	public ZipReader open() throws IOException {
		LOGGER.info("Reading zip file: {}", file);
		zipFile = new ZipFile(file);
		entries = zipFile.getEntries();
		return this;
	}

	/**
	 * Skip forward specified number of documents.
	 * 
	 * @param skipCount
	 */
	public ZipReader skip(int skipCount) {
		for (int i = 1; i <= skipCount; i++) {
			next();
		}
		return this;
	}

	/**
	 * Jump forward specified count to retrieve record.
	 * 
	 * @param recCount
	 * 
	 * @return
	 */
	public BufferedReader jumpTo(int recCount) {
		for (int i = 1; i < recCount; i++) {
			next();
		}
		return next();
	}

	@Override
	public BufferedReader next() {
		while (hasNext()) {
			ZipArchiveEntry entry = entries.nextElement();

			File entryFile = new File(entry.getName());

			if (filter.match(entryFile)) {
				//if ( (fileName != null && entry.getName().equals(fileName)) || (fileName == null && entry.getName().endsWith(fileSuffix)) ){
				currentRecCount++;
				LOGGER.info("Found {} file[{}]: {}", currentRecCount, filter, entry.getName());
				try {
					return new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
				} catch (ZipException e) {
					LOGGER.error("Error reading Zip File: {}", file, e);
				} catch (IOException e) {
					LOGGER.error("IOException when reading file: {}", file, e);
				}
			}
		}

		throw new NoSuchElementException("ZipFile failed to find matching embeded file.");
	}

	@Override
	public boolean hasNext() {
		return entries != null && entries.hasMoreElements();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Remove not supported");
	}

	public int getCurrentRecCount() {
		return currentRecCount;
	}

	@Override
	public void close() throws IOException {
		if (zipFile != null){
			zipFile.close();
		}
	}

	public static void main(String[] args) throws IOException, ParserConfigurationException, XMLStreamException,
			DocumentException, SAXException, XPathExpressionException, ParseException {

		File file = new File("C:/Users/bfeldman/Desktop/Sigma/corpus.zip");

		int skip = 0;
		//int count = 4;

		FileFilter filter = new FileFilter();
		filter.addRule(new PathRule("corpus/patents/ST32-US-Grant-025xml.dtd/"));
		filter.addRule(new SuffixRule("xml"));

		try (ZipReader fileInZip = new ZipReader(file, filter)) {
			fileInZip.open().skip(skip);

			while (fileInZip.hasNext()) {
				//for (int i=1; xmlZip.hasNext() && i <= count; i++){
				LOGGER.info("{} --------------------------", fileInZip.getCurrentRecCount() + 1);

				Reader docTxtStr = fileInZip.next();
				if (docTxtStr == null) {
					break;
				}

				try(BufferedReader reader = new BufferedReader(docTxtStr)){
					LOGGER.info(reader.readLine());
				}
			}
		}
	}

}
