package gov.uspto.common.io;

import static org.junit.Assert.*;

import java.io.StringWriter;

import org.junit.Test;

public class TeeWriterTest {

	@Test
	public void testWrite() throws Exception {
		StringWriter one = new StringWriter();
		StringWriter two = new StringWriter();
		StringWriter three = new StringWriter();

		try (TeeWriter tee = new TeeWriter(one, two, three)) {
			String text = "The quick brown fox";
			tee.append(text);
			tee.flush();
			assertEquals(text, one.getBuffer().toString());
			assertEquals(text, two.getBuffer().toString());
			assertEquals(text, one.getBuffer().toString());
		}
	}

}
