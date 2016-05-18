package gov.uspto.bulkdata;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.Iterator;
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
 * Read files from Zip files.
 * 
 * <p>1) individual file by name.
 * {@code
 *    FileInZip fileInZip = new FileInZip(file, "txt", "product.txt");
 * }
 * </p>
 * 
 * <p>
 * 2) iterate over files with same suffix.
 * {@code
 *    FileInZip fileInZip = new FileInZip(file, "xml");
 * }
 * </p>
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class ZipReader implements Iterator<Reader> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ZipReader.class);

	private final File file;
	private ZipFile zipFile;
	private Enumeration<ZipArchiveEntry> entries;
	private FileFilter find;
	private int currentRecCount = 0;

	/**
	 * Constructor
	 * 
	 * @param ZipFile
	 */
	public ZipReader(File zipfile, FileFilter find){
		Preconditions.checkArgument(zipfile.isFile() || zipfile.getName().endsWith("zip"), "Input file is not a zipfile: " + zipfile.getAbsolutePath());
		this.file = zipfile;
		this.find = find;
	}

	public void open() throws IOException{
			LOGGER.info("Reading zip file: {}", file);
			zipFile = new ZipFile( file );
			entries = zipFile.getEntries();
	}

	@Override
	public BufferedReader next() {
		while(hasNext()){
			ZipArchiveEntry entry = entries.nextElement();
			
			File entryFile = new File(entry.getName());
			
			if (find.match(entryFile)){
			//if ( (fileName != null && entry.getName().equals(fileName)) || (fileName == null && entry.getName().endsWith(fileSuffix)) ){
				currentRecCount++;
				LOGGER.info("Found {} file[{}]: {}", currentRecCount, find.getFileSuffix(), entry.getName());
				try {
					return new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
				} catch (ZipException e) {
					LOGGER.error("Error reading Zip File: {}", file, e);
				} catch (IOException e) {
					LOGGER.error("IOException when reading file: {}", file, e);
				}
			}
		}
		
		//throw new NoSuchElementException();
		return null;
	}

	@Override
	public boolean hasNext() {
		return entries.hasMoreElements();
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
	public void skip(int skipCount){
		for(int i=1; i <= skipCount; i++){
			next();
		}
	}

	/**
	 * Jump forward specified count to retrieve record.
	 * 
	 * @param recCount
	 * 
	 * @return
	 */
	public BufferedReader jumpTo(int recCount){
		for(int i=1; i < recCount; i++){
			next();
		}
		return next();
	}

	public int getCurrentRecCount() {
		return currentRecCount;
	}

	public void close() throws IOException{
		zipFile.close();
	}

	public static void main(String[] args) throws IOException, ParserConfigurationException, XMLStreamException, DocumentException, SAXException, XPathExpressionException, ParseException {
		//File file = new File("../download/CPCSchemeXML201510.zip");
		//File file = new File("download/ndc.zip");
		File file = new File("C:/Users/bfeldman/Desktop/corpus.zip");

		int skip = 0;
		//int count = 4;

		//FileInZip fileInZip = new FileInZip(file, "txt", "product.txt");
		FileFilter find = new FileFilter();
		find.setFileSuffix("xml");
		find.setParentPath("corpus/patents/ST32-US-Grant-025xml.dtd/");

		ZipReader fileInZip = new ZipReader(file, find);
		fileInZip.open();
		fileInZip.skip(skip);

		while(fileInZip.hasNext()){
		//for (int i=1; xmlZip.hasNext() && i <= count; i++){
			LOGGER.info("{} --------------------------", fileInZip.getCurrentRecCount()+1);

			Reader docTxtStr = fileInZip.next();
			if (docTxtStr == null){
				break;
			}

			BufferedReader reader = new BufferedReader(docTxtStr);
			LOGGER.info(reader.readLine());
		}

		fileInZip.close();
	}

}
