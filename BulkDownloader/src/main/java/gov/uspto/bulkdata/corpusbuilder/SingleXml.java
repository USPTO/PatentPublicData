package gov.uspto.bulkdata.corpusbuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class SingleXml implements Writer {

	private final File file;
	private FileOutputStream outputStream;
	private boolean append;

	public SingleXml(final File file, boolean append) {
		this.file = file;
		this.append = append;
	}

	@Override
	public void open() throws FileNotFoundException {
		outputStream = new FileOutputStream(file, append);
	}

	@Override
	public void write(byte[] bytes) throws IOException {
		outputStream.write(bytes);
	}

	@Override
	public void close() throws IOException {
		outputStream.close();
	}

	public File getFile() {
		return file;
	}
}
