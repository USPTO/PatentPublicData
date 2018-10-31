package gov.uspto.common.io;

import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.junit.Test;

import com.google.common.io.ByteStreams;

public class CommentNewlineOutputStreamTest {

	@Test
	public void test() throws IOException {
		String lines = "one\ntwo\nthree\rfour\r\n";
		String expect = "one\\ntwo\\nthree\\nfour\\n";

		InputStream in = new ByteArrayInputStream(lines.getBytes());

		ByteArrayOutputStream bya = new ByteArrayOutputStream();
		CommentNewlineOutputStream out = new CommentNewlineOutputStream(bya);

		ByteStreams.copy(in, out);

		String actual = bya.toString();

		assertEquals(expect, actual);
	}
	
	@Test
	public void testWriter() throws IOException {
		String lines = "one\ntwo\nthree\rfour\r\n";
		String expect = "one\\ntwo\\nthree\\nfour\\n";

		InputStream in = new ByteArrayInputStream(lines.getBytes());

		ByteArrayOutputStream bya = new ByteArrayOutputStream();
		CommentNewlineOutputStream out = new CommentNewlineOutputStream(bya);

		Writer outWriter = new BufferedWriter(new OutputStreamWriter(out));
		outWriter.write(lines);
		outWriter.close();

		String actual = bya.toString();

		assertEquals(expect, actual);
	}

}
