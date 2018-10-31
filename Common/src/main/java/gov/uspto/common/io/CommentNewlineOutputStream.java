package gov.uspto.common.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Comments out line breaks within an OutputSteam
 * 
 * <p>
 * This class wraps an output stream, replacing all line breaks with '\\n'.
 * CR(Carriage Return '\r'), LF(Line Feed '\n') and CRLF('\r\n') become '\\n'
 * <p>
 *
 * <p>
 * Useful when writing a bulk, multi-record, file in record-per-line format.
 * </p>
 *
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class CommentNewlineOutputStream extends FilterOutputStream {

	protected int lastByte;
	protected static byte[] newline = new byte[] { '\\', 'n' };

	public CommentNewlineOutputStream(OutputStream out) {
		super(out);
		lastByte = -1;
	}

	public void write(int i) throws IOException {
		if (i == '\r') {
			out.write(newline);
		} else if (i == '\n') {
			if (lastByte != '\r') {
				out.write(newline);
			}
		} else {
			out.write(i);
		}

		lastByte = i;
	}

	public void write(byte[] buf) throws IOException {
		this.write(buf, 0, buf.length);
	}

	public void write(byte buf[], int off, int len) throws IOException {
		for (int i = off; i != off + len; i++) {
			this.write(buf[i]);
		}
	}

	public void writeln() throws IOException {
		super.out.write(newline);
	}

}
