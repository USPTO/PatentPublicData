package gov.uspto.bulkdata.corpusbuilder;

import java.io.FileNotFoundException;
import java.io.IOException;

public class DummyWriter implements Writer {

	private boolean openFlag;

	@Override
	public void open() throws FileNotFoundException {
		openFlag = true;
	}

	@Override
	public void write(byte[] bytes) throws IOException {
		// do nothing.
	}

	@Override
	public void close() throws IOException {
		openFlag = false;
	}

	@Override
	public boolean isOpen() {
		return openFlag;
	}
}
