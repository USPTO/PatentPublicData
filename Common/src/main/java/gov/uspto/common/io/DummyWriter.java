package gov.uspto.common.io;

import java.io.IOException;
import java.io.Writer;

public class DummyWriter extends Writer {

	@Override
	public void close() throws IOException {
		// do nothing.
	}

	@Override
	public void flush() throws IOException {
		// do nothing.
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		// do nothing.
	}
}
