package gov.uspto.common.io;

import java.io.IOException;

public interface SerializeWriter<T> {

	public void write(DocumentBuilder<T> docBuilder, T obj) throws IOException;

}
