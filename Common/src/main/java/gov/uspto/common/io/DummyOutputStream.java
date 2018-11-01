package gov.uspto.common.io;

import java.io.IOException;
import java.io.OutputStream;

public class DummyOutputStream extends OutputStream {
	@Override
	public void write(byte[] b) throws IOException {
		// do nothing.
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		// do nothing.
	}
	
	@Override
	public void write(int b) throws IOException {
		// do nothing.
	}
}