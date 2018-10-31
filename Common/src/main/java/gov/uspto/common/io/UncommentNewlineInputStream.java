package gov.uspto.common.io;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Uncomment line breaks within an InputStream
 * 
 * <p>
 * This class wraps an input stream, replacing all commented line breaks '\\n' with a new line '\n'.
 * <p>
 *
 * <p>
 * Useful when reading a bulk, multi-record, file in record-per-line format.
 * </p>
 *
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class UncommentNewlineInputStream extends BufferedInputStream {

	public UncommentNewlineInputStream(InputStream in) {
		super(in);
	}

	public int read() throws IOException {
		int inByte = super.read();
		if (inByte == '\\') {
			super.mark(2);
			int inByte2 = super.read();
			if (inByte2 == 'n') {
				return '\n';
			} else {
				reset();
			}
		}
		return inByte;
	}

	public int read(byte buf[], int off, int len) throws IOException {
		for (int i = off; i != off + len; i++) {
			buf[i] = (byte) this.read();
		}
		return buf.length;
	}
}
