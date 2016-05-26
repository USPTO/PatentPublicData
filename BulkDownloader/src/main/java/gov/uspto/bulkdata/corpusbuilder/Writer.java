package gov.uspto.bulkdata.corpusbuilder;

import java.io.IOException;

public interface Writer {
	public void open() throws IOException;

	public boolean isOpen();

	public void write(byte[] bytes) throws IOException;

	public void close() throws IOException;
}
