package gov.uspto.common.io;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TeeWriter extends Writer {

	private final List<Writer> writers;

	public TeeWriter(Writer... writers) {
		this(Arrays.asList(writers));
	}

	public TeeWriter(Iterable<? extends Writer> writers) {
		this.writers = new ArrayList<Writer>();
		for (Writer writer : writers) {
			this.writers.add(writer);
		}
	}

	@Override
	public synchronized void write(char[] cbuf, int off, int len) throws IOException {
		for (Writer writer : writers) {
			writer.write(cbuf, off, len);
		}
	}

	@Override
	public void flush() throws IOException {
		for (Writer writer : writers) {
			writer.flush();
		}
	}

	@Override
	public void close() throws IOException {
		for (Writer writer : writers) {
			try {
				writer.close();
			} catch (Exception e) {
				// close quietly.
			}
		}
	}
}
