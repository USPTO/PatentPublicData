package gov.uspto.patent.bulk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import gov.uspto.common.file.archive.ZipReader;
import gov.uspto.common.filter.SuffixFilter;
import gov.uspto.patent.bulk.DumpFile;
import gov.uspto.patent.bulk.DumpFileXml;

/**
 * 
 * Reading multiple large Bulk files contained within a single ZipFile.
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class BulkArchive implements Iterator<DumpFile>, AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(BulkArchive.class);

    private final ZipReader zipArchive;

    public BulkArchive(File zipFile, FileFilter fileFilter) {
        Preconditions.checkArgument(zipFile.canRead(), "ZipFile not readble: " + zipFile.getAbsolutePath());

        this.zipArchive = new ZipReader(zipFile, fileFilter);
    }

    public void open() throws IOException {
        zipArchive.open();
    }

    @Override
    public void close() throws IOException {
        zipArchive.close();
    }

    @Override
    public boolean hasNext() {
        return zipArchive.hasNext();
    }

    public void skip(int skip) {
        for (int i = 0; i < skip; i++) {
            zipArchive.nextEntry();
        }
    }

    @Override
    public DumpFile next() {
        ZipArchiveEntry zipEntry = zipArchive.nextEntry();
        try {
            BufferedReader reader = zipArchive.readEntry(zipEntry);
            return new DumpFileXml(zipEntry.getName(), reader);
        } catch (IOException e) {
            LOGGER.error("Failed Reader DumpFile: {}", e);
        }
        throw new NoSuchElementException();
    }

    public static void main(String[] args) throws IOException {
        String filePath = args[0];

        FileFilter fileFilter = new SuffixFilter("xml");

        BulkArchive cpcMaster = new BulkArchive(new File(filePath), fileFilter);
        cpcMaster.open();

        while (cpcMaster.hasNext()) {
            DumpFile dumpFile = cpcMaster.next();
            dumpFile.open();

            while (dumpFile.hasNext()) {

                System.out.println(dumpFile.getFile());
                String rawRecord = dumpFile.next();
                System.out.println(rawRecord);
                break;

            }

            dumpFile.close();
            break;
        }

        cpcMaster.close();
    }
}
