package gov.uspto.patent;

import java.io.IOException;
import java.io.Reader;

public interface PatentDocReader<T> {
    public T read(Reader reader) throws PatentReaderException, IOException;
}
