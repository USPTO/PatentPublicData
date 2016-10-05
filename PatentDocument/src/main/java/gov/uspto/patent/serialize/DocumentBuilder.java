package gov.uspto.patent.serialize;

import java.io.IOException;
import java.io.Writer;

public interface DocumentBuilder<T> {
     public void write(T obj, Writer writer) throws IOException;
}
