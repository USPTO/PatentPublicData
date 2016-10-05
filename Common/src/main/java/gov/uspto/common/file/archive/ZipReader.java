package gov.uspto.common.file.archive;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.zip.ZipException;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * Read matching files from a ZipFile
 *
 * <pre><code>
 *  FileFilter filter = new FileFilter();
 *  filter.addRule(new PathFileFilter("corpus/patents/ST32-US-Grant-025xml.dtd/"));
 *  filter.addRule(new SuffixFileFilter("xml"));
 *
 *  ZipReader zipReader = new ZipReader(file, filter);
 *  zipReader.open();
 *  
 *  BufferedReader reader = zipReader.next(); // reader for next matching file
 * </pre></code>
 * </p>
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class ZipReader implements Iterator<Reader>, Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZipReader.class);

    private final File file;
    private final FileFilter filter;
    private ZipFile zipFile;
    private Enumeration<ZipArchiveEntry> entries;
    private ZipArchiveEntry currentEntry;
    private int currentRecCount = 0;

    /**
     * Constructor
     * 
     * @param ZipFile
     */
    public ZipReader(File zipfile, FileFilter filter) {
        Preconditions.checkArgument(zipfile.isFile() || zipfile.getName().endsWith("zip"),
                "Input file is not a zipfile: " + zipfile.getAbsolutePath());
        Preconditions.checkNotNull(filter, "FileFilter can not be null.");

        this.file = zipfile;
        this.filter = filter;
    }

    public ZipReader open() throws IOException {
        LOGGER.info("Reading zip file: {}", file);
        zipFile = new ZipFile(file);
        entries = zipFile.getEntries();
        return this;
    }

    public boolean isOpen() {
        return zipFile != null;
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
            currentEntry = entries.nextElement();

            File entryFile = new File(currentEntry.getName());

            if (filter.accept(entryFile)) {
                currentRecCount++;
                LOGGER.info("Found {} file[{}]: {}", currentRecCount, filter, currentEntry.getName());
                try {
                    return new BufferedReader(new InputStreamReader(zipFile.getInputStream(currentEntry)));
                } catch (ZipException e) {
                    LOGGER.error("Error reading Zip File: {}", file, e);
                } catch (IOException e) {
                    LOGGER.error("IOException when reading file: {}", file, e);
                }
            }
        }

        //throw new NoSuchElementException();
        throw new NoSuchElementException();
    }

    public ZipArchiveEntry nextEntry() {
        while (hasNext()) {
            currentEntry = entries.nextElement();
            File entryFile = new File(currentEntry.getName());
            if (filter.accept(entryFile)) {
                return currentEntry;
            }
        }
        throw new NoSuchElementException();
    }

    public BufferedReader readEntry(ZipArchiveEntry zipEntry) throws ZipException, IOException {
        return new BufferedReader(new InputStreamReader(zipFile.getInputStream(zipEntry)));
    }

    @Override
    public boolean hasNext() {
        return entries.hasMoreElements();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Remove not supported");
    }

    public ZipArchiveEntry getCurrentEntry() {
        return currentEntry;
    }

    public String currentEntryName() {
        return currentEntry.getName();
    }

    public int getCurrentRecCount() {
        return currentRecCount;
    }

    @Override
    public void close() throws IOException {
        if (zipFile != null) {
            zipFile.close();
        }
        zipFile = null;
    }
}
