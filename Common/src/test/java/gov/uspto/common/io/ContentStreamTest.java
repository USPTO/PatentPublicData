package gov.uspto.common.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import gov.uspto.common.io.ContentStream;
import gov.uspto.common.io.ContentStream.LargeAction;

public class ContentStreamTest {

	@Test
	public void Large_Default() throws IOException {
		long threshold = 10;
		ContentStream stb = new ContentStream(threshold);

		String originalString = "My custom string";
		stb.append(originalString);

		/*
		System.out.println("isLarge: " + stb.isLarge());
		System.out.println("tempFile: " + stb.getTempFile());
		System.out.println("content: " + content);
		System.out.println("Char length: " + stb.getLength());
		System.out.println("Temp File size: " + stb.getFileSize());
		*/

		InputStream is = stb.getInputStream();
		String returnedContent = ContentStream.inputStream2String(is);

		assertTrue(stb.getFileSize() > 0);
		assertEquals(stb.getLength(), originalString.length());
		assertTrue(stb.isLarge());
		assertTrue(stb.getLength() > threshold);
		assertNotNull("Large should have tempFile!", stb.getTempFile());
		assertEquals("Content not maintained!", originalString, returnedContent);
	}

	@Test
	public void SmallContent() throws IOException {
		long threshold = 100;
		ContentStream stb = new ContentStream(threshold);

		String originalString = "My custom string";
		stb.append(originalString);

		InputStream is = stb.getInputStream();
		String returnedContent = ContentStream.inputStream2String(is);

		InputStream is2 = stb.getInputStream();
		String returnedContent2 = ContentStream.inputStream2String(is2);

		assertEquals("Called twice should return new identical stream, no error", returnedContent, returnedContent2);

		/*
		System.out.println("isLarge: " + stb.isLarge());
		System.out.println("tempFile: " + stb.getTempFile());
		System.out.println("content: " + content);
		System.out.println("Char length: " + stb.getLength());
		System.out.println("Temp File size: " + stb.getFileSize());
		*/
		//assertEquals(stb.getLength(), stb.getFileSize());

		assertTrue(stb.getFileSize() == 0);
		assertEquals(stb.getLength(), originalString.length());
		assertFalse(stb.isLarge());
		assertFalse(stb.getLength() > threshold);
		assertNull("Small content should not have a tempFile!", stb.getTempFile());
		assertEquals("Content not maintained!", originalString, returnedContent);
	}

	@Test
	public void Large_AllToTemp() throws IOException {
		long threshold = 20;
		ContentStream stb = new ContentStream(threshold).setLargeAction(LargeAction.ALL_TO_TEMP);

		String originalString = "My custom string\n";
		String originalString2 = "Second line string to be ignored";
		stb.append(originalString);
		stb.append(originalString2);
		String expect = originalString + originalString2;

		InputStream is = stb.getInputStream();
		String returnedContent = ContentStream.inputStream2String(is);

		assertTrue(stb.getFileSize() > 0);
		assertEquals(expect.length(), stb.getLength());
		assertTrue(stb.isLarge());
		assertTrue(stb.getLength() > threshold);
		assertNotNull("Large should have tempFile!", stb.getTempFile());
		assertEquals("Content not maintained!", expect, returnedContent);
	}

	@Test
	public void Large_SkipFully() throws IOException {
		long threshold = 10;
		ContentStream stb = new ContentStream(threshold).setLargeAction(LargeAction.SKIP_FULLY);

		String originalString = "My custom string";
		stb.append(originalString);

		InputStream is = stb.getInputStream();
		String returnedContent = ContentStream.inputStream2String(is);

		assertTrue(returnedContent.length() == 0);
		assertTrue(stb.isLarge());
		assertTrue(stb.getFileSize() == 0);
		assertEquals(originalString.length(), stb.getLength());
		assertTrue(stb.getLength() > threshold);
		assertNull("Large ignore should not have tempFile!", stb.getTempFile());
	}

	@Test
	public void Large_TruncateTail() throws IOException {
		long threshold = 20;
		ContentStream stb = new ContentStream(threshold).setLargeAction(LargeAction.TRUNCATE_TAIL);

		String originalString = "My custom string";
		String originalString2 = "Second line string to be ignored";
		stb.append(originalString);
		stb.append(originalString2);

		InputStream is = stb.getInputStream();
		String returnedContent = ContentStream.inputStream2String(is);
		//System.out.println("Truncated: " + returnedContent);

		assertTrue(stb.isLarge());
		assertTrue(stb.getFileSize() == 0);
		assertTrue(returnedContent.length() <= threshold);
		assertTrue(stb.getLength() > threshold);
		assertNull("Large ignore should not have tempFile!", stb.getTempFile());
	}

	//@Test
	public void Large_getStream_append_fail() throws IOException {
		long threshold = 20;
		ContentStream stb = new ContentStream(threshold).setLargeAction(LargeAction.DEFAULT);

		String originalString = "My custom string";
		String originalString2 = "Second line string to be ignored";
		stb.append(originalString);
		stb.append(originalString2);

		stb.getInputStream();

		try {
			stb.append(originalString);
			fail("expected IOException: Stream Closed");
		} catch(Exception e) {
			// ok.
		}
	}

	@Test
	public void Large_mark() throws IOException {
		long threshold = 10;
		ContentStream stb = new ContentStream(threshold).setLargeAction(LargeAction.DEFAULT);
		String originalString = "My custom string\n";
		String originalString2 = "Second line";
		String originalString3 = "third line";

		stb.append(originalString);
		stb.markStart("second");
		stb.append(originalString2);
		stb.markEnd("second");
		stb.markStart("third");
		stb.append(originalString3);
		stb.markEnd("third");

		InputStream is = stb.getMarkedInputStream("second");
		String returnedContent2 = ContentStream.inputStream2String(is);
		//System.out.println("second: " + returnedContent2);
		assertEquals(originalString2, returnedContent2);
		
		InputStream is3 = stb.getMarkedInputStream("third");
		String returnedContent3 = ContentStream.inputStream2String(is3);
		//System.out.println("third: " + returnedContent3);
		assertEquals(originalString3, returnedContent3);

		InputStream is4 = stb.getMarkedInputStream("second");
		String returnedContent4 = ContentStream.inputStream2String(is4);
		//System.out.println("second: " + returnedContent4);
		assertEquals(originalString2, returnedContent4);
	}
	
	//@Test
	public void header_footer() throws IOException {
		long threshold = 10;
		ContentStream stb = new ContentStream(threshold).setLargeAction(LargeAction.DEFAULT);
		String originalString = "My custom string\n";

		stb.setHeader("HEAD");
		stb.append(originalString);
		stb.setFooter("FOOTER");

		InputStream is = stb.getInputStream();
		String returnedContent = ContentStream.inputStream2String(is);
		//System.out.println("second: " + returnedContent2);
		assertEquals("HEAD" + originalString + "FOOTER", returnedContent);
	}
}
