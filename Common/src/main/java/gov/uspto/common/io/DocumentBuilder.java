package gov.uspto.common.io;

import java.io.IOException;
import java.io.Writer;

public interface DocumentBuilder<T> {
     public void write(T obj, Writer writer) throws IOException;
}
