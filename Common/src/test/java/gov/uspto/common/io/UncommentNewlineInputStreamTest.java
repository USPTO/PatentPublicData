package gov.uspto.common.io;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Test;

public class UncommentNewlineInputStreamTest {

	@Test
	public void testReader() throws IOException {
		String input = "one\\ntwo\\nthree\\nfour\\n";
		//String expect = "one\ntwo\nthree\nfour\n";

		InputStream in = new ByteArrayInputStream(input.getBytes());

		UncommentNewlineInputStream uncomment = new UncommentNewlineInputStream(in);
		InputStreamReader inputStreamReader = new InputStreamReader(uncomment);
		BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

		assertEquals("one", bufferedReader.readLine());
		assertEquals("two", bufferedReader.readLine());
		assertEquals("three", bufferedReader.readLine());
		assertEquals("four", bufferedReader.readLine());
		
		bufferedReader.close();
	}

}
