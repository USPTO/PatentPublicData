package gov.uspto.bulkdata.corpusbuilder;

import java.io.File;
import java.io.IOException;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

/**
 * Write Directly to ZipArchive
 * 
 *<pre>
 * Limitations: 
 *   1) When writing one single large file within zipfile, if process is interrupted then zipfile will remain unreadable.
 *   2) File within ZipFile can not be appended to, once closed.
 *</pre>
 *
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class ZipArchive implements Writer {

	private final File file;
	private ZipArchiveOutputStream outputZip;

	public ZipArchive(final File file) {
		this.file = file;
	}

	@Override
	public void open() throws IOException {
		outputZip = new ZipArchiveOutputStream(file);
		outputZip.setEncoding("UTF-8");
		outputZip.setLevel(9);

		ZipArchiveEntry zipEntry = new ZipArchiveEntry("corpus.xml");
		outputZip.putArchiveEntry(zipEntry);
	}

	@Override
	public void write(byte[] bytes) throws IOException {
		outputZip.write(bytes);
		outputZip.flush();
	}

	@Override
	public void close() throws IOException {
		outputZip.closeArchiveEntry();
		outputZip.close();
	}

	public File getFile() {
		return file;
	}
}
