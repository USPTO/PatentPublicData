package gov.uspto.bulkdata.reader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Enumeration;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import com.google.common.base.Preconditions;

public class ZippedXmlReader {

	private ZipFile zipFile;
	private File file;
	private InputStream inStream;

	public ZippedXmlReader(Path zipfilePath) {
		Preconditions.checkArgument(zipfilePath.toFile().isFile() || zipfilePath.endsWith("zip"),
				"Input file is not a zipfile: " + zipfilePath.toAbsolutePath());
		this.file = zipfilePath.toFile();
	}

	public InputStream open() throws IOException {

		zipFile = new ZipFile(file);

		Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();

		while (entries.hasMoreElements()) {
			ZipArchiveEntry entry = entries.nextElement();

			if (entry.getName().endsWith("xml")) {
				inStream = zipFile.getInputStream(entry);
				return inStream;
				//return BufferedReader(new InputStreamReader(zipFile.getInputStream(entry), Charset.forName("UTF-8")));
			}
		}

		throw new FileNotFoundException("XML file not found in Zip File: " + file.getName());
	}

	public void close() throws IOException {
		inStream.close();
		zipFile.close();
	}
}
