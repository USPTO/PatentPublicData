package gov.uspto.common.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class ByteArrayInOutStream extends ByteArrayOutputStream {
	 public ByteArrayInOutStream() {
		 super();
	 }

	 public ByteArrayInOutStream(int size) {
		 super(size);
	 }

	 public ByteArrayInputStream getInputStream() {
		 return new ByteArrayInputStream(this.buf, 0, this.count);
	 }
}