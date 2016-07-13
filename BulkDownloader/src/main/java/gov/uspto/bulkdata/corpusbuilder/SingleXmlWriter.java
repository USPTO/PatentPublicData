package gov.uspto.bulkdata.corpusbuilder;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import com.google.common.base.Preconditions;

public class SingleXmlWriter implements Writer {

	private final Path filePath;
	private FileOutputStream outputStream;
	private boolean append;

	public SingleXmlWriter(final Path filePath, boolean append) {
		this.filePath = filePath;
		this.append = append;
	}

	@Override
	public void open() throws FileNotFoundException {
		outputStream = new FileOutputStream(filePath.toFile(), append);
	}

	@Override
	public void write(byte[] bytes) throws IOException {
		Preconditions.checkState(isOpen(), "SingleXML file is not open!");
		outputStream.write(bytes);
	}

	@Override
	public void close() throws IOException {
		if (outputStream != null){
			outputStream.close();
		}
		outputStream = null;
	}

	public Path getFilePath() {
		return filePath;
	}

	@Override
	public boolean isOpen() {
		return (outputStream != null);
	}
}
