package gov.uspto.patent.serialize;

import java.io.IOException;

public interface DocumentBuilder<T, F> {
     public F build(T obj) throws IOException;
}
