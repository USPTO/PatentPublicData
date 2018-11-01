package gov.uspto.common.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

import com.google.common.base.Charsets;

public class RecordPerLineWriterTest {

	@Test
	public void test() throws IOException {
		String record1 = "1. one\ntwo\nthree\rfour\r\n";
		String expect1 = "1. one\\ntwo\\nthree\\nfour\\n";

		String record2 = "2. one\ntwo\nthree\rfour\r\n";
		String expect2 = "2. one\\ntwo\\nthree\\nfour\\n";

		Path tempDirWithPrefix = Files.createTempDirectory("rpl");

		RecordPerLineWriter writer = new RecordPerLineWriter("test", "tmp", tempDirWithPrefix);
		writer.write(record1);
		writer.write(record2);
		File createFile = writer.getCurrentFile();
		writer.close();

		BufferedReader reader = new BufferedReader(new FileReader(createFile));
		
		//RecordPerLineReader reader = new RecordPerLineReader(createFile);

		assertEquals(expect1, reader.readLine());
		assertEquals(expect2, reader.readLine());
		
		reader.close();
	}

	@Test
	public void partition() throws IOException {
		String record1 = "1. one\ntwo\nthree\rfour\r\n";
		String expect1 = "1. one\\ntwo\\nthree\\nfour\\n";

		String record2 = "2. one\ntwo\nthree\rfour\r\n";
		String expect2 = "2. one\\ntwo\\nthree\\nfour\\n";

		Path tempDirWithPrefix = Files.createTempDirectory("rpl");

		RecordPerLineWriter writer = new RecordPerLineWriter("test", "tmp", tempDirWithPrefix, Charset.defaultCharset(), 1);
		writer.write(record1);
		String createFile1 = writer.getCurrentFile().getAbsolutePath();

		writer.write(record2);
		String createFile2 = writer.getCurrentFile().getAbsolutePath();
		writer.close();

		assertNotEquals(createFile1, createFile2);

		BufferedReader reader1 = new BufferedReader(new FileReader(createFile1));
		assertEquals(expect1, reader1.readLine());
		reader1.close();

		BufferedReader reader2 = new BufferedReader(new FileReader(createFile2));
		assertEquals(expect2, reader2.readLine());
		reader2.close();
	}
	
}
