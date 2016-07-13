package gov.uspto.bulkdata.corpusbuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

import com.google.common.base.Preconditions;

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
public class ZipArchiveWriter implements Writer {

	private final Path filePath;
	private ZipArchiveOutputStream outputZip;

	public ZipArchiveWriter(final Path filePath) {
		this.filePath = filePath;
	}

	@Override
	public void open() throws IOException {
		outputZip = new ZipArchiveOutputStream(filePath.toFile());
		outputZip.setEncoding("UTF-8");
		outputZip.setLevel(9);

		ZipArchiveEntry zipEntry = new ZipArchiveEntry("corpus.xml");
		outputZip.putArchiveEntry(zipEntry);
	}

	@Override
	public void write(byte[] bytes) throws IOException {
		Preconditions.checkState(isOpen(), "ZipArchive is not open!");
		outputZip.write(bytes);
		outputZip.flush();
	}

	@Override
	public void close() throws IOException {
		if (outputZip != null){
			outputZip.closeArchiveEntry();
			outputZip.close();
		}
		outputZip = null;
	}

	public Path getFilePath() {
		return filePath;
	}

	@Override
	public boolean isOpen() {
		return (outputZip != null);
	}
}
